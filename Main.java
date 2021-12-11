import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Main {
    private static String DATABASE_NAME = "lookinnabook101114541";
    private static String USERNAME = "tom107";
    private static String PASSWORD = "1072";

    public static void main(String[] args) {
        try {
            // Please change the following 3 lines as needed
            String url = String.format("jdbc:postgresql://localhost:5432/%s", DATABASE_NAME);
            Connection connection = DriverManager.getConnection(url, USERNAME, PASSWORD);
            Application app = new Application(connection);
            app.run();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}