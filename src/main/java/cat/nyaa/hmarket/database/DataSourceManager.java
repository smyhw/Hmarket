package cat.nyaa.hmarket.database;

import cat.nyaa.hmarket.HmarketConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceManager {
    private final HikariDataSource ds;
    @Nullable
    private static DataSourceManager instance;

    @Nullable
    public static DataSourceManager getInstance() {
        return instance;
    }

    public static void init(HmarketConfig conf) {
        instance = new DataSourceManager(conf);
    }
    private DataSourceManager(@NotNull HmarketConfig conf){
        File f = new File(conf.getPlugin().getDataFolder(), conf.DBFile);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + f.getAbsolutePath());
        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void close() {
        ds.close();
        instance = null;
    }
}
