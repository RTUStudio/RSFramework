package kr.rtustudio.storage.postgresql;

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

import org.jspecify.annotations.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Slf4j
public class PostgreSQL implements Storage {

    private final Config config;
    private final Gson gson = new Gson();
    private final String prefix;
    private HikariDataSource hikariDataSource;

    public PostgreSQL(Config config, List<String> tables) {
        this.config = config;
        this.prefix = config.getTablePrefix();
        this.hikariDataSource = new HikariDataSource(hikariConfig());
        initializeTables(tables);
    }

    private void initializeTables(List<String> tables) {
        try (Connection connection = hikariDataSource.getConnection()) {
            for (String table : tables) {
                String query =
                        "CREATE TABLE IF NOT EXISTS \""
                                + prefix
                                + table
                                + "\" (data JSONB NOT NULL);";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.execute();
                }
            }
        } catch (SQLException e) {
            StorageLogger.logError(log, "INIT", "tables", e);
        }
    }

    private HikariConfig hikariConfig() {
        String url =
                "jdbc:postgresql://"
                        + config.getHost()
                        + ":"
                        + config.getPort()
                        + "/"
                        + config.getDatabase();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(30);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return hikariConfig;
    }

    private boolean isNull(JsonObject json) {
        return json == null || json.isEmpty() || json.isJsonNull();
    }

    @Override
    public @NonNull CompletableFuture<Result> add(@NonNull String table, @NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (isNull(data)) return Result.FAILED;
                    String json = gson.toJson(data);
                    String query =
                            "INSERT INTO \"" + prefix + table + "\" (data) VALUES (?::jsonb);";
                    try (Connection connection = hikariDataSource.getConnection();
                            PreparedStatement ps = connection.prepareStatement(query)) {
                        ps.setString(1, json);
                        StorageLogger.logAdd(
                                log,
                                table,
                                query.replace("?", "'" + json.replace("'", "''") + "'"));
                        return ps.executeUpdate() > 0 ? Result.UPDATED : Result.UNCHANGED;
                    } catch (SQLException e) {
                        StorageLogger.logError(log, "ADD", table, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<Result> set(
            @NonNull String table, @NonNull JsonObject find, @NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    StringBuilder query = new StringBuilder();
                    List<Object> values = new ArrayList<>();
                    if (isNull(data)) {
                        query.append("DELETE FROM \"").append(prefix).append(table).append("\"");
                    } else {
                        query.append("UPDATE \"")
                                .append(prefix)
                                .append(table)
                                .append("\" SET data = data");
                        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                            query.append(" || jsonb_build_object(?, ?::jsonb)");
                            values.add(entry.getKey());
                            values.add(gson.toJson(entry.getValue()));
                        }
                    }
                    if (!isNull(find)) {
                        query.append(" WHERE ");
                        List<String> conditions = new ArrayList<>();
                        for (Map.Entry<String, JsonElement> entry : find.entrySet()) {
                            conditions.add("data->? = ?::jsonb");
                            values.add(entry.getKey());
                            values.add(gson.toJson(entry.getValue()));
                        }
                        query.append(String.join(" AND ", conditions));
                    }
                    query.append(";");
                    try (Connection connection = hikariDataSource.getConnection();
                            PreparedStatement ps = connection.prepareStatement(query.toString())) {
                        setParameters(ps, values);
                        StorageLogger.logSet(log, table, (find != null ? find.toString() : "{}"));
                        return ps.executeUpdate() > 0 ? Result.UPDATED : Result.UNCHANGED;
                    } catch (SQLException e) {
                        StorageLogger.logError(log, "SET", table, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<List<JsonObject>> get(
            @NonNull String table, @NonNull JsonObject find) {
        return CompletableFuture.supplyAsync(
                () -> {
                    List<JsonObject> result = new ArrayList<>();
                    StringBuilder query =
                            new StringBuilder("SELECT data FROM \"")
                                    .append(prefix)
                                    .append(table)
                                    .append("\"");
                    List<Object> values = new ArrayList<>();
                    if (!isNull(find)) {
                        query.append(" WHERE ");
                        List<String> conditions = new ArrayList<>();
                        for (Map.Entry<String, JsonElement> entry : find.entrySet()) {
                            conditions.add("data->? = ?::jsonb");
                            values.add(entry.getKey());
                            JsonElement el = entry.getValue();
                            if (el.isJsonPrimitive()) {
                                JsonPrimitive p = el.getAsJsonPrimitive();
                                if (p.isString()) values.add("\"" + p.getAsString() + "\"");
                                else values.add(p.toString());
                            } else {
                                values.add(gson.toJson(el));
                            }
                        }
                        query.append(String.join(" AND ", conditions));
                    }
                    query.append(";");
                    try (Connection connection = hikariDataSource.getConnection();
                            PreparedStatement ps = connection.prepareStatement(query.toString())) {
                        setParameters(ps, values);
                        StorageLogger.logGet(log, table, (find != null ? find.toString() : "{}"));
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

    public interface Config {
        String getHost();

        int getPort();

        String getDatabase();

        String getUsername();

        String getPassword();

        String getTablePrefix();
    }
}
