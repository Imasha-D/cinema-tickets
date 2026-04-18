package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     * Service Layer
     */

    // fixed ticket prices
    private static final int ADULT_PRICE = 25;
    private static final int CHILD_PRICE = 15;
    private static final int MAX_TICKETS = 25;

    // dependencies
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    // constructor
    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {

        // Basic validation : input validations
        this.validateAccount(accountId); // validate account
        this.validateTicketTypeRequests(ticketTypeRequests); // validate Ticket type requests array

        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            // validate each ticket request
            this.validateSingleTicketReq(ticketTypeRequest);

            switch (ticketTypeRequest.getTicketType()) {
                case ADULT:
                    adultCount += ticketTypeRequest.getNoOfTickets();
                    break;
                case CHILD:
                    childCount += ticketTypeRequest.getNoOfTickets();
                    break;
                case INFANT:
                    infantCount += ticketTypeRequest.getNoOfTickets();
                    break;
                default:
                    throw new InvalidPurchaseException(InvalidPurchaseException.Reason.EMPTY_REQUEST);
            }

        }

        // Create value object
        PassengerCount passengerCount = new PassengerCount(adultCount, childCount, infantCount);

        // validate business rules
        this.validateBusinessRules(passengerCount);

        // calculations
        int totalAmount = this.calculateAmount(passengerCount); // calculate total amount
        int totalSeatsToReserve = passengerCount.totalSeats(); // calculate seats

        // call third-party services
        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeatsToReserve);

    }

    // helper methods
    private void validateAccount(Long accountId) {
        // Assumption 1 : have sufficient funds to pay for any number of tickets
        if (accountId == null || accountId <= 0)
            throw new InvalidPurchaseException(InvalidPurchaseException.Reason.INVALID_ACCOUNT_ID);
    }

    private void validateTicketTypeRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0)
            throw new InvalidPurchaseException(InvalidPurchaseException.Reason.INVALID_REQUEST);
    }

    private void validateSingleTicketReq(TicketTypeRequest ticketTypeRequest) {
        if (ticketTypeRequest == null || ticketTypeRequest.getTicketType() == null)
            throw new InvalidPurchaseException(InvalidPurchaseException.Reason.INVALID_TICKET_TYPE);
        if (ticketTypeRequest.getNoOfTickets() <= 0)
            throw new InvalidPurchaseException(InvalidPurchaseException.Reason.INVALID_TICKET_QUANTITY);
    }

    private void validateBusinessRules(PassengerCount passengerCount) {
        int totalCount = passengerCount.totalCount();

        // Rule 01; Max ticket count should be 25
        if (totalCount > MAX_TICKETS)
            throw new InvalidPurchaseException(InvalidPurchaseException.Reason.MAX_TICKET_LIMIT_EXCEEDED);

        // Rule 02 : at Least one Adult ticket required to have child and infant tickets
        if (passengerCount.adultCount == 0 && (passengerCount.childCount > 0 || passengerCount.infantCount > 0))
            throw new InvalidPurchaseException(InvalidPurchaseException.Reason.ADULT_REQUIRED);

        // Rule 03 : Lap Rule (infant sit on Adult lap)
        if (passengerCount.infantCount > passengerCount.adultCount)
            throw new InvalidPurchaseException(InvalidPurchaseException.Reason.INFANTS_EXCEED_ADULTS);

    }

    private int calculateAmount(PassengerCount passengerCount) {
        return (passengerCount.adultCount * ADULT_PRICE + passengerCount.childCount * CHILD_PRICE);

    }

    // ================= VALUE OBJECT =================
    private record PassengerCount(
            int adultCount,
            int childCount,
            int infantCount) {

        public int totalCount() {
            return adultCount + childCount + infantCount;
        }

        public int totalSeats() {
            return adultCount + childCount;
        }
    }
}
