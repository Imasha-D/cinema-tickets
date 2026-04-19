package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

class TicketServiceImplTest {

        private TicketPaymentService ticketPaymentService;
        private SeatReservationService seatReservationService;
        private TicketServiceImpl ticketService;

        @BeforeEach
        void setUp() {
                ticketPaymentService = mock(TicketPaymentService.class);
                seatReservationService = mock(SeatReservationService.class);
                ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
        }

        // Success Scenario : Happy Path
        @Nested
        class ValidPurchaseTests {

                @Test
                void shouldPurchaseAdultTicketsSuccessfully() {

                        Long accountId = 1L;

                        ticketService.purchaseTickets(
                                        accountId,
                                        request(Type.ADULT, 2));

                        verify(ticketPaymentService).makePayment(accountId, 50);
                        verify(seatReservationService).reserveSeat(accountId, 2);
                        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);

                }

                @Test
                void shouldPurchaseAdultAndChildTicketsSuccessfully() {
                        Long accountId = 7L;

                        ticketService.purchaseTickets(
                                        accountId,
                                        request(Type.ADULT, 1),
                                        request(Type.CHILD, 1));

                        verify(ticketPaymentService).makePayment(accountId, 40);
                        verify(seatReservationService).reserveSeat(accountId, 2);
                        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldPurchaseAdultChildInfantTicketsSuccessfully() {
                        Long accountId = 1L;

                        ticketService.purchaseTickets(
                                        accountId,
                                        request(Type.ADULT, 2), request(Type.CHILD, 2), request(Type.INFANT, 1));

                        verify(ticketPaymentService).makePayment(accountId, 80);
                        verify(seatReservationService).reserveSeat(accountId, 4);
                        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldAllowInfantsEqualToAdults() {
                        Long accountId = 6L;

                        ticketService.purchaseTickets(
                                        accountId,
                                        request(Type.ADULT, 2),
                                        request(Type.INFANT, 2));

                        verify(ticketPaymentService).makePayment(accountId, 50);
                        verify(seatReservationService).reserveSeat(accountId, 2);
                        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldAggregateMultipleRequestsOfSameTicketType() {
                        Long accountId = 5L;

                        ticketService.purchaseTickets(
                                        accountId,
                                        request(Type.ADULT, 1),
                                        request(Type.ADULT, 2),
                                        request(Type.CHILD, 1));

                        verify(ticketPaymentService).makePayment(accountId, 90);
                        verify(seatReservationService).reserveSeat(accountId, 4);
                        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldCalculatePaymentAndSeatsCorrectlyForMixedTickets() {
                        Long accountId = 2L;

                        ticketService.purchaseTickets(
                                        accountId,
                                        request(Type.ADULT, 2),
                                        request(Type.CHILD, 3),
                                        request(Type.INFANT, 1));

                        verify(ticketPaymentService).makePayment(accountId, 95);
                        verify(seatReservationService).reserveSeat(accountId, 5);
                        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldNotAllocateSeatsForInfants() {
                        Long accountId = 3L;

                        ticketService.purchaseTickets(
                                        accountId,
                                        request(Type.ADULT, 2),
                                        request(Type.INFANT, 2));

                        verify(ticketPaymentService).makePayment(accountId, 50);
                        verify(seatReservationService).reserveSeat(accountId, 2);
                        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldAllowExactlyTwentyFiveTickets() {
                        Long accountId = 4L;

                        ticketService.purchaseTickets(
                                        accountId,
                                        request(Type.ADULT, 10),
                                        request(Type.CHILD, 10),
                                        request(Type.INFANT, 5));

                        verify(ticketPaymentService).makePayment(accountId, 400);
                        verify(seatReservationService).reserveSeat(accountId, 20);
                        verifyNoMoreInteractions(ticketPaymentService, seatReservationService);
                }

        }

        // Invalid Accounts check
        @Nested
        class InvalidAccountTests {

                @Test
                void shouldRejectWhen_AccountIdIsNull() {

                        TicketTypeRequest req = request(Type.ADULT, 2);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(null, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_ACCOUNT_ID,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldRejectWhen_AccountIdEqualsZero() {
                        TicketTypeRequest req = request(Type.ADULT, 2);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(0L, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_ACCOUNT_ID,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

                @Test
                void shouldRejectWhen_AccountIdIsNegative() {
                        TicketTypeRequest req = request(Type.ADULT, 2);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(-1L, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_ACCOUNT_ID,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

        }

        // Invalid Ticket Request Check
        @Nested
        class InvalidTicketRequests {

                @Test
                void shouldRejectWhen_TicketRequestIsNull() {

                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_REQUEST,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldRejectWhen_TicketRequestIsNotProvided() {

                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_REQUEST,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldRejectWhen_RequestContainsNullTicketTypeRequest() {

                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, (TicketTypeRequest) null));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_TICKET_TYPE,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);
                }

                @Test
                void shouldRejectWhen_RequestHasNoTicketType() {
                        TicketTypeRequest req = request(null, 2);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_TICKET_TYPE,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

                @Test
                void shouldRejectWhen_RequestTicketQuantityIsZero() {
                        TicketTypeRequest req = request(Type.ADULT, 0);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_TICKET_QUANTITY,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

                @Test
                void shouldRejectWhen_RequestTicketQuantityIsNegative() {
                        TicketTypeRequest req = request(Type.ADULT, -1);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INVALID_TICKET_QUANTITY,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

        }

        @Nested
        class BusinessRuleValidationTests {

                // Rule 01; Max ticket count should be 25
                @Test
                void shouldRejectWhen_MoreThanTwentyFiveTicketsPurchased() {
                        TicketTypeRequest req = request(Type.ADULT, 26);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.MAX_TICKET_LIMIT_EXCEEDED,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

                // Rule 02 : at Least one Adult ticket required to have child and infant tickets
                @Test
                void shouldRejectWhen_ChildTicketsWithoutAdult() {
                        TicketTypeRequest req = request(Type.CHILD, 2);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.ADULT_REQUIRED,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

                @Test
                void shouldRejectWhen_InfantTicketsWithoutAdult() {
                        TicketTypeRequest req = request(Type.INFANT, 2);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, req));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.ADULT_REQUIRED,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

                @Test
                void shouldRejectWhen_ChildAndInfantTicketsWithoutAdult() {
                        TicketTypeRequest req1 = request(Type.CHILD, 2);
                        TicketTypeRequest req2 = request(Type.INFANT, 2);
                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, req1, req2));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.ADULT_REQUIRED,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

                // Rule 03 : Lap Rule (infant sit on Adult lap)
                @Test
                void shouldRejectWhen_InfantsMoreThanAdults() {
                        TicketTypeRequest req1 = request(Type.ADULT, 1);
                        TicketTypeRequest req2 = request(Type.INFANT, 2);

                        InvalidPurchaseException exception = assertThrows(
                                        InvalidPurchaseException.class,
                                        () -> ticketService.purchaseTickets(1L, req1, req2));

                        assertEquals(uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.Reason.INFANTS_EXCEED_ADULTS,
                                        exception.getReason());
                        verifyNoInteractions(ticketPaymentService, seatReservationService);

                }

        }

        // Helper method
        private TicketTypeRequest request(Type type, int quantity) {
                return new TicketTypeRequest(type, quantity);
        }

}
