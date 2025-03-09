# Propositional Parser

Simple program for generating truth tables
from propositional logic expressions in Java.

## Prerequisites

- JDK 23 or higher

## How to Use

Simply enter a valid expression to evaluate.

Logical operators are expressed as follows:

- Negation (NOT) = `~`
- Conjunction (AND) = `&`
- Disjunction (OR) = `|`
- Implication = `>`
- Biconditional / Equality = `=`

Variables can be any letter, `A-Z`, case-**insensitive**.

## Parser

There are two implementations for the parser generating
the abstract syntax tree for given expressions:

- `PrattParser.java` - Default, uses a top-down operator
precedence approach. Defining precedence for operators
using binding power values gives a lot of flexibility.
- `RDParser.java` - Standard recursive descent approach,
but the resulting code for this implementation is 
lengthier, redundant, and not as maintainable.