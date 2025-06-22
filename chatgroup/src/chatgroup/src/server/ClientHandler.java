package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import dao.MessageDAO;
import dao.RoomDAO;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String currentRoom = "phòng mặc định";

    private static final List<ClientHandler> clients = new ArrayList<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        clients.add(this);
    }

    @Override
    public void run() {
        try {
        	in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true);

            username = in.readLine();
            currentRoom = "phòng mặc định"; // đảm bảo không null
            broadcast("🔔 " + username + " đã tham gia phòng chat!", currentRoom, false);

            String msg;
            while ((msg = in.readLine()) != null) {

                // 👉 Xử lý nhận public key từ client
                if (msg.startsWith("/publickey ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length == 3) {
                        String senderUsername = parts[1];
                        String encodedKey = parts[2];

                        try {
                            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
                            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                            KeyFactory kf = KeyFactory.getInstance("RSA");
                            PublicKey publicKey = kf.generatePublic(spec);

                            Server.publicKeyMap.put(senderUsername, publicKey);
                            System.out.println("🔑 Nhận public key từ " + senderUsername);
                        } catch (Exception e) {
                            System.err.println("❌ Lỗi khi giải mã public key từ " + senderUsername);
                            e.printStackTrace();
                        }
                    }
                    continue;
                }

                // 👉 Xử lý gửi RSA message theo nhóm phòng
                else if (msg.startsWith("/rsaroommsg ")) {
                	String clearText = msg.substring("/rsaroommsg ".length()).trim();

                    // LƯU TIN NHẮN VÀO DB
                    MessageDAO.sendMessage(username, currentRoom, clearText);

                    // SỬA LẠI VÒNG LẶP ĐỂ GỬI CHO TẤT CẢ CLIENTS TRONG PHÒNG
                    for (ClientHandler client : clients) {
                        if (client.currentRoom != null && client.currentRoom.equals(this.currentRoom)) {
                            PublicKey recipientKey = Server.publicKeyMap.get(client.username);
                            if (recipientKey == null) {
                                System.err.println("⚠️ Không tìm thấy public key cho " + client.username);
                                continue;
                            }

                            try {
                                byte[] encrypted = security.EncryptionUtil.encrypt(clearText.getBytes(StandardCharsets.UTF_8), recipientKey);

                                String base64 = Base64.getEncoder().encodeToString(encrypted);
                                // Gửi tin nhắn mã hóa đến client
                                client.sendMessage("/rsamsg " + this.username + " " + base64);
                            } catch (Exception e) {
                                System.err.println("❌ Không mã hóa được cho " + client.username);
                                e.printStackTrace();
                            }
                        }
                    }
                    continue;
                }

                // 👉 Xử lý tạo phòng
                else if (msg.startsWith("/create ")) {
                    String roomName = msg.substring(8).trim();
                    boolean created = RoomDAO.createRoom(roomName);
                    if (created) {
                        out.println("✅ Đã tạo phòng: " + roomName);
                        System.out.println("✔ Server: " + username + " tạo phòng " + roomName);
                    }
                }

                // 👉 Xử lý tham gia phòng
                else if (msg.startsWith("/join ")) {
                    String roomName = msg.substring(6).trim();
                    RoomDAO.joinRoom(username, roomName);

                    System.out.println("→ Ghi DB: " + username + " → " + roomName);

                    broadcast("📤 " + username + " rời khỏi " + currentRoom, currentRoom, false);
                    currentRoom = roomName;
                    broadcast("📥 " + username + " đã vào " + currentRoom, currentRoom, false);

                    System.out.println("[JOIN] " + username + " → " + currentRoom);
                    currentRoom = roomName;
                }

                // 👉 Xử lý rời phòng
                else if (msg.startsWith("/leave ")) {
                    String roomName = msg.substring(7).trim();
                    RoomDAO.leaveRoom(username, roomName);
                    System.out.println("[LEAVE] " + username + " → " + roomName);

                    if (currentRoom.equals(roomName)) {
                        broadcast("❌ " + username + " đã rời khỏi phòng.", currentRoom, false);
                        currentRoom = "phòng mặc định";
                    }
                }

                // 👉 Gửi tin nhắn thường
                else {
                    broadcast(username + ": " + msg, currentRoom, true);
                    MessageDAO.sendMessage(username, currentRoom, msg); // đúng vị trí
                }
            }



        } catch (IOException e) {
            System.err.println("❌ Lỗi từ client " + username + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            clients.remove(this);
            broadcast("❌ " + username + " đã thoát.", currentRoom, false);
        }
    }

    private void broadcast(String message, String room, boolean saveToDB) {
        for (ClientHandler c : clients) {
            if (c != null && c.currentRoom != null && c.currentRoom.equals(room)) {
                c.out.println(message);
            }
        }

        // Chỉ lưu tin người dùng
        if (saveToDB) {
            MessageDAO.sendMessage(username, room, message);
        }
    }
    public void sendMessage(String msg) {
        out.println(msg);
    }


}