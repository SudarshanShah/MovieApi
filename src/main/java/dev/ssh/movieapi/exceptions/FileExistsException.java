package dev.ssh.movieapi.exceptions;

public class FileExistsException extends RuntimeException {

    public FileExistsException(String message) {
        super(message);
    }
}
