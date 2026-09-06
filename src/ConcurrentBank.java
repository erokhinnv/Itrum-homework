import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Банк, управляющий счетами и переводами между ними.
 *
 * Реестр счетов — ConcurrentSkipListMap, упорядоченная по номеру счёта:
 * это даёт и потокобезопасность, и стабильный порядок обхода для getTotalBalance().
 *
 * Блокировки всегда захватываются в порядке возрастания номера счёта — единый
 * глобальный порядок исключает взаимную блокировку (deadlock) при встречных переводах.
 */
public class ConcurrentBank {

    private final ConcurrentSkipListMap<Long, BankAccount> accounts = new ConcurrentSkipListMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /** Создаёт счёт с нулевым балансом. */
    public BankAccount createAccount() {
        return createAccount(0);
    }

    /** Создаёт счёт с заданным начальным балансом и регистрирует его в банке. */
    public BankAccount createAccount(double initialBalance) {
        long id = idGenerator.getAndIncrement();
        BankAccount account = new BankAccount(id, initialBalance);
        accounts.put(id, account);
        return account;
    }

    /**
     * Атомарный перевод: списание и зачисление происходят под захваченными
     * блокировками обоих счетов, поэтому промежуточное состояние (деньги списаны,
     * но не зачислены) недоступно другим потокам.
     *
     * Вернёт true, если перевод выполнен; false, если средств недостаточно
     */
    public boolean transfer(BankAccount from, BankAccount to, double amount) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Счета перевода не должны быть null");
        }
        if (amount <= 0 || Double.isNaN(amount)) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной: " + amount);
        }
        if (from == to) {
            throw new IllegalArgumentException("Перевод на тот же счёт не имеет смысла: #" + from.getId());
        }
        requireRegistered(from);
        requireRegistered(to);

        // Порядок захвата — по возрастанию номера счёта: защита от deadlock.
        BankAccount first = from.getId() < to.getId() ? from : to;
        BankAccount second = first == from ? to : from;

        first.getLock().lock();
        try {
            second.getLock().lock();
            try {
                if (!from.withdrawLocked(amount)) {
                    return false;
                }
                to.depositLocked(amount);
                return true;
            } finally {
                second.getLock().unlock();
            }
        } finally {
            first.getLock().unlock();
        }
    }

    /** Перевод по номерам счетов. */
    public boolean transfer(long fromId, long toId, double amount) {
        BankAccount from = requireAccount(fromId);
        BankAccount to = requireAccount(toId);
        return transfer(from, to, amount);
    }

    /**
     * Общий баланс всех счетов банка.
     *
     * Все блокировки захватываются разом (в порядке возрастания номера счёта),
     * поэтому сумма — согласованный снимок: перевод не может быть учтён «наполовину».
     */
    public double getTotalBalance() {
        List<BankAccount> snapshot = new ArrayList<>(accounts.values()); // уже отсортирован по id
        List<ReentrantLock> acquired = new ArrayList<>(snapshot.size());
        try {
            for (BankAccount account : snapshot) {
                ReentrantLock lock = account.getLock();
                lock.lock();
                acquired.add(lock);
            }
            double total = 0;
            for (BankAccount account : snapshot) {
                total += account.getBalanceLocked();
            }
            return total;
        } finally {
            // Освобождаем в обратном порядке.
            for (int i = acquired.size() - 1; i >= 0; i--) {
                acquired.get(i).unlock();
            }
        }
    }

    private BankAccount requireAccount(long id) {
        BankAccount account = accounts.get(id);
        if (account == null) {
            throw new IllegalArgumentException("Счёт не найден: #" + id);
        }
        return account;
    }

    private void requireRegistered(BankAccount account) {
        if (accounts.get(account.getId()) != account) {
            throw new IllegalArgumentException("Счёт #" + account.getId() + " не принадлежит этому банку");
        }
    }
}
