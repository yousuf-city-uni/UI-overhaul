import MarketingInterface.JDBC;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ShowPanel extends JPanel {
    private JTextField movieTitleField;
    private JComboBox<String> venueCombo;
    private int[] venueIDs = {101, 102, 103};
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
    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }
    private final String[] hours = { "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00",
            "18:00", "19:00", "20:00", "21:00", "22:00" };

    public ShowPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.LINE_END;

        // Movie Title Row
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Movie Title:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        movieTitleField = new JTextField(20);
        add(movieTitleField, gbc);

        // Venue Selection Row
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Venue:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        venueCombo = new JComboBox<>(venueNames);
        venueCombo.setPrototypeDisplayValue("Rehearsal Space");
        add(venueCombo, gbc);

        // Show Date Row
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Show Date:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dateLabel = new JLabel("Not Selected");
        pickDateButton = new JButton("Pick Date");
        datePanel.add(dateLabel);
        datePanel.add(Box.createHorizontalStrut(5));
        datePanel.add(pickDateButton);
        add(datePanel, gbc);

        // Start Time Row
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Start Time:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        startTimeCombo = new JComboBox<>(hours);
        startTimeCombo.setSelectedIndex(-1);
        startTimeCombo.setPrototypeDisplayValue("22:00");
        startTimeCombo.addActionListener(e -> updateEndTimeOptions());
        add(startTimeCombo, gbc);

        // End Time Row
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("End Time:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_START;
        endTimeCombo = new JComboBox<>();
        endTimeCombo.setEnabled(false);
        endTimeCombo.setPrototypeDisplayValue("22:00");
        add(endTimeCombo, gbc);

        // Client Selection Row
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Client:"), gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.LINE_START;
        JPanel clientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        selectedClientLabel = new JLabel("None");
        selectClientButton = new JButton("Select Client");
        selectClientButton.addActionListener(e -> selectClient());
        clientPanel.add(selectedClientLabel);
        clientPanel.add(Box.createHorizontalStrut(5));
        clientPanel.add(selectClientButton);
        add(clientPanel, gbc);

        // Confirm Button Row
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        confirmShowButton = new JButton("Confirm Show Booking");
        confirmShowButton.addActionListener(e -> confirmShowBooking());
        add(confirmShowButton, gbc);

        // Reset constraints for any future additions
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
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
        int venueID = venueIDs[venueIndex];  // from your predefined array in ShowPanel
        String configurationDetails = "StartTime: " + startTime + ", EndTime: " + endTime;

        int bookingID = createShowBooking(
                selectedClientId,       // clientID
                movieTitle,             // title
                "Film",                 // showType
                selectedDate,           // showDateStr (dd/MM/yyyy)
                startTime,              // showTimeStr ("HH:mm")
                "Show",                 // bookingType
                19,                     // createdByStaffID (example)
                configurationDetails,   // marketingNotes
                venueID,                // venueID (new parameter)
                configurationDetails    // configurationDetails (new parameter)
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
    /**
     * Inserts a new show booking by inserting into the Bookings, Shows, and Booking_Venues tables.
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
            String marketingNotes,
            int venueID,                   // Add venueID parameter (from venueCombo)
            String configurationDetails    // Optional, e.g. configuration details from the UI
    ) {
        Connection conn = null;
        PreparedStatement stmtBookings = null;
        PreparedStatement stmtShows = null;
        PreparedStatement stmtBookingVenues = null;
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
            stmtBookings.close();

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
            stmtShows.close();

            // 3) Insert into Booking_Venues to store the selected venue for the show.
            String sqlBookingVenues = "INSERT INTO Booking_Venues (BookingID, VenueID, ConfigurationDetails) VALUES (?, ?, ?)";
            stmtBookingVenues = conn.prepareStatement(sqlBookingVenues);
            stmtBookingVenues.setInt(1, newBookingID);
            stmtBookingVenues.setInt(2, venueID);  // Venue ID from the venueCombo in ShowPanel
            stmtBookingVenues.setString(3, configurationDetails);
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
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return -1;
        } finally {
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }}


