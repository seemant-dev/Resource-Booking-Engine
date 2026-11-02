package com.resourcebooking.server.repository;

import com.resourcebooking.server.service.SlotWindow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SlotBatchRepository {

    private static final int INSERT_BATCH_SIZE = 50;

    private final JdbcTemplate jdbcTemplate;

    public void insertAll(long resourceId, List<SlotWindow> slots) {
        jdbcTemplate.batchUpdate(
                """
                insert into slots (resource_id, slot_start, slot_end)
                values (?, ?, ?)
                """,
                slots,
                INSERT_BATCH_SIZE,
                (statement, slot) -> {
                    statement.setLong(1, resourceId);
                    statement.setTimestamp(2, Timestamp.from(slot.start()));
                    statement.setTimestamp(3, Timestamp.from(slot.end()));
                }
        );
    }
}
