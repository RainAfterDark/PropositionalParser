# Propositional Parser

Simple program for generating truth tables
from propositional logic expressions in Java.

## Prerequisites

- JDK **24** or higher
- IntelliJ IDEA (optional, but recommended for development)

## Build

This project uses Maven for dependency management. To build,
run `mvn clean package` or the `Package` run configuration in IDEA.
It will generate an executable in `target/jpackage` directory.

## Run

In IDEA, simply run the `Main` run configuration. If you built
the project using Maven, you can run the generated executable
in the `target/jpackage` directory. To run tests, use the
`Test` run configuration in IDEA or run `mvn test`.

## Usage

Enter `?` to display the help menu.

Simply enter a valid expression, and it will generate a truth table for it.
Add `$` to minimize the expression (using Quine-McCluskey algorithm).

Logical operators are expressed as follows:

- Negation (NOT) = `~`
- Conjunction (AND) = `&`
- Disjunction (OR) = `|`
- Implication = `>`
- Biconditional / Equality = `=`

Variables can be any letter, `a-z`, case-**insensitive**.
Literals (`true` or `false`) are represented as `1` or `0`, respectively.
Brackets `(` and `)` can be used to group expressions.

## Parser

There are two implementations for the parser generating
the abstract syntax tree for given expressions:

- `PrattParser.java` - Default, uses a top-down operator
  precedence approach. Defining precedence for operators
  using binding power values gives a lot of flexibility.
- `RDParser.java` - Standard recursive descent approach,
  but the resulting code for this implementation is
  lengthier, redundant, and not as maintainable.

## Minimizer

The Quine-McCluskey algorithm is used for minimizing expressions.
It is a simple, brute-force algorithm that works by comparing
binary representations of truth tables. It is not the most
efficient algorithm, but it is simple and works well for small
expressions.

Do note that the minimizer will expand biconditionals to their
canonical sum-of-products, and will result in a much longer
expression than the original one. This is a limitation of the
algorithm.
