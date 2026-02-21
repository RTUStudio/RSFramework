package kr.rtustudio.storage.mysql;

import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageLogger;
import lombok.extern.slf4j.Slf4j;

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
import org.jspecify.annotations.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Slf4j
public class MySQL implements Storage {

    public interface Config {
        String getHost();

        int getPort();

        String getDatabase();

        String getUsername();

        String getPassword();

        String getTablePrefix();

        boolean isUseArrowOperator();
    }

    private final Config config;
    private final Gson gson = new Gson();
    private final String prefix;
    private HikariDataSource hikariDataSource;

    public MySQL(Config config, List<String> tables) {
        this.config = config;
        this.prefix = config.getTablePrefix();
        this.hikariDataSource = new HikariDataSource(hikariConfig());
        initializeTables(tables);
    }

    private void initializeTables(List<String> tables) {
        try (Connection connection = hikariDataSource.getConnection()) {
            for (String table : tables) {
                String query =
                        "CREATE TABLE IF NOT EXISTS `"
                                + prefix
                                + table
                                + "` (`data` JSON NOT NULL);";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.execute();
                }
            }
        } catch (SQLException e) {
            log.error("Failed to create tables", e);
        }
    }

    private boolean isNull(JsonObject json) {
        return json == null || json.isEmpty() || json.isJsonNull();
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
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(30);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return hikariConfig;
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
    public @NonNull CompletableFuture<Result> add(@NotNull String table, @NotNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (isNull(data)) return Result.FAILED;
                    String json = gson.toJson(data);
                    String query =
                            "INSERT INTO `" + prefix + table + "` (data) VALUES (CAST(? AS JSON));";
                    try (Connection connection = hikariDataSource.getConnection();
                            PreparedStatement ps = connection.prepareStatement(query)) {
                        ps.setString(1, json);
                        StorageLogger.logAdd(log, table, getDebugQuery(query, List.of(json)));
                        return ps.executeUpdate() > 0 ? Result.UPDATED : Result.UNCHANGED;
                    } catch (SQLException e) {
                        StorageLogger.logError(log, "ADD", table, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<Result> set(
            @NotNull String table, @NonNull JsonObject find, @NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    StringBuilder query = new StringBuilder();
                    List<Object> values = new ArrayList<>();
                    if (isNull(data)) {
                        query.append("DELETE FROM `").append(prefix).append(table).append("`");
                    } else {
                        query.append("UPDATE `")
                                .append(prefix)
                                .append(table)
                                .append("` SET data = JSON_SET(data, ");
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
                                    log.error("Unsupported primitive type for key '{}'", key);
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
                    query.append(";");
                    try (Connection connection = hikariDataSource.getConnection();
                            PreparedStatement ps = connection.prepareStatement(query.toString())) {
                        setParameters(ps, values);
                        StorageLogger.logSet(log, table, getDebugQuery(query.toString(), values));
                        return ps.executeUpdate() > 0 ? Result.UPDATED : Result.UNCHANGED;
                    } catch (SQLException e) {
                        StorageLogger.logError(log, "SET", table, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<List<JsonObject>> get(
            @NotNull String table, @NotNull JsonObject find) {
        return CompletableFuture.supplyAsync(
                () -> {
                    List<JsonObject> result = new ArrayList<>();
                    StringBuilder query =
                            new StringBuilder("SELECT `data` FROM `")
                                    .append(prefix)
                                    .append(table)
                                    .append("`");
                    List<Object> values = new ArrayList<>();
                    if (!isNull(find)) {
                        Pair<String, List<Object>> filterPair = filter(find);
                        query.append(filterPair.getLeft());
                        values.addAll(filterPair.getRight());
                    }
                    query.append(";");
                    try (Connection connection = hikariDataSource.getConnection();
                            PreparedStatement ps = connection.prepareStatement(query.toString())) {
                        setParameters(ps, values);
                        StorageLogger.logGet(log, table, getDebugQuery(query.toString(), values));
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                result.add(gson.fromJson(rs.getString("data"), JsonObject.class));
                            }
                        }
                    } catch (SQLException e) {
                        StorageLogger.logError(log, "GET", table, e);
                    }
                    return result;
                });
    }

    private Pair<String, List<Object>> filter(JsonObject find) {
        StringBuilder query = new StringBuilder(" WHERE ");
        List<String> filters = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : find.entrySet()) {
            StringBuilder filter = new StringBuilder();
            if (config.isUseArrowOperator()) {
                filter.append("data ->> '$.").append(entry.getKey()).append("'");
            } else {
                filter.append("JSON_UNQUOTE(JSON_EXTRACT(data, '$.")
                        .append(entry.getKey())
                        .append("'))");
            }

            if (entry.getValue().isJsonNull()) {
                filter.append(" IS NULL");
            } else if (entry.getValue() instanceof JsonPrimitive primitive) {
                filter.append(" = ?");
                if (primitive.isBoolean()) values.add(primitive.getAsBoolean());
                else if (primitive.isNumber()) values.add(primitive.getAsNumber());
                else if (primitive.isString()) values.add(primitive.getAsString());
            } else {
                filter.append(" = CAST(? as JSON)");
                values.add(gson.toJson(entry.getValue()));
            }
            filters.add(filter.toString());
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
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
            hikariDataSource = null;
        }
    }
}
