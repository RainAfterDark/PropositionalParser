package dark.after.rain.reducer;

import dark.after.rain.ast.BlockExpression;
import dark.after.rain.ast.Expression;
import dark.after.rain.ast.LiteralExpression;
import dark.after.rain.ast.NaryExpression;
import dark.after.rain.lexer.Token;

import java.util.*;

public class QmcMinimizer {
    private final Expression expr;
    private final List<Character> vars;

    public QmcMinimizer(Expression expr) {
        this.expr = expr;
        vars = expr.collectVariables();
    }

    private Set<Integer> collectMinTerms() {
        Set<Integer> minTerms = new HashSet<>();
        int rows = 1 << vars.size(); // 2^n
        for (int i = 0; i < rows; i++) {
            Map<Character, Boolean> context = new HashMap<>();
            for (int j = 0; j < vars.size(); j++) {
                int digit = vars.size() - j - 1;
                boolean truth = ((i >> digit) & 1) == 1;
                context.put(vars.get(digit), truth);
            }
            if (expr.evaluate(context)) {
                minTerms.add(i);
            }
        }
        return Collections.unmodifiableSet(minTerms);
    }

    private List<Set<Implicant>> generateEmptyGroups() {
        List<Set<Implicant>> groups = new ArrayList<>();
        for (int i = 0; i < vars.size() + 1; i++) {
            groups.add(new LinkedHashSet<>());
        }
        return groups;
    }

    private List<Set<Implicant>> groupImplicants(Set<Integer> minTerms) {
        List<Set<Implicant>> groups = generateEmptyGroups();
        for (int minTerm : minTerms) {
            Implicant implicant = Implicant.fromMinTerm(minTerm, vars.size());
            groups.get(implicant.countOnes()).add(implicant);
        }
        return Collections.unmodifiableList(groups);
    }

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

            // Add implicants that were not consumed
            for (Set<Implicant> current : groups) {
                for (Implicant implicant : current) {
                    if (!consumed.contains(implicant)) {
                        primeImplicants.add(implicant);
                    }
                }
            }
            groups = Collections.unmodifiableList(nextGroups);
        }

        // Add remaining implicants
        for (Set<Implicant> group : groups) {
            primeImplicants.addAll(group);
        }
        return Collections.unmodifiableSet(primeImplicants);
    }

    private Expression minimize(Set<Implicant> primeImplicants) {
        if (primeImplicants.isEmpty()) return new LiteralExpression('0');

        List<Expression> terms = new ArrayList<>();
        for (Implicant implicant : primeImplicants) {
            Expression term = implicant.toExpression(vars);
            if (term != null) terms.add(term);
        }
        if (terms.isEmpty()) return new LiteralExpression('1');

        Expression minimized = terms.size() == 1 ? terms.getFirst() :
                new NaryExpression(Token.of('|'), Collections.unmodifiableList(terms));
        if (minimized instanceof BlockExpression(Expression inner)) minimized = inner;
        return minimized;
    }

    public Expression minimize() {
        Set<Integer> minTerms = collectMinTerms();
        List<Set<Implicant>> groups = groupImplicants(minTerms);
        Set<Implicant> primeImplicants = findPrimeImplicants(groups);
        return minimize(primeImplicants);
    }
}
