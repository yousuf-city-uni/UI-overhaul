import MarketingInterface.JDBC;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageAccountsPanel extends JPanel {
    private Palette palette;
    private JFrame frame;
    private MainMenu mainMenu;

    // Tabbed pane for the two management views
    private JTabbedPane tabbedPane;

    // Client Management components
    private JTable clientTable;
    private DefaultTableModel clientTableModel;
    private JButton editClientButton;
    private JButton deleteClientButton;

    // Friends of Lancaster Management components
    private JTable friendsTable;
    private DefaultTableModel friendsTableModel;
    private JButton editFriendButton;       // New: edits both name and email
    private JButton toggleSubscriptionButton;
    private JButton deleteFriendButton;

    private JButton backButton;

    public ManageAccountsPanel(Palette palette, JFrame frame, MainMenu mainMenu) {
        this.palette = palette;
        this.frame = frame;
        this.mainMenu = mainMenu;

        setLayout(new BorderLayout());
        setBackground(palette.getBackground());

        tabbedPane = new JTabbedPane();
        // Set a custom UI for the tabbed pane so the tabs are easier to see
        tabbedPane.setUI(new CustomTabbedPaneUI(palette));

        // Create tabs
        JPanel clientManagementPanel = createClientManagementPanel();
        JPanel friendsManagementPanel = createFriendsManagementPanel();

        tabbedPane.addTab("Client Management", clientManagementPanel);
        tabbedPane.addTab("Friends of Lancaster", friendsManagementPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Back button to return to MainMenu
        backButton = new JButton("Back");
        backButton.addActionListener(e -> goBack());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(palette.getBackground());
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data initially
        loadClientsData();
        loadFriendsData();
    }

    // ----------------- Client Management -----------------

    private JPanel createClientManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(palette.getBackground());

        clientTableModel = new DefaultTableModel(
                new Object[]{"ClientID", "Name", "ContactInfo", "ClientType", "Address", "Email"}, 0
        );
        clientTable = new JTable(clientTableModel);
        JScrollPane scrollPane = new JScrollPane(clientTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(palette.getBackground());
        editClientButton = new JButton("Edit Client");
        deleteClientButton = new JButton("Delete Client");

        editClientButton.addActionListener(e -> editClient());
        deleteClientButton.addActionListener(e -> deleteClient());

        buttonPanel.add(editClientButton);
        buttonPanel.add(deleteClientButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadClientsData() {
        clientTableModel.setRowCount(0);
        String sql = "SELECT ClientID, Name, ContactInfo, ClientType, Address, Email FROM Clients";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int clientId = rs.getInt("ClientID");
                String name = rs.getString("Name");
                String contactInfo = rs.getString("ContactInfo");
                String clientType = rs.getString("ClientType");
                String address = rs.getString("Address");
                String email = rs.getString("Email");

                clientTableModel.addRow(new Object[]{clientId, name, contactInfo, clientType, address, email});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading client data:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client to edit.");
            return;
        }
        int clientId = (int) clientTableModel.getValueAt(selectedRow, 0);
        String oldEmail = (String) clientTableModel.getValueAt(selectedRow, 5);
        String oldAddress = (String) clientTableModel.getValueAt(selectedRow, 4);
        String oldContactInfo = (String) clientTableModel.getValueAt(selectedRow, 2);

        String newEmail = JOptionPane.showInputDialog(this, "Edit Email:", oldEmail);
        if (newEmail == null) return;
        String newAddress = JOptionPane.showInputDialog(this, "Edit Address:", oldAddress);
        if (newAddress == null) return;
        String newContact = JOptionPane.showInputDialog(this, "Edit Contact Info:", oldContactInfo);
        if (newContact == null) return;

        String sql = "UPDATE Clients SET Email = ?, Address = ?, ContactInfo = ? WHERE ClientID = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newEmail);
            stmt.setString(2, newAddress);
            stmt.setString(3, newContact);
            stmt.setInt(4, clientId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Client updated successfully.");
                loadClientsData();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed or record not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating client:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client to delete.");
            return;
        }
        int clientId = (int) clientTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Client ID " + clientId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM Clients WHERE ClientID = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Client deleted successfully.");
                loadClientsData();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed or record not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting client:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------- Friends of Lancaster Management -----------------

    private JPanel createFriendsManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(palette.getBackground());

        friendsTableModel = new DefaultTableModel(
                new Object[]{"FriendID", "Name", "Email", "SubscriptionStatus", "PriorityAccessStartDate"}, 0
        );
        friendsTable = new JTable(friendsTableModel);
        JScrollPane scrollPane = new JScrollPane(friendsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(palette.getBackground());
        // "Edit Friend" allows editing both name and email.
        editFriendButton = new JButton("Edit Friend");
        toggleSubscriptionButton = new JButton("Toggle Subscription");
        deleteFriendButton = new JButton("Delete Friend");

        editFriendButton.addActionListener(e -> editFriend());
        toggleSubscriptionButton.addActionListener(e -> toggleSubscription());
        deleteFriendButton.addActionListener(e -> deleteFriend());

        buttonPanel.add(editFriendButton);
        buttonPanel.add(toggleSubscriptionButton);
        buttonPanel.add(deleteFriendButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadFriendsData() {
        friendsTableModel.setRowCount(0);
        String sql = "SELECT FriendID, Name, Email, SubscriptionStatus, PriorityAccessStartDate FROM FriendsOfLancasters";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int friendId = rs.getInt("FriendID");
                String name = rs.getString("Name");
                String email = rs.getString("Email");
                String subscriptionStatus = rs.getString("SubscriptionStatus");
                Date startDate = rs.getDate("PriorityAccessStartDate");

                friendsTableModel.addRow(new Object[]{friendId, name, email, subscriptionStatus, startDate});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading Friends data:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Edit Friend Account (Name and Email)
    private void editFriend() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a friend to edit.");
            return;
        }
        int friendId = (int) friendsTableModel.getValueAt(selectedRow, 0);
        String oldName = (String) friendsTableModel.getValueAt(selectedRow, 1);
        String oldEmail = (String) friendsTableModel.getValueAt(selectedRow, 2);

        String newName = JOptionPane.showInputDialog(this, "Edit Name:", oldName);
        if (newName == null) return;
        String newEmail = JOptionPane.showInputDialog(this, "Edit Email:", oldEmail);
        if (newEmail == null) return;

        String sql = "UPDATE FriendsOfLancasters SET Name = ?, Email = ? WHERE FriendID = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setInt(3, friendId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Friend updated successfully.");
                loadFriendsData();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed or record not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLSyntaxErrorException e) {
            // Catch permission-related update error for FriendsOfLancasters table
            JOptionPane.showMessageDialog(this, "Insufficient privileges to update friend record:\n" + e.getMessage(),
                    "Permission Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating friend:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleSubscription() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to toggle subscription.");
            return;
        }
        int friendId = (int) friendsTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) friendsTableModel.getValueAt(selectedRow, 3);
        String newStatus = currentStatus.equalsIgnoreCase("Active") ? "Inactive" : "Active";

        String sql = "UPDATE FriendsOfLancasters SET SubscriptionStatus = ? WHERE FriendID = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, friendId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Subscription status changed to " + newStatus + ".");
                loadFriendsData();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed or record not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLSyntaxErrorException e) {
            // Catch permission-related update error
            JOptionPane.showMessageDialog(this, "Insufficient privileges to toggle subscription:\n" + e.getMessage(),
                    "Permission Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error toggling subscription:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteFriend() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
            return;
        }
        int friendId = (int) friendsTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Friend ID " + friendId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM FriendsOfLancasters WHERE FriendID = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, friendId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Friend deleted successfully.");
                loadFriendsData();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed or record not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting record:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------- Navigation -----------------

    private void goBack() {
        frame.remove(this);
        frame.add(mainMenu);
        frame.revalidate();
        frame.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(palette.getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // ----------------- Custom TabbedPane UI -----------------
    // This inner class customizes the look of the tab buttons.
    private class CustomTabbedPaneUI extends BasicTabbedPaneUI {
        private Palette palette;
        public CustomTabbedPaneUI(Palette palette) {
            this.palette = palette;
        }
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets = new Insets(5, 5, 5, 5);
            tabInsets = new Insets(10, 20, 10, 20);
        }
        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setColor(palette.getGreen());
            } else {
                g2.setColor(palette.getBackgroundDark());
            }
            g2.fillRoundRect(x, y, w, h, 15, 15);
        }
        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                 int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            if (isSelected) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(palette.getTextColor());
            }
            super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
        }
        @Override
        protected Insets getContentBorderInsets(int tabPlacement) {
            return new Insets(10, 10, 10, 10);
        }
    }
}

