package dark.after.rain.reducer;

import dark.after.rain.parser.ast.Expression;

public class DistributionMinimizer implements Minimizer {
    private final Expression expr;

    public DistributionMinimizer(Expression expr) {
        this.expr = expr;
    }

    @Override
    public Expression minimize() {
        return expr;
    }
}
