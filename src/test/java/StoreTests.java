import org.junit.jupiter.api.RepeatedTest;
import ru.lab11.queuingsystem.*;
import ru.lab11.queuingsystem.MyStore.Customer;
import ru.lab11.queuingsystem.MyStore.Store;
import ru.lab11.queuingsystem.RequestProcessing.DisruptorProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Класс тестов для магазина и системы обработки запросов
public class StoreTests {

    // Проверяется сценарий, когда 100 клиентов делают случайное количество покупок в многопоточном режиме.
    @RepeatedTest(500)
    public void testMultipleCustomersPurchasing() throws InterruptedException {
        // Инициализация процессора и API магазина
        DisruptorProcessor processor = new DisruptorProcessor();
        StoreAPI api = new StoreAPI(processor, new Store(100000.0));

        // Добавляем продукт в магазин
        api.addProductToStore("Laptop", 100000000, 500.0);

        // Создаем 100 случайных клиентов
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            api.createCustomer("Customer" + i, random.nextInt(15000) + 1000);
        }

        //

        List<Thread> threads = new ArrayList<>();

        // Создаем и запускаем потоки для каждого клиента
        for (Customer customer : api.getAllCustomers()) {
            Thread thread = new Thread(() -> {
                int quantity = random.nextInt(20) + 1;
                // Каждый клиент делает покупку
                api.makePurchase(customer, "Laptop", quantity);
            });
            threads.add(thread);
        }

        // Запускаем все потоки
        for (Thread thread : threads) {
            thread.start();
            thread.join();
        }

        //

        // Подсчитываем общую сумму покупок
        double totalSpentByCustomers = api.getAllCustomers().stream()
                .mapToDouble(Customer::getSpentAmount)
                .sum();
        // Рассчитываем ожидаемый баланс магазина
        double expectedBalance = 100000.0 + totalSpentByCustomers;
        double actualBalance = api.getStoreBalance();
        //Баланс магазина после всех покупок должен быть равен начальному балансу плюс сумма, потраченная клиентами.
        // Завершаем работу процессора
        processor.shutdown();

        // Проверяем, что фактический баланс магазина соответствует ожидаемому
        assertEquals(expectedBalance, actualBalance, 1e-9, "Баланс магазина не соответствует ожидаемому");
    }

    // Проверяется ситуация, когда у клиента недостаточно средств для покупки.
    @RepeatedTest(500)
    public void testCustomerInsufficientFunds() throws InterruptedException {
        // Инициализация процессора и API магазина
        DisruptorProcessor processor = new DisruptorProcessor();
        StoreAPI api = new StoreAPI(processor, new Store(100000.0));

        // Добавляем продукт с ограниченным количеством
        api.addProductToStore("Laptop", 100, 500.0);

        // Создаем клиента с начальным балансом
        double initialBalance = 1000 + new Random().nextInt(1001);
        api.createCustomer("Customer", initialBalance);

        //

        // Пытаемся выполнить покупку до тех пор, пока не хватит средств
        while (true) {
            boolean purchaseResult = api.makePurchase(api.getAllCustomers().get(0), "Laptop", 1);
            if (!purchaseResult) {
                break;
            }
        }

        // Ожидаем завершения всех операций
        processor.waitProcessor();

        // Проверяем, что последнее сообщение клиента — это сообщение о недостаточности средств
        String lastMessage = api.getAllCustomers().get(0).getMessages()
                .get(api.getAllCustomers().get(0).getMessages().size() - 1)
                .getMessage();

        // Завершаем работу процессора
        processor.shutdown();
        //Покупки продолжаются до тех пор, пока баланс клиента позволяет
        // Проверяем, что сообщение соответствует ожиданиям
        assertEquals("Недостаточно средств на счету!", lastMessage,
                "Последнее сообщение должно быть об отсутствии средств на счете");
    }

    // Проверяется корректность изменения количества продуктов на складе после серии покупок.
    @RepeatedTest(500)
    public void testRemainingProductQuantityAfterPurchases() throws InterruptedException {
        // Инициализация процессора и API магазина
        DisruptorProcessor processor = new DisruptorProcessor();
        StoreAPI api = new StoreAPI(processor, new Store(100000.0));

        // Добавляем продукты в магазин
        api.addProductToStore("Phone", 50000, 300.0);
        api.addProductToStore("Laptop", 50000, 500.0);

        // Создаем клиентов с большими балансами
        api.createCustomer("Customer1", 100000000.0);
        api.createCustomer("Customer2", 150000000.0);
        api.createCustomer("Customer3", 150000000.0);

        List<Thread> threads = new ArrayList<>();
        Random random = new Random();

        // Переменные для подсчета покупок
        AtomicInteger phoneBought = new AtomicInteger();
        AtomicInteger laptopBought = new AtomicInteger();

        processor.waitProcessor();

        // Создаем потоки для покупки продуктов
        threads.add(new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                int quantity = random.nextInt(3) + 1;
                api.makePurchase(api.getAllCustomers().get(0), "Phone", quantity);
                phoneBought.addAndGet(quantity);
            }
        }));

        threads.add(new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                int quantity = random.nextInt(2) + 1;
                api.makePurchase(api.getAllCustomers().get(1), "Laptop", quantity);
                laptopBought.addAndGet(quantity);
            }
        }));

        threads.add(new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                int quantity = random.nextInt(2) + 1;
                api.makePurchase(api.getAllCustomers().get(2), "Laptop", quantity);
                laptopBought.addAndGet(quantity);
            }
        }));

        // Запускаем все потоки
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Ожидаем завершения всех операций
        processor.waitProcessor();

        // Проверяем, что количество продуктов на складе уменьшилось на сумму покупок
        int initialPhoneQuantity = 50000;
        int initialLaptopQuantity = 50000;

        int expectedPhoneQuantity = initialPhoneQuantity - phoneBought.get();
        int expectedLaptopQuantity = initialLaptopQuantity - laptopBought.get();

        // Завершаем работу процессора
        processor.shutdown();
        //Оставшееся количество каждого продукта на складе должно быть равно начальному количеству минус общее количество, купленное клиентами.
        // Проверяем, что оставшееся количество продуктов соответствует ожидаемому
        assertEquals(expectedPhoneQuantity, api.getStoreProductAvailability("Phone"), "Оставшееся количество телефонов на складе неверно");
        assertEquals(expectedLaptopQuantity, api.getStoreProductAvailability("Laptop"), "Оставшееся количество ноутбуков на складе неверно");
    }

    // Проверяется корректность работы механизма поставок товаров в многопоточном режиме.
    @RepeatedTest(500)
    public void testConcurrentProductSupplyWithRandomQuantities() throws InterruptedException {
        // Инициализация процессора и API магазина
        DisruptorProcessor processor = new DisruptorProcessor();
        StoreAPI api = new StoreAPI(processor, new Store(100000.0));

        String productName = "Laptop";
        int initialQuantity = 100;
        int numberOfThreads = 10;

        // Добавляем продукт в магазин
        api.addProductToStore(productName, initialQuantity, 1000.0);

        AtomicInteger expectedAdditionalQuantity = new AtomicInteger(0);
        Random random = new Random();

        List<Thread> threads = new ArrayList<>();

        // Запускаем несколько потоков для поставки товаров
        for (int i = 0; i < numberOfThreads; i++) {
            int supplyQuantity = random.nextInt(20) + 1;
            expectedAdditionalQuantity.addAndGet(supplyQuantity);

            Thread thread = new Thread(() -> api.addSupply(productName, supplyQuantity));
            threads.add(thread);
            thread.start();
        }

        // Ждем завершения всех поставок
        for (Thread thread : threads) {
            thread.join();
        }

        // Ожидаем завершения всех операций
        processor.waitProcessor();

        // Проверяем, что количество товара на складе увеличилось на сумму поставок
        int expectedQuantity = initialQuantity + expectedAdditionalQuantity.get();
        int actualQuantity = api.getStoreProductAvailability(productName);

        // Завершаем работу процессора
        processor.shutdown();

        //Количество товаров на складе после всех поставок должно быть равно начальному количеству плюс суммарное количество, добавленное всеми потоками.
        // Проверяем, что количество товара на складе соответствует ожидаемому
        assertEquals(expectedQuantity, actualQuantity,
                "Количество товара на складе не соответствует ожидаемому после случайных поставок");
    }

    // Проверяется правильность механизма бронирования товаров.
    @RepeatedTest(500)
    public void testCorrectnessOfReservation() throws InterruptedException {
        // Инициализация процессора и API магазина
        DisruptorProcessor processor = new DisruptorProcessor();
        StoreAPI api = new StoreAPI(processor, new Store(100000.0));

        String productName = "Phone";
        int initialQuantity = 100;
        int numberOfThreads = 5;

        // Добавляем продукт в магазин
        api.addProductToStore(productName, initialQuantity, 500.0);
        api.createCustomer("Customer_1", 5000.0);
        Thread.sleep(10);

        AtomicInteger totalReservedQuantity = new AtomicInteger(0);
        Random random = new Random();

        List<Thread> threads = new ArrayList<>();

        // Бронирование товаров в многопоточном режиме
        for (int i = 0; i < numberOfThreads; i++) {
            int reserveQuantity = random.nextInt(10) + 1;
            Thread thread = new Thread(() -> {
                boolean reserved = api.reserveProduct(api.getAllCustomers().get(0), productName, reserveQuantity);
                if (reserved) {
                    totalReservedQuantity.addAndGet(reserveQuantity);
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Ждем завершения всех операций
        for (Thread thread : threads) {
            thread.join();
        }

        // Проверяем, что количество товара на складе уменьшилось на количество забронированных товаров
        int expectedRemainingQuantity = initialQuantity - totalReservedQuantity.get();
        int actualRemainingQuantity = api.getStoreProductAvailability(productName);

        // Проверяем, что количество забронированных товаров у клиента верно
        int reservedByCustomer = api.getAllCustomers().get(0).getReservedProducts().getOrDefault(
                api.getProduct(productName), 0);

        // Завершаем работу процессора
        processor.shutdown();

        //Оставшееся количество товаров на складе должно быть равно начальному количеству минус забронированное количество.
        // Проверяем корректность остатков на складе и забронированных товаров
        assertEquals(expectedRemainingQuantity, actualRemainingQuantity,
                "Количество товара на складе после бронирования некорректное");

        assertEquals(totalReservedQuantity.get(), reservedByCustomer,
                "Количество забронированных товаров у клиента некорректное");
    }

    // Проверяется выполнение покупки забронированных товаров в многопоточном режиме.
    @RepeatedTest(500)
    public void testPurchaseOfReservedItemsWithMultipleThreads() throws InterruptedException {
        // Инициализация процессора и API магазина
        DisruptorProcessor processor = new DisruptorProcessor();
        StoreAPI api = new StoreAPI(processor, new Store(100000.0));

        String productName = "Laptop";
        int initialQuantity = 100;
        double productPrice = 1000.0;

        // Добавляем продукт в магазин
        api.addProductToStore(productName, initialQuantity, productPrice);
        processor.waitProcessor(); // Ждем, пока продукт будет добавлен

        int numberOfCustomers = 5;
        Random random = new Random();

        // Создаем клиентов
        for (int i = 0; i < numberOfCustomers; i++) {
            double initialCustomerBalance = 10000.0;
            api.createCustomer("Customer_" + i, initialCustomerBalance);
        }
        processor.waitProcessor(); // Ждем, пока пользователи будут созданы

        List<Thread> threads = new ArrayList<>();
        AtomicInteger totalReservedQuantity = new AtomicInteger();

        // Бронирование и покупка товаров в отдельных потоках
        for (Customer customer : api.getAllCustomers()) {
            Thread thread = new Thread(() -> {
                int reservedQuantity = random.nextInt(5) + 1;
                boolean reservationSuccess = api.reserveProduct(customer, productName, reservedQuantity);
                assertTrue(reservationSuccess, "Бронирование должно быть успешным");

                boolean purchaseSuccess = api.purchaseReservedProducts(customer);
                assertTrue(purchaseSuccess, "Покупка забронированных товаров должна быть успешной");

                totalReservedQuantity.addAndGet(reservedQuantity);
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Ожидаем завершения всех операций
        processor.waitProcessor(); // Ждем завершения всех операций

        // Подсчитываем общую сумму потраченных денег
        double totalSpentByCustomers = api.getAllCustomers().stream()
                .mapToDouble(Customer::getSpentAmount)
                .sum();

        double expectedStoreBalance = 100000.0 + totalSpentByCustomers;
        int expectedRemainingStock = initialQuantity - totalReservedQuantity.get();

        // Завершаем работу процессора
        processor.shutdown();

        //Оставшееся количество товаров на складе должно быть равно начальному количеству минус общее количество, купленное клиентами.
        // Проверяем, что баланс магазина и количество товаров на складе верны
        assertEquals(expectedStoreBalance, api.getStoreBalance(),
                "Баланс магазина после покупки товаров должен быть правильным");
        assertEquals(expectedRemainingStock, api.getStoreProductAvailability(productName),
                "Количество товара на складе после покупки должно быть правильным");
    }
}
