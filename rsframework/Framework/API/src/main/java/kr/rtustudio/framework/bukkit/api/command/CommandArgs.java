package kr.rtustudio.framework.bukkit.api.command;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Provides safe index-based access, type conversion, and length comparison utilities.
 *
 * <p>명령어 인자 배열을 감싸는 유틸리티 레코드. 인덱스 기반 안전 접근, 타입 변환, 길이 비교 등의 편의 메서드를 제공한다.
 *
 * @param args raw command argument array
 */
public record CommandArgs(String[] args) {

    /**
     * Returns the argument at the given index. Returns empty string if out of bounds.
     *
     * <p>지정한 인덱스의 인자를 반환한다. 범위를 벗어나면 빈 문자열을 반환한다.
     *
     * @param index argument index (0-based)
     * @return argument string, or {@code ""} if out of bounds
     */
    @NotNull
    public String get(int index) {
        return index < 0 || index >= args.length ? "" : args[index];
    }

    /**
     * Converts the argument at the given index to {@link Integer}.
     *
     * <p>지정한 인덱스의 인자를 {@link Integer}로 변환한다.
     *
     * @param index argument index
     * @return parsed integer, or {@code null} on failure
     */
    @Nullable
    public Integer getInt(int index) {
        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts the argument at the given index to {@code int}. Returns default on failure.
     *
     * <p>지정한 인덱스의 인자를 {@code int}로 변환한다. 변환 실패 시 기본값을 반환한다.
     *
     * @param index argument index
     * @param def default value
     * @return parsed integer or default
     */
    public int getInt(int index, int def) {
        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Converts the argument at the given index to {@link Long}.
     *
     * <p>지정한 인덱스의 인자를 {@link Long}으로 변환한다.
     *
     * @param index argument index
     * @return parsed long, or {@code null} on failure
     */
    @Nullable
    public Long getLong(int index) {
        try {
            return Long.parseLong(get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts the argument at the given index to {@code long}. Returns default on failure.
     *
     * <p>지정한 인덱스의 인자를 {@code long}으로 변환한다. 변환 실패 시 기본값을 반환한다.
     *
     * @param index argument index
     * @param def default value
     * @return parsed long or default
     */
    public long getLong(int index, long def) {
        try {
            return Long.parseLong(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Returns the number of arguments.
     *
     * <p>인자 개수를 반환한다.
     */
    public int length() {
        return args.length;
    }

    /**
     * Checks whether the argument count matches the expected value.
     *
     * <p>인자 개수가 기대값과 일치하는지 확인한다.
     *
     * @param expected expected argument count
     * @return whether counts match
     */
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

    /**
     * Checks whether there are no arguments.
     *
     * <p>인자가 없는지 확인한다.
     */
    public boolean isEmpty() {
        return args.length == 0;
    }

    /**
     * Joins arguments from the given index to the end with spaces.
     *
     * <p>지정한 인덱스부터 끝까지의 인자를 공백으로 결합하여 반환한다.
     *
     * @param startIndex start index (0-based)
     * @return joined string, or {@code ""} if out of bounds
     */
    @NotNull
    public String toString(@Range(from = 0, to = Integer.MAX_VALUE) int startIndex) {
        if (startIndex >= args.length) return "";
        return String.join(" ", Arrays.copyOfRange(args, startIndex, args.length));
    }

    @NotNull
    public String toString() {
        return String.join(" ", args);
    }
}
