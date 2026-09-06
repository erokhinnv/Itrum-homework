/** Потребитель: забирает задачи из очереди и выполняет их. */
public class Consumer implements Runnable {

    private final BlockingQueue<Task> queue;
    private final int id;

    /** Сколько задач выполнил. Читать только после join() — он гарантирует видимость. */
    private int executed;

    public Consumer(BlockingQueue<Task> queue, int id) {
        this.queue = queue;
        this.id = id;
    }

    public int getExecuted() {
        return executed;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Task task = queue.dequeue(); // если очередь пуста — поток здесь заснёт
                if (task == Task.STOP) {
                    System.out.println("Потребитель " + id + " получил сигнал завершения");
                    return;
                }
                System.out.println("Потребитель " + id + " выполняет " + task);
                task.execute();
                executed++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
