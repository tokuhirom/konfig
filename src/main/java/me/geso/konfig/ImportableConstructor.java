package me.geso.konfig;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImportableConstructor extends SafeConstructor {
    private Yaml yaml;

    public ImportableConstructor() {
        this.yamlConstructors.put(new Tag("!file"), new FileConstruct());
        this.yamlConstructors.put(new Tag("!resource"), new ResourceConstruct());
    }

    public void setYaml(Yaml yaml) {
        this.yaml = yaml;
    }

    public Yaml getYaml() {
        if (this.yaml == null) {
            throw new IllegalStateException("You must set Yaml object to ImportableConstructor.");
        }
        return this.yaml;
    }

    private class FileConstruct extends AbstractConstruct {
        @Override
        public Object construct(Node nnode) {
            org.yaml.snakeyaml.nodes.ScalarNode snode = (org.yaml.snakeyaml.nodes.ScalarNode) nnode;
            String fileName = snode.getValue();
            try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(fileName))) {
                return getYaml().load(bufferedReader);
            } catch (IOException e) {
                throw new YamlImportFailedException(fileName, snode.getTag(), e);
            }
        }
    }

    private class ResourceConstruct extends AbstractConstruct {
        @Override
        public Object construct(Node nnode) {
            org.yaml.snakeyaml.nodes.ScalarNode snode = (org.yaml.snakeyaml.nodes.ScalarNode) nnode;
            String resourceName = snode.getValue();
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                return getYaml().load(resourceAsStream);
            } catch (IOException e) {
                throw new YamlImportFailedException(resourceName, snode.getTag(), e);
            }
        }
    }

    public static class YamlImportFailedException extends RuntimeException {
        public YamlImportFailedException(String fileName, Tag tag, IOException cause) {
            super("Cannot load " + tag.getValue() + " from " + fileName + " : " + cause.getMessage(), cause);
        }
    }
}
