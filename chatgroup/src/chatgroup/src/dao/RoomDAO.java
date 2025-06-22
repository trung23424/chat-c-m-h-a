package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public static boolean createRoom(String roomName) {
        String sql = "INSERT INTO chat_rooms (name) VALUES (?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi tạo phòng: " + e.getMessage());
            return false;
        }
    }

    public static void joinRoom(String username, String roomName) {
        String sql = """
            INSERT INTO room_members (user_id, room_id)
            SELECT u.id, r.id FROM users u, chat_rooms r
            WHERE u.username = ? AND r.name = ?
              AND NOT EXISTS (
                  SELECT 1 FROM room_members rm
                  WHERE rm.user_id = u.id AND rm.room_id = r.id
              )
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, roomName);
            int rows = stmt.executeUpdate();

            System.out.println("✅ joinRoom(): Ghi DB thành công? " + (rows > 0));

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi ghi room_members: " + e.getMessage());
        }
    }


    public static boolean leaveRoom(String username, String roomName) {
        String sql = """
            DELETE rm FROM room_members rm
            JOIN users u ON rm.user_id = u.id
            JOIN chat_rooms r ON rm.room_id = r.id
            WHERE u.username = ? AND r.name = ?
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, roomName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi rời phòng: " + e.getMessage());
            return false;
        }
    }
    public static List<String> getAllRooms() {
        List<String> rooms = new ArrayList<>();
        String sql = "SELECT name FROM chat_rooms";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách phòng: " + e.getMessage());
        }

        return rooms;
    }
    public static List<String> getJoinedRooms(String username) {
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
            System.err.println("❌ Lỗi lấy danh sách phòng đã tham gia: " + e.getMessage());
        }

        return rooms;
    }
    
    public static List<String> getMembersInRoom(String roomName) {
        List<String> members = new ArrayList<>();
        String sql = "SELECT u.username " +
                     "FROM users u " +
                     "JOIN room_members rm ON u.id = rm.user_id " +
                     "JOIN chat_rooms r ON rm.room_id = r.id " +
                     "WHERE r.name = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy thành viên trong phòng: " + e.getMessage());
        }

        return members;
    }


}