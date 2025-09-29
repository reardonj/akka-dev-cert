package io.example.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import io.example.domain.BookingEvent;
import io.example.domain.Participant;
import io.example.domain.Timeslot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

@ComponentId("booking-slot")
public class BookingSlotEntity extends EventSourcedEntity<Timeslot, BookingEvent> {

    private final String entityId;
    private static final Logger logger = LoggerFactory.getLogger(BookingSlotEntity.class);

    public BookingSlotEntity(EventSourcedEntityContext context) {
        this.entityId = context.entityId();
    }

    public Effect<Done> markSlotAvailable(Command.MarkSlotAvailable cmd) {
        if (currentState().available().contains(cmd.participant)) {
            return effects().error("participant already available");
        }

        // TODO check if booked already?

        var event = new BookingEvent.ParticipantMarkedAvailable(
            this.entityId,
            cmd.participant.id(),
            cmd.participant.participantType()
        );
        return effects().persist(event).thenReply((evt) -> Done.getInstance());
    }

    public Effect<Done> unmarkSlotAvailable(Command.UnmarkSlotAvailable cmd) {
        if (!currentState().available().contains(cmd.participant)) {
            return effects().error("participant not available");
        }

        var event = new BookingEvent.ParticipantUnmarkedAvailable(
            this.entityId,
            cmd.participant.id(),
            cmd.participant.participantType()
        );
        return effects().persist(event).thenReply((evt) -> Done.getInstance());
    }

    // NOTE: booking a slot should produce 3
    // `ParticipantBooked` events
    public Effect<Done> bookSlot(Command.BookReservation cmd) {
        return effects().error("not yet implemented");
    }

    // NOTE: canceling a booking should produce 3
    // `ParticipantCanceled` events
    public Effect<Done> cancelBooking(String bookingId) {
        return effects().error("not yet implemented");

    }

    public ReadOnlyEffect<Timeslot> getSlot() {
        return effects().error("not yet implemented");
    }

    @Override
    public Timeslot emptyState() {
        return new Timeslot(
            // NOTE: these are just estimates for capacity based on it being a sample
            HashSet.newHashSet(10), HashSet.newHashSet(10)
        );
    }

    @Override
    public Timeslot applyEvent(BookingEvent event) {
        return switch (event) {
            case BookingEvent.ParticipantMarkedAvailable available -> currentState().reserve(available);
            case BookingEvent.ParticipantUnmarkedAvailable unavailable -> currentState().unreserve(unavailable);
            default -> currentState();
        };
    }

    public sealed interface Command {
        record MarkSlotAvailable(Participant participant) implements Command {
        }

        record UnmarkSlotAvailable(Participant participant) implements Command {
        }

        record BookReservation(
            String studentId, String aircraftId, String instructorId, String bookingId)
            implements Command {
        }
    }
}
