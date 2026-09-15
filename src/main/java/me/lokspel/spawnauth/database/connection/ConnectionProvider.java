package me.lokspel.spawnauth.database.connection;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;

    default void close() {
    }
}