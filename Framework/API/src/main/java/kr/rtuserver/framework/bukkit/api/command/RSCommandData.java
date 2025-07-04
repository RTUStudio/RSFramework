package kr.rtuserver.framework.bukkit.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Arrays;

public record RSCommandData(String[] args) {

    @NotNull
    public String args(int argIndex) {
        return argIndex < 0 || argIndex >= args.length ? "" : args[argIndex];
    }

    public int length() {
        return args.length;
    }

    public boolean length(int equal) {
        return length() == equal;
    }

    public boolean equals(int arg, String text) {
        return args(arg).equals(text);
    }

    public boolean equalsIgnoreCase(int arg, String text) {
        return args(arg).equalsIgnoreCase(text);
    }

    public boolean contains(String text) {
        return String.join(" ", args).contains(text);
    }

    public boolean contains(int arg, String text) {
        return args(arg).contains(text);
    }

    public boolean isEmpty() {
        return args.length == 0;
    }

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
