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
                        USAGE: (prefix) (expression)
                        PREFIXES: (can also be affixed) \t  ┃ OPERATORS:
                        \t?: show this message              ┃     NOT: ~
                        \t$: minify expression (QMC)        ┃     AND: &
                        \t                                  ┃      OR: |
                        \t                                  ┃ IMPLIES: >
                        \t                                  ┃  EQUALS: =
                        VARIABLES: a-z (case insensitive, will lowercase)
                        LITERALS: 0 (false), 1 (true)
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