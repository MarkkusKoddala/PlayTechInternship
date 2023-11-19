import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class Player {
    private final UUID playerID;
    private Long balance;
    private boolean legitimate;
    private String firstIllegalOperation;
    private int totalBets;
    private int wonBets;

    // Constructor initializes a Player with a unique playerID
    public Player(UUID playerID) {
        this.playerID = playerID;
        this.balance = 0L;
        this.legitimate = true;
        this.totalBets = 0;
        this.wonBets = 0;
    }

    // Getters and setters for various attributes
    public UUID getPlayerID() {
        return playerID;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public boolean isLegitimate() {
        return legitimate;
    }

    public void setLegitimate(boolean legitimate) {
        this.legitimate = legitimate;
    }

    public String getFirstIllegalOperation() {
        return firstIllegalOperation;
    }

    public void setFirstIllegalOperation(String firstIllegalOperation) {
        this.firstIllegalOperation = firstIllegalOperation;
    }

    // Method to check if the amount is enough for a transaction
    public boolean isAmountEnough(Integer amount) {
        return amount > this.balance;
    }

    // Methods to perform deposit and withdrawal operations
    public void deposit(Integer amount) {
        this.balance += amount;
    }

    public void withdraw(Integer amount) {
        this.balance -= amount;
    }

    // Methods to calculate and retrieve the win rate of the player
    public BigDecimal calculateWinRate() {
        if (totalBets == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(wonBets).divide(new BigDecimal(totalBets), 4, RoundingMode.HALF_DOWN);
    }

    // Methods to increment total and won bets counts
    public void incrementTotalBets() {
        totalBets++;
    }

    public void incrementWonBets() {
        wonBets++;
    }
}
