package me.ele.lancet.plugin.internal.preprocess;

/**
 * Created by gengwanpeng on 17/5/4.
 */
public class ParseFailureException extends RuntimeException {
    public ParseFailureException() {
    }

    public ParseFailureException(String message) {
        super(message);
    }

    public ParseFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseFailureException(Throwable cause) {
        super(cause);
    }
}
