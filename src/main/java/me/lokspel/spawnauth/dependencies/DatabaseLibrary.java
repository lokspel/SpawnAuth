package me.lokspel.spawnauth.dependencies;

import me.lokspel.spawnauth.config.section.DatabaseSection;

import java.net.URL;
import java.nio.file.Path;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Locale;

public enum DatabaseLibrary {
    MYSQL(
            new BaseLibrary[]{BaseLibrary.MYSQL},
            "com.mysql.cj.jdbc.NonRegisteringDriver",
            section -> "jdbc:mysql://" + section.getMySqlHost() + ":" + section.getMySqlPort()
                    + "/" + section.getMySqlDatabase() + "?" + section.getMySqlUrlParameters()
    ),
    SQLITE(
            new BaseLibrary[]{BaseLibrary.SQLITE, BaseLibrary.SLF4J, BaseLibrary.SLF4J_JDK14},
            "org.sqlite.JDBC",
            section -> "jdbc:sqlite:" + section.getDatabaseFile().getAbsolutePath()
    );

    private final BaseLibrary[] baseLibraries;
    private final String driverClassName;
    private final JdbcStringGetter jdbcGetter;
    private final IsolatedDriver driver = new IsolatedDriver(
            "jdbc:spawnauth_" + name().toLowerCase(Locale.ROOT) + ":"
    );

    DatabaseLibrary(BaseLibrary[] baseLibraries, String driverClassName, JdbcStringGetter jdbcGetter) {
        this.baseLibraries = baseLibraries;
        this.driverClassName = driverClassName;
        this.jdbcGetter = jdbcGetter;
    }

    public void ensureLoaded(Path libsDir) throws Exception {
        if (driver.isLoaded()) {
            return;
        }

        URL[] urls = new URL[baseLibraries.length];
        for (int i = 0; i < baseLibraries.length; i++) {
            urls[i] = baseLibraries[i].getClassLoaderURL(libsDir);
        }

        IsolatedClassLoader classLoader = new IsolatedClassLoader(urls);
        Class<?> driverType = classLoader.loadClass(driverClassName);
        driver.setOriginal((Driver) driverType.getConstructor().newInstance());
        DriverManager.registerDriver(driver);
    }

    public String getJdbcUrl(DatabaseSection section) {
        return driver.getInitializer() + jdbcGetter.get(section);
    }

    public String getFileName() {
        return baseLibraries[0].getFileName();
    }

    private interface JdbcStringGetter {
        String get(DatabaseSection section);
    }
}