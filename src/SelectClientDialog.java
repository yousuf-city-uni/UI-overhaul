import MarketingInterface.JDBC;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SelectClientDialog extends JDialog {
    private JTable clientTable;
    private DefaultTableModel tableModel;
    private int selectedClientId = -1;
    private boolean confirmed = false;

    public SelectClientDialog(JFrame owner) {
        super(owner, "Select Client", true);
        setSize(600, 400);
        setLocationRelativeTo(owner);

        // Single column: "Client Info"
        tableModel = new DefaultTableModel(new Object[]{"Client Info"}, 0);
        clientTable = new JTable(tableModel);

        loadClients();

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            int row = clientTable.getSelectedRow();
            if (row != -1) {
                // The cell holds a ClientInfo object.
                ClientInfo client = (ClientInfo) tableModel.getValueAt(row, 0);
                selectedClientId = client.getClientId();
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a client.");
            }
        });

        setLayout(new BorderLayout());
        add(new JScrollPane(clientTable), BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);
    }

    private void loadClients() {
        // Retrieve ClientID, Name, Email from the Clients table
        String sql = "SELECT ClientID, Name, Email FROM Clients";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("ClientID");
                String name = rs.getString("Name");
                String email = rs.getString("Email");

                // Create a ClientInfo object with this data
                ClientInfo client = new ClientInfo(id, name, email);

                // Add as one cell to the table
                tableModel.addRow(new Object[]{client});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getSelectedClientId() {
        return confirmed ? selectedClientId : -1;
    }

    // Private inner class to store client info in a single object
    private class ClientInfo {
        private int clientId;
        private String name;
        private String email;

        public ClientInfo(int clientId, String name, String email) {
            this.clientId = clientId;
            this.name = name;
            this.email = email;
        }

        public int getClientId() {
            return clientId;
        }

        @Override
        public String toString() {
            // Show something like: "123 - Smith (smith@example.com)"
            return clientId + " - " + name + " (" + email + ")";
        }
    }
}



