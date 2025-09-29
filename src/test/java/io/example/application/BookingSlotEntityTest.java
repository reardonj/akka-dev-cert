package io.example.application;

import akka.Done;
import akka.javasdk.testkit.EventSourcedTestKit;
import akka.javasdk.testkit.TestKitSupport;
import io.example.domain.Participant;
import io.example.domain.Participant.ParticipantType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingSlotEntityTest extends TestKitSupport {

    @Test
    public void testInitialBookingSlotStateIsEmpty() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);
        var state = testKit.getState();
        assertTrue(state.bookings().isEmpty());
        assertTrue(state.available().isEmpty());
    }

    @Test
    public void testAvailabilityIsAdded() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var availability = new BookingSlotEntity.Command.MarkSlotAvailable(
            new Participant("student-1", ParticipantType.STUDENT)
        );
        var result = testKit.method(BookingSlotEntity::markSlotAvailable).invoke(availability);
        assertEquals(Done.getInstance(), result.getReply());

        var state = testKit.getState();
        assertFalse(state.available().isEmpty());
    }

    @Test
    public void testDuplicateAvailabilityIsRejected() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var availability = new BookingSlotEntity.Command.MarkSlotAvailable(
            new Participant("student-1", ParticipantType.STUDENT)
        );
        testKit.method(BookingSlotEntity::markSlotAvailable).invoke(availability);
        var result = testKit.method(BookingSlotEntity::markSlotAvailable).invoke(availability);
        assertTrue(result.isError());
    }

    // TODO test that cannot be available when booked
}
