package view.components;

import javax.swing.*;
import java.awt.*;

public class MessageBubble extends JPanel {
    public MessageBubble(String text, boolean isMine) {
        setLayout(new BorderLayout());
        setOpaque(false);

        JTextArea area = new JTextArea(text);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBackground(isMine ? new Color(0, 120, 215) : new Color(230, 230, 230));
        area.setForeground(isMine ? Color.WHITE : Color.BLACK);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel bubble = new JPanel();
        bubble.setLayout(new BorderLayout());
        bubble.setBackground(area.getBackground());
        bubble.setBorder(area.getBorder());
        bubble.add(area, BorderLayout.CENTER);

        JPanel alignPanel = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
        alignPanel.setOpaque(false);
        alignPanel.add(bubble);

        add(alignPanel, BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, super.getPreferredSize().height);
    }
}