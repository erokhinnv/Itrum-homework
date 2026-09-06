import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Имитация трёх независимых внешних источников данных.
 * Каждый вызов «ходит по сети» 1-3 секунды и с вероятностью 20% падает.
 */
public class ProductService {

    /** Вероятность сбоя источника. */
    private static final double FAILURE_PROBABILITY = 0.2;

    private static final int MIN_DELAY_MS = 1000;
    private static final int MAX_DELAY_MS = 3000;

    public double fetchPrice(String productName) {
        simulateCall("цены", productName);
        return 899.99;
    }

    public String fetchDescription(String productName) {
        simulateCall("описания", productName);
        return "Игровой " + productName.toLowerCase(Locale.ROOT) + " с диагональю 15.6\"";
    }

    public double fetchRating(String productName) {
        simulateCall("рейтинга", productName);
        return 4.7;
    }

    /**
     * Задержка сети плюс случайный сбой источника.
     * RuntimeException примерно в 20% случаев
     */
    private void simulateCall(String source, String productName) {
        long delay = ThreadLocalRandom.current().nextLong(MIN_DELAY_MS, MAX_DELAY_MS + 1);
        System.out.printf("  [%s] запрос %s для '%s' (%d мс)...%n",
                Thread.currentThread().getName(), source, productName, delay);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запрос " + source + " прерван", e);
        }
        if (ThreadLocalRandom.current().nextDouble() < FAILURE_PROBABILITY) {
            throw new RuntimeException("Сервис " + source + " недоступен");
        }
    }
}
