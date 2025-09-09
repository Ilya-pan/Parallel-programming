package ru.lab11.queuingsystem;

import ru.lab11.queuingsystem.MyStore.Customer;
import ru.lab11.queuingsystem.MyStore.Message;
import ru.lab11.queuingsystem.MyStore.Product;
import ru.lab11.queuingsystem.MyStore.Store;
import ru.lab11.queuingsystem.RequestProcessing.DisruptorProcessor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * StoreAPI предоставляет потокобезопасный интерфейс для взаимодействия с магазином.
 * Использует DisruptorProcessor для асинхронной обработки запросов.
 */
public class StoreAPI implements IStoreAPI {
    private final DisruptorProcessor disruptorProcessor; // Обрабатывает задачи асинхронно.
    private final Store store; // Основная логика магазина для управления продуктами, клиентами и транзакциями.

    /**
     * Конструктор StoreAPI.
     *
     * @param disruptorProcessor процессор запросов для выполнения задач асинхронно.
     * @param store              объект магазина для управления данными.
     */
    public StoreAPI(DisruptorProcessor disruptorProcessor, Store store) {
        this.disruptorProcessor = disruptorProcessor;
        this.store = store;
    }

    /**
     * Создает нового клиента с указанным логином и балансом.
     *
     * @param login   логин клиента.
     * @param balance начальный баланс клиента.
     */
    public void createCustomer(String login, double balance) {
        disruptorProcessor.submitRequest(() -> store.addCustomer(new Customer(login, balance)));
    }

    /**
     * Добавляет новый продукт в магазин с заданным названием, количеством и ценой.
     *
     * @param name     название продукта.
     * @param quantity количество продукта.
     * @param price    цена продукта.
     */
    public void addProductToStore(String name, int quantity, double price) {
        disruptorProcessor.submitRequest(() -> store.addProduct(name, quantity, price));
    }

    /**
     * Выполняет покупку для клиента, пытаясь приобрести указанное количество продукта.
     *
     * @param customer    клиент, совершающий покупку.
     * @param productName название продукта.
     * @param quantity    количество продукта для покупки.
     * @return true, если покупка успешна, иначе false.
     */
    public boolean makePurchase(Customer customer, String productName, int quantity) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        disruptorProcessor.submitRequest(() -> {
            boolean success = store.handlePurchase(customer, productName, quantity);
            result.complete(success);
        });
        return result.join();
    }

    /**
     * Возвращает сумму, которую клиент потратил в магазине.
     *
     * @param customer клиент, чьи затраты нужно узнать.
     * @return общая сумма, потраченная клиентом.
     */
    public double getCustomerSpentAmount(Customer customer) {
        return customer.getSpentAmount();
    }

    /**
     * Возвращает текущее количество доступного продукта в магазине.
     *
     * @param productName название продукта.
     * @return количество продукта на складе.
     */
    public int getStoreProductAvailability(String productName) {
        Product product = store.getProduct(productName);
        return product != null ? product.getQuantity() : 0;
    }

    /**
     * Пытается зарезервировать указанное количество продукта для клиента.
     *
     * @param customer    клиент, для которого резервируется продукт.
     * @param productName название продукта.
     * @param quantity    количество для резервирования.
     * @return true, если резервирование успешно, иначе false.
     */
    public boolean reserveProduct(Customer customer, String productName, int quantity) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        disruptorProcessor.submitRequest(() -> {
            Product product = store.getProduct(productName);
            if (product == null) {
                // Если продукт отсутствует, уведомляем клиента и отменяем резервирование.
                customer.addMessage(new Message("Продукт " + productName + " отсутствует!", new java.util.Date()));
                result.complete(false);
            } else {
                boolean success = store.reserveProduct(customer, product, quantity);
                result.complete(success);
            }
        });
        return result.join();
    }

    /**
     * Позволяет клиенту приобрести товары, которые он ранее зарезервировал.
     *
     * @param customer клиент, совершающий покупку.
     * @return true, если покупка успешна, иначе false.
     */
    public boolean purchaseReservedProducts(Customer customer) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        disruptorProcessor.submitRequest(() -> {
            boolean success = store.purchaseReservedItems(customer);
            result.complete(success);
        });
        return result.join();
    }

    /**
     * Добавляет поставку указанного продукта в магазин.
     *
     * @param productName название продукта.
     * @param quantity    количество продукта для поставки.
     */
    public void addSupply(String productName, int quantity) {
        disruptorProcessor.submitRequest(() -> store.addSupply(productName, quantity));
    }

    /**
     * Возвращает список всех клиентов магазина.
     *
     * @return список клиентов.
     */
    public List<Customer> getAllCustomers() {
        return store.getCustomers();
    }

    /**
     * Возвращает продукт по его названию из магазина.
     *
     * @param productName название продукта.
     * @return объект продукта, если найден, или null, если продукт отсутствует.
     */
    public Product getProduct(String productName) {
        return store.getProduct(productName);
    }

    /**
     * Возвращает текущий баланс магазина.
     *
     * @return баланс магазина.
     */
    public double getStoreBalance() {
        return store.getStoreBalance();
    }
}
