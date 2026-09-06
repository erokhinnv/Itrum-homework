/**
 * Демонстрация блокирующей очереди.
 * Производители кладут задачи в очередь, потребители их разбирают.
 * Очередь маленькая, поэтому производителям приходится ждать, когда она
 * заполнится, а потребителям — когда она опустеет.
 */
public class Main {

    private static final int CAPACITY = 5;  // размер очереди
    private static final int PRODUCERS = 3; // сколько производителей
    private static final int CONSUMERS = 2; // сколько потребителей
    private static final int TASKS = 6;     // сколько задач создаёт каждый производитель

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Task> queue = new BlockingQueue<>(CAPACITY);

        // Запускаем производителей
        Thread[] producerThreads = new Thread[PRODUCERS];
        for (int i = 0; i < PRODUCERS; i++) {
            producerThreads[i] = new Thread(new Producer(queue, i + 1, TASKS));
            producerThreads[i].start();
        }

        // Запускаем потребителей
        Consumer[] consumers = new Consumer[CONSUMERS];
        Thread[] consumerThreads = new Thread[CONSUMERS];
        for (int i = 0; i < CONSUMERS; i++) {
            consumers[i] = new Consumer(queue, i + 1);
            consumerThreads[i] = new Thread(consumers[i]);
            consumerThreads[i].start();
        }

        // Ждём, пока все производители разложат свои задачи
        for (Thread thread : producerThreads) {
            thread.join();
        }

        // Задачи кончились — просим потребителей остановиться.
        // Каждый потребитель забирает ровно один сигнал.
        for (int i = 0; i < CONSUMERS; i++) {
            queue.enqueue(Task.STOP);
        }

        // Ждём, пока потребители доработают
        for (Thread thread : consumerThreads) {
            thread.join();
        }

        int executed = 0;
        for (Consumer consumer : consumers) {
            executed += consumer.getExecuted();
        }

        System.out.println();
        System.out.println("Выполнено задач: " + executed + " из " + PRODUCERS * TASKS);
        System.out.println("Очередь в конце: " + queue);
    }
}
