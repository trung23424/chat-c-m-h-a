package client;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;
import security.MyKeyManager;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.spec.X509EncodedKeySpec;

public class ChatClient {
	private static final java.util.Map<String, PublicKey> publicKeyMap = new java.util.HashMap<>();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private MyKeyManager keyManager;
    
    public ChatClient(String host, int port) {
    	this.keyManager = new MyKeyManager();

        try {
            socket = new Socket(host, port);
            System.out.println("✅ Kết nối đến server!");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Nhập tên của bạn: ");
            username = scanner.nextLine();
            out.println(username); // gửi username lên server
         // Gửi public key RSA lên server
            try {
                PublicKey myPublicKey = MyKeyManager.getPublicKey();
                String encodedKey = Base64.getEncoder().encodeToString(myPublicKey.getEncoded());
                out.println("/publickey " + username + " " + encodedKey);
            } catch (Exception e) {
                e.printStackTrace();
            }


            // Luồng nhận tin
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                    	if (msg.startsWith("/rsamsg ")) {
                    	    String[] parts = msg.split(" ", 3);
                    	    if (parts.length == 3) {
                    	        String sender = parts[1];
                    	        String encryptedBase64 = parts[2];

                    	        try {
                    	            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
                    	            byte[] decryptedBytes = security.EncryptionUtil.decrypt(encryptedBytes, MyKeyManager.getPrivateKey());
                    	            String originalMessage = new String(decryptedBytes);

                    	            System.out.println("🔐 " + sender + ": " + originalMessage);
                    	        } catch (Exception e) {
                    	            System.err.println("❌ Lỗi giải mã tin nhắn từ " + sender);
                    	            e.printStackTrace();
                    	        }
                    	    }
                    	} else {
                    	    System.out.println(msg); // các tin nhắn hệ thống khác
                    	}

                    }
                } catch (IOException e) {
                    System.err.println("❌ Mất kết nối từ server.");
                }
            }).start();

            // Luồng gửi tin
            while (true) {
                String msg = scanner.nextLine();
                out.println("/rsaroommsg " + msg); // Gửi nội dung rõ cho server mã hóa broadcast
            }

        } catch (IOException e) {
            System.err.println("❌ Không thể kết nối server: " + e.getMessage());
        }
    }
    
    public void sendEncryptedMessage(String message) {
        for (String recipient : publicKeyMap.keySet()) {
            PublicKey key = publicKeyMap.get(recipient);
            if (key == null || recipient.equals(username)) continue;

            try {
                byte[] encryptedBytes = security.EncryptionUtil.encrypt(message.getBytes(), key);
                String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
                out.println("/rsamsg " + recipient + " " + encryptedBase64);
            } catch (Exception e) {
                System.err.println("❌ Không thể mã hóa cho " + recipient);
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        new ChatClient("localhost", 5000);
    }
}