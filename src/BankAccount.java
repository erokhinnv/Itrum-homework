import java.util.concurrent.locks.ReentrantLock;

/**
 * Банковский счёт с уникальным номером и потокобезопасными операциями.
 *
 * Все изменения баланса защищены собственной блокировкой счёта. Блокировка
 * доступна банку (пакетный доступ), чтобы ConcurrentBank мог захватить
 * сразу два счёта и выполнить перевод как одну атомарную операцию.
 */
public class BankAccount {

    private final long id;
    private final ReentrantLock lock = new ReentrantLock();

    /** Изменяется только под lock. */
    private double balance;

    /** Счета создаются через ConcurrentBank. */
    BankAccount(long id, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным: " + initialBalance);
        }
        this.id = id;
        this.balance = initialBalance;
    }

    public long getId() {
        return id;
    }

    /** Пополнение счёта. */
    public void deposit(double amount) {
        requirePositive(amount);
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Снятие со счёта.
     *
     * Вернёт true, если средств хватило и деньги сняты; иначе false
     */
    public boolean withdraw(double amount) {
        requirePositive(amount);
        lock.lock();
        try {
            if (balance < amount) {
                return false;
            }
            balance -= amount;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public double getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    /** Блокировка счёта — нужна банку для атомарных переводов и снимка балансов. */
    ReentrantLock getLock() {
        return lock;
    }

    /**
     * Снятие без захвата блокировки: вызывается банком, когда блокировка уже удерживается.
     */
    boolean withdrawLocked(double amount) {
        if (balance < amount) {
            return false;
        }
        balance -= amount;
        return true;
    }

    /** Пополнение без захвата блокировки: вызывается банком под уже удерживаемой блокировкой. */
    void depositLocked(double amount) {
        balance += amount;
    }

    /** Чтение баланса без захвата блокировки: вызывается банком под уже удерживаемой блокировкой. */
    double getBalanceLocked() {
        return balance;
    }

    private static void requirePositive(double amount) {
        if (amount <= 0 || Double.isNaN(amount)) {
            throw new IllegalArgumentException("Сумма операции должна быть положительной: " + amount);
        }
    }

    @Override
    public String toString() {
        return "Account#" + id + "{balance=" + String.format("%.2f", getBalance()) + "}";
    }
}
