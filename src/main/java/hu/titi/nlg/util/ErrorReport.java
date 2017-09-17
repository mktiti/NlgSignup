package hu.titi.nlg.util;

public class ErrorReport {

    public enum ErrorType {
        LOGIN("Hibás felhasználónév vagy jelszó"),
        SIGNUP("A jelentkezés nem sikerült"),
        ADD("A hozzáadás nem sikerült"),
        REPORT("Hiba lépett fel a jelentés elkészítése közben"),
        DELETE("Hiba lépett fel a törlés közben"),
        UPLOAD("Hiba lépett fel a fájl feldolgozása közben"),
        MODIFY("Hiba lépett fel a módosítás közben");

        public final String message;

        ErrorType(String message) {
            this.message = message;
        }
    }

    private final ErrorType type;
    private final String message;

    public ErrorReport(ErrorType type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getText() {
        if (message != null) {
            return type.message + " [" + message + "]";
        } else {
            return type.message;
        }
    }

}