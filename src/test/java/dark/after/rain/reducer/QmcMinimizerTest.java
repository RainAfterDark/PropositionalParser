package dark.after.rain.reducer;

import dark.after.rain.parser.PrattParser;
import dark.after.rain.parser.ast.Expression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QmcMinimizerTest {
    @Test
    void testMinimize() {
        String input = "((p & ~q) | (r & (q | ~q))) & ((p & ~q) | (r | ~(~r)))";
        //String input = "p > q > r";
        //String input = "~(~~p) & ((p > q) | (~p & q))";
        //String input = "(p & (p = q)) | (p & ~(p = q))";
        //String input = "(p > q) = (q > p)";
        //String input = "p = q = r";
        //Expression expr = new PrattParser(input).parseReduced();
        Expression expr = new QmcMinimizer(new PrattParser(input).parse()).minimize();
        System.out.println("Minimized: " + expr);
        assertNotNull(expr);
    }
}