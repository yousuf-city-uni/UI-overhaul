import MarketingInterface.JDBC;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SelectStaffDialog extends JDialog {
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private int selectedStaffId = -1;
    private boolean confirmed = false;

    public SelectStaffDialog(JFrame owner) {
        super(owner, "Select Staff", true);
        setSize(600, 400);
        setLocationRelativeTo(owner);

        // Single column: "Staff Info"
        tableModel = new DefaultTableModel(new Object[]{"Staff Info"}, 0);
        staffTable = new JTable(tableModel);

        loadStaff();

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            int row = staffTable.getSelectedRow();
            if (row != -1) {
                // The cell contains a StaffInfo object.
                StaffInfo staff = (StaffInfo) tableModel.getValueAt(row, 0);
                selectedStaffId = staff.getStaffId();
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a staff member.");
            }
        });

        setLayout(new BorderLayout());
        add(new JScrollPane(staffTable), BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);
    }

    private void loadStaff() {
        String sql = "SELECT StaffID, Name, Role FROM Staff";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("StaffID");
                String name = rs.getString("Name");
                String role = rs.getString("Role");
                StaffInfo staff = new StaffInfo(id, name, role);
                tableModel.addRow(new Object[]{staff});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getSelectedStaffId() {
        return confirmed ? selectedStaffId : -1;
    }

    // Private inner class to hold staff info in one object.
    private class StaffInfo {
        private int staffId;
        private String name;
        private String role;

        public StaffInfo(int staffId, String name, String role) {
            this.staffId = staffId;
            this.name = name;
            this.role = role;
        }

        public int getStaffId() {
            return staffId;
        }

        @Override
        public String toString() {
            // For example: "102 - Alice Johnson (Manager)"
            return staffId + " - " + name + " (" + role + ")";
        }
    }
}


