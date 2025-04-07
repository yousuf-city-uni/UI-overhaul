import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JPanel {
    private final Palette palette;
    private final JFrame frame;
    private final Intro intro;
    private final Login login;
    private final Scene scene;
    private final BookingMenu bookingMenu;
    private JButton settingsButton;
    private JButton clientsButton;
    private JButton bookingsButton;
    private JButton signOutButton;
    private JButton manageAccountsButton;
    private JLabel welcomeLabel;
    private Image settingsIconLight;
    private Image settingsIconDark;
    private int pWidth = 1280;
    private int pHeight = 720;
    private AddClientWindow clientWindow;
    private ManageAccountsPanel manageAccountsPanel;

    private boolean isSignedOut = false;
    public boolean isSignedOut() {
        return isSignedOut;
    }
    public void setSignedOut(boolean signedOut) {
        isSignedOut = signedOut;
    }

    public MainMenu(Palette palette, Scene scene, JFrame frame, Intro intro, Login login, BookingMenu bookingMenu) {
        this.palette = palette;
        this.frame = frame;
        this.intro = intro;
        this.login = login;
        this.scene = scene;
        this.bookingMenu = bookingMenu;

        setLayout(new BorderLayout());

        // Settings icon
        settingsIconLight = new ImageIcon("src/assets/icons/settings_light.png").getImage();
        settingsIconDark = new ImageIcon("src/assets/icons/settings_dark.png").getImage();
        Image scaledImage;
        if (palette.getIsDark()) {
            scaledImage = settingsIconLight.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        } else {
            scaledImage = settingsIconDark.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        }
        settingsButton = new JButton(new ImageIcon(scaledImage));
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setOpaque(false);
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog settingsDialog = new JDialog(frame, "Settings", true);
                settingsDialog.setBackground(palette.getBackground());
                settingsDialog.setSize(300, 150);
                settingsDialog.setLocationRelativeTo(frame);
                settingsDialog.setLayout(new BorderLayout());

                JPanel togglePanel = new JPanel();
                togglePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                togglePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                JToggleButton darkModeToggle = new JToggleButton("Enable Light Mode");
                darkModeToggle.setSelected(false);
                darkModeToggle.setFocusPainted(false);
                darkModeToggle.addActionListener(e1 -> {
                    boolean isLight = darkModeToggle.isSelected();
                    palette.setIsDark(!isLight);

                    if (isLight) {
                        darkModeToggle.setText("Enable Dark Mode");
                    } else {
                        darkModeToggle.setText("Enable Light Mode");
                    }

                    resizeSettingsButton(getHeight() / 15);
                    updateButtonSizes();
                    updateWelcomeFont();
                    revalidate();
                    repaint();
                });

                togglePanel.add(darkModeToggle);
                settingsDialog.add(togglePanel, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel();
                JButton closeButton = new JButton("Close");
                closeButton.setFocusPainted(false);
                closeButton.addActionListener(event -> settingsDialog.dispose());
                buttonPanel.add(closeButton);
                settingsDialog.add(buttonPanel, BorderLayout.SOUTH);

                settingsDialog.setVisible(true);
            }
        });

        // Top bar with welcome message and settings button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        String welcomeMessage = "Welcome User";
        welcomeLabel = new JLabel(welcomeMessage);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(palette.getTextColor());
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(settingsButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int size = getHeight() / 15;
                resizeSettingsButton(size);
                updateButtonSizes();
                updateWelcomeFont();
            }
        });

        // Main buttons
        clientsButton = new JButton("Add Client");
        bookingsButton = new JButton("Bookings");
        signOutButton = new JButton("Sign Out");
        manageAccountsButton = new JButton("Manage Accounts");

        Font buttonFont = new Font("Arial", Font.PLAIN, 18);
        clientsButton.setFont(buttonFont);
        bookingsButton.setFont(buttonFont);
        signOutButton.setFont(buttonFont);
        manageAccountsButton.setFont(buttonFont);

        clientsButton.setBackground(palette.getGreen());
        bookingsButton.setBackground(palette.getGreen());
        signOutButton.setBackground(palette.getGreen());
        manageAccountsButton.setBackground(palette.getGreen());

        clientsButton.setForeground(palette.getTextColor());
        bookingsButton.setForeground(palette.getTextColor());
        signOutButton.setForeground(palette.getTextColor());
        manageAccountsButton.setForeground(palette.getTextColor());

        clientsButton.setBorder(BorderFactory.createLineBorder(palette.getGreen(), 2));
        bookingsButton.setBorder(BorderFactory.createLineBorder(palette.getGreen(), 2));
        signOutButton.setBorder(BorderFactory.createLineBorder(palette.getGreen(), 2));
        manageAccountsButton.setBorder(BorderFactory.createLineBorder(palette.getGreen(), 2));

        clientsButton.setFocusPainted(false);
        bookingsButton.setFocusPainted(false);
        signOutButton.setFocusPainted(false);
        manageAccountsButton.setFocusPainted(false);

        MouseAdapter hoverEffect = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton button = (JButton) e.getSource();
                button.setBackground(palette.getTextColor());
                button.setForeground(palette.getGreen());
                button.setBorder(BorderFactory.createLineBorder(palette.getTextColor(), 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                JButton button = (JButton) e.getSource();
                button.setBackground(palette.getGreen());
                button.setForeground(palette.getTextColor());
                button.setBorder(BorderFactory.createLineBorder(palette.getGreen(), 2));
            }
        };

        clientsButton.addMouseListener(hoverEffect);
        bookingsButton.addMouseListener(hoverEffect);
        signOutButton.addMouseListener(hoverEffect);
        manageAccountsButton.addMouseListener(hoverEffect);

        clientsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signOutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageAccountsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Center panel to hold buttons
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(clientsButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(bookingsButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(manageAccountsButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(signOutButton);
        centerPanel.add(Box.createVerticalGlue());

        // Button actions
        clientsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientWindow = new AddClientWindow();
                clientWindow.setVisible(true);
            }
        });
        bookingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.remove(MainMenu.this);
                frame.add(bookingMenu);
                scene.setScene("Booking");
                frame.revalidate();
                frame.repaint();
            }
        });
        manageAccountsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.remove(MainMenu.this);
                if (manageAccountsPanel == null) {
                    manageAccountsPanel = new ManageAccountsPanel(palette, frame, MainMenu.this);
                }
                frame.add(manageAccountsPanel);
                frame.revalidate();
                frame.repaint();
            }
        });
        signOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isSignedOut = true;
            }
        });

        add(centerPanel, BorderLayout.CENTER);

        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                    updateButtonSizes();
                    resizeSettingsButton(getHeight() / 15);
                    updateWelcomeFont();
                    revalidate();
                    repaint();
                    removeHierarchyListener(this);
                }
            }
        });
    }

    private void resizeSettingsButton(int size) {
        settingsIconLight = new ImageIcon("src/assets/icons/settings_light.png").getImage();
        settingsIconDark = new ImageIcon("src/assets/icons/settings_dark.png").getImage();

        Image scaledImage;
        if (palette.getIsDark()){
            scaledImage = settingsIconLight.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        } else {
            scaledImage = settingsIconDark.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        }
        settingsButton.setIcon(new ImageIcon(scaledImage));
    }

    private void updateWelcomeFont() {
        int height = getHeight();
        int fontSize = Math.max(12, height / 30);
        welcomeLabel.setForeground(palette.getTextColor());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, fontSize));
    }

    private void updateButtonSizes() {
        int buttonWidth = pWidth / 4;
        int panelHeight = pHeight;
        int buttonHeight = (int)(panelHeight * 0.05);
        Dimension newButtonSize = new Dimension(buttonWidth, buttonHeight);

        clientsButton.setPreferredSize(newButtonSize);
        bookingsButton.setPreferredSize(newButtonSize);
        signOutButton.setPreferredSize(newButtonSize);
        manageAccountsButton.setPreferredSize(newButtonSize);

        clientsButton.setMaximumSize(newButtonSize);
        bookingsButton.setMaximumSize(newButtonSize);
        signOutButton.setMaximumSize(newButtonSize);
        manageAccountsButton.setMaximumSize(newButtonSize);
        frame.revalidate();
        frame.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (pWidth <= 0 || pHeight <= 0) {
            updateButtonSizes();
        }
        pWidth = getWidth();
        pHeight = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(palette.getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
