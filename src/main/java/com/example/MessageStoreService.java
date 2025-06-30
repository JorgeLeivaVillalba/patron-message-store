package com.example;

import javax.sql.DataSource;

import org.apache.camel.Body;
import org.apache.camel.Headers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

public class MessageStoreService {

    private final DataSource dataSource;

    public MessageStoreService(DataSource ds) {
        this.dataSource = ds;
    }

    public void createTable() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS message_store (
                    id VARCHAR(100),
                    timestamp VARCHAR(30),
                    content VARCHAR(255)
                )
            """);
        }
    }

    public void storeMessage(@Body String body, @Headers Map<String, Object> headers) {
    // Ahora 'headers' nunca será null
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO message_store (id, timestamp, content) VALUES (?, ?, ?)")) {
        stmt.setString(1, headers.get("messageId").toString());
        stmt.setString(2, headers.get("timestamp").toString());
        stmt.setString(3, body);
        stmt.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
