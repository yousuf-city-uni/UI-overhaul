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
        dateLabel.setText("Tour Date: " + selectedDate);
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

        // Organised By (Staff)
        add(new JLabel("Organised By (Staff):"));
        organisedByLabel = new JLabel("None");
        selectOrganiserButton = new JButton("Select Organiser");
        selectOrganiserButton.addActionListener(e -> openSelectStaffDialog());
        add(organisedByLabel);
        add(selectOrganiserButton);

        dateLabel = new JLabel("Tour Date: Not Selected");
        add(dateLabel);

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
     * Validates input and creates a tour booking by inserting into the Bookings, Tours,
     * and Booking_Venues tables.
     */
    private void confirmTour() {
        String institution = institutionField.getText().trim();
        String participantsStr = participantsField.getText().trim();

        if (institution.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter institution name.");
            return;
        }
        if (selectedDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a tour date from the calendar.");
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
        if (!checkTourAvailability(selectedDate)) {
            JOptionPane.showMessageDialog(this, "Tour date not available or invalid.");
            return;
        }

        int bookingID = createTourBooking(institution, selectedDate, participants, organiserStaffID);
        if (bookingID > 0) {
            JOptionPane.showMessageDialog(this, "Tour booked successfully!\nBookingID = " + bookingID);
            resetFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to book tour.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Stub method to check if the selected date is available.
     */
    private boolean checkTourAvailability(String dateStr) {
        // For now, always return true. Implement real logic as needed.
        return true;
    }

    /**
     * Inserts a new tour booking into the Bookings, Tours, and Booking_Venues tables.
     * Assumes selectedDate is in dd/MM/yyyy format and converts it to ISO format ("yyyy-MM-dd").
     * Returns the new BookingID or -1 if insertion fails.
     */
    private int createTourBooking(String institution, String dateStr, int participants, int organiser) {
        Connection conn = null;
        PreparedStatement stmtBookings = null;
        PreparedStatement stmtTours = null;
        PreparedStatement stmtBookingVenues = null;
        ResultSet generatedKeys = null;
        try {
            conn = JDBC.getConnection();
            conn.setAutoCommit(false);

            // Convert dd/MM/yyyy to ISO format "yyyy-MM-dd"
            String[] parts = dateStr.split("/");
            if (parts.length != 3) return -1;
            // Trim each part in case there are extra spaces
            String day = parts[0].trim();
            String month = parts[1].trim();
            String year = parts[2].trim();
            String isoDate = year + "-" + month + "-" + day;

            // Insert into Bookings table with BookingType = 'Tour'
            // Use a dummy ClientID (e.g., 1) that exists in the Clients table.
            String sqlBookings = "INSERT INTO Bookings (ClientID, StartDate, EndDate, BookingType, CreatedBy) " +
                    "VALUES (?, ?, ?, 'Tour', ?)";
            stmtBookings = conn.prepareStatement(sqlBookings, Statement.RETURN_GENERATED_KEYS);
            stmtBookings.setInt(1, 1);  // Dummy ClientID; ensure a Client with ID 1 exists
            stmtBookings.setDate(2, java.sql.Date.valueOf(isoDate));
            stmtBookings.setDate(3, java.sql.Date.valueOf(isoDate));
            stmtBookings.setInt(4, organiser);

            int affectedRows = stmtBookings.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return -1;
            }
            generatedKeys = stmtBookings.getGeneratedKeys();
            int newBookingID;
            if (generatedKeys.next()) {
                newBookingID = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return -1;
            }
            generatedKeys.close();
            stmtBookings.close();

            // Insert into Tours table with the new BookingID
            String sqlTours = "INSERT INTO Tours (InstitutionName, TourDate, NumberOfParticipants, OrganisedBy, BookingID) " +
                    "VALUES (?, ?, ?, ?, ?)";
            stmtTours = conn.prepareStatement(sqlTours);
            stmtTours.setString(1, institution);
            stmtTours.setDate(2, java.sql.Date.valueOf(isoDate));
            stmtTours.setInt(3, participants);
            stmtTours.setInt(4, organiser);
            stmtTours.setInt(5, newBookingID);

            affectedRows = stmtTours.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return -1;
            }
            stmtTours.close();

            // Insert into Booking_Venues with default values for tours:
            // Use dummy VenueID = 9999 and ConfigurationDetails = "LMH"
            String sqlBookingVenues = "INSERT INTO Booking_Venues (BookingID, VenueID, ConfigurationDetails) VALUES (?, ?, ?)";
            stmtBookingVenues = conn.prepareStatement(sqlBookingVenues);
            stmtBookingVenues.setInt(1, newBookingID);
            stmtBookingVenues.setInt(2, 9999); // Use dummy VenueID 9999 for tours
            stmtBookingVenues.setString(3, "LMH"); // Default configuration details "LMH"
            affectedRows = stmtBookingVenues.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return -1;
            }
            stmtBookingVenues.close();

            conn.commit();
            return newBookingID;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return -1;
        } finally {
            if (generatedKeys != null) {
                try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (stmtBookings != null) {
                try { stmtBookings.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (stmtTours != null) {
                try { stmtTours.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (stmtBookingVenues != null) {
                try { stmtBookingVenues.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Resets the form fields after a successful booking.
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
