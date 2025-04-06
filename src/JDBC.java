import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC{
    private static final String URL = "jdbc:mysql://sst-stuproj.city.ac.uk:3306/in2033t30?useSSL=false&serverTimezone=UTC";
    private static final String USER = "in2033t30_d";
    private static final String PASSWORD = "yIxb1LGjdSg";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public JDBC(){

    }
    //public static void main(String[] args) {
    //    try (Connection conn = JDBC.getConnection()) {
    //        if (conn != null) {
    //            System.out.println("Successfully connected to the database.");
    //        } else {
    //            System.out.println("Failed to connect to the database.");
    //        }
    //    } catch (SQLException e) {
    //        e.printStackTrace();
    //    }
    //}
}
