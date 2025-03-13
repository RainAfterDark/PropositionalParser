package dark.after.rain;

import dark.after.rain.input.PrefixArgs;
import dark.after.rain.output.TruthTable;
import dark.after.rain.parser.PrattParser;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("┃ Propositional Parser ┃ Enter an expression or ? for help ┃");
        Scanner scanner;
        while (true) {
            System.out.print(": ");
            scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) break;

            PrefixArgs prefix = new PrefixArgs(input);
            if (prefix.shouldShowHelp()) {
                System.out.println("""
                Usage: <prefix> <expression>
                Prefixes:                       \t  ┃ Operators:
                \t?: help                           ┃     NOT: ~
                \t$: simplify expression            ┃     AND: &
                \t$!: simplify and show truth table ┃      OR: |
                \t                                  ┃ IMPLIES: >
                \t                                  ┃  EQUALS: =
                Variables: a-z, case insensitive
                Literals: 0 (false), 1 (true)
                Enter empty input to exit""");
                continue;
            }

            input = prefix.getUnPrefixedInput();
            try {
                if (prefix.shouldSimplify()) {
                    String original = input;
                    input = new PrattParser(input).parseReduced().toString();
                    System.out.println(original + " -> " + input);
                }
                if (prefix.shouldShowTable())
                    System.out.println(new TruthTable(input).generate());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}