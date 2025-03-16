package dark.after.rain.reducer;

import dark.after.rain.ast.*;
import dark.after.rain.lexer.Token;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Implicant(int bits, int mask, int numVars) {
    // Create an implicant from a single minTerm (no don't care bits yet)
    public static Implicant fromMinTerm(int minTerm, int numVars) {
        return new Implicant(minTerm, 0, numVars);
    }

    // Count ones in fixed bits (ignoring don't care positions)
    public int countOnes() {
        int count = 0;
        for (int i = 0; i < numVars; i++) {
            int bit = 1 << i;
            if ((mask & bit) == 0 && (bits & bit) != 0) {
                count++;
            }
        }
        return count;
    }

    // Determine if two implicants can combine.
    // They must have identical masks and differ in exactly one bit (outside the mask).
    public boolean canCombine(Implicant other) {
        if (this.mask != other.mask) return false;
        int diff = this.bits ^ other.bits;
        return Integer.bitCount(diff) == 1;
    }

    // Combine two implicants. The differing bit becomes a don't care.
    public Implicant combine(Implicant other) {
        int diff = this.bits ^ other.bits;
        int newMask = this.mask | diff;
        int newBits = this.bits & other.bits; // common fixed bits
        return new Implicant(newBits, newMask, numVars);
    }

    // Convert this implicant into a logical expression using the provided variable ordering.
    public Expression toExpression(List<Character> variables) {
        List<Expression> terms = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            int bitMask = 1 << i;
            if ((mask & bitMask) == 0) {
                Expression var = new VariableExpression(variables.get(i));
                if ((bits & bitMask) != 0) {
                    terms.add(var);
                } else {
                    terms.add(new UnaryExpression(Token.of('~'), var));
                }
            }
        }
        if (terms.isEmpty()) return null;
        terms = terms.stream().sorted(Comparator.comparing(
                e -> e.toString().replaceAll("[^a-z]", ""))).toList();
        return terms.size() == 1 ? terms.getFirst() :
                new BlockExpression(new NaryExpression(Token.of('&'), terms));
    }

    @Override
    public String toString() {
        // For debugging: print as a binary string with '-' representing don't care bits.
        StringBuilder sb = new StringBuilder();
        for (int i = numVars - 1; i >= 0; i--) {
            int bitMask = 1 << i;
            if ((mask & bitMask) != 0)
                sb.append("-");
            else
                sb.append((bits & bitMask) != 0 ? "1" : "0");
        }
        return sb.toString();
    }
}
