/**
 * Потокобезопасная блокирующая очередь фиксированного размера (FIFO).
 *
 * Координация производителей и потребителей построена на встроенном мониторе
 * объекта: wait() / notifyAll(). Все обращения к состоянию очереди
 * происходят внутри synchronized-блоков на одном и том же мониторе (this).
 *
 * @param <E> тип элементов очереди
 */
public class BlockingQueue<E> {

    /** Кольцевой буфер фиксированного размера. */
    private final Object[] items;

    /** Индекс головы — откуда извлекаем. */
    private int head;

    /** Индекс хвоста — куда кладём. */
    private int tail;

    /** Текущее количество элементов. */
    private int count;

    public BlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Ёмкость очереди должна быть положительной: " + capacity);
        }
        this.items = new Object[capacity];
    }

    /**
     * Добавляет элемент в конец очереди.
     * Если очередь заполнена, поток блокируется до появления свободного места.
     */
    public synchronized void enqueue(E item) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("null-элементы не поддерживаются");
        }
        // Именно while, а не if: после пробуждения условие нужно перепроверить —
        // место могло занять другой производитель, плюс возможны spurious wakeups.
        while (count == items.length) {
            wait();
        }
        items[tail] = item;
        tail = (tail + 1) % items.length;
        count++;
        // notifyAll(), а не notify(): на одном мониторе ждут и производители,
        // и потребители. notify() может разбудить "не ту" сторону и привести
        // к взаимной блокировке при потерянном сигнале.
        notifyAll();
    }

    /**
     * Извлекает элемент из головы очереди.
     * Если очередь пуста, поток блокируется до появления нового элемента.
     *
     * возвращает извлечённый элемент
     * InterruptedException если поток прерван во время ожидания
     */
    @SuppressWarnings("unchecked")
    public synchronized E dequeue() throws InterruptedException {
        while (count == 0) {
            wait();
        }
        E item = (E) items[head];
        items[head] = null;
        head = (head + 1) % items.length;
        count--;
        notifyAll();
        return item;
    }

    public synchronized int size() {
        return count;
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("BlockingQueue[");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(items[(head + i) % items.length]);
        }
        return sb.append("] ").append(count).append('/').append(items.length).toString();
    }
}
