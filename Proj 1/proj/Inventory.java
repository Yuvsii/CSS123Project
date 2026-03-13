package proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// This class acts as our data access object
// All SQL logic is isolated here para clean yung MainFrame GUI. 

public class Inventory {
	
	// A local copy of the database
    private List<Product> cachedProducts;

    public Inventory() {
        this.cachedProducts = new ArrayList<>();
    }

    public void addProduct(Product product) {
    	// PREPARED STATEMENT: Using '?' prevents SQL Injection
        // If a user types malicious code into the product description, 
        // the PreparedStatement treats it strictly as text, not executable SQL
        String sql = "INSERT INTO products (id, name, price, quantity, description) VALUES (?, ?, ?, ?, ?)";
        // Automatically closes the database connection when done preventing memory leaks.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        	// Binding the actual data to the '?' placeholders
            stmt.setInt(1, product.getProductId());
            stmt.setString(2, product.getProductName());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.setString(5, product.getDescription());
            stmt.executeUpdate(); // Executes the INSERT command on sql
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // Other operations (updateFullProduct, updateProductQuantity, removeProduct)
    // This method takes the updated details from the GUI and pushes them to MySQL
    public void updateFullProduct(int id, String name, double price, int qty, String desc) {
        String sql = "UPDATE products SET name=?, price=?, quantity=?, description=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); // Opens the connection and prepares the statement
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        	// Replacing the '?' marks with the actual variables
            // The numbers (1, 2, 3...) correspond to the order of the '?' in the SQL string.
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, qty);
            stmt.setString(4, desc);
            stmt.setInt(5, id);
            stmt.executeUpdate(); // Sends the finalized command to the MySQL server
        } catch (SQLException e) { e.printStackTrace(); } // If the database is down, it prints the error to the console
    }
    
    // Handles the database side of a purchase
    public void sellProduct(int id, int qtyToSell) throws ProductNotFoundException, InvalidQuantityException {
        Product found = null;
        // Fetch the latest stock count directly from the database 
        // (to ensure we don't rely on an outdated cached list)
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                found = new Product(rs.getInt("id"), rs.getString("name"), 
                                    rs.getDouble("price"), rs.getInt("quantity"), rs.getString("description"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // If they fail, throw our custom exceptions
        // to alert the UI that the transaction must be aborted.
        if (found == null) throw new ProductNotFoundException("Product ID " + id + " not found!");
        if (found.getQuantity() < qtyToSell) throw new InvalidQuantityException("Not enough stock!");

        updateProductQuantity(id, found.getQuantity() - qtyToSell);
    }
    
    // Helper update method
    // Private because only the sellProduct method needs to use this internally
    private void updateProductQuantity(int id, int quantity) {
        String sql = "UPDATE products SET quantity=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // Fetches every row in the table to populate the UI
    public List<Product> getProductsList() {
        cachedProducts.clear(); // Empty the old list to avoid duplicates
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
        	// Loop through every single row returned by the database
            while (rs.next()) {
            	// Add each row as a new Product object into our ArrayList
                cachedProducts.add(new Product(rs.getInt("id"), rs.getString("name"),
                                   rs.getDouble("price"), rs.getInt("quantity"), rs.getString("description")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return cachedProducts;
    }
    
    // To delete item in sql and in gui
    public void removeProduct(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Finds a specific product in our local memory using its exact ID
    // This protects us from JTable row index mismatches
    public Product getProductAt(int index) {
        if (index >= 0 && index < cachedProducts.size()) return cachedProducts.get(index);
        return null;
    }
}