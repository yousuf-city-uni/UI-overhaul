import MarketingInterface.JDBC;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class AddClientWindow extends JFrame {
    private JTextField nameField;
    private JTextField contactField;
    private JTextField typeField;
    private JTextField addressField;
    private JTextField emailField;
    private JCheckBox friendsOfLancasterCheckBox;
    private JButton createButton;

    public AddClientWindow() {
        super("Add New Client");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(0, 2, 10, 10));

        // Form fields
        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Contact Info:"));
        contactField = new JTextField();
        add(contactField);

        add(new JLabel("Client Type:"));
        typeField = new JTextField();
        add(typeField);

        add(new JLabel("Address:"));
        addressField = new JTextField();
        add(addressField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        // Friends of Lancaster checkbox
        add(new JLabel("Friends of Lancaster:"));
        friendsOfLancasterCheckBox = new JCheckBox("Yes");
        add(friendsOfLancasterCheckBox);

        // Create button
        createButton = new JButton("Create Client");
        createButton.addActionListener(e -> createClient());
        // Use an empty label on the left so the button aligns in the second column
        add(new JLabel());
        add(createButton);
    }

    private void createClient() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String clientType = typeField.getText().trim();
        String address = addressField.getText().trim();
        String email = emailField.getText().trim();

        // Basic validation
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return;
        }

        // Insert into Clients table
        String sql = "INSERT INTO Clients (Name, ContactInfo, ClientType, Address, Email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setString(3, clientType);
            stmt.setString(4, address);
            stmt.setString(5, email);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // If the Friends of Lancaster checkbox is selected,
                // insert into FriendsOfLancaster using the client's name (not the client type)
                if (friendsOfLancasterCheckBox.isSelected()) {
                    insertIntoFriendsOfLancaster(conn, name, email);
                }
                JOptionPane.showMessageDialog(this, "Client created successfully!");
                dispose();  // Close this window
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create client.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inserting client:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertIntoFriendsOfLancaster(Connection conn, String clientName, String email) throws SQLException {
        // Insert the client's name and email into FriendsOfLancaster table
        String sqlFoL = "INSERT INTO FriendsOfLancasters (Name, Email, SubscriptionStatus, PriorityAccessStartDate) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlFoL)) {
            stmt.setString(1, clientName); // Use the client's name (not the client type)
            stmt.setString(2, email);
            stmt.setString(3, "Active"); // Default subscription status
            stmt.setDate(4, Date.valueOf(LocalDate.now())); // Today's date
            stmt.executeUpdate();
        }
    }
}

