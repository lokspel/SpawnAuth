package me.lokspel.spawnauth.database.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider extends AutoCloseable {

    Connection getConnection() throws SQLException;

    @Override
    void close();
}