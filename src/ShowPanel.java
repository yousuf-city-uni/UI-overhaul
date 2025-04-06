import MarketingInterface.JDBC;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ShowPanel extends JPanel {
    private JTextField movieTitleField;
    private JComboBox<String> venueCombo; // For venue selection
    private int[] venueIDs = {101, 102, 103}; // Example venue IDs; replace with real data if available
    private String[] venueNames = {"Main Hall", "Small Hall", "Rehearsal Space"};

    private JLabel dateLabel;
    private JButton pickDateButton;
    private JComboBox<String> startTimeCombo;
    private JComboBox<String> endTimeCombo;

    private JLabel selectedClientLabel;
    private JButton selectClientButton;
    private int selectedClientId = -1;

    private JButton confirmShowButton;

    private String selectedDate = "";
    private final String[] hours = { "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00",
            "18:00", "19:00", "20:00", "21:00", "22:00" };

    public ShowPanel() {
        setLayout(new GridLayout(0, 2, 10, 10));

        // Movie Title
        add(new JLabel("Movie Title:"));
        movieTitleField = new JTextField();
        add(movieTitleField);

        // Venue Selection
        add(new JLabel("Venue:"));
        venueCombo = new JComboBox<>(venueNames);
        add(venueCombo);

        // Show Date
        add(new JLabel("Show Date:"));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateLabel = new JLabel("Not Selected");
        pickDateButton = new JButton("Pick Date");
        datePanel.add(dateLabel);
        datePanel.add(pickDateButton);
        add(datePanel);

        // Start Time
        add(new JLabel("Start Time:"));
        startTimeCombo = new JComboBox<>(hours);
        startTimeCombo.setSelectedIndex(-1);
        startTimeCombo.addActionListener(e -> updateEndTimeOptions());
        add(startTimeCombo);

        // End Time
        add(new JLabel("End Time:"));
        endTimeCombo = new JComboBox<>();
        endTimeCombo.setEnabled(false);
        add(endTimeCombo);

        // Client selection
        add(new JLabel("Client:"));
        JPanel clientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectedClientLabel = new JLabel("None");
        selectClientButton = new JButton("Select Client");
        selectClientButton.addActionListener(e -> selectClient());
        clientPanel.add(selectedClientLabel);
        clientPanel.add(selectClientButton);
        add(clientPanel);

        // Confirm Button (spanning two columns)
        add(new JLabel()); // filler
        confirmShowButton = new JButton("Confirm Show Booking");
        confirmShowButton.addActionListener(e -> confirmShowBooking());
        add(confirmShowButton);
    }

    // Updates the endTimeCombo so that only times equal to or after the selected start time are available.
    private void updateEndTimeOptions() {
        int startIndex = startTimeCombo.getSelectedIndex();
        if (startIndex >= 0) {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (int i = startIndex; i < hours.length; i++) {
                model.addElement(hours[i]);
            }
            endTimeCombo.setModel(model);
            endTimeCombo.setSelectedIndex(0);
            endTimeCombo.setEnabled(true);
        } else {
            endTimeCombo.setEnabled(false);
        }
    }

    // Opens the client selection dialog.
    private void selectClient() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        SelectClientDialog clientDialog = new SelectClientDialog(owner);
        clientDialog.setVisible(true);
        int clientId = clientDialog.getSelectedClientId();
        if (clientId != -1) {
            selectedClientId = clientId;
            selectedClientLabel.setText(String.valueOf(clientId));
        } else {
            selectedClientId = -1;
            selectedClientLabel.setText("None");
        }
    }

    // Validates input and creates a show booking.
    private void confirmShowBooking() {
        String movieTitle = movieTitleField.getText().trim();
        if (movieTitle.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a movie title.");
            return;
        }
        if (selectedDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please pick a show date.");
            return;
        }
        if (startTimeCombo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a start time.");
            return;
        }
        String startTime = (String) startTimeCombo.getSelectedItem();
        if (endTimeCombo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select an end time.");
            return;
        }
        String endTime = (String) endTimeCombo.getSelectedItem();
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int endHour = Integer.parseInt(endTime.split(":")[0]);
        if (endHour < startHour) {
            JOptionPane.showMessageDialog(this, "End time cannot be before start time.");
            return;
        }
        if (selectedClientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client.");
            return;
        }
        int venueIndex = venueCombo.getSelectedIndex();
        int venueID = venueIDs[venueIndex];

        // Build configuration details (if needed)
        String configurationDetails = "StartTime: " + startTime + ", EndTime: " + endTime;

        // Call createShowBooking to insert into the database.
        int bookingID = createShowBooking(
                selectedClientId,
                movieTitle,
                "Film",              // showType
                selectedDate,
                startTime,           // We'll use startTime as the show time
                "Show",              // bookingType
                19,                  // createdBy staff ID (example)
                configurationDetails // marketing notes or configuration details
        );

        if (bookingID > 0) {
            String receipt = "Show Booking Confirmed (BookingID: " + bookingID + "):\n" +
                    "Movie: " + movieTitle + "\n" +
                    "Date: " + selectedDate + "\n" +
                    "Time: " + startTime + " - " + endTime + "\n" +
                    "Venue: " + venueCombo.getSelectedItem() + "\n" +
                    "Client: " + selectedClientId;
            JOptionPane.showMessageDialog(this, receipt, "Booking Receipt", JOptionPane.INFORMATION_MESSAGE);
            System.out.println(receipt);
            resetFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to book show.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Resets the fields after a successful booking.
    private void resetFields() {
        movieTitleField.setText("");
        selectedDate = "";
        dateLabel.setText("Not Selected");
        startTimeCombo.setSelectedIndex(-1);
        endTimeCombo.setEnabled(false);
        selectedClientId = -1;
        selectedClientLabel.setText("None");
    }

    /**
     * Inserts a new show booking by inserting into the Bookings and Shows tables.
     * Dates are provided in dd/MM/yyyy format.
     * showTimeStr is provided as "HH:mm".
     */
    private int createShowBooking(
            int clientID,
            String title,
            String showType,
            String showDateStr,
            String showTimeStr,
            String bookingType,
            int createdByStaffID,
            String marketingNotes
    ) {
        Connection conn = null;
        PreparedStatement stmtBookings = null;
        PreparedStatement stmtShows = null;
        ResultSet generatedKeys = null;
        try {
            conn = JDBC.getConnection();
            conn.setAutoCommit(false);

            // 1) Insert into Bookings table.
            String sqlBookings = "INSERT INTO Bookings (ClientID, StartDate, EndDate, BookingType, CreatedBy) VALUES (?, ?, ?, ?, ?)";
            stmtBookings = conn.prepareStatement(sqlBookings, Statement.RETURN_GENERATED_KEYS);

            // Convert dd/MM/yyyy to yyyy-MM-dd
            String[] parts = showDateStr.split("/");
            String isoDate = parts[2] + "-" + parts[1] + "-" + parts[0];

            // For a one-day show, StartDate == EndDate.
            stmtBookings.setInt(1, clientID);
            stmtBookings.setDate(2, java.sql.Date.valueOf(isoDate));
            stmtBookings.setDate(3, java.sql.Date.valueOf(isoDate));
            stmtBookings.setString(4, bookingType);
            stmtBookings.setInt(5, createdByStaffID);

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

            // 2) Insert into Shows table with the new BookingID.
            String sqlShows = "INSERT INTO Shows (BookingID, Title, ShowType, ShowDate, ShowTime, MarketingNotes) VALUES (?, ?, ?, ?, ?, ?)";
            stmtShows = conn.prepareStatement(sqlShows);

            stmtShows.setInt(1, newBookingID);
            stmtShows.setString(2, title);
            stmtShows.setString(3, showType);
            stmtShows.setDate(4, java.sql.Date.valueOf(isoDate));
            stmtShows.setString(5, showTimeStr);
            stmtShows.setString(6, marketingNotes);

            affectedRows = stmtShows.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return -1;
            }

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
            if (stmtShows != null) {
                try { stmtShows.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}

