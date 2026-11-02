CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM slots first_slot
        JOIN slots second_slot
          ON first_slot.resource_id = second_slot.resource_id
         AND first_slot.id < second_slot.id
         AND tstzrange(first_slot.slot_start, first_slot.slot_end, '[)')
             && tstzrange(second_slot.slot_start, second_slot.slot_end, '[)')
    ) THEN
        RAISE EXCEPTION 'Cannot add overlap constraint: existing slots overlap';
    END IF;
END
$$;

ALTER TABLE slots
    ADD CONSTRAINT ex_slots_resource_time_overlap
    EXCLUDE USING gist (
        resource_id WITH =,
        tstzrange(slot_start, slot_end, '[)') WITH &&
    );
