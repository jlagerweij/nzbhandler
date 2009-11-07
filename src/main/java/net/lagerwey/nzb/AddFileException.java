package net.lagerwey.nzb;

/**
 * Thrown when adding a file to Sabnzbd fails.
 */
public class AddFileException extends Exception {

    public AddFileException() {
    }

    public AddFileException(String s) {
        super(s);
    }

    public AddFileException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AddFileException(Throwable throwable) {
        super(throwable);
    }
}
