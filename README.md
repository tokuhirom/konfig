# konfig

Tiny config loader for Java

## Usage

### Load by `$profile`

Konfig loads `konfig-${profile}.yml` from resource by default.
Default `$profile` is `local`. You can overwrite `$profile` by `-Dkonfig.profile`
system property.

    # load konfig-release.yml from resources
    java -Dkonfig.profile=release

    # load konfig-staging.yml from resources
    java -Dkonfig.profile=staging

### Load from specified file

If there's a configuration file specified by `konfig.file` system property,
konfig loads it.

    java -Dkonfig.file=path/to/config.yml

## Property resolution

## Supported Java version

Java 8+

## LICENSE

MIT
