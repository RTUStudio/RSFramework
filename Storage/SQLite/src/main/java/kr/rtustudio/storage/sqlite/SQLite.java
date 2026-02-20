package kr.rtustudio.storage.sqlite;

import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageLogger;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Slf4j
public class SQLite implements Storage {

    public interface Config {
        String getFilePath();
    }

    private final Gson gson = new Gson();
    private Connection connection;

    public SQLite(Config config, List<String> tables) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + config.getFilePath());
            try (PreparedStatement pragma =
                    connection.prepareStatement("PRAGMA journal_mode=WAL;")) {
                pragma.execute();
            }
            for (String table : tables) {
                String query = "CREATE TABLE IF NOT EXISTS `" + table + "` (`data` JSON NOT NULL);";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.execute();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLite storage", e);
        }
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
                    String query = "INSERT INTO `" + table + "` (data) VALUES (json(?));";
                    try (PreparedStatement ps = connection.prepareStatement(query)) {
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
                    try {
                        StringBuilder query = new StringBuilder();
                        List<Object> values = new ArrayList<>();
                        if (isNull(data)) {
                            query.append("DELETE FROM `").append(table).append("`");
                        } else {
                            query.append("UPDATE `")
                                    .append(table)
                                    .append("` SET data = json_patch(data, json(?))");
                            values.add(gson.toJson(data));
                        }

                        if (!isNull(find)) {
                            Pair<String, List<Object>> filterPair = filter(find);
                            query.append(filterPair.getLeft());
                            values.addAll(filterPair.getRight());
                        }
                        query.append(";");

                        try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
                            setParameters(ps, values);
                            StorageLogger.logSet(
                                    log, table, (find != null ? find.toString() : "{}"));
                            return ps.executeUpdate() > 0 ? Result.UPDATED : Result.UNCHANGED;
                        }
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
                            new StringBuilder("SELECT data FROM `").append(table).append("`");
                    List<Object> values = new ArrayList<>();

                    if (!isNull(find)) {
                        Pair<String, List<Object>> filterPair = filter(find);
                        query.append(filterPair.getLeft());
                        values.addAll(filterPair.getRight());
                    }
                    query.append(";");

                    try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
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

    private Pair<String, List<Object>> filter(JsonObject find) {
        StringBuilder query = new StringBuilder(" WHERE ");
        List<String> conditions = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : find.entrySet()) {
            String key = entry.getKey();
            JsonElement el = entry.getValue();
            if (el.isJsonNull()) {
                conditions.add("json_extract(data, '$." + key + "') IS NULL");
            } else if (el.isJsonPrimitive()) {
                conditions.add("data ->> '$." + key + "' = ?");
                JsonPrimitive p = el.getAsJsonPrimitive();
                if (p.isNumber()) values.add(p.getAsNumber());
                else if (p.isBoolean()) values.add(p.getAsBoolean());
                else values.add(p.getAsString());
            } else {
                conditions.add("json_extract(data, '$." + key + "') = json(?)");
                values.add(gson.toJson(el));
            }
        }
        query.append(String.join(" AND ", conditions));
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
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            StorageLogger.logError(log, "CLOSE", "connection", e);
        }
    }
}
