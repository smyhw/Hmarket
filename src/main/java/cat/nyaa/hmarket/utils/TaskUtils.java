package cat.nyaa.hmarket.utils;

import cat.nyaa.hmarket.Hmarket;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

// from https://github.com/NyaaCat/aolib
public class TaskUtils {
    public static class async {
        public static Executor mainThreadExecutor = Bukkit.getScheduler().getMainThreadExecutor(Hmarket.getInstance());

        public static <T> T getSyncDefault(@NotNull Supplier<@NotNull T> supplier, @Nullable T defaultValue) {
            var result = getSync(supplier);
            if (result.isEmpty()) {
                return defaultValue;
            }
            return result.get();
        }

        public static <T> Optional<T> getSync(@NotNull Supplier<T> supplier) {
            try {
                return Optional.ofNullable(getSyncThrow(supplier));
            } catch (CancellationException cancellationException) {
                Bukkit.getLogger().warning("Exception in main thread executor");
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }

        public static <T> T getSyncThrow(@NotNull Supplier<T> supplier) throws ExecutionException, InterruptedException,
                CancellationException {
            var result = callSync(supplier);
            return result.get();
        }

        public static <T> CompletableFuture<T> callSync(@NotNull Supplier<T> supplier) {
            if (Bukkit.isPrimaryThread()) {
                return CompletableFuture.completedFuture(supplier.get());
            } else {
                return runSyncMethod(supplier);
            }
        }

        public static @NotNull CompletableFuture<Void> callSync(@NotNull Runnable runnable) {
            if (Bukkit.isPrimaryThread()) {
                runnable.run();
                return CompletableFuture.completedFuture(null);
            } else {
                return runSyncMethod(runnable);
            }

        }

        public static <T> @NotNull CompletableFuture<T> runSyncMethod(@NotNull Supplier<T> task) {
            return CompletableFuture.supplyAsync(task, mainThreadExecutor);
        }

        public static @NotNull CompletableFuture<Void> runSyncMethod(@NotNull Runnable task) {
            return CompletableFuture.runAsync(task, mainThreadExecutor);
        }
    }
}