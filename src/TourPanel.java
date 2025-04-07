import MarketingInterface.JDBC;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TourPanel extends JPanel {
    private JTextField institutionField;
    private JTextField participantsField;
    private JLabel organisedByLabel;
    private JButton selectOrganiserButton;
    private JButton pickDateButton;
    private JLabel dateLabel;
    private JButton confirmTourButton;

    private String selectedDate = "";
    private int organiserStaffID = -1;

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public TourPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(new JLabel("Tour Booking:"));

        // Institution name
        add(new JLabel("Institution:"));
        institutionField = new JTextField(10);
        add(institutionField);

        // Number of participants
        add(new JLabel("Participants:"));
        participantsField = new JTextField(5);
        add(participantsField);

        // OrganisedBy (Staff)
        add(new JLabel("Organised By (Staff):"));
        organisedByLabel = new JLabel("None");
        selectOrganiserButton = new JButton("Select Organiser");
        selectOrganiserButton.addActionListener(e -> openSelectStaffDialog());
        add(organisedByLabel);
        add(selectOrganiserButton);

        // Tour date
        dateLabel = new JLabel("Tour Date: Not Selected");
        //pickDateButton = new JButton("Pick Tour Date");
        add(dateLabel);
        //add(pickDateButton);

        // Confirm booking
        confirmTourButton = new JButton("Confirm Tour");
        confirmTourButton.addActionListener(e -> confirmTour());
        add(confirmTourButton);
    }

    /**
     * Opens a staff selection dialog so the user can pick who is organising the tour.
     */
    private void openSelectStaffDialog() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        SelectStaffDialog staffDialog = new SelectStaffDialog(owner);
        staffDialog.setVisible(true);
        int staffId = staffDialog.getSelectedStaffId();
        if (staffId != -1) {
            organiserStaffID = staffId;
            organisedByLabel.setText(String.valueOf(staffId));
        } else {
            organiserStaffID = -1;
            organisedByLabel.setText("None");
        }
    }

    /**
     * Validates fields and inserts a row into the Tours table if valid.
     */
    private void confirmTour() {
        String institution = institutionField.getText().trim();
        String participantsStr = participantsField.getText().trim();

        if (institution.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter institution name.");
            return;
        }
        if (selectedDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please pick a tour date.");
            return;
        }
        int participants;
        try {
            participants = Integer.parseInt(participantsStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number of participants.");
            return;
        }
        if (organiserStaffID == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to organise the tour.");
            return;
        }

        // (Optional) check if date is valid or available
        if (!checkTourAvailability(selectedDate)) {
            JOptionPane.showMessageDialog(this, "Tour date not available or invalid.");
            return;
        }

        // Insert into Tours
        insertTour(institution, selectedDate, participants, organiserStaffID);
    }

    /**
     * Stub method to check if the selected date is valid or available.
     */
    private boolean checkTourAvailability(String dateStr) {
        // For now, always return true. You can add real logic here.
        return true;
    }

    /**
     * Inserts a row into the Tours table.
     * We convert dd/MM/yyyy to yyyy-MM-dd before storing.
     */
    private void insertTour(String institution, String dateStr, int participants, int organiser) {
        // Convert dd/MM/yyyy -> yyyy-MM-dd
        String[] parts = dateStr.split("/");
        String isoDate = parts[2] + "-" + parts[1] + "-" + parts[0];

        String sql = "INSERT INTO Tours (InstitutionName, TourDate, NumberOfParticipants, OrganisedBy) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, institution);
            stmt.setDate(2, java.sql.Date.valueOf(isoDate));
            stmt.setInt(3, participants);
            stmt.setInt(4, organiser);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newTourID = keys.getInt(1);
                        JOptionPane.showMessageDialog(this,
                                "Tour booked successfully!\nTourID = " + newTourID);
                        resetFields();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to book tour.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error booking tour:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Resets the form fields after successful insert.
     */
    private void resetFields() {
        institutionField.setText("");
        participantsField.setText("");
        organiserStaffID = -1;
        organisedByLabel.setText("None");
        selectedDate = "";
        dateLabel.setText("Tour Date: Not Selected");
    }
}


