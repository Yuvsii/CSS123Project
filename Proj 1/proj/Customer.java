package proj;

public class Customer extends User {
    private double budget; // Unique attribute of customer having a budget 

    public Customer(int id, String name, double budget) {
        super(id, name);
        this.budget = budget;
    }

    public double getBudget() {
        return budget;
    }

    // Since the budget is private other classes needs to use this method to change it to prevent negative balances
    public boolean deductBudget(double amount) {
        if (budget >= amount) {
            budget -= amount;
            return true; // Returns true if they have enough money to buy
        } 
        return false;
    }
    
    // Refunds money if wala nang stock
    public void addBudget(double amount) {
        budget += amount;
    }
}