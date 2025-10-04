package io.example.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import io.example.application.ParticipantSlotEntity.Event.Booked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ComponentId("view-participant-slots")
public class ParticipantSlotsView extends View {

    private static Logger logger = LoggerFactory.getLogger(ParticipantSlotsView.class);

    @Consume.FromEventSourcedEntity(ParticipantSlotEntity.class)
    public static class ParticipantSlotsViewUpdater extends TableUpdater<SlotRow> {

        public Effect<SlotRow> onEvent(ParticipantSlotEntity.Event event) {
            this.updateContext().metadata().forEach(metadata -> logger.info("{}: {}", metadata.getKey(), metadata.getValue()));

            return switch (event) {
                case Booked booked -> effects().updateRow(new SlotRow(
                    booked.slotId(),
                    booked.participantId(),
                    booked.participantType().toString(),
                    booked.bookingId(),
                    "booked"
                ));
                case ParticipantSlotEntity.Event.MarkedAvailable markedAvailable -> effects().updateRow(new SlotRow(
                    markedAvailable.slotId(),
                    markedAvailable.participantId(),
                    markedAvailable.participantType().toString(),
                    "",
                    "available"
                ));
                case ParticipantSlotEntity.Event.Canceled canceled -> effects().deleteRow();
                case ParticipantSlotEntity.Event.UnmarkedAvailable unmarkedAvailable -> effects().deleteRow();
            };
        }
    }

    public record SlotRow(
        String slotId,
        String participantId,
        String participantType,
        String bookingId,
        String status
    ) {
    }

    public record ParticipantStatusInput(String participantId, String status) {
    }

    public record SlotList(List<SlotRow> slots) {
    }

    @Query("SELECT * AS slots FROM participant_slots WHERE participantId = :participantId AND status = :status")
    public QueryEffect<SlotList> getSlotsByParticipantAndStatus(ParticipantStatusInput input) {
        return queryResult();
    }
}
