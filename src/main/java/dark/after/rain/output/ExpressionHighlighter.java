package dark.after.rain.output;

import org.jline.reader.LineReader;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class ExpressionHighlighter extends DefaultHighlighter {
    private boolean isOperator(char ch) {
        return ch == '~' || ch == '&' || ch == '|' || ch == '>' || ch == '=';
    }

    private boolean isBracket(char ch) {
        return ch == '(' || ch == ')';
    }

    public AttributedString highlight(String buffer) {
        AttributedStringBuilder builder = new AttributedStringBuilder();

        for (char ch : buffer.toCharArray()) {
            if (isOperator(ch)) {
                builder.append(String.valueOf(ch),
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.CYAN));
            } else if (isBracket(ch)) {
                builder.append(String.valueOf(ch),
                        AttributedStyle.DEFAULT.foreground(125, 125, 255));
            } else if (ch == '0') {
                builder.append(String.valueOf(ch),
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.RED));
            } else if (ch == '1') {
                builder.append(String.valueOf(ch),
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.GREEN));
            } else if (ch == '?' || ch == '$') {
                builder.append(String.valueOf(ch),
                        AttributedStyle.BOLD.foreground(255, 125, 255));
            } else if (Character.isLetter(ch) && Character.isLowerCase(ch)) {
                builder.append(String.valueOf(ch),
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT + AttributedStyle.YELLOW));
            } else {
                builder.append(String.valueOf(ch));
            }
        }
        return builder.toAttributedString();
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        return highlight(buffer);
    }
}