package ru.lab11.queuingsystem.RequestProcessing;

import com.lmax.disruptor.EventFactory;
//Событие, которое используется для передачи задачи между очередями.
//хранит запрос на выполнение
public class RequestEvent {
    private Runnable task; // Задача, связанная с этим событием

    // Возвращает задачу
    public Runnable getTask() {
        return task;
    }

    // Устанавливает задачу
    public void setTask(Runnable task) {
        this.task = task;
    }

    // Очищает задачу после выполнения
    public void clear() {
        task = null;
    }

    // Фабрика для создания новых экземпляров событий
    public static final EventFactory<RequestEvent> EVENT_FACTORY = RequestEvent::new;
}
