package uk.gov.dwp.uc.pairtest.exception;

// exception package
public class InvalidPurchaseException extends RuntimeException {

    private final Reason reason;

    public InvalidPurchaseException(Reason reason) {
        super(reason.getMessage());
        this.reason = reason;
    }

    public InvalidPurchaseException(Reason reason, String customMessage) {
        super(customMessage);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {

        INVALID_ACCOUNT_ID("Invalid Account ID"),

        EMPTY_REQUEST("At least one ticket type must be requested"),

        INVALID_REQUEST("Invalid ticket requested"),

        INVALID_TICKET_TYPE("Ticket type must not be null"),

        INVALID_TICKET_QUANTITY("Number of tickets must be greater than zero"),

        MAX_TICKET_LIMIT_EXCEEDED("Cannot purchase more than 25 tickets at once"),

        ADULT_REQUIRED("Child and infant tickets require at least one adult ticket"),

        INFANTS_EXCEED_ADULTS("Each infant must be accompanied by one adult");

        private final String message;

        Reason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    // without customized error massages : default way
    // public InvalidPurchaseException() {
    //     super("Ticket purchase request is invalid");
    // }

    // public InvalidPurchaseException(String message) {
    //     super(message);
    // }

}
