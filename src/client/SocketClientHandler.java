package client;

import view.ChatRoomView;

import java.io.BufferedReader;

public class SocketClientHandler implements Runnable {
    private BufferedReader in;
    private ChatRoomView chatView;
    private String currentUser;

    public SocketClientHandler(BufferedReader in, ChatRoomView chatView, String currentUser) {
        this.in = in;
        this.chatView = chatView;
        this.currentUser = currentUser;
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                boolean isMine = msg.startsWith(currentUser + ":");
                String display = msg;
                javax.swing.SwingUtilities.invokeLater(() -> {
                    chatView.addMessage(display, isMine);
                });
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi nhận tin từ server: " + e.getMessage());
        }
    }
}