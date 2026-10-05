# Connection Pool Config Companion

Warning on a HikariCP connection-pool setting
(`spring.datasource.hikari.*` or bare `hikari.*`) set outside its own
documented safe range in a `.properties`/`.yml`/`.yaml` config file:
`connectionTimeout` below 250ms, `maxLifetime` below 30000ms,
`idleTimeout` below 10000ms, or `idleTimeout` set greater than or
equal to `maxLifetime` (HikariCP silently ignores `idleTimeout` in
that case — a real, documented gotcha, not an invented rule).

## Why it exists

These four thresholds are documented in HikariCP's own README and
wiki, but nothing enforces them at config-write time — a typo or a
copy-pasted value from an unrelated setting (seconds instead of
milliseconds is a real, common mistake) silently produces a pool that
either fails fast on every borrow or never actually rotates
connections. Nothing in the IDE flags either case today.

## Why built this way

- **100% static text analysis** — a plain-text line scanner, not a real
  properties/YAML parser, so it works whether the real HikariCP jar is
  on the classpath or not.
- **Only the four thresholds HikariCP itself documents** — this plugin
  never invents a "recommended" number for a setting (like
  `maximumPoolSize`) that genuinely depends on real workload sizing.

## v0.1 scope — stated honestly, not exhaustively

Only single-line `key=value`/`key: value` pairs are scanned. The
`idleTimeout` vs `maxLifetime` cross-check only fires when both appear
in the same file.

## Usage

Open any `.properties`/`.yml`/`.yaml` file with a `hikari.*` setting.
A value outside its documented safe range shows a warning.

## Support

- **Bugs and feature requests:** [GitHub Issues](https://github.com/GapHunterLabs/connection-pool-config-companion/issues)
- **Questions, or custom rules for a team's codebase:** **gaphunterlabs@gmail.com**
- **Security vulnerabilities:** report privately as described in [SECURITY.md](SECURITY.md), not in a public issue.
- **Privacy and network behavior:** [PRIVACY.md](PRIVACY.md)

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
