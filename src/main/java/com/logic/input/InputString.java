package com.logic.input;

/**
 * An object initialized with an input string that performs whitespace trimming.
 * Also comes with a method for creating exceptions that point to the position of
 * the error in the malformed or invalid input string at runtime.
 */
public abstract class InputString {
    protected final String input;

    /**
     * Initialize an input string, trimming whitespace.
     *
     * @param input The input string
     */
    protected InputString(String input) {
        this.input = input.trim().toLowerCase()
                .replaceAll("\\n", " ");
    }

    /**
     * Generate a runtime error pointing to the error in the input string.
     *
     * @param message The error message
     * @param index   The position where the error occurred in the input string
     * @return The runtime exception to be thrown
     */
    protected RuntimeException error(String message, int index) {
        String arrow = String.format("%" + (index + 1) + "s", "^");
        return new RuntimeException(message + "\n\t" + input + "\n\t" + arrow);
    }
}
