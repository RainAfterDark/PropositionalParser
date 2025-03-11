package dark.after.rain.parser;

import dark.after.rain.parser.ast.Expression;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ParserTest {
    protected abstract Parser getParser(String input);
    
    @ParameterizedTest
    @MethodSource("provideSimplifyInput")
    void testSimplify(String input, String expected) {
        System.out.println("\nInput: " + input);
        Parser parser = getParser(input);
        Expression expr = parser.parseReduced();
        System.out.println("Reduced: " + expr);
        assertNotNull(expr);
        assertEquals(expected, expr.toString());
    }
    
    private static Stream<Arguments> provideSimplifyInput() {
        return Stream.of(
                // Test 1: Double Negation, Idempotence, and Negation Laws
                // (p & p) simplifies to p; (p & ~p) is false; ~~p simplifies to p;
                // so the whole expression simplifies to p.
                Arguments.of("(p & p) | (p & ~p) | (~~p)", "p"),

                // Test 2: Distributive and Absorption Laws
                // (p & q) | (p & ~q) factors to p & (q | ~q) which is p.
                Arguments.of("(p & q) | (p & ~q)", "p"),

                // Test 3: De Morgan's and Absorption Laws
                // ~(p | ~q) becomes ~p & q via De Morgan's.
                // Then (~p & q) & (~p | q) absorbs to ~p & q.
                Arguments.of("~(p | ~q) & (~p | q)", "~p & q"),

                // Test 4: Double Negation and Implication Conversion
                // ~~(p > q) simplifies to (p > q), which is equivalent to ~p | q.
                Arguments.of("~~(p > q)", "~p | q"),

                // Test 5: Biconditional Idempotence
                // (p = q) & (p = q) simplifies to just p = q.
                Arguments.of("(p = q) & (p = q)", "p = q"),

                // Test 6: Complex Combination (Distributive, Biconditional Conversion, and Negation)
                // Left side: (p & q) | (p & ~q) simplifies to p.
                // Right side: (p = q) | ~(p | q) simplifies (after conversion and absorption) to p = q.
                // Overall, p & (p = q) simplifies further to p & q.
                Arguments.of("((p & q) | (p & ~q)) & ((p = q) | ~(p | q))", "p & q"),

                // Test 7: Mixing Negation, Biconditional, and Double Negation
                // ~(~~p) simplifies to ~p.
                // The second part ((p = q) | (~p & q)) should absorb to ~p.
                // Overall, ~p & ... simplifies to ~p.
                Arguments.of("~(~~p) & ((p = q) | (~p & q))", "~p"),

                // Test 8: Biconditional from Implications
                // (p > q) = (q > p) should simplify to the biconditional p = q.
                Arguments.of("(p > q) = (q > p)", "p = q"),

                // Test 9: Tautology and Implication
                // (p | ~p) and (q | ~q) are tautologies, so the expression simplifies to (p > q) which is ~p | q.
                Arguments.of("((p | ~p) & (q | ~q)) & (p > q)", "~p | q"),

                // Test 10: Distributing p over a Biconditional and its Negation
                // (p & (p = q)) | (p & ~(p = q)) factors p out and the disjunction is a tautology, so it simplifies to p.
                Arguments.of("(p & (p = q)) | (p & ~(p = q))", "p"),

                // Absorption Test
                Arguments.of("p & r & (p | q)", "p & r"),
                Arguments.of("~p & q & (~p | q)", "~p & q"),
                Arguments.of("p | (p & q)", "p"),

                // Domination Test
                Arguments.of("p | 1", "1"),
                Arguments.of("p & 0", "0"),

                // Negation Test
                Arguments.of("p & ~p", "0"),
                Arguments.of("p | ~p", "1"),

                // Block test
                Arguments.of("p & (p & (p & p) & (p & p & p))", "p"),

                // De Morgan's Law Test
                Arguments.of("~(p & q & r)", "~p | ~q | ~r")
        );
    }
}
