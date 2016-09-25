package me.geso.konfig;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportableConstructorTest {
    @Test
    public void testFile() {
        ImportableConstructor importableConstructor = new ImportableConstructor();
        Yaml yaml = new Yaml(importableConstructor);
        importableConstructor.setYaml(yaml);
        Object load = yaml.load("---\nfoo: !file src/test/resources/konfig-local.yml");
        assertThat(load instanceof Map);
        Object foo = ((Map) load).get("foo");
        assertThat(foo instanceof Map);
        Object env = ((Map) foo).get("env");
        assertThat(env)
                .isEqualTo("local");
    }

    @Test
    public void testResource() {
        ImportableConstructor importableConstructor = new ImportableConstructor();
        Yaml yaml = new Yaml(importableConstructor);
        importableConstructor.setYaml(yaml);
        Object load = yaml.load("---\nfoo: !resource konfig-local.yml");
        assertThat(load instanceof Map);
        Object foo = ((Map) load).get("foo");
        assertThat(foo instanceof Map);
        Object env = ((Map) foo).get("env");
        assertThat(env)
                .isEqualTo("local");
    }

}