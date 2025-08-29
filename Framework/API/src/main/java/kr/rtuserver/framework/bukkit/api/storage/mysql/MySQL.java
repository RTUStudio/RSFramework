package kr.rtuserver.framework.bukkit.api.storage.mysql;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.storage.Storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQL implements Storage {

    private final RSPlugin plugin;
    private final MySQLConfig config;

    private final Gson gson = new Gson();
    private final String driver = "com.mysql.cj.jdbc.Driver";
    private final String prefix;
    private HikariDataSource hikariDataSource;
    private Connection connection;

    public MySQL(RSPlugin plugin, List<String> list) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration().getStorage().getMysql();
        this.prefix = config.getTablePrefix();
        hikariDataSource = new HikariDataSource(hikariConfig());
        hikariDataSource.setMaximumPoolSize(30);
        try {
            connection = hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            for (String table : list) {
                String query =
                        "CREATE TABLE IF NOT EXISTS `"
                                + prefix
                                + table
                                + "` (`data` JSON NOT NULL);";
                PreparedStatement ps = getConnection().prepareStatement(query);
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isNull(JsonObject json) {
        return json == null || json.isEmpty() || json.isJsonNull();
    }

    private Connection getConnection() throws SQLException {
        if (connection.isClosed()) {
            connection = hikariDataSource.getConnection();
        }
        return connection;
    }

    @NotNull
    private HikariConfig hikariConfig() {
        String serverHost = config.getHost() + ":" + config.getPort();
        String url =
                "jdbc:mysql://"
                        + serverHost
                        + "/"
                        + config.getDatabase()
                        + "?serverTimezone=UTC&useUniCode=yes&characterEncoding=UTF-8";
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

    private void debug(String type, String collection, String json) {
        plugin.verbose("[Storage] " + type + ": " + collection + " - " + json);
    }

    private String getDebugQuery(String query, List<Object> values) {
        String finalQuery = query;
        for (Object value : values) {
            String formattedValue;
            if (value instanceof String) {
                formattedValue = "'" + value.toString().replace("'", "''") + "'";
            } else if (value == null) {
                formattedValue = "NULL";
            } else {
                formattedValue = value.toString();
            }
            finalQuery =
                    finalQuery.replaceFirst(
                            "\\?", java.util.regex.Matcher.quoteReplacement(formattedValue));
        }
        return finalQuery;
    }

    @Override
    public CompletableFuture<Result> add(@NotNull String table, @NotNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (isNull(data)) return Result.FAILED;
                    String json = gson.toJson(data);
                    String query = "INSERT INTO " + prefix + table + " (data) VALUES (?);";
                    try {
                        PreparedStatement ps = getConnection().prepareStatement(query);
                        ps.setString(1, json);
                        debug("ADD", table, getDebugQuery(query, List.of(json)));
                        if (ps.execute()) return Result.UPDATED;
                        else return Result.UNCHANGED;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public CompletableFuture<Result> set(
            @NotNull String table, @Nullable JsonObject find, @Nullable JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    StringBuilder query = new StringBuilder();
                    List<Object> values = new ArrayList<>();
                    if (isNull(data)) {
                        query.append("DELETE FROM ").append(prefix).append(table);
                    } else {
                        query.append("UPDATE ")
                                .append(prefix)
                                .append(table)
                                .append(" SET data = JSON_SET(data, ");
                        List<String> jsonSetArgs = new ArrayList<>();
                        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                            String key = entry.getKey();
                            JsonElement element = entry.getValue();
                            jsonSetArgs.add("'$." + key + "'");
                            if (element.isJsonNull()) {
                                jsonSetArgs.add("NULL");
                            } else if (element.isJsonPrimitive()) {
                                JsonPrimitive primitive = element.getAsJsonPrimitive();
                                jsonSetArgs.add("?");
                                if (primitive.isNumber()) values.add(primitive.getAsNumber());
                                else if (primitive.isBoolean())
                                    values.add(primitive.getAsBoolean());
                                else if (primitive.isString()) values.add(primitive.getAsString());
                                else {
                                    plugin.console(
                                            "<red>Unsupported type of data tried to be saved! Only supports JsonElement, Number, Boolean and String</red>");
                                    plugin.console(
                                            "<red>지원하지 않는 타입의 데이터가 저장되려고 했습니다! JsonElement, Number, Boolean, String만 지원합니다</red>");
                                    return Result.FAILED;
                                }
                            } else {
                                jsonSetArgs.add("CAST(? AS JSON)");
                                values.add(gson.toJson(element));
                            }
                        }
                        query.append(String.join(", ", jsonSetArgs)).append(")");
                    }
                    if (!isNull(find)) {
                        Pair<String, List<Object>> filterPair = filter(find);
                        query.append(filterPair.getLeft());
                        values.addAll(filterPair.getRight());
                    }
                    try {
                        PreparedStatement ps = getConnection().prepareStatement(query + ";");
                        setParameters(ps, values);
                        debug("SET", table, getDebugQuery(query + ";", values));
                        if (ps.execute()) return Result.UPDATED;
                        else return Result.UNCHANGED;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public CompletableFuture<List<JsonObject>> get(
            @NotNull String table, @NotNull JsonObject find) {
        return CompletableFuture.supplyAsync(
                () -> {
                    List<JsonObject> result = new ArrayList<>();
                    StringBuilder query =
                            new StringBuilder("SELECT * FROM ").append(prefix).append(table);
                    List<Object> values = new ArrayList<>();
                    if (!isNull(find)) {
                        Pair<String, List<Object>> filterPair = filter(find);
                        query.append(filterPair.getLeft());
                        values.addAll(filterPair.getRight());
                    }
                    try {
                        PreparedStatement ps = getConnection().prepareStatement(query + ";");
                        setParameters(ps, values);
                        debug("GET", table, getDebugQuery(query + ";", values));
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            result.add(gson.fromJson(rs.getString("data"), JsonObject.class));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return result;
                });
    }

    private Pair<String, List<Object>> filter(JsonObject find) {
        StringBuilder query = new StringBuilder(" WHERE ");
        List<String> filters = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : find.entrySet()) {
            String filter =
                    config.isUseArrowOperator()
                            ? "data ->> '$."
                            : "JSON_UNQUOTE(JSON_EXTRACT(data, '$.";
            filter += entry.getKey() + (config.isUseArrowOperator() ? "'" : "'))");
            if (entry.getValue().isJsonNull()) {
                filter += " IS NULL";
            } else if (entry.getValue() instanceof JsonPrimitive primitive) {
                filter += " = ?";
                if (primitive.isBoolean()) values.add(primitive.getAsBoolean());
                else if (primitive.isNumber()) values.add(primitive.getAsNumber());
                else if (primitive.isString()) values.add(primitive.getAsString());
            } else {
                filter += " = CAST(? as JSON)";
                values.add(gson.toJson(entry.getValue()));
            }
            filters.add(filter);
        }
        query.append(String.join(" AND ", filters));
        return Pair.of(query.toString(), values);
    }

    private void setParameters(PreparedStatement statement, List<Object> values)
            throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            statement.setObject(i + 1, values.get(i));
        }
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
