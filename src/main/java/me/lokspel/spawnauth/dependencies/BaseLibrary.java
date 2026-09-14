package me.lokspel.spawnauth.dependencies;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public enum BaseLibrary {
    MYSQL(
            "com.mysql",
            "mysql-connector-j",
            "9.4.0"
    ),
    SQLITE(
            "org.xerial",
            "sqlite-jdbc",
            "3.45.1.0"
    ),
    SLF4J(
            "org.slf4j",
            "slf4j-api",
            "2.0.13"
    ),
    SLF4J_JDK14(
            "org.slf4j",
            "slf4j-jdk14",
            "2.0.13"
    );

    private final String fileName;
    private final String mavenUrl;

    BaseLibrary(String groupId, String artifactId, String version) {
        String mavenPath = String.format("%s/%s/%s/%s-%s.jar",
                groupId.replace(".", "/"),
                artifactId,
                version,
                artifactId,
                version
        );

        this.fileName = artifactId + "-" + version + ".jar";
        this.mavenUrl = "https://repo1.maven.org/maven2/" + mavenPath;
    }

    public String getFileName() {
        return fileName;
    }

    public URL getClassLoaderURL(Path libsDir) throws IOException {
        Path jar = libsDir.resolve(fileName);
        if (!Files.exists(jar)) {
            try (InputStream in = URI.create(mavenUrl).toURL().openStream()) {
                Files.createDirectories(libsDir);
                Files.copy(in, jar, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return jar.toUri().toURL();
    }
}