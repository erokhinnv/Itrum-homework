public class ConcurrentBankExample {

    public static void main(String[] args) throws InterruptedException {
        basicDemo();
    }

    private static void basicDemo() throws InterruptedException {
        ConcurrentBank bank = new ConcurrentBank();

        // Создание счетов
        BankAccount account1 = bank.createAccount(1000);
        BankAccount account2 = bank.createAccount(500);

        // Перевод между счетами
        Thread transferThread1 = new Thread(() -> bank.transfer(account1, account2, 200));
        Thread transferThread2 = new Thread(() -> bank.transfer(account2, account1, 100));

        transferThread1.start();
        transferThread2.start();

        transferThread1.join();
        transferThread2.join();

        System.out.println(account1);              // 900.00
        System.out.println(account2);              // 600.00
        System.out.println("Total balance: " + bank.getTotalBalance());
    }
}
