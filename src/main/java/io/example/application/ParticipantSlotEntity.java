package io.example.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.TypeName;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import io.example.domain.Participant.ParticipantType;

@ComponentId("participant-slot")
public class ParticipantSlotEntity extends EventSourcedEntity<ParticipantSlotEntity.State,
    ParticipantSlotEntity.Event> {

    public Effect<Done> unmarkAvailable(ParticipantSlotEntity.Commands.UnmarkAvailable unmark) {
        var event = new ParticipantSlotEntity.Event.UnmarkedAvailable(
            unmark.slotId(),
            unmark.participantId(),
            unmark.participantType()
        );
        return effects().persist(event).thenReply((state) -> Done.done());
    }

    public Effect<Done> markAvailable(ParticipantSlotEntity.Commands.MarkAvailable mark) {
        var event = new ParticipantSlotEntity.Event.MarkedAvailable(
            mark.slotId(),
            mark.participantId(),
            mark.participantType()
        );
        return effects().persist(event).thenReply((state) -> Done.done());
    }

    public Effect<Done> book(ParticipantSlotEntity.Commands.Book book) {
        var event = new ParticipantSlotEntity.Event.Booked(
            book.slotId(),
            book.participantId(),
            book.participantType(),
            book.bookingId()
        );
        return effects().persist(event).thenReply((state) -> Done.done());
    }

    public Effect<Done> cancel(ParticipantSlotEntity.Commands.Cancel cancel) {
        var event = new ParticipantSlotEntity.Event.Canceled(
            cancel.slotId(),
            cancel.participantId(),
            cancel.participantType(),
            cancel.bookingId()
        );
        return effects().persist(event).thenReply((state) -> Done.done());
    }

    record State(
        String slotId, String participantId, ParticipantType participantType, String status
    ) {
    }

    public sealed interface Commands {
        record MarkAvailable(String slotId, String participantId, ParticipantType participantType) implements Commands { }

        record UnmarkAvailable(String slotId, String participantId, ParticipantType participantType) implements Commands { }

        record Book(String slotId, String participantId, ParticipantType participantType, String bookingId) implements Commands { }

        record Cancel(String slotId, String participantId, ParticipantType participantType, String bookingId) implements Commands { }
    }

    public sealed interface Event {
        @TypeName("marked-available")
        record MarkedAvailable(String slotId, String participantId, ParticipantType participantType) implements Event {
        }

        @TypeName("unmarked-available")
        record UnmarkedAvailable(
            String slotId, String participantId, ParticipantType participantType
        ) implements Event {
        }

        @TypeName("participant-booked")
        record Booked(
            String slotId, String participantId, ParticipantType participantType, String bookingId
        ) implements Event {
        }

        @TypeName("participant-canceled")
        record Canceled(
            String slotId, String participantId, ParticipantType participantType, String bookingId
        ) implements Event {
        }
    }

    @Override
    public ParticipantSlotEntity.State applyEvent(ParticipantSlotEntity.Event event) {
        return switch (event) {
            case ParticipantSlotEntity.Event.MarkedAvailable marked -> new ParticipantSlotEntity.State(
                marked.slotId(),
                marked.participantId(),
                marked.participantType(),
                "available"
            );
            case ParticipantSlotEntity.Event.UnmarkedAvailable unmarked -> new ParticipantSlotEntity.State(
                unmarked.slotId(),
                unmarked.participantId(),
                unmarked.participantType(),
                "UNAVAILABLE"
            );
            case ParticipantSlotEntity.Event.Booked booked -> new ParticipantSlotEntity.State(
                booked.slotId(),
                booked.participantId(),
                booked.participantType(),
                "booked"
            );
            case ParticipantSlotEntity.Event.Canceled canceled -> new ParticipantSlotEntity.State(
                canceled.slotId(),
                canceled.participantId(),
                canceled.participantType(),
                "CANCELLED"
            );
        };
    }
}
