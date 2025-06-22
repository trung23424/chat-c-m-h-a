package view;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginView extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister;

    public LoginView() {
        setTitle("Đăng nhập");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("ĐĂNG NHẬP HỆ THỐNG", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Panel nhập liệu
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.add(new JLabel("Tên đăng nhập:"));
        txtUsername = new JTextField();
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        formPanel.add(txtPassword);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnLogin = new JButton("Đăng nhập");
        btnRegister = new JButton("Đăng ký");
       
        btnLogin.setPreferredSize(new Dimension(120, 30));
        btnRegister.setPreferredSize(new Dimension(120, 30));
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Sự kiện nút
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.");
                return;
            }

            User user = new User(username, password);
            boolean success = UserDAO.login(user);

            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Đăng nhập thành công!");
                dispose(); // Đóng LoginView
                new ChatRoomView(username); // Mở giao diện chat nhóm
            } else {
                JOptionPane.showMessageDialog(this, "❌ Sai tài khoản hoặc mật khẩu.");
            }
        });
        
        btnRegister.addActionListener(e -> {
            dispose(); // đóng cửa sổ hiện tại
            new RegisterView(); // mở cửa sổ đăng ký
        });
        setVisible(true);
    }
}