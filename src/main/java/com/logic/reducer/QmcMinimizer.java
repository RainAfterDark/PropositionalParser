package com.logic.reducer;

import com.logic.ast.BlockExpression;
import com.logic.ast.Expression;
import com.logic.ast.LiteralExpression;
import com.logic.ast.NaryExpression;
import com.logic.lexer.Token;

import java.util.*;

/**
 * Implementation of the Quine-McCluskey algorithm with Petrick's method for boolean function minimization.
 * This class takes a boolean expression and reduces it to its minimal form in sum-of-products (SOP) representation.
 * <p>
 * The algorithm works in several steps:
 * <ul>
 *  <li> Collect all minterms from the expression </li>
 *  <li> Group implicants by number of ones </li>
 *  <li> Find prime implicants by combining implicants that differ by one bit</li>
 *  <li> Apply Petrick's method to find the minimal cover of prime implicants</li>
 *  <li> Convert the minimal implicants back to an expression </li>
 * </ul>
 */
public class QmcMinimizer {
    private final Expression expr;
    private final List<Character> vars;

    /**
     * Constructs a QmcMinimizer for the given expression.
     *
     * @param expr The boolean expression to minimize
     */
    public QmcMinimizer(Expression expr) {
        this.expr = expr;
        vars = expr.collectVariables();
    }

    /**
     * Collects all minterms from the expression by evaluating it for all possible input combinations.
     * A minterm is a product term in which each variable appears exactly once in either true or complemented form.
     *
     * @return A set of integers representing the minterms of the expression
     */
    private Set<Integer> collectMinTerms() {
        Set<Integer> minTerms = new HashSet<>();
        int rows = 1 << vars.size(); // 2^n (number of possible input combinations)
        for (int i = 0; i < rows; i++) {
            Map<Character, Boolean> context = new HashMap<>();
            for (int j = 0; j < vars.size(); j++) {
                int digit = vars.size() - j - 1;
                boolean truth = ((i >> digit) & 1) == 1; // Extract the bit at the position 'digit'
                context.put(vars.get(digit), truth);
            }
            if (expr.evaluate(context)) {
                minTerms.add(i);
            }
        }
        return Collections.unmodifiableSet(minTerms);
    }

    /**
     * Generates a list of empty sets to hold implicants grouped by the number of ones.
     * The number of groups is equal to the number of variables plus one (0 to n).
     *
     * @return A list of empty sets for grouping implicants
     */
    private List<Set<Implicant>> generateEmptyGroups() {
        List<Set<Implicant>> groups = new ArrayList<>();
        for (int i = 0; i < vars.size() + 1; i++) {
            groups.add(new LinkedHashSet<>());
        }
        return groups;
    }

    /**
     * Groups implicants by the number of ones in their binary representation.
     * This is the first step of the Quine-McCluskey algorithm.
     *
     * @param minTerms The set of minterms to group
     * @return A list of sets, where each set contains implicants with the same number of ones
     */
    private List<Set<Implicant>> groupImplicants(Set<Integer> minTerms) {
        List<Set<Implicant>> groups = generateEmptyGroups();
        for (int minTerm : minTerms) {
            Implicant implicant = Implicant.fromMinTerm(minTerm, vars.size());
            groups.get(implicant.countOnes()).add(implicant);
        }
        return Collections.unmodifiableList(groups);
    }

    /**
     * Finds all prime implicants by iteratively combining implicants that differ by one bit.
     * A prime implicant is an implicant that cannot be combined with any other implicant to form a larger implicant.
     *
     * @param groups The initial groups of implicants, grouped by number of ones
     * @return A set of all prime implicants
     */
    private Set<Implicant> findPrimeImplicants(List<Set<Implicant>> groups) {
        Set<Implicant> primeImplicants = new LinkedHashSet<>();
        boolean combinedOnce = true;

        while (combinedOnce) {
            combinedOnce = false;
            List<Set<Implicant>> nextGroups = generateEmptyGroups();
            Set<Implicant> consumed = new HashSet<>();

            // Combine implicants in adjacent groups that differ by one bit
            for (int i = 0; i < groups.size() - 1; i++) {
                Set<Implicant> group1 = groups.get(i);
                Set<Implicant> group2 = groups.get(i + 1);
                if (group1.isEmpty() || group2.isEmpty()) continue;
                for (Implicant implicant : group1) {
                    for (Implicant other : group2) {
                        if (implicant.canCombine(other)) {
                            Implicant combined = implicant.combine(other);
                            int ones = combined.countOnes();
                            nextGroups.get(ones).add(combined);
                            consumed.add(implicant);
                            consumed.add(other);
                            combinedOnce = true;
                        }
                    }
                }
            }

            // Add implicants that were not consumed (these are prime implicants)
            for (Set<Implicant> current : groups) {
                for (Implicant implicant : current) {
                    if (!consumed.contains(implicant)) {
                        primeImplicants.add(implicant);
                    }
                }
            }
            groups = Collections.unmodifiableList(nextGroups);
        }

        // Add remaining implicants from the final iteration
        for (Set<Implicant> group : groups) {
            primeImplicants.addAll(group);
        }
        return Collections.unmodifiableSet(primeImplicants);
    }

    /**
     * Gets the sum term for a minterm in Petrick's method.
     * A sum term represents all prime implicants that cover a specific minterm.
     *
     * @param minTerm                  The minterm to find covering implicants for
     * @param remainingPrimeImplicants The set of remaining prime implicants
     * @param implicantIndex           A map from indices to implicants for reference
     * @return A set of indices representing prime implicants that cover the minterm
     */
    private Set<Integer> getSumTerm(int minTerm, Set<Implicant> remainingPrimeImplicants,
                                    Map<Integer, Implicant> implicantIndex) {
        Set<Integer> sumTerm = new HashSet<>();
        for (Implicant implicant : remainingPrimeImplicants) {
            if (implicant.covers(minTerm)) {
                // Add the index of this implicant to the sum term
                for (Map.Entry<Integer, Implicant> entry : implicantIndex.entrySet()) {
                    if (entry.getValue().equals(implicant)) {
                        sumTerm.add(entry.getKey());
                        break;
                    }
                }
            }
        }
        return sumTerm;
    }

    /**
     * Applies Petrick's method to find the minimal cover of prime implicants.
     * This method first identifies essential prime implicants (those that are the only ones covering a minterm),
     * then uses Petrick's method to find the minimal cover for the remaining minterms.
     *
     * @param minTerms        The set of minterms to cover
     * @param primeImplicants The set of prime implicants to choose from
     * @return A minimal set of prime implicants that cover all minterms
     */
    private Set<Implicant> applyPetricksMethod(Set<Integer> minTerms, Set<Implicant> primeImplicants) {
        // If there are no minterms or prime implicants, return an empty set
        if (minTerms.isEmpty() || primeImplicants.isEmpty()) {
            return Collections.emptySet();
        }

        // Create the prime implicant chart (coverage matrix)
        // For each minterm, which prime implicants cover it
        Map<Integer, List<Implicant>> mintermCoverage = new HashMap<>();
        for (int minTerm : minTerms) {
            List<Implicant> coveringImplicants = new ArrayList<>();
            for (Implicant implicant : primeImplicants) {
                if (implicant.covers(minTerm)) {
                    coveringImplicants.add(implicant);
                }
            }
            mintermCoverage.put(minTerm, coveringImplicants);
        }

        // Find essential prime implicants (those that are the only ones covering a minterm)
        Set<Implicant> essentialPrimeImplicants = new LinkedHashSet<>();
        Set<Integer> coveredMinterms = new HashSet<>();

        for (Map.Entry<Integer, List<Implicant>> entry : mintermCoverage.entrySet()) {
            List<Implicant> coveringImplicants = entry.getValue();

            if (coveringImplicants.size() == 1) {
                // This is an essential prime implicant
                Implicant essential = coveringImplicants.getFirst();
                essentialPrimeImplicants.add(essential);

                // Mark all minterms covered by this essential prime implicant
                for (int mt : minTerms) {
                    if (essential.covers(mt)) {
                        coveredMinterms.add(mt);
                    }
                }
            }
        }

        // If all minterms are covered by essential prime implicants, we're done
        if (coveredMinterms.size() == minTerms.size()) {
            return essentialPrimeImplicants;
        }

        // Apply Petrick's method for the remaining minterms
        // Create a set of remaining minterms (those not covered by essential prime implicants)
        Set<Integer> remainingMinterms = new HashSet<>(minTerms);
        remainingMinterms.removeAll(coveredMinterms);

        // Create a set of remaining prime implicants (those not already selected as essential)
        Set<Implicant> remainingPrimeImplicants = new LinkedHashSet<>(primeImplicants);
        remainingPrimeImplicants.removeAll(essentialPrimeImplicants);

        // Create the product of sums expression for Petrick's method
        // Each sum term represents a minterm and contains variables for each prime implicant that covers it
        List<Set<Integer>> productOfSums = new ArrayList<>();
        Map<Integer, Implicant> implicantIndex = new HashMap<>();
        int index = 0;
        for (Implicant implicant : remainingPrimeImplicants) {
            implicantIndex.put(index++, implicant);
        }

        for (int minTerm : remainingMinterms) {
            Set<Integer> sumTerm = getSumTerm(minTerm, remainingPrimeImplicants, implicantIndex);
            if (!sumTerm.isEmpty()) {
                productOfSums.add(sumTerm);
            }
        }

        // If there are no remaining minterms to cover, return just the essential prime implicants
        if (productOfSums.isEmpty()) {
            return essentialPrimeImplicants;
        }

        // Multiply out the product of sums to get a sum of products
        Set<Set<Integer>> sumOfProducts = new HashSet<>();
        sumOfProducts.add(new HashSet<>()); // Start with an empty set

        for (Set<Integer> sumTerm : productOfSums) {
            Set<Set<Integer>> newSumOfProducts = new HashSet<>();

            for (Set<Integer> product : sumOfProducts) {
                for (Integer term : sumTerm) {
                    Set<Integer> newProduct = new HashSet<>(product);
                    newProduct.add(term);
                    newSumOfProducts.add(newProduct);
                }
            }

            sumOfProducts = newSumOfProducts;
        }

        // Find the product term with the fewest variables
        Set<Integer> minimalProduct = null;
        for (Set<Integer> product : sumOfProducts) {
            if (minimalProduct == null || product.size() < minimalProduct.size()) {
                minimalProduct = product;
            }
        }

        // Convert the minimal product term back to implicants
        Set<Implicant> minimalImplicants = new LinkedHashSet<>(essentialPrimeImplicants);
        if (minimalProduct != null) {
            for (Integer idx : minimalProduct) {
                minimalImplicants.add(implicantIndex.get(idx));
            }
        }

        return minimalImplicants;
    }

    /**
     * Converts a set of implicants to an Expression in sum-of-products form.
     * This method handles special cases like empty implicant sets and simplifies the result.
     *
     * @param implicants The set of implicants to convert
     * @return An Expression representing the implicants in sum-of-products form
     */
    private Expression minimize(Set<Implicant> implicants) {
        // If there are no implicants, the expression is false (0)
        if (implicants.isEmpty()) return new LiteralExpression('0');

        List<Expression> terms = new ArrayList<>();
        for (Implicant implicant : implicants) {
            Expression term = implicant.toExpression(vars);
            if (term != null) terms.add(term);
        }
        // If no terms were created, the expression is true (1)
        if (terms.isEmpty()) return new LiteralExpression('1');

        // Create a single term or an OR expression of multiple terms
        Expression minimized = terms.size() == 1 ? terms.getFirst() :
                new NaryExpression(Token.of('|'), Collections.unmodifiableList(terms));
        // Unwrap block expressions
        if (minimized instanceof BlockExpression(Expression inner)) minimized = inner;
        return minimized;
    }

    /**
     * Minimizes the boolean expression using the Quine-McCluskey algorithm with Petrick's method.
     * This is the main public method that orchestrates the entire minimization process.
     *
     * @return A minimized expression in sum-of-products (SOP) form
     */
    public Expression minimize() {
        Set<Integer> minTerms = collectMinTerms();
        List<Set<Implicant>> groups = groupImplicants(minTerms);
        Set<Implicant> primeImplicants = findPrimeImplicants(groups);
        Set<Implicant> minimalImplicants = applyPetricksMethod(minTerms, primeImplicants);
        return minimize(minimalImplicants);
    }
}
