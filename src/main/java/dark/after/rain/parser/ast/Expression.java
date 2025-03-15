package dark.after.rain.parser.ast;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Expression {
    boolean evaluate(Map<Character, Boolean> context);
    Expression reduce(ReductionStep step);
    List<Character> collectVariables();

    static private List<Expression> tryFlatten(Expression expr, TokenType type) {
        if (expr instanceof BinaryExpression(Expression l, Token op, Expression r)) {
            if (op.type() == type) {
                List<Expression> operands = new ArrayList<>();
                operands.addAll(flatten(l, type));
                operands.addAll(flatten(r, type));
                return Collections.unmodifiableList(operands);
            }
        }
        if (expr instanceof NaryExpression(Token op, List<Expression> operands)) {
            if (op.type() == type) {
                return Collections.unmodifiableList(operands);
            }
        }
        return null;
    }

    static List<Expression> flatten(Expression expr, TokenType type) {
        List<Expression> flattened;
        if (expr instanceof BlockExpression(Expression inner) &&
                (flattened = tryFlatten(inner, type)) != null) {
            return flattened;
        }
        if ((flattened = tryFlatten(expr, type)) != null) {
            return flattened;
        }
        return List.of(expr);
    }
}
