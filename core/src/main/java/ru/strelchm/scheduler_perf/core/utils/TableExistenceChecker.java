package ru.strelchm.scheduler_perf.core.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class TableExistenceChecker {
    public static boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_name = ?"
        )) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}
