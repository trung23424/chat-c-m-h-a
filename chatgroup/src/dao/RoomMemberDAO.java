package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomMemberDAO {

    public static List<String> getMembersByRoomName(String roomName) {
        List<String> members = new ArrayList<>();

        String sql = """
            SELECT u.username
            FROM users u
            JOIN room_members rm ON u.id = rm.user_id
            JOIN chat_rooms r ON rm.room_id = r.id
            WHERE r.name = ?
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách thành viên: " + e.getMessage());
        }

        return members;
    }
}
