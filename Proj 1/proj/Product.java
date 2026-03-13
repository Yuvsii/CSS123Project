package proj;

public class Product { // Encapuslated can not be directly access 
    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private String description; 

    public Product(int productId, String productName, double price, int quantity, String description) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
    }

    // Getters the only way to access to read the data
    
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getDescription() { return description; } 
    
    // Setters the only way to access for editing
    
    public void setPrice(double price) { this.price = price; }
    public void updateQuantity(int quantity) { this.quantity = quantity; }
    public void setProductName(String name) { this.productName = name; }
    public void setDescription(String description) { this.description = description; } 
    
    // Helper method used by the JTable to easily display data in rows
    
    public Object[] toRow() {
        return new Object[]{productId, productName, String.format("₱ %.2f", price), quantity};
    }
    
    @Override
    public String toString() {
        return productName;
    }
}