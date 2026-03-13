package proj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
//The connection

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String USER = "root"; 
    private static final String PASS = "4kekh9cr60g1cl2h"; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Test if the data base is connected or not
    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                System.out.println("Connected to Database! ");
            }
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            System.out.println("Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }
}