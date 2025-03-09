package dark.after.rain;

import dark.after.rain.output.TruthTable;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        while (true) {
            System.out.print("Enter expression (or empty to exit): ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) break;
            try {
                System.out.println(new TruthTable(input).generate());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}