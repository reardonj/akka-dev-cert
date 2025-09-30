package io.example.api;

import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.testkit.TestKitSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class FlightEndpointIntegrationTest extends TestKitSupport {

    @Test
    public void availabilityEndpointRejectsInvalidParticipantType() {
        String slotId = UUID.randomUUID().toString();
        var response = httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody("{\"participantId\": \"alice\", \"participantType\": \"bad type\"}")
            .invoke();

        Assertions.assertEquals(StatusCodes.BAD_REQUEST, response.status());
    }

    @Test
    public void availabilityEndpointAcceptsValidParticipantType() {
        String slotId = UUID.randomUUID().toString();
        var response = httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("student-1", "student"))
            .invoke();

        Assertions.assertEquals(StatusCodes.OK, response.status());
    }


    @Test
    public void removeAvailabilityEndpointRejectsInvalidParticipantType() {
        String slotId = UUID.randomUUID().toString();
        var response = httpClient
            .DELETE("/flight/availability/" + slotId)
            .withRequestBody("{\"participantId\": \"alice\", \"participantType\": \"bad type\"}").invoke();

        Assertions.assertEquals(StatusCodes.BAD_REQUEST, response.status());
    }

    @Test
    public void removeAvailabilityEndpointAcceptsValidParticipantType() {
        String slotId = UUID.randomUUID().toString();

        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("student-1", "student"))
            .invoke();
        var response = httpClient
            .DELETE("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("student-1", "student")).invoke();

        Assertions.assertEquals(StatusCodes.OK, response.status());
    }

    @Test
    public void bookSlot() {
        String slotId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();

        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("student-1", "student"))
            .invoke();
        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("aircraft-1", "aircraft"))
            .invoke();
        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("instructor-1", "instructor"))
            .invoke();
        var response = httpClient
            .POST("/flight/bookings/" + slotId)
            .withRequestBody(new FlightEndpoint.BookingRequest("student-1", "aircraft-1", "instructor-1", bookingId))
            .invoke();

        Assertions.assertEquals(StatusCodes.CREATED, response.status());
    }

    @Test
    public void cancelExistingBooking() {
        String slotId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();

        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("student-1", "student"))
            .invoke();
        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("aircraft-1", "aircraft"))
            .invoke();
        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("instructor-1", "instructor"))
            .invoke();
        httpClient
            .POST("/flight/bookings/" + slotId)
            .withRequestBody(new FlightEndpoint.BookingRequest("student-1", "aircraft-1", "instructor-1", bookingId))
            .invoke();
        var response = httpClient
            .DELETE("/flight/bookings/" + slotId + "/" + bookingId)
            .invoke();

        Assertions.assertEquals(StatusCodes.OK, response.status());
    }

    @Test
    public void cancelNonExistentBooking() {
        String slotId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();

        var response = httpClient
            .DELETE("/flight/bookings/" + slotId + "/" + bookingId)
            .invoke();

        Assertions.assertEquals(StatusCodes.BAD_REQUEST, response.status());
    }

    @Test
    public void cancelBookingWhenSlotNotBooked() {
        String slotId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();

        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("student-1", "student"))
            .invoke();
        var response = httpClient
            .DELETE("/flight/bookings/" + slotId + "/" + bookingId)
            .invoke();

        Assertions.assertEquals(StatusCodes.BAD_REQUEST, response.status());
    }

    @Test
    public void bookUnavailableSlot() {
        String slotId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();

        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("student-1", "student"))
            .invoke();
        httpClient
            .POST("/flight/availability/" + slotId)
            .withRequestBody(new FlightEndpoint.AvailabilityRequest("aircraft-1", "aircraft"))
            .invoke();
        var response = httpClient
            .POST("/flight/bookings/" + slotId)
            .withRequestBody(new FlightEndpoint.BookingRequest("student-1", "aircraft-1", "instructor-1", bookingId))
            .invoke();

        Assertions.assertEquals(StatusCodes.BAD_REQUEST, response.status());
    }

}
