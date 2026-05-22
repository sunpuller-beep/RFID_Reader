# Menu Function Summary

## Current Screen Policy

The app is simplified for users who only need RFID reading and do not use barcode features.

## Main User Menu

| Menu | Summary | Purpose |
| --- | --- | --- |
| CONNECTIVITY | Connects or disconnects the RFID reader and checks the current connection state. | Required before reading tags. |
| SCAN | Scans unique EPC tags in range, then reads TID and USER memory and exports collected values to CSV. | Primary screen for normal users. |
| BATTERY | Shows battery level, charging state, and smart battery information. | Operational status check. |
| INFORMATION | Shows Android build, RFID library/module version, firmware version, serial number, app version, bootloader version, and sled type. | Device and app information check. |

## Admin Menu

Admin items are available from the left Drawer menu instead of the main home screen.

| Menu | Summary | Purpose |
| --- | --- | --- |
| Admin - RF Config | Configures RFID radio behavior, including region, RF power, duty, access timeout, dwell time, singulation, RF mode, LBT, ISO code, Gen2x, RSSI, channels, antenna, and tag focus. | Initial setup or tuning by an administrator. |
| Admin - RF Selection | Configures tag selection criteria using memory type, action, mask, start position, and mask length. Can set, get, or remove selection. | Administrator-only filtering for reading a specific tag group. |
| Admin - SD Function | Configures sled/device behavior such as trigger mode, sleep, buzzer, mode key, trigger key, tag buzzer, LED, charging/battery checks, serial number, firmware version, firmware update, and default reset. | Device management and maintenance. |
| Admin - Inventory | Advanced RFID inventory screen with EPC list, count, speed, session, SL flag, turbo, RSSI, filter, sound, locating, phase/frequency inventory, custom inventory, RSSI limit, and encoding inventory. | Advanced testing, diagnostics, or special operation. |

## Removed From Navigation

| Menu | Reason |
| --- | --- |
| BARCODE(BC) | Barcode is not used. |
| BARCODE(SB) | Barcode is not used. |
| RF ACCESS | Provides direct tag memory operations such as read, write, lock, kill, and password operations. This is risky for normal users. |
| Test | Developer-only channel test screen. |

## Practical Interpretation

For normal RFID work, users should use CONNECTIVITY first and then SCAN. BATTERY and INFORMATION remain available for status checks. RF CONFIG, RF SELECTION, SD FUNCTION, and INVENTORY are kept available only through the admin Drawer because they can change device behavior or expose advanced testing controls.
