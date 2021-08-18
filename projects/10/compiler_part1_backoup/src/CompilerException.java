/**
 *  Define the compile exception
 */
public class CompilerException extends Exception {

    private final Integer lineNumber;
    private final Integer colNumber;

    public CompilerException(String message) {
        this(message, null, null);
    }

    public CompilerException(String message, Integer lineNumber, Integer colNumber) {
        super(message);
        this.lineNumber = lineNumber;
        this.colNumber = colNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public Integer getColNumber() {
        return colNumber;
    }
}
