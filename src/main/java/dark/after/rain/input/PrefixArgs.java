package dark.after.rain.input;

public class PrefixArgs extends InputString {
    private final boolean showHelp;
    private final boolean simplify;
    private final String unPrefixedInput;

    public PrefixArgs(String input) {
        super(input);
        boolean showHelp = false;
        boolean simplify = false;
        String unPrefixedInput = input;

        if (input.contains("?")) {
            unPrefixedInput = input.replace("?", "");
            showHelp = true;
        } else if (input.contains("$")) {
            unPrefixedInput = input.replace("$", "");
            simplify = true;
        }

        this.showHelp = showHelp;
        this.simplify = simplify;
        this.unPrefixedInput = unPrefixedInput;
    }

    public boolean shouldShowHelp() {
        return showHelp;
    }

    public boolean shouldSimplify() {
        return simplify;
    }

    public String getUnPrefixedInput() {
        return unPrefixedInput;
    }
}
