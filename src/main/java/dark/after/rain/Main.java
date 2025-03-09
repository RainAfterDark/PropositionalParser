package dark.after.rain;

import dark.after.rain.output.TruthTable;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter expression (or empty to exit): ");
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) break;
            System.out.println(new TruthTable(input).generate());
        }
        scanner.close();
    }
}