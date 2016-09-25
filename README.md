# konfig

Tiny config loader for Java

## Usage

### Load from resources

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

###

## Overwrite configuration at runtime

You can overwrite configurations at runtime by environment variable and
system properties.

For example, declare the following configuration file model.

```java
@Data
public static class ConfigFile {
    private String env;
    private DataSourceConfig dataSource;

    @Data
    public static class DataSourceConfig {
        private String uri;
    }
}
```

### System properties

You can overwrite dataSource.uri via `-DdataSource.uri`.

### Environment variable

You can write overwrite configuration by environment variable.

If there's a configuration file like this, you can overwrite dataSource.uri by
`ENV['DATA_SOURCE_URI']`.

If you want to check the real environment variable name, you can enable the TRACE level
logging.

## Include YAML

You can include YAML from YAML. Konfig defines custom tag for including.

```yaml
---
mybatis: !file /etc/mybatis.yml
freemarker: !resource config/freemarker-devel.yml
```

## Supported Java version

Java 8+

## LICENSE

    The MIT License (MIT)
    Copyright © 2016 Tokuhiro Matsuno, http://64p.org/ <tokuhirom@gmail.com>
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the “Software”), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.

