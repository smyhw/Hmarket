package cat.nyaa.hmarket.message;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.message.data.AoMessageData;
import cat.nyaa.hmarket.utils.ChatComponentUtils;
import cat.nyaa.hmarket.utils.DBFunctionUtils;
import cat.nyaa.hmarket.utils.DatabaseUtils;
import cat.nyaa.hmarket.utils.TaskUtils;
import cat.nyaa.ukit.api.UKitAPI;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static cat.nyaa.hmarket.message.data.AoMessageData.MessageType.JSON;
import static cat.nyaa.hmarket.message.data.AoMessageData.MessageType.STRING_MESSAGE;

// from https://github.com/NyaaCat/aolib
public class AoMessage {
    private static final LinkedBlockingQueue<Runnable> databaseExecutorQueue = new LinkedBlockingQueue<>();
    public static final ExecutorService databaseExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, databaseExecutorQueue);
    @Nullable
    private static AoMessage instance;
    private final JavaPlugin plugin;
    private final SimpleDateFormat simpleDateFormat;
    private final MessageListener listener;
    private Connection jdbcConnection;
    private boolean ukitMessageEnabled = false;

    public AoMessage(JavaPlugin plugin) {
        if (instance != null) {
            throw new IllegalStateException("AoMessage already exists");
        }
        ukitMessageEnabled = Bukkit.getPluginManager().getPlugin("Ukit") != null;
        this.plugin = plugin;
        initDB();
        this.simpleDateFormat = new SimpleDateFormat("'['yy/MM/dd HH:mm Z']'");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        this.listener = new MessageListener(plugin);
        instance = this;
    }

    public static @Nullable AoMessage getInstance() {
        return instance;
    }

    public static Optional<AoMessage> getInstanceOptional() {
        return Optional.ofNullable(instance);
    }

    public void destructor() {
        if (this.jdbcConnection != null) {
            try {
                this.jdbcConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        HandlerList.unregisterAll(listener);
        instance = null;
    }

    public void sendMessageTo(UUID playerId, String... messages) {
        var player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            for (String message : messages) {
                this.newOfflineMessage(playerId, STRING_MESSAGE, message);
            }
        } else {
            player.sendMessage(messages);
        }
    }

    public void sendMessageTo(UUID playerId, Component... messages) {
        var player = Bukkit.getPlayer(playerId);
        for (Component message : messages) {
            if (player == null || !player.isOnline()) {
                if (ukitMessageEnabled) {
                    Bukkit.getAsyncScheduler().runNow(plugin, (task) -> {
                        try {
                            UKitAPI.getAPIInstance().createLoginPush(playerId, message, HMI18n.format("info.message.sender_name"));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    this.newOfflineMessage(playerId, JSON, ChatComponentUtils.toJson(message));
                }
            } else {
                player.sendMessage(message);
            }
        }
    }

    @Deprecated
    // BungeeCord Chat API has been deprecated in favor of Adventure API.
    public void sendMessageTo(UUID playerId, BaseComponent... messages) {
        var player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            this.newOfflineMessage(playerId, JSON, ComponentSerializer.toString(messages));
        } else {
            player.spigot().sendMessage(messages);
        }
    }


    private CompletableFuture<Boolean> newOfflineMessage(UUID playerId, AoMessageData.MessageType messageType, String messageStr) {
        var result = getConnection(
                (conn) ->
                        DatabaseUtils.executeUpdateAsync(
                                        conn,
                                        plugin,
                                        "aomsg/new_offline_message.sql",
                                        databaseExecutor,
                                        messageStr,
                                        messageType.toString(),
                                        playerId.toString(),
                                        System.currentTimeMillis()
                                )
                                .thenApply(optInt -> optInt.isPresent() && optInt.get() > 0)
        );
        if (result.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        } else {
            return result.get();
        }
    }

    private CompletableFuture<List<AoMessageData>> getPlayerOfflineMessageList(UUID playerId) {
        var result = getConnection(
                (conn) ->
                        DatabaseUtils.executeQueryAsync(
                                        conn,
                                        plugin,
                                        "aomsg/get_player_message_data.sql",
                                        databaseExecutor,
                                        DBFunctionUtils.getDataListFromResultSet(AoMessageData.class),
                                        playerId.toString()
                                )
                                .thenApply(optList -> optList.orElse(List.of()))
        );
        if (result.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        } else {
            return result.get();
        }
    }

    private CompletableFuture<Optional<int[]>> deleteOfflineMessage(int... id) {
        return deleteOfflineMessage(Ints.asList(id));
    }

    private CompletableFuture<Optional<int[]>> deleteOfflineMessage(@NotNull List<Integer> id) {
        if (id.isEmpty())
            return CompletableFuture.completedFuture(Optional.empty());
        Optional<CompletableFuture<Optional<int[]>>> result = getConnection((conn) -> CompletableFuture.supplyAsync(() -> {
            try (var ps = conn.prepareStatement("DELETE FROM ao_msg WHERE msg_id=?;")) {

                var autoCommit = conn.getAutoCommit();
                if (autoCommit) {
                    conn.setAutoCommit(false);
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////
                for (int i : id) {
                    ps.setInt(1, i);
                    ps.addBatch();
                }
                var data = Optional.of(ps.executeBatch());
                ////////////////////////////////////////////////////////////////////////////////////////////////
                conn.commit();
                if (autoCommit) conn.setAutoCommit(true);

                return data;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, databaseExecutor));
        if (result.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        } else {
            return result.get();
        }

    }

    private List<Integer> sendMessageData(List<AoMessageData> messageData) {//async
        if (messageData.isEmpty()) return List.of();
        var resultOptional = TaskUtils.async.getSync(() -> {
            List<Integer> result = Lists.newArrayList();
            for (AoMessageData data : messageData) {
                if (sendMessageData0(data)) {
                    result.add(data.msgId());
                }
            }
            return result;
        });
        if (resultOptional.isEmpty()) return List.of();
        return resultOptional.get();
    }

    private boolean sendMessageData0(@NotNull AoMessageData messageData) {//sync
        Player player = Bukkit.getPlayer(messageData.player());
        if (player == null || !player.isOnline()) return false;
        var aft = Component.text(simpleDateFormat.format(new Date(messageData.createdAt())))
                .color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC);
        Component message;
        switch (messageData.msgType()) {
            case STRING_MESSAGE ->
                    message = ChatComponentUtils.fromLegacyText(messageData.msg());
            case JSON ->
                    message = ChatComponentUtils.fromJson(messageData.msg());
            default -> {
                message = ChatComponentUtils.fromLegacyText("unknown message type: " + messageData.msgType());
            }
        }
        player.sendMessage(message.appendNewline().append(aft));
        return true;
    }

    public void initDB() {
        getConnection(conn -> DatabaseUtils.executeUpdateAsync(conn, plugin, "aomsg/init.sql", databaseExecutor));
    }

    private <T> Optional<T> getConnection(Function<Connection, T> function) {
        var optConn = getJdbcConnection();
        if (optConn.isEmpty()) return Optional.empty();
        return optConn.map(function);
    }

    private Optional<Connection> getJdbcConnection() {
        try {
            if (this.jdbcConnection != null && !this.jdbcConnection.isClosed())
                return Optional.of(this.jdbcConnection);
        } catch (SQLException e) {
            if (jdbcConnection != null) {
                try {
                    jdbcConnection.close();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        }
        Optional<Connection> conn = Optional.empty();
        try {
            conn = DatabaseUtils.newSqliteJdbcConnection(plugin, "ao_message.db").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (conn.isEmpty()) {
            plugin.getLogger().warning("[AO MSG]Failed to create jdbc connection");
        } else {
            this.jdbcConnection = conn.get();
            try {
                this.jdbcConnection.createStatement().executeUpdate("PRAGMA synchronous = NORMAL;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.of(this.jdbcConnection);
        }
        return Optional.empty();
    }

    void AfterPlayerJoin(UUID playerId) {
        getPlayerOfflineMessageList(playerId)
                .thenAcceptAsync(list -> {
                    try {
                        deleteOfflineMessage(sendMessageData(list)).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
    }
}
