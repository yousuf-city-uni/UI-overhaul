import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.awt.event.*;

public class BookingMenu extends JPanel {
    private final JDBC jdbc;
    private JLabel selectedDateLabel;
    private final Palette palette;
    private final JTabbedPane tabbedPane;
    private final JLabel headerLabel;
    private final Scene scene;
    private final JFrame frame;
    private JButton clearFilterButton;
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JButton removeButton;
    private JButton refreshButton;
    private String selectedDate = "";

    public void setMainMenu(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    private MainMenu mainMenu;
    private RoomPanel roomPanel;
    private TourPanel tourPanel;
    private ShowPanel showPanel;
    private StringReceiver receiver = new StringReceiver();
    private final CustomCalendar calendar;
    private JComboBox<String> bookingTypeCombo;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public BookingMenu(Palette palette, Scene scene, JFrame frame, JDBC jdbc) {
        this.palette = palette;
        this.scene = scene;
        this.frame = frame;
        this.jdbc = jdbc;


        roomPanel = new RoomPanel();
        tourPanel = new TourPanel();
        showPanel = new ShowPanel();
        calendar  = new CustomCalendar(receiver, roomPanel, showPanel, tourPanel);

        this.setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new CustomTabbedPaneUI(palette));
        tabbedPane.setBackground(palette.getBackgroundDark());
        tabbedPane.setForeground(palette.getTextColor());

        headerLabel = new JLabel("Bookings", SwingConstants.CENTER);
        headerLabel.setForeground(palette.getTextColor());
        headerLabel.setOpaque(false);
        adjustHeaderFont();

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setOpaque(false);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton returnButton = new JButton("Menu");

        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.remove(BookingMenu.this);
                frame.add(mainMenu);
                scene.setScene("Menu");
                frame.revalidate();
                frame.repaint();
            }
        });

        headerPanel.add(returnButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        JPanel bookingTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookingTypePanel.setOpaque(false);
        JLabel bookingTypeLabel = new JLabel("Booking Type:");
        bookingTypeCombo = new JComboBox<>(new String[]{"Room", "Tour", "Show"});
        bookingTypePanel.add(bookingTypeLabel);
        bookingTypePanel.add(bookingTypeCombo);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(roomPanel, "Room");
        cardPanel.add(tourPanel, "Tour");
        cardPanel.add(showPanel, "Show");

        bookingTypeCombo.addActionListener(e -> {
            String selected = (String) bookingTypeCombo.getSelectedItem();
            cardLayout.show(cardPanel, selected);
        });

        JPanel tabbedPaneContainer = new JPanel(new BorderLayout());
        tabbedPaneContainer.setOpaque(false);

        JPanel addBookingPanel = new JPanel(new BorderLayout());
        addBookingPanel.setOpaque(false);
        addBookingPanel.add(bookingTypePanel, BorderLayout.NORTH);
        addBookingPanel.add(cardPanel, BorderLayout.CENTER);


        JPanel bookingRemove = new JPanel();
        JPanel removeBookingPanel = new JPanel(new BorderLayout());
        selectedDateLabel = new JLabel("Selected Date: " + selectedDate);
        removeBookingPanel.add(selectedDateLabel);
        tableModel = new DefaultTableModel(new Object[]{"BookingID", "Venue Name", "VenueID", "ClientID", "BookingType", "StartDate", "EndDate"}, 0);
        bookingTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bookingTable);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadBookings());
        removeButton = new JButton("Remove Booking");
        removeButton.addActionListener(e -> removeSelectedBooking());
        bookingRemove.add(scrollPane, BorderLayout.CENTER);
        bookingRemove.add(refreshButton);
        bookingRemove.add(removeButton);
        removeBookingPanel.add(bookingRemove);


        tabbedPane.addTab("Add Booking", addBookingPanel);
        tabbedPane.addTab("Remove Booking", removeBookingPanel);
        tabbedPaneContainer.add(tabbedPane, BorderLayout.CENTER);

        loadBookings();

        leftPanel.add(headerPanel);
        leftPanel.add(tabbedPaneContainer);

        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        rightPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                rightPanel.repaint();
            }
        });
        rightPanel.setBackground(palette.getBackground());
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(calendar, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(0);
        splitPane.setOpaque(true);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);

        splitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int totalWidth = splitPane.getWidth();
                splitPane.setDividerLocation(totalWidth / 2);
            }
        });

        this.add(splitPane, BorderLayout.CENTER);
    }

    private void adjustHeaderFont() {
        int width = getWidth();
        int fontSize = Math.max(20, width / 20);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, fontSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(palette.getBackgroundDark());
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private static class CustomTabbedPaneUI extends BasicTabbedPaneUI {
        private final Palette palette;

        public CustomTabbedPaneUI(Palette palette) {
            this.palette = palette;
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected ? palette.getGreen() : palette.getBackgroundDark());
            g2.fillRect(x, y, w, h);
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // No border
        }
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        String sql;
        if (selectedDate.isEmpty()) {
            sql = "SELECT b.BookingID, v.Name as VenueName, v.VenueID, b.ClientID, b.BookingType, b.StartDate, b.EndDate " +
                    "FROM Bookings b " +
                    "JOIN Booking_Venues bv ON b.BookingID = bv.BookingID " +
                    "JOIN Venues v ON bv.VenueID = v.VenueID " +
                    "WHERE b.StartDate >= CURDATE() " +
                    "ORDER BY b.StartDate";
        } else {
            String[] parts = selectedDate.split("/");
            String isoDate = parts[2] + "-" + parts[1] + "-" + parts[0];
            sql = "SELECT b.BookingID, v.Name as VenueName, v.VenueID, b.ClientID, b.BookingType, b.StartDate, b.EndDate " +
                    "FROM Bookings b " +
                    "JOIN Booking_Venues bv ON b.BookingID = bv.BookingID " +
                    "JOIN Venues v ON bv.VenueID = v.VenueID " +
                    "WHERE b.StartDate = ? AND b.StartDate >= CURDATE() " +
                    "ORDER BY b.StartDate";
        }
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (!selectedDate.isEmpty()) {
                String[] parts = selectedDate.split("/");
                String isoDate = parts[2] + "-" + parts[1] + "-" + parts[0];
                stmt.setDate(1, java.sql.Date.valueOf(isoDate));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("BookingID"));
                row.add(rs.getString("VenueName"));
                row.add(rs.getInt("VenueID"));
                row.add(rs.getInt("ClientID"));
                row.add(rs.getString("BookingType"));
                row.add(rs.getDate("StartDate"));
                row.add(rs.getDate("EndDate"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to remove.");
            return;
        }
        int bookingID = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove booking ID " + bookingID + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = jdbc.getConnection()) {
            conn.setAutoCommit(false);

            // Delete from Invoices first (child table)
            String deleteInvoices = "DELETE FROM Invoices WHERE BookingID = ?";
            try (PreparedStatement psInv = conn.prepareStatement(deleteInvoices)) {
                psInv.setInt(1, bookingID);
                psInv.executeUpdate();
            }

            // Then delete from Booking_Venues
            String deleteBookingVenues = "DELETE FROM Booking_Venues WHERE BookingID = ?";
            try (PreparedStatement psBV = conn.prepareStatement(deleteBookingVenues)) {
                psBV.setInt(1, bookingID);
                psBV.executeUpdate();
            }

            // Finally, delete from Bookings (parent table)
            String deleteBooking = "DELETE FROM Bookings WHERE BookingID = ?";
            try (PreparedStatement psBook = conn.prepareStatement(deleteBooking)) {
                psBook.setInt(1, bookingID);
                int affectedRows = psBook.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Failed to remove booking.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Booking removed successfully.");
            loadBookings();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}