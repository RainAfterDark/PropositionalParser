package com.logic.output;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;

/**
 * Custom console implementation using JLine
 */
public class Console {
    private static final ExpressionHighlighter highlighter = new ExpressionHighlighter();
    private static Terminal terminal;
    private static LineReader reader;

    public static void init() {
        if (terminal != null && reader != null) return;
        // This does not create a system console for IDEA.
        // Only works in the produced executable.
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .history(new DefaultHistory())
                    .highlighter(highlighter)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void println(String message) {
        if (terminal == null) init();
        highlighter.highlight(message).println(terminal);
    }

    public static void error(String message) {
        if (terminal == null) init();
        AttributedStringBuilder builder = new AttributedStringBuilder();
        builder.append("Error: " + message,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.RED));
        builder.toAttributedString().println(terminal);
    }

    public static String readLine(String prompt) {
        if (reader == null) init();
        return reader.readLine(prompt);
    }
}
