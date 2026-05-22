# RFID-Reader Implementation Plan

## Goal

Build a field scanning app from the Bluebird RFID sample. The user walks through a target area, scans all RFID tags in range, reads stored tag data, and saves the collected result to a file.

## Data To Collect

The app should collect business data from tag memory, not scan diagnostics.

| Field | Source | Notes |
| --- | --- | --- |
| EPC | Inventory result | Used as the unique key for each discovered tag. |
| TID | TID memory bank | Stored with the export for tag identity/chip traceability. |
| USER | USER memory bank | Main business data. The exact structure is not known yet, so the app stores raw hex data first. |
| Status | App processing state | `DISCOVERED`, `READING_TID`, `READING_USER`, `DONE`, or `FAILED`. |
| Error | App/SDK failure result | Saved only when reading fails. |

RSSI, read count, first seen, and last seen are diagnostic data and should not be primary user-facing fields.

## Recommended UX

The main screen should be a single `SCAN` workflow instead of a developer-style menu.

Main actions:

- `START`: begin inventory and collect unique EPC values.
- `STOP`: stop inventory.
- `READ DATA`: stop inventory if needed, then read TID and USER memory for each EPC.
- `SAVE CSV`: export EPC, TID, USER, status, and error.
- `CLEAR`: reset the current session.

Main screen metrics:

- Found: number of unique EPC tags.
- Read: number of tags with TID and USER read successfully.
- Failed: number of tags that failed memory read.
- File path after export.

## Technical Flow

1. Use `RF_PerformInventory(true, false, false)` to scan tags.
2. Parse each inventory callback and extract the EPC before any `;` suffix.
3. Store tags in a `LinkedHashMap<String, ScanTag>` so duplicate EPCs are merged.
4. When the user taps `READ DATA`, stop inventory.
5. Process tags one by one:
   - Apply RF selection for the EPC.
   - Read TID memory.
   - Read USER memory.
   - Save raw hex results.
6. Export to CSV in the public Documents directory under `Documents/RFID-Reader/`.

## Initial Memory Read Defaults

Because the USER data structure is not confirmed, the scan screen should allow the operator to change the USER read range and access password. The initial defaults are:

| Memory | Start Word | Word Length | Access Password |
| --- | --- | --- | --- |
| TID | 0 | 6 | `00000000` |
| USER | 0 | 16 | `00000000` |

These should become settings later if real tags require a different range or access password.

## Architecture

Add a dedicated `ScanFragment` instead of directly exposing the sample `InventoryFragment`.

Reasons:

- `InventoryFragment` contains many developer/test controls that are risky for field users.
- `ScanFragment` can keep one clear business workflow.
- Existing sample fragments remain available through Admin menus for debugging.

## Export Format

CSV columns:

```csv
epc,tid,user,status,error
```

Example:

```csv
300833B2DDD9014000000001,E2801191A5030067,414243313233,OK,
300833B2DDD9014000000002,,,FAILED,USER_READ_FAILED:-1
```

## Future Improvements

- Add configurable USER start/length.
- Decode USER data when the real business format is confirmed.
- Add saved scan history.
- Add share/export intent.
- Add admin lock or password before RF settings.
