package dao;

import model.Message;
import view.components.MessageBubble;
import view.ChatRoomView;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;


public class MessageDAO {
	private String username;
	private JPanel chatArea;

    public static List<Message> getMessagesByRoomName(String roomName) {
        List<Message> messages = new ArrayList<>();

        String sql = """
            SELECT u.username AS sender, m.content, m.sent_at
            FROM messages m
            JOIN users u ON m.sender_id = u.id
            JOIN chat_rooms r ON m.room_id = r.id
            WHERE r.name = ?
            ORDER BY m.sent_at ASC
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(new Message(
                    rs.getString("sender"),
                    rs.getString("content"),
                    rs.getTimestamp("sent_at").toLocalDateTime()
                ));
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy tin nhắn: " + e.getMessage());
        }

        return messages;
    }
    public static boolean sendMessage(String senderName, String roomName, String content) {
        String sql = """
            INSERT INTO messages (room_id, sender_id, content)
            VALUES (
                (SELECT id FROM chat_rooms WHERE name = ?),
                (SELECT id FROM users WHERE username = ?),
                ?
            )
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomName);
            stmt.setString(2, senderName);
            stmt.setString(3, content);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
//            System.err.println("❌ Gửi tin thất bại: " + e.getMessage());
            return false;
        }
    }
    
    private void loadRoomData(String roomName, String username) {
        chatArea.removeAll();
        List<Message> messages = MessageDAO.getMessagesByRoomName(roomName);
        for (Message msg : messages) {
            boolean isMine = msg.getSenderName().equals(username);
            MessageBubble bubble = new MessageBubble(msg.getContent(), isMine);
            chatArea.add(bubble);
            chatArea.add(Box.createVerticalStrut(10));
        }
        chatArea.revalidate();
        chatArea.repaint();
    }
    public static void saveMessage(String roomName, String senderName, String content) {
        String getRoomIdSQL = "SELECT id FROM chat_rooms WHERE name = ?";
        String getUserIdSQL = "SELECT id FROM users WHERE username = ?";
        String insertSQL = "INSERT INTO messages (room_id, sender_id, content) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.connect()) {
            int roomId = -1;
            int senderId = -1;

            // 🔸 Lấy room_id
            try (PreparedStatement stmt = conn.prepareStatement(getRoomIdSQL)) {
                stmt.setString(1, roomName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    roomId = rs.getInt("id");
                }
            }

            // 🔸 Lấy sender_id
            try (PreparedStatement stmt = conn.prepareStatement(getUserIdSQL)) {
                stmt.setString(1, senderName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    senderId = rs.getInt("id");
                }
            }

            // 🔸 Kiểm tra trước khi lưu
            if (roomId != -1 && senderId != -1) {
                try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                    stmt.setInt(1, roomId);
                    stmt.setInt(2, senderId);
                    stmt.setString(3, content);
                    stmt.executeUpdate();
                }
            } else {
                System.err.println("⚠ Không tìm thấy room_id hoặc sender_id để lưu tin nhắn!");
            }

        } catch (SQLException e) {
            System.err.println("❌ Gửi tin thất bại: " + e.getMessage());
        }
    }






}