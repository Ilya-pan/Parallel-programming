package ru.lab11.queuingsystem;

import ru.lab11.queuingsystem.MyStore.Customer;
import ru.lab11.queuingsystem.MyStore.Product;

public interface IStoreAPI {
    /**
     * Процедура создания пользователя и добавления его в онлайн-магазин
     * @param login логин пользователя
     * @param balance начальный баланс пользователя
     */
    void createCustomer(String login, double balance);

    /**
     * Процедура добавления товара в онлайн-магазин
     * @param name название товара или артикул
     * @param quantity количество товара на складе
     * @param price цена товара
     */
    void addProductToStore(String name, int quantity, double price);

    /**
     * Функция покупки товара пользователем
     * @param customer пользователь, купивший товар
     * @param productName название покупаемого товара
     * @param quantity количество покупаемого товара
     * @return Возвращает true, если товар был успешно продан, false – если
     * товар не был продан независимо от причины ошибки
     */
    boolean makePurchase(Customer customer, String productName, int quantity);

    /**
     * Функция получения денежных средств, потраченных выбранным пользователем
     * @param customer выбранный пользователь
     * @return Количество денежных средств, потраченных пользователем customer
     */
    double getCustomerSpentAmount(Customer customer);

    /**
     * Функция получения количества выбранного товара на складе
     * @param productName выбранный товар
     * @return Количество товара на складе
     */
    int getStoreProductAvailability(String productName);

    /**
     * Функция бронирования предмета
     * @param customer клиент
     * @param productName бронируемый товар
     * @param quantity количество бронируемого товара
     * @return Возвращает true, если бронирование прошло успешно, false – в
     * случае ошибки бронирования
     */
    boolean reserveProduct(Customer customer, String productName, int quantity);

    /**
     * Функция оплаты забронированных товаров
     * @param customer клиент
     * @return Возвращает true, если товары были успешно оплачены, false – в
     * случае, когда оплата была не проведена
     */
    boolean purchaseReservedProducts(Customer customer);

    /**
     * Процедура поставки товаров на склад
     * @param productName название товара (или артикул)
     * @param quantity количество поставляемого товара
     */
    void addSupply(String productName, int quantity);

    /**
     * Функция получения продукта по его имени
     * @param productName имя продукта
     * @return Продукт с данным именем
     */
    Product getProduct(String productName);

    /**
     * Функция получения баланса магазина
     * @return баланс магазина
     */
    double getStoreBalance();
}
