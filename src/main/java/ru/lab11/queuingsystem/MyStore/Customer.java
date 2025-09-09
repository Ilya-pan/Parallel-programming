package ru.lab11.queuingsystem.MyStore;

import java.util.*;

public class Customer {
    // Логин клиента
    private String login;
    // Баланс клиента
    private double balance;
    // Общая сумма, потраченная клиентом
    private double spentAmount;
    // Список сообщений, отправленных клиенту
    private List<Message> messages;
    // Карта зарезервированных товаров и их количества
    private Map<Product, Integer> reservedProducts;

    // Конструктор для создания клиента с заданным логином и балансом
    public Customer(String login, double balance) {
        this.login = login;
        this.balance = balance;
        this.spentAmount = 0;
        messages = new ArrayList<>();
        reservedProducts = new HashMap<>();
    }

    // Покупка указанного количества товара
    public double purchase(Product product, int quantity) {
        // Рассчитываем общую стоимость
        double totalCost = product.getPrice() * quantity;

        // Проверяем наличие товара
        if (product.getQuantity() < quantity) {
            Message message = new Message("Недостаточно товара на складе!", new Date());
            this.messages.add(message);
            return 0;
        }
        // Проверяем достаточность средств
        else if (balance < totalCost) {
            Message message = new Message("Недостаточно средств на счету!", new Date());
            this.messages.add(message);
            return 0;
        }
        // Успешная покупка
        else {
            product.decreaseQuantity(quantity); // Уменьшаем количество товара
            spentAmount += totalCost;          // Увеличиваем сумму потраченных средств
            balance -= totalCost;              // Списываем деньги с баланса
            return totalCost;
        }
    }

    // Возвращает общую сумму, потраченную клиентом
    public double getSpentAmount() {
        return spentAmount;
    }

    // Возвращает список сообщений клиента
    public List<Message> getMessages() {
        return messages;
    }

    // Возвращает текущий баланс клиента
    public double getBalance() {
        return balance;
    }

    // Устанавливает новый баланс клиента
    public void setBalance(double balance) {
        this.balance = balance;
    }

    // Устанавливает общую сумму, потраченную клиентом
    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    // Добавляет сообщение клиенту
    public void addMessage(Message message) {
        messages.add(message);
    }

    // Резервирует указанный товар и его количество
    public void reserveProduct(Product product, int quantity) {
        reservedProducts.put(product, reservedProducts.getOrDefault(product, 0) + quantity);
    }

    // Очищает список зарезервированных товаров
    public void clearReservedItems() {
        reservedProducts.clear();
    }

    // Возвращает карту зарезервированных товаров и их количества
    public Map<Product, Integer> getReservedProducts() {
        return reservedProducts;
    }
}
