package ru.lab11.queuingsystem.RequestProcessing;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

//Этот класс управляет двумя очередями обработки запросов (двумя Disruptor):
public class DisruptorProcessor {
    // Два Disruptor: первый для входящих запросов, второй для обработки
    private final Disruptor<RequestEvent> inputDisruptor;
    private final Disruptor<RequestEvent> processingDisruptor;

    // Кольцевые буферы для входящих запросов и обработки
    private final RingBuffer<RequestEvent> inputRingBuffer;
    private final RingBuffer<RequestEvent> processingRingBuffer;

    // Счетчик необработанных запросов
    private final AtomicLong pendingRequests = new AtomicLong(0);

    public DisruptorProcessor() {

        int bufferSize = 1024;

        // Фабрика потоков для обработки
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        // Создание первого Disruptor для получения входящих заявок
        inputDisruptor = new Disruptor<>(
                RequestEvent.EVENT_FACTORY,        // Фабрика событий (создает объекты RequestEvent)
                bufferSize,                        // Размер буфера
                threadFactory,                     // Фабрика потоков
                ProducerType.MULTI,                // Несколько потоков могут публиковать заявки
                new BlockingWaitStrategy()         // Стратегия ожидания при нехватке данных
        );

        // Создание второго Disruptor для обработки заявок
        processingDisruptor = new Disruptor<>(
                RequestEvent.EVENT_FACTORY,        // Фабрика событий
                bufferSize,                        // Размер буфера
                threadFactory,                     // Фабрика потоков
                ProducerType.SINGLE,               // Обработка выполняется одним потоком
                new BusySpinWaitStrategy()         // Стратегия ожидания при нехватке данных
        );

        // Получение ссылок на кольцевые буферы
        inputRingBuffer = inputDisruptor.getRingBuffer();
        processingRingBuffer = processingDisruptor.getRingBuffer();

        // Обработка событий из первого буфера и передача их во второй
        inputDisruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            try {
                // Получаем следующий свободный индекс во втором буфере
                long processingSequence = processingRingBuffer.next();
                try {
                    // Переносим задачу из первого буфера во второй
                    RequestEvent processingEvent = processingRingBuffer.get(processingSequence);
                    processingEvent.setTask(event.getTask());
                } finally {
                    // Публикуем событие во втором буфере
                    processingRingBuffer.publish(processingSequence);
                }
            } finally {
                // Очищаем событие в первом буфере
                event.clear();
            }
        });

        // Обработка событий во втором буфере
        processingDisruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            try {
                // Выполняем задачу, если она существует
                if (event.getTask() != null) {
                    event.getTask().run();
                }
            } finally {
                // Очищаем событие после обработки
                event.clear();
                // Уменьшаем счетчик необработанных запросов
                pendingRequests.decrementAndGet();
            }
        });

        // Запускаем оба Disruptor
        inputDisruptor.start();
        processingDisruptor.start();
    }

    // Метод для публикации нового запроса
    public void submitRequest(Runnable task) {
        // Получаем следующий индекс в первом буфере
        long sequence = inputRingBuffer.next();
        try {
            // Добавляем задачу в буфер
            RequestEvent event = inputRingBuffer.get(sequence);
            event.setTask(task);
            // Увеличиваем счетчик необработанных запросов
            pendingRequests.incrementAndGet();
        } finally {
            // Публикуем событие в первом буфере
            inputRingBuffer.publish(sequence);
        }
    }

    // Метод ожидания обработки всех запросов
    public void waitProcessor() {
        // Ожидаем, пока счетчик необработанных запросов не станет равным 0
        while (true) {
            if (pendingRequests.get() == 0) {
                break;
            }
        }
    }

    // Получение текущего количества необработанных запросов
    public long getPendingRequests() {
        return pendingRequests.get();
    }

    // Метод завершения работы обоих Disruptor
    public void shutdown() {
        inputDisruptor.shutdown();
        processingDisruptor.shutdown();
    }
}
