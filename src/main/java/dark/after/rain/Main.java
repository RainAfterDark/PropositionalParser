package dark.after.rain;

import dark.after.rain.input.PrefixArgs;
import dark.after.rain.output.Console;
import dark.after.rain.output.TruthTable;
import dark.after.rain.parser.PrattParser;

public class Main {
    public static void main(String[] args) {
        Console.println("┃ PROPOSITIONAL PARSER ┃ ENTER AN EXPRESSION OR ? FOR HELP ┃");
        while (true) {
            String input = Console.readLine(":: ");
            if (input.trim().isEmpty()) break;
            PrefixArgs prefix = new PrefixArgs(input);
            if (prefix.shouldShowHelp()) {
                Console.println("""
                        USAGE: <PREFIX> <EXPRESSION>
                        PREFIXES: (CAN ALSO BE AFFIXED) \t  ┃ OPERATORS:
                        \t?: SHOW THIS MESSAGE              ┃     NOT: ~
                        \t$: MINIFY EXPRESSION (QMC)        ┃     AND: &
                        \t                                  ┃      OR: |
                        \t                                  ┃ IMPLIES: >
                        \t                                  ┃  EQUALS: =
                        VARIABLES: a-z (CASE INSENSITIVE)
                        LITERALS: 0 (FALSE), 1 (TRUE)
                        ENTER EMPTY LINE TO EXIT""");
                continue;
            }
            input = prefix.getUnPrefixedInput();
            try {
                if (prefix.shouldSimplify()) {
                    input = new PrattParser(input).parseReduced().toString();
                    Console.println("MINIMIZED: " + input);
                }
                Console.println(new TruthTable(input).generate());
            } catch (Exception e) {
                Console.error(e.getMessage());
            }
        }
    }
}