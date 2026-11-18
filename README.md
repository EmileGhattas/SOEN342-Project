# SOEN342-Project

Appointment Tracking System — Java CLI.

## Team

> **Emile Ghattas** 		(id: 40282552)			*aka. @EmileGhattas*
>
> **Manlai Munkhbold**	(id: 40282487)			*aka. @ManlaiM*
>
> **Nada Ouriachi**			(id: 40317294)			*aka. @nadaouriachi*

## Run

```sh
SOEN342_TEST_DIR="$(mktemp -d)"
javac -encoding UTF-8 --release 11 -d "$SOEN342_TEST_DIR" $(find src tests -name '*.java')
for SOEN342_TEST_CLASS in BookingRulesTest BookingControllerTest BookingSetupTest BookingCliTest MakeAvailabilitiestest; do
    java -cp "$SOEN342_TEST_DIR" "$SOEN342_TEST_CLASS"
done
```
Use JDK 11+. In IntelliJ, run `src/App.java` with the repository root as the working directory.

Create a staff account in **Setup**, log in, import `data/availabilities.csv`, then add a patient and service to book an appointment. Records stay in memory; CSV export saves availability only.
