package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
import security.EncryptionUtil;


public class Server {
	public static HashMap<String, PublicKey> publicKeyMap = new HashMap<>();
	public static HashMap<String, ClientHandler> clientMap = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("✅ Server đang chạy tại cổng 5000...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 Client mới kết nối!");
                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            System.err.println("❌ Lỗi server: " + e.getMessage());
        }
    }
}