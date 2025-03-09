package dark.after.rain.input;

public abstract class InputString {
    protected final String input;

    protected InputString(String input) {
        this.input = input.trim().toLowerCase()
                .replaceAll("\\n", " ");
    }

    protected RuntimeException error(String message, int index) {
        String arrow = String.format("%" + (index + 1) + "s", "^");
        return new RuntimeException(message + "\n\t" + input + "\n\t" + arrow);
    }
}
