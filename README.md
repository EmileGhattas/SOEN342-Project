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

Everything is saved in `data/appointment_tracking.db`. The first run fills it with sample data. Delete file to start over.

Three kinds of users can interact with the system: Administrative Staff, Healthcare Professionals and Patients.

### Sample logins

| Role | Name | Id | Password | Notes |
|---|---|---|---|---|
| Administrative staff | Sam Desk | `1` | `admin` | |
| Healthcare professional | Alice Tremblay | `2` | `changeit` | Cardiology, licence `LIC1001` |
| Patient | Ana Lopez | `100` | `pw` | RAMQ `LOPA90050412`, confirmed Cardiology referral |


Professionals created by the CSV import get the temporary password `changeit`. 
