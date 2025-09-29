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

        var
            availability =
            new BookingSlotEntity.Command.MarkSlotAvailable(new Participant("student-1", ParticipantType.STUDENT));
        var result = testKit.method(BookingSlotEntity::markSlotAvailable).invoke(availability);
        assertEquals(Done.done(), result.getReply());

        var state = testKit.getState();
        assertFalse(state.available().isEmpty());
    }

    @Test
    public void testDuplicateAvailabilityIsRejected() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var
            availability =
            new BookingSlotEntity.Command.MarkSlotAvailable(new Participant("student-1", ParticipantType.STUDENT));
        testKit.method(BookingSlotEntity::markSlotAvailable).invoke(availability);
        var result = testKit.method(BookingSlotEntity::markSlotAvailable).invoke(availability);
        assertTrue(result.isError());
    }

    @Test
    public void testAvailabilityIsUnmarked() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var participant = new Participant("student-1", ParticipantType.STUDENT);
        var availability = new BookingSlotEntity.Command.MarkSlotAvailable(participant);
        var unavailability = new BookingSlotEntity.Command.UnmarkSlotAvailable(participant);

        testKit.method(BookingSlotEntity::markSlotAvailable).invoke(availability);
        testKit.method(BookingSlotEntity::unmarkSlotAvailable).invoke(unavailability);

        var state = testKit.getState();
        assertTrue(state.available().isEmpty());
    }

    @Test
    public void testUnmarkAvailabilityIsRejected() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var participant = new Participant("student-1", ParticipantType.STUDENT);
        var unavailability = new BookingSlotEntity.Command.UnmarkSlotAvailable(participant);

        var result = testKit.method(BookingSlotEntity::unmarkSlotAvailable).invoke(unavailability);
        assertTrue(result.isError());
    }

    // TODO test that cannot be available when booked?


    @Test
    public void testUnavailableParticipantsNotBooked() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var result = testKit
            .method(BookingSlotEntity::bookSlot)
            .invoke(new BookingSlotEntity.Command.BookReservation(
                "student-1",
                "aircraft-1",
                "instructor-1",
                "booking-1"
            ));

        assertTrue(result.isError());
    }

    @Test
    public void testParticipantsBooked() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var student = new Participant("student-1", ParticipantType.STUDENT);
        var instructor = new Participant("instructor-1", ParticipantType.INSTRUCTOR);
        var aircraft = new Participant("airplane-1", ParticipantType.AIRCRAFT);

        var markAvailable = testKit.method(BookingSlotEntity::markSlotAvailable);
        markAvailable.invoke(new BookingSlotEntity.Command.MarkSlotAvailable(student));
        markAvailable.invoke(new BookingSlotEntity.Command.MarkSlotAvailable(instructor));
        markAvailable.invoke(new BookingSlotEntity.Command.MarkSlotAvailable(aircraft));

        var result = testKit
            .method(BookingSlotEntity::bookSlot)
            .invoke(new BookingSlotEntity.Command.BookReservation(
                student.id(),
                aircraft.id(),
                instructor.id(),
                "booking-1"
            ));

        assertEquals(Done.done(), result.getReply());
    }

    @Test
    public void testBookingCancelled() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var student = new Participant("student-1", ParticipantType.STUDENT);
        var instructor = new Participant("instructor-1", ParticipantType.INSTRUCTOR);
        var aircraft = new Participant("airplane-1", ParticipantType.AIRCRAFT);

        var markAvailable = testKit.method(BookingSlotEntity::markSlotAvailable);
        markAvailable.invoke(new BookingSlotEntity.Command.MarkSlotAvailable(student));
        markAvailable.invoke(new BookingSlotEntity.Command.MarkSlotAvailable(instructor));
        markAvailable.invoke(new BookingSlotEntity.Command.MarkSlotAvailable(aircraft));

        // First book the slot
        var bookResult = testKit
            .method(BookingSlotEntity::bookSlot)
            .invoke(new BookingSlotEntity.Command.BookReservation(
                student.id(),
                aircraft.id(),
                instructor.id(),
                "booking-1"
            ));
        assertEquals(Done.done(), bookResult.getReply());

        // Then cancel the booking
        var cancelResult = testKit.method(BookingSlotEntity::cancelBooking).invoke("booking-1");
        assertEquals(Done.done(), cancelResult.getReply());

        // Verify that the booking is removed from the state
        var state = testKit.getState();
        assertTrue(state.bookings().isEmpty());

        // Verify that the participants are now available again
        assertFalse(state.available().isEmpty());
    }

    @Test
    public void testCancelNonExistentBooking() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var result = testKit
            .method(BookingSlotEntity::cancelBooking)
            .invoke("non-existent-booking");
        assertTrue(result.isError());
    }

    @Test
    public void testCancelBookingWhenSlotNotBooked() {
        // Create an instance of the test kit for BookingSlotEntity
        var testKit = EventSourcedTestKit.of(BookingSlotEntity::new);

        var student = new Participant("student-1", ParticipantType.STUDENT);
        var markAvailable = testKit.method(BookingSlotEntity::markSlotAvailable);
        markAvailable.invoke(new BookingSlotEntity.Command.MarkSlotAvailable(student));

        // Try to cancel a booking when no booking exists for this slot
        var result = testKit
            .method(BookingSlotEntity::cancelBooking)
            .invoke("booking-1");
        assertTrue(result.isError());
    }

}
