package dark.after.rain.parser.ast;

import java.util.Map;

public interface Expression {
    boolean evaluate(Map<Character, Boolean> context);
}
