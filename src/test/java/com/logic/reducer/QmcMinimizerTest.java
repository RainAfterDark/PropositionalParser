package com.logic.reducer;

import com.logic.ast.Expression;
import com.logic.parser.Parser;
import com.logic.parser.PrattParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QmcMinimizerTest {

    @Test
    public void testSimpleMinimization() {
        // Test a simple expression: a & b | a & ~b -> a
        testMinimization("a & b | a & ~b", "a");
    }

    @Test
    public void testPetricksMethod() {
        // Test an expression that requires Petrick's method
        // (a & b & ~c) | (a & ~b & c) | (~a & b & c) | (a & b & c)
        // This should minimize to (a & b) | (a & c) | (b & c)
        testMinimization("(a & b & ~c) | (a & ~b & c) | (~a & b & c) | (a & b & c)",
                "(a & b) | (a & c) | (b & c)");
    }

    @Test
    public void testEssentialPrimeImplicants() {
        // Test an expression with essential prime implicants
        // (a & b & c) | (a & b & ~c) | (a & ~b & c) | (~a & b & c)
        // This should minimize to (a & b) | (a & c) | (b & c)
        testMinimization("(a & b & c) | (a & b & ~c) | (a & ~b & c) | (~a & b & c)",
                "(a & b) | (a & c) | (b & c)");
    }

    @Test
    public void testWikipediaExample() {
        // Test the Wikipedia example from https://en.wikipedia.org/wiki/Petrick%27s_method#Example_of_Petrick's_method
        // The example uses minterms: 4, 8, 10, 11, 12, 15 (in decimal) for a 4-variable function (w,x,y,z)
        // minterm 4 = 0100 = ~w & x & ~y & ~z
        // minterm 8 = 1000 = w & ~x & ~y & ~z
        // minterm 10 = 1010 = w & ~x & y & ~z
        // minterm 11 = 1011 = w & ~x & y & z
        // minterm 12 = 1100 = w & x & ~y & ~z
        // minterm 15 = 1111 = w & x & y & z

        // The expression is the OR of all these minterms
        String input = "(~w & x & ~y & ~z) | (w & ~x & ~y & ~z) | (w & ~x & y & ~z) | " +
                "(w & ~x & y & z) | (w & x & ~y & ~z) | (w & x & y & z)";

        Parser parser = new PrattParser(input);
        Expression expr = parser.parse();
        QmcMinimizer minimizer = new QmcMinimizer(expr);
        Expression minimized = minimizer.minimize();

        System.out.println("Original: " + expr);
        System.out.println("Minimized: " + minimized);

        // Verify that the minimized expression has 3 terms
        String minimizedStr = minimized.toString();
        int termCount = minimizedStr.split("\\|").length;
        assertEquals(3, termCount, "The minimized expression should have 3 terms");

        // Verify that the minimized expression covers all the original minterms
        // This is a more thorough test than string comparison
        for (int minterm : new int[]{4, 8, 10, 11, 12, 15}) {
            assertTrue(coversMinterm(minimized, minterm, new char[]{'w', 'x', 'y', 'z'}),
                    "Minimized expression should cover minterm " + minterm);
        }
    }

    // Helper method to check if an expression covers a specific minterm
    private boolean coversMinterm(Expression expr, int minterm, char[] vars) {
        // Create a context with variable assignments based on the minterm
        java.util.Map<Character, Boolean> context = new java.util.HashMap<>();
        for (int i = 0; i < vars.length; i++) {
            // Extract the bit at position i from the minterm
            boolean value = ((minterm >> (vars.length - 1 - i)) & 1) == 1;
            context.put(vars[i], value);
        }

        // Evaluate the expression with this context
        return expr.evaluate(context);
    }

    private void testMinimization(String input, String expected) {
        Parser parser = new PrattParser(input);
        Expression expr = parser.parse();
        QmcMinimizer minimizer = new QmcMinimizer(expr);
        Expression minimized = minimizer.minimize();

        // Parse the expected result for comparison
        Parser expectedParser = new PrattParser(expected);
        Expression expectedExpr = expectedParser.parse();

        System.out.println("Original: " + expr);
        System.out.println("Minimized: " + minimized);
        System.out.println("Expected: " + expectedExpr);

        // Compare the string representations as a simple way to check equality
        assertEquals(expectedExpr.toString(), minimized.toString());
    }
}
