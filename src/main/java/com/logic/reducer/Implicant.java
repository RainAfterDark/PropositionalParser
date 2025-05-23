package com.logic.reducer;

import com.logic.ast.*;
import com.logic.lexer.Token;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents an implicant in the Quine-McCluskey algorithm for boolean function minimization.
 * An implicant is a product term that may contain don't care bits, which can represent multiple minterms.
 * <p>
 * This record class stores:
 * <ul>
 *  <li> bits: The actual bit values (0 or 1) for non-don't-care positions </li>
 *  <li> mask: A bit mask where 1 indicates a don't care position </li>
 *  <li> numVars: The number of variables in the boolean function </li>
 * </ul>
 */
public record Implicant(int bits, int mask, int numVars) {
    /**
     * Creates an implicant from a single minterm with no don't care bits.
     *
     * @param minTerm The minterm value
     * @param numVars The number of variables in the boolean function
     * @return A new Implicant representing the minterm
     */
    public static Implicant fromMinTerm(int minTerm, int numVars) {
        return new Implicant(minTerm, 0, numVars);
    }

    /**
     * Checks if this implicant covers the given minterm.
     * An implicant covers a minterm if, for each bit position that is not a don't care bit,
     * the implicant's bit matches the minterm's bit.
     *
     * @param minTerm The minterm to check
     * @return true if this implicant covers the minterm, false otherwise
     */
    public boolean covers(int minTerm) {
        // For each bit position that is not a don't care bit (mask bit is 0),
        // the implicant's bit must match the minterm's bit
        for (int i = 0; i < numVars; i++) {
            int bitMask = 1 << i;
            // If this bit position is not a don't care bit (mask bit is 0)
            if ((mask & bitMask) == 0) {
                // Check if the bits match at this position
                boolean implicantBit = (bits & bitMask) != 0;
                boolean mintermBit = (minTerm & bitMask) != 0;
                if (implicantBit != mintermBit) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Counts the number of ones in the fixed bits of this implicant.
     * Don't care bit positions are ignored in this count.
     *
     * @return The number of ones in the fixed bit positions
     */
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

    /**
     * Determines if two implicants can be combined.
     * Two implicants can be combined if they have identical masks and differ in exactly one-bit position.
     *
     * @param other The other implicant to check against
     * @return true if the implicants can be combined, false otherwise
     */
    public boolean canCombine(Implicant other) {
        if (this.mask != other.mask) return false;
        int diff = this.bits ^ other.bits;
        return Integer.bitCount(diff) == 1;
    }

    /**
     * Combines this implicant with another implicant.
     * The differing bit position becomes a don't care in the resulting implicant.
     *
     * @param other The other implicant to combine with
     * @return A new implicant that represents the combination
     */
    public Implicant combine(Implicant other) {
        int diff = this.bits ^ other.bits;
        int newMask = this.mask | diff;
        int newBits = this.bits & other.bits;
        return new Implicant(newBits, newMask, numVars);
    }

    /**
     * Converts this implicant into a logical expression using the provided variable ordering.
     * The expression will be in product form (AND of variables or their negations).
     *
     * @param variables The list of variables in the boolean function
     * @return An Expression representing this implicant, or null if the implicant has no fixed bits
     */
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
        // Sort terms alphabetically for a consistent output
        terms = terms.stream().sorted(Comparator.comparing(
                e -> e.toString().replaceAll("[^a-z]", ""))).toList();
        return terms.size() == 1 ? terms.getFirst() :
                new BlockExpression(new NaryExpression(Token.of('&'), terms));
    }

    /**
     * Returns a string representation of this implicant.
     * The string is a binary representation with '-' representing "don't care" bits.
     *
     * @return A string representation of this implicant
     */
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
