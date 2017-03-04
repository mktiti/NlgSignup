package hu.titi.nlg.util;

public class ConfirmRequest {

    private final String path;
    private final String message;

    public static final Object CONFIRM = new Object();

    public ConfirmRequest(String path, String message) {
        this.path = path;
        this.message = message;
    }

    public ConfirmRequest(String path) {
        this.path = path;
        message = "Biztos vagy benne?";
    }

}