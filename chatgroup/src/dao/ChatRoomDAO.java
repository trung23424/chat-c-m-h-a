package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomDAO {

    public static List<String> getRoomsByUsername(String username) {
        List<String> rooms = new ArrayList<>();
        String sql = """
            SELECT cr.name
            FROM chat_rooms cr
            JOIN room_members rm ON cr.id = rm.room_id
            JOIN users u ON rm.user_id = u.id
            WHERE u.username = ?
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rooms.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy phòng chat: " + e.getMessage());
        }

        return rooms;
    }
}