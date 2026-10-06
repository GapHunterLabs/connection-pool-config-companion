<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Connection Pool Config Companion Changelog

## [Unreleased]

### Added

- A description page for the inspection in **Settings | Editor |
  Inspections**, which showed "Under construction".

### Changed

- The rating prompt's local counter keeps one-way fingerprints of findings
  instead of their file paths, and deletes the list that earlier versions
  kept.
- `PRIVACY.md` describes the values the plugin keeps in the IDE's local
  settings.

## [0.1.1]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.1.0]

### Added

- Warning on a HikariCP setting outside its own documented safe range:
  `connectionTimeout` &lt; 250ms, `maxLifetime` &lt; 30000ms,
  `idleTimeout` &lt; 10000ms, or `idleTimeout` &gt;= `maxLifetime`.
- 100% static text analysis, no network calls, no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/connection-pool-config-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/connection-pool-config-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/connection-pool-config-companion/commits/0.1.0
