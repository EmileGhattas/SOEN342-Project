# SOEN342-Project

Appointment Tracking System — Java CLI.

## Team

> **Emile Ghattas** 		(id: 40282552)			*aka. @EmileGhattas*
>
> **Manlai Munkhbold**	(id: 40282487)			*aka. @ManlaiM*
>
> **Nada Ouriachi**			(id: 40317294)			*aka. @nadaouriachi*

## Run

Requires JDK 11+. From the repository root:

```
javac -cp "lib/*" -sourcepath src -d out src/App.java
```

Then on macOS / Linux:
```
java -cp "out:lib/*" App
```

Or on Windows:
```
java -cp "out;lib/*" App
```

IntelliJ: add `lib/` as a library (Project Structure → Libraries), then run `src/App.java`.

Everything is saved in `data/appointment_tracking.db`. The first run fills it with sample data: log in with staff id **1**, password **admin**. Delete file to start over.

Sample patients (find by id or insurance number): Ana Lopez `100` / RAMQ `LOPA90050412` (with confirmed Cardiology referral), Ben Roy `101` / `SUN-4471`, Chloe Martin `102` (no insurance).
