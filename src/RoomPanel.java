import MarketingInterface.JDBC;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomPanel extends JPanel {
    // UI components for room selection and details
    private JComboBox<String> roomCombo;
    private JTextArea roomDetailsArea;
    public JLabel getSelectedDateLabel() { return selectedDateLabel; }
    // Date selection components
    private JLabel selectedDateLabel;
    private JButton pickDateButton;
    // All-day checkbox
    private JCheckBox allDayCheckBox;
    // Time selection components
    private JComboBox<String> timeCombo;    // Start hour
    private JComboBox<String> endHourCombo;   // End hour
    // Client selection components
    private JLabel selectedClientLabel;
    private JButton selectClientButton;
    private int selectedClientId = -1;
    // Confirm booking button
    private JButton confirmButton;
    // Data storage
    private List<Venue> venues = new ArrayList<>();
    private String selectedDate = "";
    // Full list of possible hours.
    private final String[] hours = {
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00",
            "18:00", "19:00", "20:00", "21:00", "22:00"
    };

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public RoomPanel() {
        setLayout(new BorderLayout(5, 5));
        // Clear the venues list each time loadVenuesFromDatabase() is called.
        venues.clear();
        loadVenuesFromDatabase();

        JPanel topPanel = new JPanel();
        JPanel roomSelectionPanel = new JPanel();
        selectedDateLabel = new JLabel("Select Room:" + selectedDate);
        roomSelectionPanel.add(selectedDateLabel);

        String[] venueNames = venues.stream().map(Venue::getName).toArray(String[]::new);
        roomCombo = new JComboBox<>(venueNames);
        roomCombo.addActionListener(e -> updateRoomDetails(roomCombo.getSelectedIndex()));
        roomSelectionPanel.add(roomCombo);

        topPanel.add(roomSelectionPanel, BorderLayout.NORTH);

        // Text area for venue details
        roomDetailsArea = new JTextArea();
        roomDetailsArea.setEditable(false);
        roomDetailsArea.setLineWrap(true);
        roomDetailsArea.setWrapStyleWord(true);
        roomDetailsArea.setColumns(1);
        roomDetailsArea.setRows(7);
        roomDetailsArea.setBackground(Color.PINK);

        if (!venues.isEmpty()) {
            updateRoomDetails(0);
        }

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(roomDetailsArea, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new GridLayout(0, 1, 1, 1));
        JPanel datePanel = new JPanel();
        selectedDateLabel = new JLabel("Selected Date: " + selectedDate);
        datePanel.add(selectedDateLabel);
        bottomPanel.add(datePanel);

        // All Day panel
        JPanel allDayPanel = new JPanel();
        allDayCheckBox = new JCheckBox("All Day Booking");
        allDayCheckBox.addActionListener(e -> toggleAllDay());
        allDayPanel.add(allDayCheckBox);
        bottomPanel.add(allDayPanel);

        // Start Hour panel
        JPanel startTimePanel = new JPanel();
        startTimePanel.add(new JLabel("Start Hour:"));
        timeCombo = new JComboBox<>(hours);
        timeCombo.setSelectedIndex(-1);
        timeCombo.addActionListener(e -> updateEndHourOptions());
        startTimePanel.add(timeCombo);
        bottomPanel.add(startTimePanel);

        // End Hour panel
        JPanel endTimePanel = new JPanel();
        endTimePanel.add(new JLabel("End Hour:"));
        endHourCombo = new JComboBox<>();
        endHourCombo.setEnabled(false);
        endTimePanel.add(endHourCombo);
        bottomPanel.add(endTimePanel);

        // Client selection panel
        JPanel clientPanel = new JPanel();
        selectedClientLabel = new JLabel("Selected Client: None");
        selectClientButton = new JButton("Select Client");
        selectClientButton.setPreferredSize(new Dimension(150, 30));
        selectClientButton.addActionListener(e -> openSelectClientDialog());
        clientPanel.add(selectedClientLabel);
        clientPanel.add(selectClientButton);
        bottomPanel.add(clientPanel);

        // Confirm booking button panel
        JPanel confirmPanel = new JPanel();
        confirmButton = new JButton("Confirm Booking");
        confirmButton.addActionListener(e -> performBooking());
        confirmPanel.add(confirmButton);
        bottomPanel.add(confirmPanel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Toggles time selection based on whether All Day is checked
    private void toggleAllDay() {
        boolean allDay = allDayCheckBox.isSelected();
        timeCombo.setEnabled(!allDay);
        endHourCombo.setEnabled(!allDay);
    }

    // Load venues from the database
    private void loadVenuesFromDatabase() {
        venues.clear();
        String sql = "SELECT VenueID, Name, Type, Capacity, SeatingConfiguration, HourlyRate, AllDayRate " +
                "FROM Venues WHERE VenueID NOT IN (9999, 101, 102, 103)";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Venue v = new Venue();
                v.setVenueID(rs.getInt("VenueID"));
                v.setName(rs.getString("Name"));
                v.setType(rs.getString("Type"));
                v.setCapacity(rs.getInt("Capacity"));
                v.setSeatingConfiguration(rs.getString("SeatingConfiguration"));
                v.setHourlyRate(rs.getDouble("HourlyRate"));
                v.setAllDayRate(rs.getDouble("AllDayRate"));
                venues.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update the details area based on selected venue.
    private void updateRoomDetails(int index) {
        if (index >= 0 && index < venues.size()) {
            Venue selectedVenue = venues.get(index);
            String details = selectedVenue.getName() + "\n" +
                    "Capacity: " + selectedVenue.getCapacity() + "\n" +
                    "Seating Config: " + selectedVenue.getSeatingConfiguration() + "\n" +
                    "Hourly Rate: £" + selectedVenue.getHourlyRate() + "\n" +
                    "All Day Rate: £" + selectedVenue.getAllDayRate();
            roomDetailsArea.setText(details);
        }
    }

    // Update end-hour options based on start-hour selection.
    private void updateEndHourOptions() {
        int startIndex = timeCombo.getSelectedIndex();
        if (startIndex >= 0) {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (int i = startIndex; i < hours.length; i++) {
                model.addElement(hours[i]);
            }
            endHourCombo.setModel(model);
            endHourCombo.setSelectedIndex(0);
            endHourCombo.setEnabled(true);
        } else {
            endHourCombo.setEnabled(false);
        }
    }

    // Open client selection dialog.
    private void openSelectClientDialog() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        SelectClientDialog clientDialog = new SelectClientDialog(owner);
        clientDialog.setVisible(true);
        int clientId = clientDialog.getSelectedClientId();
        if (clientId != -1) {
            selectedClientId = clientId;
            selectedClientLabel.setText("Selected Client: " + clientId);
        } else {
            selectedClientId = -1;
            selectedClientLabel.setText("Selected Client: None");
        }
    }

    // Perform booking logic.
    private void performBooking() {
        if (selectedDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a date.");
            return;
        }
        if (selectedClientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client.");
            return;
        }
        int index = roomCombo.getSelectedIndex();
        if (index < 0 || index >= venues.size()) {
            JOptionPane.showMessageDialog(this, "Please select a valid room.");
            return;
        }
        Venue selectedVenue = venues.get(index);
        boolean allDay = allDayCheckBox.isSelected();
        double basePrice = 0.0;
        String configDetails = "";
        if (allDay) {
            basePrice = selectedVenue.getAllDayRate();
            configDetails = "All Day Booking";
        } else {
            String startHourStr = (String) timeCombo.getSelectedItem();
            String endHourStr = (String) endHourCombo.getSelectedItem();
            if (startHourStr == null || endHourStr == null) {
                JOptionPane.showMessageDialog(this, "Please select start and end hours.");
                return;
            }
            int startHour = Integer.parseInt(startHourStr.split(":")[0]);
            int endHour = Integer.parseInt(endHourStr.split(":")[0]);
            if (endHour < startHour) {
                JOptionPane.showMessageDialog(this, "End hour cannot be before start hour.");
                return;
            }
            int duration = endHour - startHour;
            if (duration <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid duration.");
                return;
            }
            basePrice = selectedVenue.getHourlyRate() * duration;
            configDetails = "StartHour: " + startHourStr + ", EndHour: " + endHourStr;
        }
        double vat = basePrice * 0.20; // 20% VAT
        double total = basePrice + vat;
        int bookingID = createBooking(
                selectedClientId,
                selectedDate,
                selectedDate,
                "Meeting",
                "19",
                selectedVenue.getVenueID(),
                configDetails,
                basePrice,
                vat,
                total
        );
        if (bookingID > 0) {
            String receipt = "Booking Confirmed (ID: " + bookingID + "):\n" +
                    "Room: " + selectedVenue.getName() + "\n" +
                    "Date: " + selectedDate + "\n" +
                    (allDay ? "All Day" : configDetails) + "\n" +
                    String.format("Base Price: £%.2f, VAT: £%.2f, Total: £%.2f", basePrice, vat, total);
            JOptionPane.showMessageDialog(this, receipt, "Booking Receipt", JOptionPane.INFORMATION_MESSAGE);
            System.out.println(receipt);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to insert booking.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inserts a new booking into the Bookings, Booking_Venues, and Invoices tables.
     * Dates are in dd/MM/yyyy format. basePrice, vat, total are computed by performBooking().
     */
    private int createBooking(
            int clientID,
            String startDateStr,
            String endDateStr,
            String bookingType,
            String createdBy,
            int venueID,
            String configurationDetails,
            double basePrice,
            double vat,
            double total
    ) {
        Connection conn = null;
        PreparedStatement stmtBookings = null;
        PreparedStatement stmtBookingVenues = null;
        PreparedStatement stmtInvoice = null;
        ResultSet generatedKeys = null;
        try {
            conn = JDBC.getConnection();
            conn.setAutoCommit(false);
            String sqlBookings = "INSERT INTO Bookings (ClientID, StartDate, EndDate, BookingType, CreatedBy) VALUES (?, ?, ?, ?, ?)";
            stmtBookings = conn.prepareStatement(sqlBookings, Statement.RETURN_GENERATED_KEYS);
            String[] startParts = startDateStr.split("/");
            String isoStart = startParts[2] + "-" + startParts[1] + "-" + startParts[0];
            String[] endParts = endDateStr.split("/");
            String isoEnd = endParts[2] + "-" + endParts[1] + "-" + endParts[0];
            stmtBookings.setInt(1, clientID);
            stmtBookings.setDate(2, java.sql.Date.valueOf(isoStart));
            stmtBookings.setDate(3, java.sql.Date.valueOf(isoEnd));
            stmtBookings.setString(4, bookingType);
            stmtBookings.setString(5, createdBy);
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
            String sqlBookingVenues = "INSERT INTO Booking_Venues (BookingID, VenueID, ConfigurationDetails) VALUES (?, ?, ?)";
            stmtBookingVenues = conn.prepareStatement(sqlBookingVenues);
            stmtBookingVenues.setInt(1, newBookingID);
            stmtBookingVenues.setInt(2, venueID);
            stmtBookingVenues.setString(3, configurationDetails);
            int affectedRows2 = stmtBookingVenues.executeUpdate();
            if (affectedRows2 == 0) {
                conn.rollback();
                return -1;
            }
            stmtBookingVenues.close();
            String sqlInvoice = "INSERT INTO Invoices (InvoiceDate, Costs, Total, BookingID, ClientID) VALUES (?, ?, ?, ?, ?)";
            stmtInvoice = conn.prepareStatement(sqlInvoice);
            stmtInvoice.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            stmtInvoice.setDouble(2, basePrice);
            stmtInvoice.setDouble(3, total);
            stmtInvoice.setInt(4, newBookingID);
            stmtInvoice.setInt(5, clientID);
            int affectedRows3 = stmtInvoice.executeUpdate();
            if (affectedRows3 == 0) {
                conn.rollback();
                return -1;
            }
            stmtInvoice.close();
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
                try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
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
