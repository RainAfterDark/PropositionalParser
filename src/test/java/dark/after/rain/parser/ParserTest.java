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
        assertNotNull(expr);
        assertEquals(expected, expr.toString());
    }
    
    private static Stream<Arguments> provideSimplifyInput() {
        return Stream.of(
            // 1. Double Negation, Idempotence, and Negation
            Arguments.of("(p & p) | (p & ~p) | (~~p)", "p"),

            // 2. Distributive and Absorption
            Arguments.of("(p & q) | (p & ~q)", "p"),

            // 3. De Morgan’s and Absorption
            Arguments.of("~(p | ~q) & (~p | q)", "~p & q"),

            // 4. Double Negation and Implication Conversion
            Arguments.of("~~(p > q)", "~p | q"),

            // 5. Biconditional Idempotence
            Arguments.of("(p = q) & (p = q)", "p = q"),

            // 6. Complex Combination (Distributive, Biconditional Conversion, and Negation)
            Arguments.of("((p & q) | (p & ~q)) & ((p = q) | ~(p | q))", "p & q"),

            // 7. Mixing Negation, Biconditional, and Double Negation
            Arguments.of("~(~~p) & ((p = q) | (~p & q))", "~p"),

            // 8. Biconditional from Implications
            Arguments.of("(p > q) = (q > p)", "p = q"),

            // 9. Tautology and Implication
            Arguments.of("((p | ~p) & (q | ~q)) & (p > q)", "~p | q"),

            // 10. Distributing p over a Biconditional and its Negation
            Arguments.of("(p & (p = q)) | (p & ~(p = q))", "p")
        );
    }
}
