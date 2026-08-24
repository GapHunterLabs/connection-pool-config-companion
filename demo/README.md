# Demo data for screenshots

`application.properties` — `connection-timeout=100` is below the
250ms documented minimum (flagged); `max-lifetime`/`idle-timeout` are
healthy (not flagged).

## How to get the screenshot

1. `./gradlew runIde` from `connection-pool-config-companion`, open
   this `demo/` folder as the project.
2. Full Screen, open `application.properties` — a warning should
   appear on the `connection-timeout` line only.
3. Screenshot with all 3 lines visible, save into
   `connection-pool-config-companion/docs/screenshots/`. Close the
   sandbox.
