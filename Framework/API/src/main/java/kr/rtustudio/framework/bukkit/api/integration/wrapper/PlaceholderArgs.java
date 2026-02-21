package kr.rtustudio.framework.bukkit.api.integration.wrapper;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public record PlaceholderArgs(String[] args) {

    public PlaceholderArgs(String params) {
        this(params.split("_"));
    }

    @NotNull
    public String get(int index) {
        return index < 0 || index >= args.length ? "" : args[index];
    }

    @Nullable
    public Integer getInt(int index) {
        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getInt(int index, int def) {
        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Nullable
    public Long getLong(int index) {
        try {
            return Long.parseLong(get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public long getLong(int index, long def) {
        try {
            return Long.parseLong(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public int length() {
        return args.length;
    }

    public boolean length(int expected) {
        return length() == expected;
    }

    public boolean equals(int index, String text) {
        return get(index).equals(text);
    }

    public boolean equals(int index, int number) {
        return Integer.valueOf(number).equals(getInt(index));
    }

    public boolean equals(int index, long number) {
        return Long.valueOf(number).equals(getLong(index));
    }

    public boolean equalsIgnoreCase(int index, String text) {
        return get(index).equalsIgnoreCase(text);
    }

    public boolean contains(String text) {
        return toString().contains(text);
    }

    public boolean contains(int index, String text) {
        return get(index).contains(text);
    }

    public boolean isEmpty() {
        return args.length == 0;
    }

    @NotNull
    public String toString(@Range(from = 0, to = Integer.MAX_VALUE) int startIndex) {
        if (startIndex >= args.length) return "";
        return String.join("_", Arrays.copyOfRange(args, startIndex, args.length));
    }

    @NotNull
    public String toString() {
        return String.join("_", args);
    }
}
