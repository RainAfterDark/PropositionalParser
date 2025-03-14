package dark.after.rain.parser;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import dark.after.rain.parser.ast.Expression;

public class QuineMinimizer {
    private final Expression expr;

    public QuineMinimizer(Expression expr) {
        this.expr = expr;
    }

    private List<Integer> collectMinterms() {
        List<Integer> minterms = List.of();
        List<Character> vars = expr.getVariables();
        int numVars = 1 << vars.size(); // 2^n
        for (int i = 0; i < numVars; i++) {
            Map<Character, Boolean> context = new HashMap<>();
            for (int j = 0; j < vars.size(); j++) {
                int digit = vars.size() - j - 1;
                boolean truth = ((i >> digit) & 1) == 1;
                context.put(vars.get(j), truth);
            }
            if (expr.evaluate(context)) {
                minterms.add(i);
            }
        }
        return minterms;
    }

    public Expression minimize() {
        return expr;
    }
}
