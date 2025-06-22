package view;

import view.components.MessageBubble;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import dao.ChatRoomDAO;
import java.util.List;

import dao.MessageDAO;
import dao.RoomDAO;
import model.Message;
import model.User;
import dao.RoomMemberDAO;

import java.io.*;
import java.net.Socket;
import client.SocketClientHandler;



public class ChatRoomView extends JFrame {
    private JList<String> roomList;
    private JPanel chatArea;
    private JScrollPane scrollPane;
    private JTextField txtMessage;
    private JButton btnSend;
    private DefaultListModel<String> memberListModel;
    private JList<String> memberList;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DefaultListModel<String> allRoomListModel;
    private JList<String> allRoomList;
    private JButton btnJoinRoom, btnCreateRoom, btnLeaveRoom;
    private DefaultListModel<String> roomListModel;
    private User user;
    private String selectedRoom;
    private String currentRoomName;
    private String username; // khai báo ở đầu lớp
    private String selectedRoomName;


    public ChatRoomView(String currentUser) {
        setTitle("💬 Chat nhóm - " + currentUser);
        try {
            socket = new Socket("localhost", 5000); // hoặc cổng bạn dùng
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(currentUser); // Gửi tên khi mới kết nối

            // Nhận tin từ server
            new Thread(new SocketClientHandler(in, this, currentUser)).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "❌ Không kết nối được đến server.");
            dispose(); // đóng cửa sổ nếu lỗi
        }

        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        Font font = new Font("Segoe UI", Font.PLAIN, 14);
        Color accent = new Color(0, 120, 215);

        // ===== TRÁI: Danh sách phòng =====
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);

        // 🧠 Lấy danh sách phòng đã tham gia từ DB
        List<String> joinedRooms = RoomDAO.getJoinedRooms(currentUser);
        for (String room : joinedRooms) {
            roomListModel.addElement(room);
        }
        roomList = new JList<>(roomListModel);

        roomList.setFont(font);
        roomList.setBackground(Color.WHITE);
        roomList.setFixedCellHeight(40);
        roomList.setSelectionBackground(accent);
        roomList.setSelectionForeground(Color.WHITE);
        JScrollPane roomScroll = new JScrollPane(roomList);
        roomScroll.setPreferredSize(new Dimension(180, 0));
        roomScroll.setBorder(BorderFactory.createTitledBorder("🗂 Phòng chat"));
        add(roomScroll, BorderLayout.WEST);
        
        roomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedRoom = roomList.getSelectedValue();
                if (selectedRoom != null && out != null) {

                    // ✅ Gửi lệnh join phòng (giúp server biết bạn đang ở phòng nào)
                    out.println("/join " + selectedRoom);
                    selectedRoomName = selectedRoom;

                    // ✅ Xóa khung chat cũ
                    chatArea.removeAll();
                    chatArea.revalidate();
                    chatArea.repaint();

                    // ✅ Load lại tin nhắn cũ từ DB
                    loadRoomData(selectedRoom, currentUser); // cần có hàm này

                    // ✅ Load lại danh sách thành viên trong phòng
                    List<String> members = RoomDAO.getMembersInRoom(selectedRoom);
                    memberListModel.clear();
                    for (String member : members) {
                        memberListModel.addElement(member);
                    }
                }
            }
        });


     // ===== PHẢI: Danh sách thành viên (gắn với phòng đang chọn) =====
        memberListModel = new DefaultListModel<>();
        memberListModel.addElement(currentUser); // tạm thời
        memberList = new JList<>(memberListModel);
        memberList.setFont(font);
        memberList.setBackground(Color.WHITE);
        memberList.setFixedCellHeight(35);
        JScrollPane memberScroll = new JScrollPane(memberList);

        // Gói vào panel riêng
        JPanel memberPanel = new JPanel(new BorderLayout());
        memberPanel.setBorder(BorderFactory.createTitledBorder("👥 Thành viên"));
        memberPanel.add(memberScroll, BorderLayout.CENTER);
        memberPanel.setPreferredSize(new Dimension(160, 120)); // chiều cao thành viên

        // ===== GIỮA: Khung chat bubble =====
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        centerPanel.setBackground(Color.WHITE);

        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createTitledBorder("💬 Tin nhắn"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        txtMessage = new JTextField();
        txtMessage.setFont(font);
        txtMessage.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        btnSend = new JButton("Gửi");
        btnSend.setBackground(accent);
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setPreferredSize(new Dimension(80, 35));

        inputPanel.add(txtMessage, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        inputPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        centerPanel.add(inputPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // ===== PHẢI: Panel quản lý phòng =====
        JPanel roomPanel = new JPanel();
        roomPanel.setLayout(new BorderLayout(5, 5));
        roomPanel.setBorder(BorderFactory.createTitledBorder("🏠 Phòng có sẵn"));
        roomPanel.setBackground(Color.WHITE);

        // Danh sách tất cả phòng
        allRoomListModel = new DefaultListModel<>();
        for (String roomName : dao.RoomDAO.getAllRooms()) {
            allRoomListModel.addElement(roomName);
        }


        allRoomList = new JList<>(allRoomListModel);
        allRoomList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        allRoomList.setFixedCellHeight(35);
        JScrollPane allRoomScroll = new JScrollPane(allRoomList);
        roomPanel.add(allRoomScroll, BorderLayout.CENTER);

        // Nút thao tác
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        btnJoinRoom = new JButton("🔄 Tham gia");
        btnCreateRoom = new JButton("➕ Tạo phòng");
        btnLeaveRoom = new JButton("❌ Rời phòng");
        
        
        btnJoinRoom.addActionListener(e -> {
            int selectedIndex = allRoomList.getSelectedIndex();
            if (selectedIndex != -1 && out != null) {
                String selectedRoom = allRoomListModel.getElementAt(selectedIndex);

                // Gửi lệnh /join đến server
                out.println("/join " + selectedRoom);

                // ✅ Thêm vào danh sách phòng bên trái nếu chưa có
                if (!roomListModel.contains(selectedRoom)) {
                    roomListModel.addElement(selectedRoom);
                }

                // ✅ Đặt selected để chọn luôn phòng vừa tham gia
                roomList.setSelectedValue(selectedRoom, true);

                // ✅ Xoá tin cũ ở khung chat
                chatArea.removeAll();
                chatArea.revalidate();
                chatArea.repaint();

                // 🔥 BỔ SUNG: Tải danh sách thành viên từ DB
                List<String> members = RoomDAO.getMembersInRoom(selectedRoom);
                memberListModel.clear();
                for (String member : members) {
                    memberListModel.addElement(member);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để tham gia.");
            }
        });

        
        btnCreateRoom.addActionListener(e -> {
            String newRoom = JOptionPane.showInputDialog(this, "Nhập tên phòng mới:");
            if (newRoom != null && !newRoom.trim().isEmpty()) {
                newRoom = newRoom.trim();

                // Kiểm tra phòng đã tồn tại trong danh sách chưa
                if (!allRoomListModel.contains(newRoom)) {
                    allRoomListModel.addElement(newRoom); // thêm vào danh sách phòng bên phải
                }

                // Gửi lệnh tạo và tham gia phòng mới
                if (out != null) {
                    out.println("/create " + newRoom);
                    out.println("/join " + newRoom);
                }

                // Cập nhật UI trái
                if (!roomListModel.contains(newRoom)) {
                    roomListModel.addElement(newRoom);
                }
                roomList.setSelectedValue(newRoom, true);
                chatArea.removeAll();
                chatArea.revalidate();
                chatArea.repaint();
            }
        });
        
        btnLeaveRoom.addActionListener(e -> {
            String currentRoom = roomList.getSelectedValue();
            if (currentRoom != null && out != null) {
                out.println("/leave " + currentRoom); // gửi lệnh rời phòng

                // Xóa khỏi danh sách trái
                roomListModel.removeElement(currentRoom);

                // Clear giao diện chat
                chatArea.removeAll();
                chatArea.revalidate();
                chatArea.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để rời.");
            }
        });





        btnPanel.add(btnJoinRoom);
        btnPanel.add(btnCreateRoom);
        btnPanel.add(btnLeaveRoom);
        roomPanel.add(btnPanel, BorderLayout.SOUTH);

        // ===== GỘP THÀNH PANEL BÊN PHẢI CHÍNH =====
        JPanel rightMainPanel = new JPanel(new BorderLayout(5, 5));
        rightMainPanel.setPreferredSize(new Dimension(160, 0));
        rightMainPanel.add(memberPanel, BorderLayout.NORTH);
        rightMainPanel.add(roomPanel, BorderLayout.CENTER);
        add(rightMainPanel, BorderLayout.EAST);


        // ===== Test demo =====
//        addMessage("trung: hello", false);
//        addMessage("mình: hello bạn!", true);

        btnSend.addActionListener(e -> {
            String text = txtMessage.getText().trim();
            if (!text.isEmpty() && out != null) {
                // 1. Gửi qua socket cho server và các client khác
                out.println(text);

                // 2. Lưu vào cơ sở dữ liệu
                boolean success = MessageDAO.sendMessage(currentUser, selectedRoom, text);
                if (!success) {
                    System.err.println("⚠️ Không lưu được tin nhắn vào DB.");
                }

                // 3. Xóa input
                txtMessage.setText("");
            }
        });





        setVisible(true);
    }

    public void addMessage(String text, boolean isMine) {
        // Bỏ qua các dòng thông báo hệ thống
        if (text.startsWith("📥 ") || text.startsWith("📤 ") || text.startsWith("❌ ") || text.startsWith("🔔 ")) {
            return;
        }

        MessageBubble bubble = new MessageBubble(text, isMine);
        chatArea.add(bubble);
        chatArea.revalidate();
        chatArea.repaint();
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }

    
    private void loadRoomData(String roomName, String currentUser) {
        // Load tin nhắn
        chatArea.removeAll();
        for (Message msg : MessageDAO.getMessagesByRoomName(roomName)) {
            // BỎ QUA nếu nội dung là tin hệ thống (có thể bắt đầu bằng biểu tượng 📱 hoặc chứa từ “đã vào”, “rời khỏi”)
            String content = msg.getContent();
            if (content.startsWith("📱") || content.contains("đã vào") || content.contains("rời khỏi")) {
                continue; // bỏ qua không hiển thị
            }

            boolean isMine = msg.getSenderName().equals(currentUser);
            String formatted = msg.getSenderName() + ": " + content;
            addMessage(formatted, isMine);
        }

        for (String name : RoomMemberDAO.getMembersByRoomName(roomName)) {
            memberListModel.addElement(name);
        }

        chatArea.revalidate();
        chatArea.repaint();
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }


}