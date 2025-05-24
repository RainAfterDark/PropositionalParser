package com.logic.ast;

import java.util.List;
import java.util.Map;

/**
 * Building block of the AST. An expression simply implements
 * evaluation and variable collection.
 */
public interface Expression {
    /**
     * Evaluates the expression provided a context map for variables.
     *
     * @param context A map of variable names to their truth values
     * @return The evaluated truth value
     */
    boolean evaluate(Map<Character, Boolean> context);

    /**
     * Collects the list of variables used in the expression.
     *
     * @return The list of character variables
     */
    List<Character> collectVariables();
}
