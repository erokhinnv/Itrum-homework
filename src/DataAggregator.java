import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Собирает карточку товара из трёх источников параллельно.
 * Сбой любого источника не роняет сборку: вместо него подставляется значение по умолчанию.
 */
public class DataAggregator {

    private static final double DEFAULT_PRICE = 0.0;
    private static final String DEFAULT_DESCRIPTION = "Нет данных";
    private static final double DEFAULT_RATING = 0.0;

    private final ProductService service;
    private final ExecutorService executor;

    public DataAggregator(ProductService service) {
        this.service = service;
        // Задачи блокирующие (sleep), поэтому свой пул, а не общий ForkJoinPool.
        this.executor = Executors.newFixedThreadPool(3, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    /** Промежуточный результат первого thenCombine. */
    private record PriceAndDescription(double price, String description) {
    }

    /**
     * Асинхронная сборка: возвращает управление мгновенно, основной поток не блокируется.
     */
    public CompletableFuture<ProductInfo> aggregateProductInfoAsync(String productName) {
        CompletableFuture<Double> priceFuture =
                CompletableFuture.supplyAsync(() -> service.fetchPrice(productName), executor)
                        .exceptionally(ex -> fallback("цена", ex, DEFAULT_PRICE));

        CompletableFuture<String> descriptionFuture =
                CompletableFuture.supplyAsync(() -> service.fetchDescription(productName), executor)
                        .exceptionally(ex -> fallback("описание", ex, DEFAULT_DESCRIPTION));

        CompletableFuture<Double> ratingFuture =
                CompletableFuture.supplyAsync(() -> service.fetchRating(productName), executor)
                        .exceptionally(ex -> fallback("рейтинг", ex, DEFAULT_RATING));

        return priceFuture
                .thenCombine(descriptionFuture, (Double price, String description) -> {
                    return new PriceAndDescription(price, description);
                })
                .thenCombine(ratingFuture, (PriceAndDescription partial, Double rating) -> {
                    return new ProductInfo(productName, partial.price(), partial.description(), rating);
                });
    }

    /** Логирует сбой источника и подставляет значение по умолчанию. */
    private static <T> T fallback(String source, Throwable ex, T defaultValue) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        System.out.printf("  [!] %s: %s -> подставляем '%s'%n", source, cause.getMessage(), defaultValue);
        return defaultValue;
    }

    /** Останавливает пул потоков агрегатора. */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        DataAggregator aggregator = new DataAggregator(new ProductService());
        try {
            String productName = "Ноутбук";
            long start = System.currentTimeMillis();

            System.out.println("Запускаем сбор данных о товаре '" + productName + "'...");
            CompletableFuture<ProductInfo> future = aggregator.aggregateProductInfoAsync(productName);

            // Основной поток свободен, пока источники отвечают.
            System.out.println("Основной поток не заблокирован, продолжаем работу...");

            ProductInfo info = future.join();
            long elapsed = System.currentTimeMillis() - start;

            System.out.println();
            System.out.println(info);
            System.out.printf("Затрачено: %d мс (три запроса по 1-3 с выполнялись параллельно)%n", elapsed);
        } finally {
            aggregator.shutdown();
        }
    }
}
