package com.thales.server.database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Database {
    protected final String url;

    protected Database(String url) {
        this.url = url;
    }

    public abstract Connection getConnection() throws SQLException;
}
