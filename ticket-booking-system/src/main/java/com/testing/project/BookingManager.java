package com.testing.project;

public class BookingManager {

    private final IPaymentGateway paymentGateway;
    private final INotificationService notificationService;
    private final IEventRepository eventRepository;

    public BookingManager(IPaymentGateway paymentGateway,
                          INotificationService notificationService,
                          IEventRepository eventRepository) {
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
    }

    public boolean bookTicket(String eventId, String customerEmail, double amount) {
        if (eventId == null || eventId.isBlank()
                || customerEmail == null || customerEmail.isBlank()
                || amount <= 0) {
            return false;
        }

        if (eventRepository.isSoldOut(eventId)) {
            return false;
        }

        String transactionId = paymentGateway.processPayment(amount);

        if (transactionId == null || transactionId.isBlank()) {
            return false;
        }

        eventRepository.saveBooking(eventId, customerEmail, transactionId);
        notificationService.sendConfirmation(customerEmail, eventId, transactionId);

        return true;
    }
}

interface IPaymentGateway {
    String processPayment(double amount);
}

interface INotificationService {
    void sendConfirmation(String customerEmail, String eventId, String transactionId);
}

interface IEventRepository {
    boolean isSoldOut(String eventId);
    void saveBooking(String eventId, String customerEmail, String transactionId);
}