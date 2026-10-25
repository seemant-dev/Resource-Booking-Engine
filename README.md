# Resource-Booking-Engine

A Resource Booking Engine — a system where users reserve time-slotted resources (think: meeting rooms, equipment, or service appointments)

## Local configuration

The backend reads `DB_PASSWORD` and `JWT_SECRET` from its environment. Copy the [backend environment example](SlotEngine-Server/.env.example) to `SlotEngine-Server/.env` and fill in both values. The JWT secret should be a unique random value of at least 32 bytes. Optional `DB_URL` and `DB_USERNAME` override the local defaults. Spring Boot does not load `.env` files automatically, so also set the same values in the IntelliJ `ServerApplication` run configuration or the deployment environment.

To start local PostgreSQL with that password, run `docker compose --env-file SlotEngine-Server/.env up -d` from the repository root. If the database volume already exists, use its existing password; changing the Compose variable does not reset the database user's password.

Admin slot dates and times use the backend's `app.timezone=Asia/Kolkata`. Slot timestamps are stored as instants, and the slot list uses the same zone to interpret its `date` filter. Slots previously generated through the API with UTC interpretation retain their old timestamps; review and recreate those slots if they were intended as India local times. The performance seed uses explicit UTC timestamps and should remain unchanged for benchmark comparisons.
