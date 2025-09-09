package ru.lab11.queuingsystem.RequestProcessing;

import com.lmax.disruptor.EventFactory;
//Используется для создания новых объектов RequestEvent.
//используется в конструкторе дизрапторов и создаёт запросы для дизрапторов
public class RequestEventFactory implements EventFactory<RequestEvent> {
    @Override
    public RequestEvent newInstance() {
        return new RequestEvent();
    }
}
