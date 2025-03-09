package dark.after.rain;

import dark.after.rain.output.TruthTable;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter expression: ");
            String input = scanner.nextLine();
            System.out.println(new TruthTable(input).generate());
            System.out.print("Enter again? (y/n, default y): ");
            String next = scanner.nextLine();
            if (!next.isEmpty() &&
                !next.equalsIgnoreCase("y"))
                break;
        }
        scanner.close();
    }
}