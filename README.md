# SOEN342-Project
### Team Members:
> **Emile Ghattas** 		(id: 40282552)			*aka. @EmileGhattas*
>
> **Manlai Munkhbold**	(id: 40282487)			*aka. @ManlaiM*
> 
> **Nada Ouriachi**			(id: 40317294)			*aka. @nadaouriachi*

## Running the CLI

Use JDK 11 or newer. In IntelliJ, run `src/App.java` with the repository root as the working directory.

From a macOS/Linux terminal at the repository root:

```sh
SOEN342_BUILD_DIR="$(mktemp -d)"
javac -encoding UTF-8 --release 11 -d "$SOEN342_BUILD_DIR" $(find src -name '*.java')
java -cp "$SOEN342_BUILD_DIR" App
```

The application starts empty. Records entered in Setup and appointments created during a run stay in memory until the application closes.

## Trying a booking

1. Choose **3. Setup booking records**, then **1. Add administrative staff**. Enter an unused positive staff ID, name and password. The first administrative account can be created before login.
2. Return to the main menu with **0**, then choose **4. Staff login** using that ID and password. Further staff accounts and booking-record setup require a staff session.
3. Choose **1. Import availabilities from CSV** and enter `data/availabilities.csv` (or another compatible file). This loads professionals, specialties and their slots.
4. In **Setup**, register a patient and add a medical service. Insurance is optional; uninsured patients can be found by their patient ID. For a simple first booking, use a 30-minute service without a referral requirement.
5. Choose **6. Book appointment**, find the patient by ID or insurance number, and select a service. Search by professional name/licence, specialty and an inclusive date range, select a slot, and confirm the request. Blank professional/specialty fields search all professionals/specialties.
6. The sample file has an available slot for `LIC1001` on `2026-12-01` from `09:00` to `09:30`. A successful request displays `REQUESTED`; that slot is then excluded from availability searches.
7. Use **2. Export availabilities to CSV** to write all slots and their current `AVAILABLE`/`BOOKED` statuses to a file you choose.

For a service requiring a referral, use **Setup > Record an external referral** to enter the received referral's patient, requested specialty, referring physician, reason, priority, type and confirmation status. Booking accepts a confirmed referral matching the patient and the selected professional's specialty. An unconfirmed referral can be recorded but cannot satisfy the booking requirement.

The current CLI covers staff-assisted bookings in person or over the phone. Patient self-service, complete access control, and recurring availability are separate work. The setup menu is for this in-memory prototype; CSV export saves availability and slot statuses, not patient or appointment records. The sample CSV is not modified when booking.

## Running the tests

The tests use plain Java and do not require a test framework. From the repository root on macOS/Linux:

```sh
SOEN342_TEST_DIR="$(mktemp -d)"
javac -encoding UTF-8 --release 11 -d "$SOEN342_TEST_DIR" $(find src tests -name '*.java')
for SOEN342_TEST_CLASS in BookingRulesTest BookingControllerTest BookingSetupTest BookingCliTest; do
    java -cp "$SOEN342_TEST_DIR" "$SOEN342_TEST_CLASS"
done
```

The tests cover booking state changes, double-booking rejection, referral checks, search filters, setup validation, CLI cancellation/error handling, and CSV import/export integration. Compile output goes into the temporary directory, keeping generated `.class` files out of the repository.
