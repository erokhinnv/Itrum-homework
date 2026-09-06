/**
 * Задача, которую производитель кладёт в очередь, а потребитель забирает и выполняет.
 */
public class Task {

    /**
     * Особая задача-метка: получив её, потребитель заканчивает работу.
     * Сравнивается по ссылке (==), поэтому экземпляр ровно один.
     */
    public static final Task STOP = new Task("СТОП");

    private final String name;

    public Task(String name) {
        this.name = name;
    }

    /** "Выполнение" задачи — здесь просто пауза, изображающая полезную работу. */
    public void execute() throws InterruptedException {
        Thread.sleep(60);
    }

    @Override
    public String toString() {
        return name;
    }
}
