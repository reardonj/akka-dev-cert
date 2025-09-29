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
        // Verify the cart via the GET endpoint
        var response = httpClient.POST("/flight/availability/" + slotId).withRequestBody("{\"participantId\": \"alice\", \"participantType\": \"bad type\"}").invoke();

        Assertions.assertEquals(StatusCodes.BAD_REQUEST, response.status());
    }

    @Test
    public void availabilityEndpointAcceptsValidParticipantType() {
        String slotId = UUID.randomUUID().toString();
        // Verify the cart via the GET endpoint
        var response = httpClient.POST("/flight/availability/" + slotId).withRequestBody(new FlightEndpoint.AvailabilityRequest("student-1", "student")).invoke();

        Assertions.assertEquals(StatusCodes.OK, response.status());
    }

}
