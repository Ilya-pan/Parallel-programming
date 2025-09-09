package ru.lab11.queuingsystem;

import ru.lab11.queuingsystem.MyStore.Customer;
import ru.lab11.queuingsystem.MyStore.Store;
import ru.lab11.queuingsystem.RequestProcessing.DisruptorProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        DisruptorProcessor processor = new DisruptorProcessor();
        StoreAPI api = new StoreAPI(processor, new Store(100000.0));

        String productName = "Laptop";
        int initialQuantity = 50;
        double productPrice = 1000.0;

        api.addProductToStore(productName, initialQuantity, productPrice);

        int numberOfCustomers = 5;
        Random random = new Random();

        for (int i = 0; i < numberOfCustomers; i++) {
            double initialCustomerBalance = 10000.0;
            api.createCustomer("Customer_" + i, initialCustomerBalance);
        }

        List<Thread> threads = new ArrayList<>();
        AtomicInteger totalReservedQuantity = new AtomicInteger();

        processor.waitProcessor();

        for (Customer customer : api.getAllCustomers()) {
            Thread thread = new Thread(() -> {
                int reservedQuantity = random.nextInt(5) + 1;
                boolean reservationSuccess = api.reserveProduct(customer, productName, reservedQuantity);
                if(!reservationSuccess){
                    System.out.println("Бронирование должно быть успешным");
                }

                boolean purchaseSuccess = api.purchaseReservedProducts(customer);

                if(!purchaseSuccess){
                    System.out.println("Покупка забронированных товаров должна быть успешной");
                }

                totalReservedQuantity.addAndGet(reservedQuantity);
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        processor.waitProcessor();

        double totalSpentByCustomers = api.getAllCustomers().stream()
                .mapToDouble(Customer::getSpentAmount)
                .sum();

        double expectedStoreBalance = 100000.0 + totalSpentByCustomers;
        int expectedRemainingStock = initialQuantity - totalReservedQuantity.get();

        processor.shutdown();

        if(expectedStoreBalance != api.getStoreBalance()){
            System.out.println("Баланс магазина после покупки товаров должен быть правильным");
        }

        if(expectedRemainingStock != api.getStoreProductAvailability(productName)){
            System.out.println("Количество товара на складе после покупки должно быть правильным");
        }
    }
}