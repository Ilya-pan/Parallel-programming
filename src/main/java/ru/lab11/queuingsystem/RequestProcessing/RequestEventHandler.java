package ru.lab11.queuingsystem.RequestProcessing;

import com.lmax.disruptor.EventHandler;
//Обработчик событий. Этот класс отвечает за выполнение задачи, связанной с каждым событием, и за очистку события после завершения.
//класс для получения и запуска запросов, тоже используется в дизрапторе.
public class RequestEventHandler implements EventHandler<RequestEvent> {
    @Override
    public void onEvent(RequestEvent event, long sequence, boolean endOfBatch) {
        // Проверяем, есть ли задача в событии
        if (event.getTask() != null) {
            event.getTask().run(); // Выполняем задачу
            event.clear();         // Очищаем событие после выполнения
        }
    }
}
