package kr.rtuserver.framework.bukkit.api.storage.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.storage.Storage;
import kr.rtuserver.framework.bukkit.api.storage.config.MySQLConfig;
import kr.rtuserver.protoweaver.api.protocol.internal.StorageSync;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQL implements Storage {

    private final RSPlugin plugin;
    private final MySQLConfig config;
    private final boolean verbose;

    private final Gson gson = new Gson();
    private final String driver = "com.mysql.cj.jdbc.Driver";
    private final String prefix;
    private HikariDataSource hikariDataSource;
    private Connection connection;

    public MySQL(RSPlugin plugin, List<String> list) {
        this.plugin = plugin;
        this.config = plugin.getConfigurations().getMysql();
        this.verbose = plugin.getConfigurations().getSetting().isVerbose();
        this.prefix = config.getTablePrefix();
        HikariConfig hikariConfig = getHikariConfig(plugin);
        hikariDataSource = new HikariDataSource(hikariConfig);
        hikariDataSource.setMaximumPoolSize(30);
        try {
            connection = hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            for (String table : list) {
                String query = "CREATE TABLE IF NOT EXISTS `" + prefix + table + "` (`data` JSON NOT NULL);";
                PreparedStatement ps = getConnection().prepareStatement(query);
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection.isClosed()) {
            connection = hikariDataSource.getConnection();
        }
        return connection;
    }

    @NotNull
    private HikariConfig getHikariConfig(RSPlugin plugin) {
        String serverHost = config.getHost() + ":" + config.getPort();
        String url = "jdbc:mysql://" + serverHost + "/" + config.getDatabase() + "?serverTimezone=UTC&useUniCode=yes&characterEncoding=UTF-8";
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driver);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return hikariConfig;
    }

    @Override
    public boolean add(@NotNull String table, @NotNull JsonObject data) {
        String json = gson.toJson(data);
        try {
            //INSERT INTO `test` (`data`) VALUES ('{"A": B"}');
            PreparedStatement ps = getConnection().prepareStatement("INSERT INTO " + prefix + table + " (data) VALUES ('" + json + "');");
            if (!ps.execute()) return false;
            StorageSync sync = new StorageSync(plugin.getName(), table);
            plugin.getFramework().getProtoWeaver().sendPacket(sync);
            return true;
        } catch (SQLException e) {
            //if (verbose) throw new RuntimeException(e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean set(@NotNull String table, Pair<String, Object> find, Pair<String, Object> data) {
        String query;
        if (data != null) {

            String value;
            Object object = data.getValue();
            if (object instanceof JsonElement element) value = "CAST('" + element + "' as JSON)";
            else if (object instanceof Number number) value = String.valueOf(number);
            else if (object instanceof Boolean bool) value = String.valueOf(bool);
            else if (object instanceof String str) value = "'" + str + "'";
            else {
                plugin.console("<red>Unsupported type of data tried to be saved! Only supports JsonElement, Number, Boolean, and String</red>");
                plugin.console("<red>지원하지 않는 타입의 데이터가 저장되려고 했습니다! JsonElement, Number, Boolean, String만 지원합니다</red>");
                return false;
            }

            query = "UPDATE " + prefix + table + " SET data = JSON_SET(data, '$." + data.getKey() + "', " + value + ")";
        } else query = "DELETE FROM " + prefix + table;
        if (find != null) {
            Object value = find.getValue();
            if (value instanceof JsonObject jsonObject) value = jsonObject.toString();
            if (config.isUseArrowOperator()) query += " WHERE data ->> '$." + find.getKey() + "' LIKE '" + value + "'";
            else query += " WHERE JSON_UNQUOTE( JSON_EXTRACT(data, '$." + find.getKey() + "')) LIKE '" + value + "'";
        }
        try {
            PreparedStatement ps = getConnection().prepareStatement(query + ";");
            if (!ps.execute()) return false;
            StorageSync sync = new StorageSync(plugin.getName(), table);
            plugin.getFramework().getProtoWeaver().sendPacket(sync);
            return true;
        } catch (SQLException e) {
            //if (verbose) throw new RuntimeException(e);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<JsonObject> get(@NotNull String table, Pair<String, Object> find) {
        List<JsonObject> result = new ArrayList<>();
        String query = "SELECT * FROM " + prefix + table;
        if (find != null) {
            Object value = find.getValue();
            if (value instanceof JsonObject jsonObject) value = jsonObject.toString();

            if (config.isUseArrowOperator()) query += " WHERE data ->> '$." + find.getKey() + "' LIKE '" + value + "'";
            else query += " WHERE JSON_UNQUOTE( JSON_EXTRACT(data, '$." + find.getKey() + "')) LIKE '" + value + "'";
        }
        try {
            PreparedStatement ps = getConnection().prepareStatement(query + ";");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(gson.fromJson(rs.getString("data"), JsonObject.class));
            }
        } catch (SQLException e) {
            //if (verbose) throw new RuntimeException(e);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() {
        try {
            if (hikariDataSource != null) {
                connection.close();
                hikariDataSource.close();
                hikariDataSource = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
