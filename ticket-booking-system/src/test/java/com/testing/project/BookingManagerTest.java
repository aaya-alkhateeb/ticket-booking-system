package com.testing.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    public void testInvalidInput_shouldNotProcessBoking() {
        String eventId = "";
        String customerEmail = "test@ala.com";
        double amount = 100;

        boolean result = bookingManager.bookTicket(eventId, customerEmail, amount);
        assertFalse(result);

        verify(paymentGateway, never()).processPayment(anyDouble());
        verify(eventRepository, never()).saveBooking(any(), any(), any());
        verify(notificationService, never()).sendConfirmation(any(), any(), any());
    }

    @Test
    public void testBookTicket_WhenSoldOut_ShouldReturnFalse() {
        String eventId = "E123";
        String customerEmail = "user@example.com";
        double amount = 100.0;

        when(eventRepository.isSoldOut(eventId)).thenReturn(true);[cite: 1]

        boolean result = bookingManager.bookTicket(eventId, customerEmail, amount);

        assertFalse(result);[cite: 1]
        verify(eventRepository, times(1)).isSoldOut(eventId);[cite: 1]
        verify(paymentGateway, never()).processPayment(anyDouble());[cite: 1]
        verify(eventRepository, never()).saveBooking(anyString(), anyString(), anyString());[cite: 1]
        verify(notificationService, never()).sendConfirmation(anyString(), anyString(), anyString());[cite: 1]
    }
}
