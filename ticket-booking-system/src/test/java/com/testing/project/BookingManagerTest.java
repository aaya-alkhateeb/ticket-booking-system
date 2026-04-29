package com.testing.project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


public class BookingManagerTest {

    private IPaymentGateway paymentGateway;
    private INotificationService notificationService;
    private IEventRepository eventRepository;
    private BookingManager bookingManager;

    @BeforeEach
    void setUp() {
        paymentGateway = mock(IPaymentGateway.class);
        notificationService = mock(INotificationService.class);
        eventRepository = mock(IEventRepository.class);

        bookingManager = new BookingManager(
                paymentGateway,
                notificationService,
                eventRepository
        );
    }

    @Test
    void happyPath_shouldSaveBookingAndSendConfirmationExactlyOnce() {
        String eventId = "EVT-101";
        String customerEmail = "student@example.com";
        double amount = 25.0;
        String transactionId = "TXN-999";

        when(eventRepository.isSoldOut(eventId)).thenReturn(false);
        when(paymentGateway.processPayment(amount)).thenReturn(transactionId);

        boolean result = bookingManager.bookTicket(eventId, customerEmail, amount);

        assertTrue(result);

        verify(eventRepository, times(1)).isSoldOut(eventId);
        verify(paymentGateway, times(1)).processPayment(amount);
        verify(eventRepository, times(1))
                .saveBooking(eventId, customerEmail, transactionId);
        verify(notificationService, times(1))
                .sendConfirmation(customerEmail, eventId, transactionId);
    }
}