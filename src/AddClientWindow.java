import MarketingInterface.JDBC;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddClientWindow extends JFrame {
    private JTextField nameField;
    private JTextField contactField;
    private JTextField typeField;
    private JTextField addressField;
    private JTextField emailField;
    private JButton createButton;

    public AddClientWindow() {
        super("Add New Client");
        setSize(400, 300);
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

        // Create button
        createButton = new JButton("Create Client");
        createButton.addActionListener(e -> createClient());
        // We'll use an empty label on the left so the button can align in the second column
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
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setString(3, clientType);
            stmt.setString(4, address);
            stmt.setString(5, email);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Client created successfully!");
                dispose();  // close this window
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create client.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inserting client:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

