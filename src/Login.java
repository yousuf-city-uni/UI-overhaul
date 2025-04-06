import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JPanel {
    private final Palette palette;

    private JTextField usernameField;
    public JTextField getUsernameField() {
        return usernameField;
    }

    private JPasswordField passwordField;
    private JButton loginButton;
    private Image image;

    public JSplitPane getSplitPane() {
        return splitPane;
    }

    private JSplitPane splitPane;

    private JDBC jdbc;
    private Boolean isLoggedIn = false;
    public Boolean getLoggedIn() {
        return isLoggedIn;
    }
    public void setIsLoggedIn(Boolean value) {
        isLoggedIn = value;
    }

    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public Login(Palette palette, JDBC jdbc) {
        this.palette = palette;
        this.jdbc = jdbc;

        image = Toolkit.getDefaultToolkit().getImage("src/assets/lancaster/logo.png");

        usernameField = new JTextField(40);
        passwordField = new JPasswordField(40);
        loginButton = new JButton("Login");

        usernameField.setPreferredSize(new Dimension(usernameField.getPreferredSize().width, 25));  // Increased height
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 25));  // Increased height

        styleLoginButton();

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel usernameLabel = new JLabel("Username: ");
        usernameLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(usernameLabel, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(usernameField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setForeground(Color.WHITE);
        add(passwordLabel, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // Add ActionListener to loginButton
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword()); // Convert password char array to string
                setIsLoggedIn(authenticate(username, password));

                if (getLoggedIn() && onLoginSuccess != null) {
                    onLoginSuccess.run();
                }

                if (!getLoggedIn()) {
                    JOptionPane.showMessageDialog(Login.this, "Invalid username or password");
                }
            }
        });
    }

    private void styleLoginButton() {
        loginButton.setForeground(Color.WHITE);

        loginButton.setBackground(palette.getGreen());

        loginButton.setFont(new Font("Arial", Font.BOLD, 20));

        loginButton.setBorder(BorderFactory.createLineBorder(palette.getGreen(), 2));
        loginButton.setFocusPainted(false);

        loginButton.setPreferredSize(new Dimension(200, 40));

        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setForeground(palette.getGreen());
                loginButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                loginButton.setBackground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setForeground(Color.WHITE);
                loginButton.setBorder(BorderFactory.createLineBorder(palette.getGreen(), 2));
                loginButton.setBackground(palette.getGreen());
            }
        });
    }

    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(palette.getBackgroundDark());
                g2d.fillRect(0, 0, getWidth(), getHeight());

                int imageWidth = getWidth();
                int imageHeight = (imageWidth * 15) / 20;

                if (imageHeight > getHeight()) {
                    imageHeight = getHeight();
                    imageWidth = (imageHeight * 20) / 15;
                }

                int x = (getWidth() - imageWidth) / 2;
                int y = (getHeight() - imageHeight) / 2;

                g2d.drawImage(image, x, y, imageWidth, imageHeight, this);
            }
        };
        imagePanel.setPreferredSize(new Dimension(400, getHeight()));
        return imagePanel;
    }

    public JSplitPane createSplitPane() {
        JPanel imagePanel = createImagePanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePanel, this);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(0);
        splitPane.setResizeWeight(0);
        return splitPane;
    }

    private boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM Staff WHERE Username = ? AND Password = ?";
        try (Connection conn = jdbc.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                     return true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(palette.getBackgroundDark());
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
