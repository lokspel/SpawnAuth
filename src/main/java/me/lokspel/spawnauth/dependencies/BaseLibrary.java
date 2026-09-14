package me.lokspel.spawnauth.dependencies;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    private final Path filenamePath;
    private final URL mavenRepoURL;

    BaseLibrary(String groupId, String artifactId, String version) {
        String mavenPath = String.format("%s/%s/%s/%s-%s.jar",
                groupId.replace(".", "/"),
                artifactId,
                version,
                artifactId,
                version
        );

        try {
            this.filenamePath = Path.of("libraries/" + mavenPath);
            this.mavenRepoURL = new URL("https://repo1.maven.org/maven2/" + mavenPath);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public URL getClassLoaderURL() throws MalformedURLException {
        if (!Files.exists(this.filenamePath)) {
            try {
                try (InputStream in = this.mavenRepoURL.openStream()) {
                    Files.createDirectories(this.filenamePath.getParent());
                    Files.copy(in, Files.createFile(this.filenamePath), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return this.filenamePath.toUri().toURL();
    }
}