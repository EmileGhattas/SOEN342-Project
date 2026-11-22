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

Create a staff account in **Setup**, log in, import `data/availabilities.csv`, then add a patient and service to book an appointment. Records stay in memory; CSV export saves availability only.
