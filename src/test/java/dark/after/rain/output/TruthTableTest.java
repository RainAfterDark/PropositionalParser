package dark.after.rain.output;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TruthTableTest {
    private static Stream<Arguments> provideGenerateInput() {
        return Stream.of(
                // Test 1: Sole variable.
                Arguments.of("p", "p = p"),

                // Test 2: Sole literal.
                Arguments.of("1", "1 = 1"),

                // Test 3: Negation of a variable.
                Arguments.of("~p", """
                        ┃ p ┃ ~p ┃\s
                        ┃ 1 ┃ 0  ┃\s
                        ┃ 0 ┃ 1  ┃\s
                        THE EXPRESSION IS A CONTINGENCY
                        """),

                // Test 4: Conjunction of two variables.
                Arguments.of("p & q", """
                        ┃ p q ┃ p & q ┃\s
                        ┃ 1 1 ┃ 1     ┃\s
                        ┃ 1 0 ┃ 0     ┃\s
                        ┃ 0 1 ┃ 0     ┃\s
                        ┃ 0 0 ┃ 0     ┃\s
                        THE EXPRESSION IS A CONTINGENCY
                        """),

                // Test 5: Disjunction of two variables.
                Arguments.of("p | q", """
                        ┃ p q ┃ p | q ┃\s
                        ┃ 1 1 ┃ 1     ┃\s
                        ┃ 1 0 ┃ 1     ┃\s
                        ┃ 0 1 ┃ 1     ┃\s
                        ┃ 0 0 ┃ 0     ┃\s
                        THE EXPRESSION IS A CONTINGENCY
                        """),

                // Test 6: Tautology.
                Arguments.of("p | ~p", """
                        ┃ p ┃ ~p ┃ p | ~p ┃\s
                        ┃ 1 ┃ 0  ┃ 1      ┃\s
                        ┃ 0 ┃ 1  ┃ 1      ┃\s
                        THE EXPRESSION IS A TAUTOLOGY
                        """),

                // Test 7: Contradiction.
                Arguments.of("p & ~p", """
                        ┃ p ┃ ~p ┃ p & ~p ┃\s
                        ┃ 1 ┃ 0  ┃ 0      ┃\s
                        ┃ 0 ┃ 1  ┃ 0      ┃\s
                        THE EXPRESSION IS A CONTRADICTION
                        """),

                // Test 8: Complex expression.
                Arguments.of("((p & q) | (p & ~q))", """
                        ┃ p q ┃ p & q ┃ ~q ┃ p & ~q ┃ (p & q) | (p & ~q) ┃\s
                        ┃ 1 1 ┃ 1     ┃ 0  ┃ 0      ┃ 1                  ┃\s
                        ┃ 1 0 ┃ 0     ┃ 1  ┃ 1      ┃ 1                  ┃\s
                        ┃ 0 1 ┃ 0     ┃ 0  ┃ 0      ┃ 0                  ┃\s
                        ┃ 0 0 ┃ 0     ┃ 1  ┃ 0      ┃ 0                  ┃\s
                        THE EXPRESSION IS A CONTINGENCY
                        """),

                // Test 9: Implication.
                Arguments.of("p > q", """
                        ┃ p q ┃ p > q ┃\s
                        ┃ 1 1 ┃ 1     ┃\s
                        ┃ 1 0 ┃ 0     ┃\s
                        ┃ 0 1 ┃ 1     ┃\s
                        ┃ 0 0 ┃ 1     ┃\s
                        THE EXPRESSION IS A CONTINGENCY
                        """),

                // Test 10: Biconditional.
                Arguments.of("p = q", """
                        ┃ p q ┃ p = q ┃\s
                        ┃ 1 1 ┃ 1     ┃\s
                        ┃ 1 0 ┃ 0     ┃\s
                        ┃ 0 1 ┃ 0     ┃\s
                        ┃ 0 0 ┃ 1     ┃\s
                        THE EXPRESSION IS A CONTINGENCY
                        """)
        );
    }

    @ParameterizedTest
    @MethodSource("provideGenerateInput")
    void testGenerate(String input, String expected) {
        System.out.println("\nINPUT: " + input);
        String table = new TruthTable(input).generate();
        System.out.println("TRUTH TABLE:\n" + expected);
        assertEquals(expected.trim(), table.trim());
    }

}