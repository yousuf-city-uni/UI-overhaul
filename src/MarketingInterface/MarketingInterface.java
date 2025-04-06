package MarketingInterface;

import java.sql.SQLException;
import java.util.List;

public interface MarketingInterface extends AutoCloseable  {
    //seat prices and discounts
    List<String> getAllDiscounts() throws SQLException;
    boolean setDiscount(String discountType, double percentage) throws SQLException;

    //room usage
    List<String> getRoomUsage(String venueName) throws SQLException;

    //film schedule
    List<String> getFilmSchedule() throws SQLException;

    //tour schedule
    List<String> getTourSchedule() throws SQLException;

    //manage group bookings
    boolean createGroupBooking(int clientId, String startDate, String endDate) throws SQLException;

    //FofL update for box office team
    List<String> getFriendsOfLancasters() throws SQLException;

    void close() throws SQLException;
}