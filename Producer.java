/** Производитель: создаёт задачи и кладёт их в очередь. */
public class Producer implements Runnable {

    private final BlockingQueue<Task> queue;
    private final int id;
    private final int taskCount;

    public Producer(BlockingQueue<Task> queue, int id, int taskCount) {
        this.queue = queue;
        this.id = id;
        this.taskCount = taskCount;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= taskCount; i++) {
                Task task = new Task("задача-" + id + "." + i);
                queue.enqueue(task); // если очередь полна — поток здесь заснёт
                System.out.println("Производитель " + id + " положил " + task
                        + ", в очереди " + queue.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
