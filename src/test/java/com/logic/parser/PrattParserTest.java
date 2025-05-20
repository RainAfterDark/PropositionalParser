package com.logic.parser;

public class PrattParserTest extends ParserTest {
    @Override
    protected Parser getParser(String input) {
        return new PrattParser(input);
    }
}
