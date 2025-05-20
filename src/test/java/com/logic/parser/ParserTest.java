package com.logic.parser;

import com.logic.ast.Expression;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class ParserTest {
    private static Stream<Arguments> provideSimplifyInput() {
        return Stream.of(
                // Test 1: Double Negation - ~(~~p) should simplify to ~p.
                Arguments.of("~(~~p)", "~p"),

                // Test 2: Tautology - p | ~p is always true (represented as 1).
                Arguments.of("p | ~p", "1"),

                // Test 3: Implication Conversion - p > q converts to ~p | q.
                Arguments.of("p > q", "~p | q"),

                // Test 4: Absorption/Distribution - ((p & q) | (p & ~q)) simplifies to p.
                Arguments.of("((p & q) | (p & ~q))", "p"),

                // Test 5: Complex Nested Expression with Unaries -
                // ((p & ~q) | (r & (q | ~q))) simplifies (via tautology in (q | ~q)) to (p & ~q) | r.
                Arguments.of("((p & ~q) | (r & (q | ~q))) & ((p & ~q) | (r | ~(~r)))", "(p & ~q) | r"),

                // Test 6: Tautology and Implication -
                // ((p | ~p) & (q | ~q)) is a tautology, so the whole expression ((p | ~p) & (q | ~q)) & (p > q)
                // simplifies to p > q, which in turn converts to ~p | q.
                Arguments.of("((p | ~p) & (q | ~q)) & (p > q)", "~p | q"),

                // Test 7: Mixing Negation and Implication -
                // ~(~~p) simplifies to ~p, and (~p | q) | (~p & q) absorbs to ~p.
                Arguments.of("~(~~p) & ((p > q) | (~p & q))", "~p"),

                // Test 8: Complex Expression with Multiple Variables -
                // ((p & q & r & s) | (p & q & r & ~s) | (p & q & ~r) | (~p & q)) simplifies by grouping
                // to p & q (from the first three terms) combined with (~p & q) which absorbs to q.
                Arguments.of("((p & q & r & s) | (p & q & r & ~s) | (p & q & ~r) | (~p & q))", "q"),

                // Test 9: Tautology with Implication -
                // (p > q) is ~p | q and (q | ~q) is a tautology; the disjunction simplifies to 1.
                Arguments.of("(p > q) | (q | ~q)", "1"),

                // Test 10: Biconditional Absorption -
                // (p & (p = q)) | (p & ~(p = q)) factors p out and the disjunction is a tautology, so it simplifies to p.
                Arguments.of("(p & (p = q)) | (p & ~(p = q))", "p"),

                // Test 11: Contradiction via absorption – (p & q) and (~p | ~q) cannot both be true.
                Arguments.of("((p & q) & (~p | ~q))", "0"),

                // Test 12: Hypothetical syllogism – if p > q and q > r, then p > r is always true.
                Arguments.of("((p > q) & (q > r)) > (p > r)", "1"),

                // Test 13: Identity law – (r & 0) is 0, so ((p & q) | (r & 0)) simplifies to p & q.
                Arguments.of("((p & q) | (r & 0))", "p & q"),

                // Test 14: Contradiction via conflicting terms – (p | q) and (~p & ~q) together yield a false statement.
                Arguments.of("((p | q) & (~p & ~q))", "0"),

                // Test 15: Consensus – ((p & q) | (p & ~q) | (~p & q)) factors to p | q.
                Arguments.of("((p & q) | (p & ~q) | (~p & q))", "p | q"),

                // Test 16: Extended hypothetical syllogism – a chain of implications is a tautology.
                Arguments.of("((p > q) & (q > r) & (r > s)) > (p > s)", "1"),

                // Test 17: Contrapositive equivalence – (p > q) is equivalent to (~q > ~p), hence a tautology.
                Arguments.of("((p > q) = (~q > ~p))", "1"),

                // Test 18: Covering all but the false case – "((p & q) | (~p & q) | (p & ~q))" simplifies to "p | q".
                Arguments.of("((p & q) | (~p & q) | (p & ~q))", "p | q"),

                // Test 19: Consensus with contradiction – ((p & q) | (~p & q)) simplifies to q, but q & ~q yields 0.
                Arguments.of("((p & q) | (~p & q)) & (~q)", "0"),

                // Test 20: Grouping and elimination – combining min terms over 4 variables simplifies to q.
                Arguments.of("((p & q & r) | (p & q & ~r)) | ((~p & q & r) | (~p & q & ~r))", "q")
        );
    }

    protected abstract Parser getParser(String input);

    @ParameterizedTest
    @MethodSource("provideSimplifyInput")
    void testSimplify(String input, String expected) {
        System.out.println("\nINPUT: " + input);
        Parser parser = getParser(input);
        Expression expr = parser.parseReduced();
        System.out.println("REDUCED: " + expr);
        assertNotNull(expr);
        assertEquals(expected, expr.toString());
    }
}
