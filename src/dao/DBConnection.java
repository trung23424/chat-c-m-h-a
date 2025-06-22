package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/chatgroup?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Kết nối và thông báo kết quả
    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Kết nối MySQL thành công!");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Kết nối MySQL thất bại: " + e.getMessage());
            return null;
        }
    }

    // Kiểm tra nhanh kết nối
    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
