package ru.lab11.queuingsystem.MyStore;

/**
 * Класс Product представляет продукт, доступный в магазине.
 * Содержит информацию о названии, количестве и цене продукта,
 * а также методы для управления количеством.
 */
public class Product {
    private String name; // Название продукта
    private int quantity; // Текущее количество продукта на складе
    private double price; // Цена продукта

    /**
     * Конструктор для создания нового продукта.
     *
     * @param name     название продукта.
     * @param quantity начальное количество продукта.
     * @param price    цена продукта.
     */
    public Product(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * Возвращает название продукта.
     *
     * @return название продукта.
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает текущее количество продукта на складе.
     *
     * @return количество продукта.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Устанавливает новое количество продукта.
     *
     * @param quantity новое количество продукта.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Уменьшает количество продукта на указанное значение.
     *
     * @param quantity количество, на которое нужно уменьшить.
     */
    public void decreaseQuantity(int quantity) {
        this.quantity -= quantity;
    }

    /**
     * Увеличивает количество продукта на указанное значение.
     *
     * @param quantity количество, на которое нужно увеличить.
     */
    public void increaseQuantity(int quantity) {
        this.quantity += quantity;
    }

    /**
     * Возвращает цену продукта.
     *
     * @return цена продукта.
     */
    public double getPrice() {
        return price;
    }
}
