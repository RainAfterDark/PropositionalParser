package dark.after.rain.lexer;

public record Token(TokenType type, char value, int index) {
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
