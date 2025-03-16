package dark.after.rain.ast;

import java.util.List;
import java.util.Map;

public interface Expression {
    boolean evaluate(Map<Character, Boolean> context);

    List<Character> collectVariables();
}
