package MarketingInterface;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarketingDB_ForBoxOffice implements MarketingInterface {

    @Override
    public List<String> getAllDiscounts() throws SQLException {
        List<String> discounts = new ArrayList<>();
        String sql = "SELECT DiscountType, Percentage FROM Discounts";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String discountType = rs.getString("DiscountType");
                double percentage = rs.getDouble("Percentage");
                discounts.add(discountType + ": " + percentage + "%");
            }
        }
        return discounts;
    }

    @Override
    public boolean setDiscount(String discountType, double percentage) throws SQLException {
        String sql = "UPDATE Discounts SET Percentage = ? WHERE DiscountType = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, percentage);
            stmt.setString(2, discountType);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<String> getRoomUsage(String venueName) throws SQLException {
        List<String> usage = new ArrayList<>();
        String sql = """
            SELECT b.BookingID, b.StartDate, b.EndDate
            FROM Bookings b
            JOIN Booking_Venues bv ON b.BookingID = bv.BookingID
            JOIN Venues v ON bv.VenueID = v.VenueID
            WHERE v.Name = ?
        """;
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, venueName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int bookingID = rs.getInt("BookingID");
                    Date start = rs.getDate("StartDate");
                    Date end = rs.getDate("EndDate");
                    usage.add("Booking " + bookingID + ": " + start + " to " + end);
                }
            }
        }
        return usage;
    }

    @Override
    public List<String> getFilmSchedule() throws SQLException {
        List<String> films = new ArrayList<>();
        String sql = "SELECT FilmTitle, OrderDate FROM FilmOrders";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String title = rs.getString("FilmTitle");
                Date date = rs.getDate("OrderDate");
                films.add(title + " scheduled on " + date);
            }
        }
        return films;
    }

    @Override
    public List<String> getTourSchedule() throws SQLException {
        List<String> tours = new ArrayList<>();
        String sql = "SELECT InstitutionName, TourDate FROM Tours";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String institution = rs.getString("InstitutionName");
                Date tourDate = rs.getDate("TourDate");
                tours.add(institution + " on " + tourDate);
            }
        }
        return tours;
    }

    @Override
    public boolean createGroupBooking(int clientId, String startDate, String endDate) throws SQLException {
        String sql = "INSERT INTO Bookings (ClientID, StartDate, EndDate, BookingType, CreatedBy) VALUES (?, ?, ?, 'Group', 1)";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<String> getFriendsOfLancasters() throws SQLException {
        List<String> friends = new ArrayList<>();
        String sql = "SELECT Name, Email FROM FriendsOfLancasters";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("Name");
                String email = rs.getString("Email");
                friends.add(name + " <" + email + ">");
            }
        }
        return friends;
    }

    @Override
    public void close() throws SQLException {
        // No persistent connection to close
    }

    public static void main(String[] args) {
        try (MarketingInterface marketingDB = new MarketingDB_ForBoxOffice()) {
            System.out.println("Discounts: " + marketingDB.getAllDiscounts());
            System.out.println("Film Schedule: " + marketingDB.getFilmSchedule());
            System.out.println("Tour Schedule: " + marketingDB.getTourSchedule());
            System.out.println("Room Usage for 'Main Hall': " + marketingDB.getRoomUsage("Main Hall"));
            System.out.println("Group Booking created? " + marketingDB.createGroupBooking(1, "2025-05-01", "2025-05-02"));
            System.out.println("Friends of Lancaster's: " + marketingDB.getFriendsOfLancasters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
