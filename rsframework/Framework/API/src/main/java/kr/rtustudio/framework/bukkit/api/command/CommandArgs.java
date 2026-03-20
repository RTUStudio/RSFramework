package kr.rtustudio.framework.bukkit.api.command;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * 명령어 인자 배열을 감싸는 유틸리티 레코드입니다.
 *
 * <p>인덱스 기반 안전 접근, 타입 변환, 길이 비교 등의 편의 메서드를 제공합니다.
 *
 * @param args 원본 명령어 인자 배열
 */
public record CommandArgs(String[] args) {

    /**
     * 지정한 인덱스의 인자를 반환한다. 범위를 벗어나면 빈 문자열을 반환한다.
     *
     * @param index 인자 인덱스 (0부터 시작)
     * @return 인자 문자열, 범위 밖이면 {@code ""}
     */
    @NotNull
    public String get(int index) {
        return index < 0 || index >= args.length ? "" : args[index];
    }

    /**
     * 지정한 인덱스의 인자를 {@link Integer}로 변환한다.
     *
     * @param index 인자 인덱스
     * @return 변환된 정수, 변환 실패 시 {@code null}
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
     * 지정한 인덱스의 인자를 {@code int}로 변환한다. 변환 실패 시 기본값을 반환한다.
     *
     * @param index 인자 인덱스
     * @param def 기본값
     * @return 변환된 정수 또는 기본값
     */
    public int getInt(int index, int def) {
        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 지정한 인덱스의 인자를 {@link Long}으로 변환한다.
     *
     * @param index 인자 인덱스
     * @return 변환된 정수, 변환 실패 시 {@code null}
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
     * 지정한 인덱스의 인자를 {@code long}으로 변환한다. 변환 실패 시 기본값을 반환한다.
     *
     * @param index 인자 인덱스
     * @param def 기본값
     * @return 변환된 정수 또는 기본값
     */
    public long getLong(int index, long def) {
        try {
            return Long.parseLong(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /** 인자 개수를 반환한다. */
    public int length() {
        return args.length;
    }

    /**
     * 인자 개수가 기대값과 일치하는지 확인한다.
     *
     * @param expected 기대하는 인자 개수
     * @return 일치 여부
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

    /** 인자가 없는지 확인한다. */
    public boolean isEmpty() {
        return args.length == 0;
    }

    /**
     * 지정한 인덱스부터 끝까지의 인자를 공백으로 결합하여 반환한다.
     *
     * @param startIndex 시작 인덱스 (0부터 시작)
     * @return 결합된 문자열, 범위 밖이면 {@code ""}
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
