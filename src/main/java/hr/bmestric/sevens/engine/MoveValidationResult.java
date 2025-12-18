package hr.bmestric.sevens.engine;

public class MoveValidationResult {
    private final boolean valid;
    private final String reason;

    public static MoveValidationResult valid() {
        return new MoveValidationResult(true, null);
    }

    public static MoveValidationResult invalid(String reason) {
        return new MoveValidationResult(false, reason);
    }

    private MoveValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return valid ? "Valid" : "Invalid: " + reason;
    }
}
