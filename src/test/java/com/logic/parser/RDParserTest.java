package com.logic.parser;

public class RDParserTest extends ParserTest {
    @Override
    protected Parser getParser(String input) {
        return new RDParser(input);
    }
}
