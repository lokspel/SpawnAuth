package me.lokspel.spawnauth.utils;

import me.lokspel.spawnauth.SpawnAuth;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public class FoliaAPI {
    private static final Map<String, Method> cachedMethods = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();

    private static final BukkitScheduler bS = Bukkit.getScheduler();
    private static final Object globalRegionScheduler;
    private static final Object regionScheduler;
    private static final Object asyncScheduler;
    private static final boolean IS_FOLIA;
    private static JavaPlugin plugin;

    // Cache everything as early as possible
    static {
        // Cache classes first
        cacheClasses();

        // Initialize schedulers
        globalRegionScheduler = getGlobalRegionScheduler();
        regionScheduler = getRegionScheduler();
        asyncScheduler = getAsyncScheduler();

        // Determine if Folia is present once
        IS_FOLIA = determineFolia();

        // Cache all methods
        cacheMethods();
    }

    public static void init(JavaPlugin javaPlugin) {
        plugin = javaPlugin;
    }

    private static void cacheClasses() {
        // Try to cache Folia-specific classes
        tryLoadClass("io.papermc.paper.threadedregions.RegionizedServer");
    }

    private static void tryLoadClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            cachedClasses.put(className, clazz);
        } catch (ClassNotFoundException | LinkageError ignored) {
            // ConcurrentHashMap cannot store null values.
            // Absence is represented by the key simply not being present.
        }
    }

    private static boolean determineFolia() {
        Class<?> regionizedServerClass = cachedClasses.get("io.papermc.paper.threadedregions.RegionizedServer");
        return regionizedServerClass != null && globalRegionScheduler != null && regionScheduler != null;
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            method.setAccessible(true); // Set accessible once during caching
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static void cacheMethods() {
        // Cache methods for globalRegionScheduler
        if (globalRegionScheduler != null) {
            Class<?> grsClass = globalRegionScheduler.getClass();

            Method runAtFixedRateMethod = getMethod(grsClass, "runAtFixedRate", Plugin.class, Consumer.class,
                    long.class, long.class);
            if (runAtFixedRateMethod != null) {
                cachedMethods.put("globalRegionScheduler.runAtFixedRate", runAtFixedRateMethod);
            }

            Method runMethod = getMethod(grsClass, "run", Plugin.class, Consumer.class);
            if (runMethod != null) {
                cachedMethods.put("globalRegionScheduler.run", runMethod);
            }

            Method runDelayedMethod = getMethod(grsClass, "runDelayed", Plugin.class, Consumer.class, long.class);
            if (runDelayedMethod != null) {
                cachedMethods.put("globalRegionScheduler.runDelayed", runDelayedMethod);
            }

            Method cancelTasksMethod = getMethod(grsClass, "cancelTasks", Plugin.class);
            if (cancelTasksMethod != null) {
                cachedMethods.put("globalRegionScheduler.cancelTasks", cancelTasksMethod);
            }
        }

        // Cache methods for regionScheduler
        if (regionScheduler != null) {
            Class<?> rsClass = regionScheduler.getClass();

            Method executeMethod = getMethod(rsClass, "execute", Plugin.class, World.class, int.class, int.class,
                    Runnable.class);
            if (executeMethod != null) {
                cachedMethods.put("regionScheduler.execute", executeMethod);
            }

            Method executeLocationMethod = getMethod(rsClass, "execute", Plugin.class, Location.class, Runnable.class);
            if (executeLocationMethod != null) {
                cachedMethods.put("regionScheduler.executeLocation", executeLocationMethod);
            }

            Method runAtFixedRateMethod = getMethod(rsClass, "runAtFixedRate", Plugin.class, Location.class,
                    Consumer.class, long.class, long.class);
            if (runAtFixedRateMethod != null) {
                cachedMethods.put("regionScheduler.runAtFixedRate", runAtFixedRateMethod);
            }

            Method runDelayedMethod = getMethod(rsClass, "runDelayed", Plugin.class, Location.class, Consumer.class,
                    long.class);
            if (runDelayedMethod != null) {
                cachedMethods.put("regionScheduler.runDelayed", runDelayedMethod);
            }
        }

        // Cache methods for entity scheduler
        Method getSchedulerMethod = getMethod(Entity.class, "getScheduler");
        if (getSchedulerMethod != null) {
            cachedMethods.put("entity.getScheduler", getSchedulerMethod);
        }

        // Cache method for Player teleportAsync
        Method teleportAsyncMethod = getMethod(Player.class, "teleportAsync", Location.class);
        if (teleportAsyncMethod != null) {
            cachedMethods.put("player.teleportAsync", teleportAsyncMethod);
        }

        // Cache methods for asyncScheduler
        if (asyncScheduler != null) {
            Class<?> asClass = asyncScheduler.getClass();

            Method cancelTasksMethod = getMethod(asClass, "cancelTasks", Plugin.class);
            if (cancelTasksMethod != null) {
                cachedMethods.put("asyncScheduler.cancelTasks", cancelTasksMethod);
            }

            Method runNowMethod = getMethod(asClass, "runNow", Plugin.class, Consumer.class);
            if (runNowMethod != null) {
                cachedMethods.put("asyncScheduler.runNow", runNowMethod);
            }

            Method runDelayedMethod = getMethod(asClass, "runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
            if (runDelayedMethod != null) {
                cachedMethods.put("asyncScheduler.runDelayed", runDelayedMethod);
            }

            Method runAtFixedRateMethod = getMethod(asClass, "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
            if (runAtFixedRateMethod != null) {
                cachedMethods.put("asyncScheduler.runAtFixedRate", runAtFixedRateMethod);
            }
        }
    }

    private static Object invokeMethod(Method method, Object object, Object... args) {
        try {
            if (method != null && object != null) {
                return method.invoke(object, args);
            }
        } catch (Exception e) {
            final SpawnAuth plugin = SpawnAuth.getInstance();
            if (plugin != null) {
                plugin.getLogger().log(Level.SEVERE,
                        "A reflective Folia scheduler call failed. This usually means the running server API does not match the expected Paper/Folia methods.",
                        e);
            } else {
                Bukkit.getLogger().log(Level.SEVERE,
                        "[SpawnAuth] A reflective Folia scheduler call failed. This usually means the running server API does not match the expected Paper/Folia methods.",
                        e);
            }
        }
        return null;
    }

    private static Object getGlobalRegionScheduler() {
        Method method = getMethod(Server.class, "getGlobalRegionScheduler");
        return invokeMethod(method, Bukkit.getServer());
    }

    private static Object getRegionScheduler() {
        Method method = getMethod(Server.class, "getRegionScheduler");
        return invokeMethod(method, Bukkit.getServer());
    }

    private static Object getAsyncScheduler() {
        Method method = getMethod(Server.class, "getAsyncScheduler");
        return invokeMethod(method, Bukkit.getServer());
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runTaskAsync(Runnable run, long delay) {
        if (!IS_FOLIA) {
            bS.runTaskLaterAsynchronously(SpawnAuth.getInstance(), run, delay);
            return;
        }

        if (delay <= 0L) {
            Method method = cachedMethods.get("asyncScheduler.runNow");
            invokeMethod(method, asyncScheduler, SpawnAuth.getInstance(), (Consumer<Object>) ignored -> run.run());
            return;
        }

        Method method = cachedMethods.get("asyncScheduler.runDelayed");
        invokeMethod(
                method,
                asyncScheduler,
                SpawnAuth.getInstance(),
                (Consumer<Object>) ignored -> run.run(),
                delay * 50L,
                TimeUnit.MILLISECONDS
        );
    }

    public static void runTaskAsync(Runnable run) {
        runTaskAsync(run, 1L);
    }

    public static void runTaskTimerAsync(Consumer<Object> run, long delay, long period) {
        if (!IS_FOLIA) {
            bS.runTaskTimerAsynchronously(SpawnAuth.getInstance(), () -> run.accept(null), delay, period);
            return;
        }
        Method method = cachedMethods.get("asyncScheduler.runAtFixedRate");
        invokeMethod(
                method,
                asyncScheduler,
                SpawnAuth.getInstance(),
                run,
                delay * 50L,
                period * 50L,
                TimeUnit.MILLISECONDS
        );
    }

    public static void runTaskTimerAsync(Runnable runnable, long delay, long period) {
        runTaskTimerAsync(obj -> runnable.run(), delay, period);
    }

    public static void runTaskTimer(Consumer<Object> run, long delay, long period) {
        if (!IS_FOLIA) {
            bS.runTaskTimer(SpawnAuth.getInstance(), () -> run.accept(null), delay, period);
            return;
        }
        Method method = cachedMethods.get("globalRegionScheduler.runAtFixedRate");
        invokeMethod(method, globalRegionScheduler, SpawnAuth.getInstance(), run, delay, period);
    }

    public static void runTask(Runnable run) {
        if (!IS_FOLIA) {
            bS.runTask(SpawnAuth.getInstance(), run);
            return;
        }
        Method method = cachedMethods.get("globalRegionScheduler.run");
        invokeMethod(method, globalRegionScheduler, SpawnAuth.getInstance(),
                (Consumer<Object>) ignored -> run.run());
    }

    public static void runTask(Consumer<Object> run) {
        if (!IS_FOLIA) {
            bS.runTask(SpawnAuth.getInstance(), () -> run.accept(null));
            return;
        }
        Method method = cachedMethods.get("globalRegionScheduler.run");
        invokeMethod(method, globalRegionScheduler, SpawnAuth.getInstance(), run);
    }

    // NEW METHOD ADDED FOR CHUNK-BASED TASK SCHEDULING
    public static void runTask(Chunk chunk, Runnable run) {
        if (!IS_FOLIA) {
            bS.runTask(SpawnAuth.getInstance(), run);
            return;
        }
        if (chunk == null)
            return;
        runTaskForRegion(chunk.getWorld(), chunk.getX(), chunk.getZ(), run);
    }

    public static void runTaskLater(Runnable run, long delay) {
        if (!IS_FOLIA) {
            bS.runTaskLater(SpawnAuth.getInstance(), run, delay);
            return;
        }
        Method method = cachedMethods.get("globalRegionScheduler.runDelayed");
        invokeMethod(method, globalRegionScheduler, SpawnAuth.getInstance(), (Consumer<Object>) ignored -> run.run(),
                delay);
    }

    public static void runTaskLater(Consumer<Object> run, long delay) {
        if (!IS_FOLIA) {
            bS.runTaskLater(SpawnAuth.getInstance(), () -> run.accept(null), delay);
            return;
        }
        Method method = cachedMethods.get("globalRegionScheduler.runDelayed");
        invokeMethod(method, globalRegionScheduler, SpawnAuth.getInstance(), run, delay);
    }

    public static void runTaskForEntity(Entity entity, Runnable run, Runnable retired, long delay) {
        if (!IS_FOLIA) {
            if (delay == 0 && Bukkit.isPrimaryThread()) {
                run.run();
                return;
            }
            bS.runTaskLater(SpawnAuth.getInstance(), run, delay);
            return;
        }
        if (entity == null)
            return;

        Method getSchedulerMethod = cachedMethods.get("entity.getScheduler");
        Object entityScheduler = invokeMethod(getSchedulerMethod, entity);

        if (entityScheduler != null) {
            String executeKey = "entityScheduler.execute";
            Method executeMethod = cachedMethods.get(executeKey);

            if (executeMethod == null) {
                executeMethod = getMethod(entityScheduler.getClass(), "execute", Plugin.class, Runnable.class,
                        Runnable.class, long.class);
                if (executeMethod != null) {
                    cachedMethods.put(executeKey, executeMethod);
                }
            }

            invokeMethod(executeMethod, entityScheduler, SpawnAuth.getInstance(), run, retired, delay);
        }
    }

    public static void runTaskForEntity(Entity entity, Runnable run) {
        runTaskForEntity(entity, run, () -> {
        }, 0L);
    }

    public static void runTaskForEntityRepeating(Entity entity, Consumer<Object> task, Runnable retired,
                                                 long initialDelay, long period) {
        if (!IS_FOLIA) {
            bS.runTaskTimer(SpawnAuth.getInstance(), () -> task.accept(null), initialDelay, period);
            return;
        }
        if (entity == null)
            return;

        Method getSchedulerMethod = cachedMethods.get("entity.getScheduler");
        Object entityScheduler = invokeMethod(getSchedulerMethod, entity);

        if (entityScheduler != null) {
            String runAtFixedRateKey = "entityScheduler.runAtFixedRate";
            Method runAtFixedRateMethod = cachedMethods.get(runAtFixedRateKey);

            if (runAtFixedRateMethod == null) {
                runAtFixedRateMethod = getMethod(entityScheduler.getClass(), "runAtFixedRate", Plugin.class,
                        Consumer.class, Runnable.class, long.class, long.class);
                if (runAtFixedRateMethod != null) {
                    cachedMethods.put(runAtFixedRateKey, runAtFixedRateMethod);
                }
            }

            invokeMethod(runAtFixedRateMethod, entityScheduler, SpawnAuth.getInstance(), task, retired, initialDelay,
                    period);
        }
    }

    public static void runTaskForRegion(World world, int chunkX, int chunkZ, Runnable run) {
        if (!IS_FOLIA) {
            bS.runTask(SpawnAuth.getInstance(), run);
            return;
        }
        if (world == null)
            return;
        Method executeMethod = cachedMethods.get("regionScheduler.execute");
        invokeMethod(executeMethod, regionScheduler, SpawnAuth.getInstance(), world, chunkX, chunkZ, run);
    }

    public static void runTaskForRegion(Location location, Runnable run) {
        if (!IS_FOLIA) {
            bS.runTask(SpawnAuth.getInstance(), run);
            return;
        }
        if (location == null)
            return;
        Method executeMethod = cachedMethods.get("regionScheduler.executeLocation");
        invokeMethod(executeMethod, regionScheduler, SpawnAuth.getInstance(), location, run);
    }

    public static void runTaskForRegion(Chunk chunk, Runnable run) {
        if (!IS_FOLIA) {
            bS.runTask(SpawnAuth.getInstance(), run);
            return;
        }
        if (chunk == null)
            return;
        runTaskForRegion(chunk.getWorld(), chunk.getX(), chunk.getZ(), run);
    }

    public static void runTaskForRegionOrAsync(Chunk chunk, Runnable run) {
        if (!IS_FOLIA) {
            bS.runTaskAsynchronously(SpawnAuth.getInstance(), run);
            return;
        }
        if (chunk == null)
            return;
        runTaskForRegion(chunk.getWorld(), chunk.getX(), chunk.getZ(), run);
    }

    public static void runTaskForRegionRepeating(Location location, Consumer<Object> task, long initialDelay,
                                                 long period) {
        if (!IS_FOLIA) {
            bS.runTaskTimer(SpawnAuth.getInstance(), () -> task.accept(null), initialDelay, period);
            return;
        }
        if (location == null)
            return;
        Method runAtFixedRateMethod = cachedMethods.get("regionScheduler.runAtFixedRate");
        invokeMethod(runAtFixedRateMethod, regionScheduler, SpawnAuth.getInstance(), location, task, initialDelay,
                period);
    }

    public static void runTaskForRegionDelayed(Location location, Consumer<Object> task, long delay) {
        if (!IS_FOLIA) {
            bS.runTaskLater(SpawnAuth.getInstance(), () -> task.accept(null), delay);
            return;
        }
        if (location == null)
            return;
        Method runDelayedMethod = cachedMethods.get("regionScheduler.runDelayed");
        invokeMethod(runDelayedMethod, regionScheduler, SpawnAuth.getInstance(), location, task, delay);
    }

    public static CompletableFuture<Boolean> teleportPlayer(Player e, Location location, Boolean async, Runnable complete) {
        if (!IS_FOLIA) {
            FoliaAPI.runTask(() -> {
                e.teleport(location);
                if (complete != null) complete.run();
            });
            return CompletableFuture.completedFuture(true);
        } else {
            Method teleportMethod = cachedMethods.get("player.teleportAsync");
            FoliaAPI.runTaskForEntity(e, () -> {
                Object result = invokeMethod(teleportMethod, e, location);
                if (result instanceof CompletableFuture<?> future && complete != null) {
                    future.whenComplete((ignored, throwable) -> complete.run());
                    return;
                }

                if (complete != null) complete.run();
            }, () -> {
            }, 1L);
            return CompletableFuture.completedFuture(true);
        }
    }

    public static CompletableFuture<Boolean> teleportPlayer(Player e, Location location, Boolean async) {
        return teleportPlayer(e, location, async, null);
    }

    public static void cancelAllTasks() {
        Plugin plugin = SpawnAuth.getInstance();
        if (!IS_FOLIA) {
            bS.cancelTasks(plugin);
            return;
        }

        Method cancelGlobalMethod = cachedMethods.get("globalRegionScheduler.cancelTasks");
        invokeMethod(cancelGlobalMethod, globalRegionScheduler, plugin);

        Method cancelAsyncMethod = cachedMethods.get("asyncScheduler.cancelTasks");
        invokeMethod(cancelAsyncMethod, asyncScheduler, plugin);
    }

    public static void runTaskLater(Location location, Runnable run, long delay) {
        if (!IS_FOLIA) {
            // Standard Bukkit scheduling
            bS.runTaskLater(SpawnAuth.getInstance(), run, delay);
            return;
        }

        if (location == null)
            return;

        // Folia region-based delayed scheduling
        Method runDelayedMethod = cachedMethods.get("regionScheduler.runDelayed");
        invokeMethod(
                runDelayedMethod,
                regionScheduler,
                SpawnAuth.getInstance(),
                location,
                (Consumer<Object>) ignored -> run.run(),
                delay);
    }

    public static void runTaskLater(Chunk chunk, Runnable run, long delay) {
        if (!IS_FOLIA) {
            bS.runTaskLater(SpawnAuth.getInstance(), run, delay);
            return;
        }
        if (chunk == null)
            return;

        // Create a temporary location at the chunk's coordinates to target the correct
        // region
        Location location = new Location(chunk.getWorld(), chunk.getX() << 4, 0, chunk.getZ() << 4);
        runTaskLater(location, run, delay);
    }
}