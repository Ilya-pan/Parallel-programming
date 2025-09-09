package ru.lab11.queuingsystem.MyStore;

import java.util.*;

public class Store {
    // Список товаров, доступных в магазине
    private Map<String, Product> products = new HashMap<>();
    // Баланс магазина
    private double balance;
    // Список клиентов магазина
    private List<Customer> customers = new ArrayList<>();

    // Конструктор для инициализации магазина с заданным балансом
    public Store(double balance) {
        this.balance = balance;
    }

    // Добавляет новый продукт или обновляет существующий
    public void addProduct(String name, int quantity, double price) {
        products.put(name, new Product(name, quantity, price));
    }

    // Возвращает количество доступного товара
    public int getAvailableQuantity(String productName) {
        Product product = products.get(productName);
        return product != null ? product.getQuantity() : 0;
    }

    // Возвращает текущий баланс магазина
    public double getStoreBalance() {
        return balance;
    }

    // Возвращает объект продукта по его названию
    public Product getProduct(String productName) {
        return products.get(productName);
    }

    // Возвращает список всех клиентов магазина
    public List<Customer> getCustomers() {
        return customers;
    }

    // Добавляет клиента в магазин
    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    // Резервирует указанный товар для клиента
    public boolean reserveProduct(Customer customer, Product product, int quantity) {
        // Проверяем, что товар существует и его достаточно для бронирования
        if (product == null || product.getQuantity() < quantity) {
            customer.addMessage(new Message("Недостаточно товара для бронирования!", new Date()));
            return false;
        }
        // Уменьшаем количество товара и добавляем его в список зарезервированных товаров клиента
        product.decreaseQuantity(quantity);
        customer.reserveProduct(product, quantity);
        return true;
    }

    // Осуществляет покупку всех зарезервированных товаров клиента
    public boolean purchaseReservedItems(Customer customer) {
        // Рассчитываем общую стоимость всех зарезервированных товаров
        double totalCost = customer.getReservedProducts().entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();

        // Проверяем, хватает ли средств у клиента
        if (customer.getBalance() < totalCost) {
            customer.addMessage(new Message("Недостаточно средств для выкупа забронированных товаров!", new Date()));
            return false;
        }

        // Списываем средства, обновляем траты клиента и баланс магазина
        customer.setBalance(customer.getBalance() - totalCost);
        customer.setSpentAmount(customer.getSpentAmount() + totalCost);
        customer.clearReservedItems();
        balance += totalCost;
        return true;
    }

    // Добавляет указанное количество товара в магазин
    public void addSupply(String productName, int quantity) {
        // Если товар отсутствует, создаем его с ценой 0.0
        Product product = products.get(productName);
        if (product == null) {
            products.put(productName, new Product(productName, quantity, 0.0));
        } else {
            product.increaseQuantity(quantity);
        }
    }

    // Обрабатывает покупку товара клиентом без бронирования
    public boolean handlePurchase(Customer customer, String productName, int quantity) {
        // Проверяем, существует ли товар
        Product product = products.get(productName);
        if (product == null) {
            Message message = new Message("Такого продукта нет!", new Date());
            customer.addMessage(message);
            return false;
        } else {
            // Рассчитываем стоимость и обновляем баланс, если покупка прошла успешно
            double cost = customer.purchase(product, quantity);
            if (cost <= 0) {
                return false;
            } else {
                balance += cost;
                return true;
            }
        }
    }
}
