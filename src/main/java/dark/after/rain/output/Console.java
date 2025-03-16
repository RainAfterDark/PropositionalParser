package dark.after.rain.output;

import dark.after.rain.input.PrefixArgs;
import dark.after.rain.parser.PrattParser;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;

public class Console {
    private static final ExpressionHighlighter highlighter = new ExpressionHighlighter();
    private static Terminal terminal;
    private static LineReader reader;

    public static void init() {
        if (terminal != null && reader != null) return;
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

    public static void inputLoop() {
        if (terminal == null) init();
        Console.println("┃ PROPOSITIONAL PARSER ┃ ENTER AN EXPRESSION OR ? FOR HELP ┃");

        while (true) {
            String input = reader.readLine(":: ");
            if (input.trim().isEmpty()) break;
            PrefixArgs prefix = new PrefixArgs(input);
            if (prefix.shouldShowHelp()) {
                Console.println("""
                        USAGE: <PREFIX> <EXPRESSION>
                        PREFIXES: (CAN ALSO BE AFFIXED) \t  ┃ OPERATORS:
                        \t?: HELP                           ┃     NOT: ~
                        \t$: MINIFY EXPRESSION              ┃     AND: &
                        \t                                  ┃      OR: |
                        \t                                  ┃ IMPLIES: >
                        \t                                  ┃  EQUALS: =
                        VARIABLES: a-z (CASE INSENSITIVE)
                        LITERALS: 0 (FALSE), 1 (TRUE)
                        ENTER EMPTY LINE TO EXIT""");
                continue;
            }

            input = prefix.getUnPrefixedInput();
            try {
                if (prefix.shouldSimplify()) {
                    input = new PrattParser(input).parseReduced().toString();
                    Console.println("MINIMIZED: " + input);
                }
                Console.println(new TruthTable(input).generate());
            } catch (Exception e) {
                Console.error(e.getMessage());
            }
        }
    }
}
