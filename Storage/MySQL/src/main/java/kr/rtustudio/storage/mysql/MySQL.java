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
import java.util.regex.Matcher;

import org.jspecify.annotations.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Slf4j
public class MySQL implements Storage {

    private static final Gson GSON = new Gson();

    private final Pool connection;
    private final Config config;
    private final String table;

    public MySQL(Pool connection, Config config, String table) {
        this.connection = connection;
        this.config = config;
        this.table = config.getTablePrefix() + table;
        initializeTable();
    }

    private void initializeTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + table + "` (`data` JSON NOT NULL);";
        try (Connection conn = connection.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.execute();
        } catch (SQLException e) {
            StorageLogger.logError(log, "INIT", table, e);
        }
    }

    @Override
    public @NonNull CompletableFuture<Result> add(@NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (isNull(data)) return Result.FAILED;
                    String json = GSON.toJson(data);
                    String query = "INSERT INTO `" + table + "` (data) VALUES (CAST(? AS JSON));";
                    try (Connection conn = connection.getConnection();
                            PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, json);
                        StorageLogger.logAdd(log, table, debugQuery(query, List.of(json)));
                        return ps.executeUpdate() > 0 ? Result.UPDATED : Result.UNCHANGED;
                    } catch (SQLException e) {
                        StorageLogger.logError(log, "ADD", table, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<Result> set(
            @NonNull JsonObject find, @NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    StringBuilder query = new StringBuilder();
                    List<Object> values = new ArrayList<>();
                    if (isNull(data)) {
                        query.append("DELETE FROM `").append(table).append("`");
                    } else {
                        query.append("UPDATE `")
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
                                values.add(GSON.toJson(element));
                            }
                        }
                        query.append(String.join(", ", jsonSetArgs)).append(")");
                    }
                    appendFilter(query, values, find);
                    query.append(";");
                    try (Connection conn = connection.getConnection();
                            PreparedStatement ps = conn.prepareStatement(query.toString())) {
                        setParameters(ps, values);
                        StorageLogger.logSet(log, table, debugQuery(query.toString(), values));
                        return ps.executeUpdate() > 0 ? Result.UPDATED : Result.UNCHANGED;
                    } catch (SQLException e) {
                        StorageLogger.logError(log, "SET", table, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<List<JsonObject>> get(@NonNull JsonObject find) {
        return CompletableFuture.supplyAsync(
                () -> {
                    List<JsonObject> result = new ArrayList<>();
                    StringBuilder query =
                            new StringBuilder("SELECT `data` FROM `").append(table).append("`");
                    List<Object> values = new ArrayList<>();
                    appendFilter(query, values, find);
                    query.append(";");
                    try (Connection conn = connection.getConnection();
                            PreparedStatement ps = conn.prepareStatement(query.toString())) {
                        setParameters(ps, values);
                        StorageLogger.logGet(log, table, debugQuery(query.toString(), values));
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                result.add(GSON.fromJson(rs.getString("data"), JsonObject.class));
                            }
                        }
                    } catch (SQLException e) {
                        StorageLogger.logError(log, "GET", table, e);
                    }
                    return result;
                });
    }

    private void appendFilter(StringBuilder query, List<Object> values, JsonObject find) {
        if (isNull(find)) return;
        query.append(" WHERE ");
        List<String> filters = new ArrayList<>();
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
                values.add(GSON.toJson(entry.getValue()));
            }
            filters.add(filter.toString());
        }
        query.append(String.join(" AND ", filters));
    }

    private boolean isNull(JsonObject json) {
        return json == null || json.size() == 0 || json.isJsonNull();
    }

    private void setParameters(PreparedStatement statement, List<Object> values)
            throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            statement.setObject(i + 1, values.get(i));
        }
    }

    private String debugQuery(String query, List<Object> values) {
        String result = query;
        for (Object value : values) {
            String formatted;
            if (value instanceof String) {
                formatted = "'" + value.toString().replace("'", "''") + "'";
            } else if (value == null) {
                formatted = "NULL";
            } else {
                formatted = value.toString();
            }
            result = result.replaceFirst("\\?", Matcher.quoteReplacement(formatted));
        }
        return result;
    }

    @Override
    public void close() {}

    public static class Pool implements AutoCloseable {

        private HikariDataSource dataSource;

        public Pool(Config config) {
            String serverHost = config.getHost() + ":" + config.getPort();
            String url = "jdbc:mysql://" + serverHost + "/" + config.getDatabase()
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
            this.dataSource = new HikariDataSource(hikariConfig);
        }

        public Connection getConnection() throws SQLException {
            return dataSource.getConnection();
        }

        @Override
        public void close() {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                dataSource = null;
            }
        }
    }

    public interface Config {

        String getHost();

        int getPort();

        String getDatabase();

        String getUsername();

        String getPassword();

        String getTablePrefix();

        boolean isUseArrowOperator();
    }
}
