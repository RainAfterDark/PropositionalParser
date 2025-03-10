package dark.after.rain.input;

public class PrefixArgs extends InputString {
    private final boolean showHelp;
    private final boolean simplify;
    private final boolean showTable;
    private final String unPrefixedInput;

    public PrefixArgs(String input) {
        super(input);
        boolean showHelp = false;
        boolean simplify = false;
        boolean showTable = true;
        String unPrefixedInput = input;

        if (input.startsWith("?")) {
            unPrefixedInput = input.substring(1);
            showHelp = true;
            showTable = false;
        } else if (input.startsWith("$!")) {
            unPrefixedInput = input.substring(2);
            simplify = true;
        } else if (input.startsWith("$")) {
            unPrefixedInput = input.substring(1);
            simplify = true;
            showTable = false;
        }

        this.showHelp = showHelp;
        this.simplify = simplify;
        this.showTable = showTable;
        this.unPrefixedInput = unPrefixedInput;
    }

    public boolean shouldShowHelp() {
        return showHelp;
    }

    public boolean shouldSimplify() {
        return simplify;
    }

    public boolean shouldShowTable() {
        return showTable;
    }

    public String getUnPrefixedInput() {
        return unPrefixedInput;
    }
}
