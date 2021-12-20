public class Main {
    /**
     * Please modify these constants to connect to your local Postgresql Server
     */
    private static final String PORT = "5434";
    private static final String DATABASE_NAME = "lookinnabook101114541";
    private static final String USERNAME = "tom107";
    private static final String PASSWORD = "1072";

    /**
     * Program entry
     */
    public static void main(String[] args) {
        Application app = new Application(PORT, DATABASE_NAME, USERNAME, PASSWORD);
        app.run();
    }
}