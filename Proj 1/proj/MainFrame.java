package proj;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// The main window of the application
public class MainFrame extends JFrame {

    private Inventory inventory; // The bridge to the database
    private User currentUser;  // Stores whether we are Admin or Customer 
    private JTable table;
    private DefaultTableModel tableModel; // Manages the row/column data for the JTable
    private JSpinner qtySpinner;
    private JLabel lblName, lblId, lblPrice, lblStock, lblUserInfo;
    private JTextArea txtDesc; 
    private JButton btnAdd, btnEdit, btnBuy;
    private JPanel rightPanel;

    public MainFrame() {
        inventory = new Inventory();
        
        // Starts the login popup before showing the main window
        boolean loggedIn = showLoginDialog();
        
        if (loggedIn) {
            initComponents(); // Draw the window
            applyUserPermissions(); // Lock/Unlock buttons based on role
            refreshTable(); // Load data from MySQL
            setVisible(true); // Finally, show the window to the user
        } else {
            System.exit(0); // Kill the program if they cancel the login
        }
    }
    
    // Login Logic
    private boolean showLoginDialog() {
        String[] options = {"Customer", "Admin"};
        // Shows a popup asking them to pick a role. Returns 0 for Customer, 1 for Admin
        int choice = JOptionPane.showOptionDialog(null, "Select User Mode:", "System Login",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) { // Customer Choice
            String name = JOptionPane.showInputDialog("Enter your Name:");
            if (name == null || name.trim().isEmpty()) return false;
           
            currentUser = new Customer(1, name, 0); 
            return true;
        } else if (choice == 1) { // Admin Choice
            // Password prompt for Admin
            JPasswordField pwdField = new JPasswordField();
            int action = JOptionPane.showConfirmDialog(null, pwdField, "Enter Admin Password", JOptionPane.OK_CANCEL_OPTION);
            
            if (action == JOptionPane.OK_OPTION) {
            	// Extract password from the secure field
                String enteredPwd = new String(pwdField.getPassword());
                // Hardcoded password check 
                if ("admin123".equals(enteredPwd)) {
                	// Create an Admin object
                    currentUser = new Admin(999, "Administrator");
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect Password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                return false; // Cancel pressed
            }
        }
        return false;
    }

    
    // Checks what subclass the currentUser is, and configures the UI
    private void applyUserPermissions() {
    	// 'instanceof' checks if the User object is actually an Admin in memory
        if (currentUser instanceof Admin) {
            btnAdd.setEnabled(true);
            btnEdit.setEnabled(true);
            btnBuy.setEnabled(false); // Admins can't buy things
            lblUserInfo.setText("Logged in as: ADMIN");
            lblUserInfo.setForeground(Color.RED);
        } else if (currentUser instanceof Customer) {
            btnAdd.setEnabled(false); // Customers can't add items
            btnEdit.setEnabled(false); // Customers can't edit items
            btnBuy.setEnabled(true);
            
         // Downcast the User to a Customer so we can access getBudget()
            Customer c = (Customer) currentUser;
            lblUserInfo.setText("Customer: " + c.getName() + " | Wallet: ₱" + String.format("%.2f", c.getBudget()));
            lblUserInfo.setForeground(new Color(0, 100, 0));
        }
    }

    // Center and North Panels
    private void initComponents() {
        setTitle(" Inventa Inventory Management System"); 
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     // BorderLayout divides the screen into North, South, East, West, Center
        setLayout(new BorderLayout());

     // Top panel the user info and add Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        topPanel.setBackground(new Color(245, 245, 245));

        lblUserInfo = new JLabel("User Info");
        lblUserInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(lblUserInfo, BorderLayout.WEST);

        btnAdd = new JButton("+ Add New Item");
        btnAdd.setBackground(new Color(0, 120, 215)); 
        btnAdd.setForeground(Color.WHITE);
     // Lambda listener when clicked run showProductDialog with 'null' meaning new item
        btnAdd.addActionListener(e -> showProductDialog(null)); 
        topPanel.add(btnAdd, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH); // Attach to top of window

        // Center panel aka the table 
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 20, 20, 20)); 

        String[] columns = {"ID", "Product Name", "Price", "Quantity"};
        // Custom TableModel to prevent users from double-clicking cells to edit them
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(35);
        // Listener it detects when a user clicks a row in the table
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
        	// isValueAdjusting so we don't accidentally trigger the action multiple times while they are still pressing down
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) updateRightPanel(table.getSelectedRow());
        });
        
        // Adds a scrollbar if the table gets too long
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Attach the side panel
        initRightPanel();
        add(rightPanel, BorderLayout.EAST);
    }
    
    // East Panel
    private void initRightPanel() {
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(320, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(30, 25, 30, 25));
        
        // Top section for Name, ID, Stock
        JPanel topBox = new JPanel(new GridLayout(4, 1, 0, 10));
        topBox.setBackground(Color.WHITE);
        
        lblName = new JLabel("Select Item");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblId = new JLabel("ID: ---");
        lblStock = new JLabel("Qty: ---");
        
        topBox.add(lblName); 
        topBox.add(lblId); 
        topBox.add(lblStock);
        rightPanel.add(topBox, BorderLayout.NORTH);
        
        // Middle section for Description
        txtDesc = new JTextArea("Select a product to view details...");
        txtDesc.setLineWrap(true); // Prevent text going off screen
        txtDesc.setWrapStyleWord(true); // Break lines at word spaces not mid-word
        txtDesc.setEditable(false);
        txtDesc.setBackground(new Color(250, 250, 250));
        txtDesc.setBorder(new EmptyBorder(10, 10, 10, 10));
        rightPanel.add(txtDesc, BorderLayout.CENTER);
        
        // Bottom section for Price and Buttons
        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setBackground(Color.WHITE);
        
        lblPrice = new JLabel("₱ 0.00", SwingConstants.RIGHT);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblPrice.setForeground(new Color(0, 100, 200)); 
       
        
        // Expanded to 4 rows to fit Buy, Top Up, Edit, and Remove buttons
        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        btnPanel.setBackground(Color.WHITE);

        // Quantity selector for customers
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        qtySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        qtySpinner.setBorder(BorderFactory.createTitledBorder("Quantity"));
        btnPanel.add(qtySpinner);
        
        // Buy button
        btnBuy = new JButton("Buy Item");
        btnBuy.setBackground(new Color(0, 150, 0)); 
        btnBuy.setForeground(Color.WHITE);
        btnBuy.addActionListener(e -> processSale()); 

        // Top Up button for customers
        JButton btnTopUp = new JButton("Top Up Wallet");
        btnTopUp.setBackground(new Color(0, 120, 215));
        btnTopUp.setForeground(Color.WHITE);
        btnTopUp.addActionListener(e -> {
            if (currentUser instanceof Customer) {
                Customer cust = (Customer) currentUser; // Downcast
                String input = JOptionPane.showInputDialog(this, "Enter amount to top up:");
                if (input != null) {
                    try {
                        double amount = Double.parseDouble(input);// Convert string to double
                        if (amount > 0) {
                            cust.addBudget(amount); // Add to object state
                            JOptionPane.showMessageDialog(this, "Wallet topped up successfully!");
                            // Update the UI label
                            lblUserInfo.setText("Customer: " + cust.getName() + " | Wallet: ₱" + String.format("%.2f", cust.getBudget()));
                        } else {
                            JOptionPane.showMessageDialog(this, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) { // If they typed letters instead of numbers
                        JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Edit button for admin only
        btnEdit = new JButton("Edit Details (Admin)");
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if(selectedRow != -1) showProductDialog(inventory.getProductAt(selectedRow)); // Open dialog
        });
        
        // Remove button
        JButton btnRemove = new JButton("Remove Item (Admin)");
        btnRemove.setBackground(new Color(200, 0, 0));
        btnRemove.setForeground(Color.WHITE);
        btnRemove.addActionListener(e -> {
        	if (currentUser instanceof Admin) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    Product p = inventory.getProductAt(selectedRow);
                    // Confirmation popup
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to remove " + p.getProductName() + "?",
                        "Confirm Removal", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        inventory.removeProduct(p.getProductId());
                        JOptionPane.showMessageDialog(this, "Item removed successfully!");
                        refreshTable(); // Sync UI with DB
                        updateRightPanel(-1); // To clear right panel
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select an item to remove.");
                }
            }
        });
        

        // Stack the buttons vertically
        btnPanel.add(qtySpinner);
        btnPanel.add(btnBuy);
        btnPanel.add(btnTopUp);
        btnPanel.add(btnEdit);
        btnPanel.add(btnRemove);
        
        // Assemble the bottom box
        bottomBox.add(lblPrice, BorderLayout.NORTH);
        bottomBox.add(Box.createRigidArea(new Dimension(0, 15)), BorderLayout.CENTER);
        bottomBox.add(btnPanel, BorderLayout.SOUTH);
        rightPanel.add(bottomBox, BorderLayout.SOUTH);
    }

    // Processing a purchase
    private void processSale() {
        if (!(currentUser instanceof Customer)) return; // Double check
        Customer cust = (Customer) currentUser;
        
        // Fetch product via secure ID extraction
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item.");
            return;
        }

        Product p = inventory.getProductAt(selectedRow);

        /// Calculate cost
        int qtyToBuy = (int) qtySpinner.getValue();
        double totalCost = p.getPrice() * qtyToBuy;

        // Try to deduct money first
        if (!cust.deductBudget(totalCost)) {
            JOptionPane.showMessageDialog(this, "Insufficient Funds in Wallet!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Reduce stock by chosen quantity to sql 
            inventory.sellProduct(p.getProductId(), qtyToBuy);
            JOptionPane.showMessageDialog(this, "Purchase Successful! Bought " + qtyToBuy + " units.");
            lblUserInfo.setText("Customer: " + cust.getName() + " | Wallet: ₱" + String.format("%.2f", cust.getBudget()));
            refreshTable();
            updateRightPanel(selectedRow); // Refresh the side panel stock count
        } catch (ProductNotFoundException ex) { // Product not found exception
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Product Error", JOptionPane.ERROR_MESSAGE);
            cust.addBudget(totalCost); // Refund
        } catch (InvalidQuantityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Stock Error", JOptionPane.WARNING_MESSAGE);
            cust.addBudget(totalCost); // Refund
        }
    }

    
    // Add/Edit Product
    private void showProductDialog(Product productToEdit) {
        JDialog dialog = new JDialog(this, productToEdit == null ? "Add Item" : "Edit Item", true);
        dialog.setSize(400, 500); 
        dialog.setLocationRelativeTo(this);
        
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10)); 
        form.setBorder(new EmptyBorder(10,10,10,10));
        
        JTextField idF = new JTextField(); 
        JTextField nameF = new JTextField();
        JTextField prF = new JTextField(); 
        JTextField qtF = new JTextField();
        JTextArea descF = new JTextArea(); 
        descF.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // If editing an existing product, populate the fields with current data
        if(productToEdit != null) {
            idF.setText(""+productToEdit.getProductId()); 
            idF.setEditable(false);
            nameF.setText(productToEdit.getProductName());
            prF.setText(""+productToEdit.getPrice());
            qtF.setText(""+productToEdit.getQuantity());
            descF.setText(productToEdit.getDescription());
        }
        
        // Build the form
        form.add(new JLabel("ID")); form.add(idF);
        form.add(new JLabel("Name")); form.add(nameF);
        form.add(new JLabel("Price (₱)")); form.add(prF);
        form.add(new JLabel("Qty")); form.add(qtF);
        form.add(new JLabel("Description")); form.add(new JScrollPane(descF));
        
        JButton save = new JButton("Save");
        save.addActionListener(ev -> {
            try {
            	// Parse strings from text fields into numbers
                int id = Integer.parseInt(idF.getText());
                double pr = Double.parseDouble(prF.getText());
                int qt = Integer.parseInt(qtF.getText());
                String desc = descF.getText();
                
                // Route to the correct DAO method based on Add vs Edit
                if(productToEdit == null) {
                    // Saves new product to MySQL
                    inventory.addProduct(new Product(id, nameF.getText(), pr, qt, desc));
                } else { 
                    // Uses the new updateFullProduct method to update DB
                    inventory.updateFullProduct(id, nameF.getText(), pr, qt, desc);
                }
                refreshTable(); // Sync to DB
                dialog.dispose(); // Close window
            } catch(Exception ex) { 
            	// Catches error if they type "five" instead of "5"
                JOptionPane.showMessageDialog(dialog, "Invalid Input! Price/Qty must be numbers."); 
            }
        });
        
        dialog.add(form, BorderLayout.CENTER); 
        dialog.add(save, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateRightPanel(int row) {
        Product p = inventory.getProductAt(row);
        if (p != null) {
            lblName.setText(p.getProductName());
            lblId.setText("ID: " + p.getProductId());
            lblStock.setText("Qty: " + p.getQuantity());
            lblPrice.setText(String.format("₱ %.2f", p.getPrice()));
            txtDesc.setText(p.getDescription()); 
        }
    }
    
    // Pulls fresh data and redraws the JTable
    private void refreshTable() {
        tableModel.setRowCount(0); // Erase all visual rows
        // Fetches updated list from MySQL
        for (Product p : inventory.getProductsList()) tableModel.addRow(p.toRow());  // Add rows back using the Product helper method
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}