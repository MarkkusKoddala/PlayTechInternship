import java.io.*;
import java.util.*;

public class BettingProcessor {
    // HashMap to store matches using their UUID as keys for quick access
    private final HashMap<UUID, Match> matchSet; //UUID should be unique and therefore insertion and access O(1)
    // TreeMap to store players sorted by their UUID
    private final TreeMap<UUID,Player> players; //insertion and search O(log n)
    private Long casinoBalance; //balance of the casino
    private Long currentPlayerBetsBalance; // current balance of bets placed by the player

    public BettingProcessor() {
        // Initialize data structures and balances
        this.matchSet = new HashMap<>();
        this.players = new TreeMap<>(Comparator.comparing(UUID::toString)); //
        this.casinoBalance = 0L;
        this.currentPlayerBetsBalance = 0L;
    }

    public void readMatchDataIn(String filepath) throws IOException {
        // Open the file and create a BufferedReader to read its contents
        BufferedReader matchDataReaderIn = new BufferedReader(new FileReader(filepath));
        String newLine = matchDataReaderIn.readLine();

        // Process each line until reaching the end of the file
        while (newLine != null) {
            // Split the line by commas to extract individual data elements
            String[] lineData = newLine.split(",");
            // Evaluate the match data obtained from the line
            evaluateMatchData(lineData);
            newLine = matchDataReaderIn.readLine();
        }
        // Close the BufferedReader to release system resources
        matchDataReaderIn.close();
    }
    /**
     * Evaluates match data obtained from an array of string elements.
     * Checks the validity of data and creates Match objects, then stores them in matchSet.
     */
    public void evaluateMatchData(String [] lineData){
        try {
            if (lineData.length == 4) { //String [] must have a length of 4
                // Extract match-related data from the array
                UUID matchID = UUID.fromString(lineData[0]); //line must be a valid UUID

                double ASideRate = Double.parseDouble(lineData[1]);
                double BSideRate = Double.parseDouble(lineData[2]);
                // Check if the fourth element represents a valid match result ('A', 'B', or 'DRAW')
                String wonSide;
                if (lineData[3].equals("A") || lineData[3].equals("B") || lineData[3].equals("DRAW") )  // Only 'A' or 'B' values are accepted if the player places a bet.
                    wonSide = lineData[3];
                else
                    throw new Exception("The result of the match is wrong!");
                // Create a new Match object with the extracted data and store it in the matchSet
                Match newMatch = new Match(matchID, ASideRate, BSideRate, wonSide);
                matchSet.put(matchID, newMatch);
            } else throw new Exception("Wrong number of elements on the row");
        } catch (Exception e) {
            // Catch any exceptions thrown during the process and print an error message

            System.out.printf("WRONG LINE: (%s) - %s%n", Arrays.toString(lineData),e.getMessage());
        }
    }
    //reads player data from a file and processes each line
    public void readPlayerDataIn (String filepath) throws IOException {
        BufferedReader playerDataReaderIn = new BufferedReader(new FileReader(filepath));
        String newLine = playerDataReaderIn.readLine();
        while (newLine != null) {
            String[] lineData = newLine.split(",");
            evaluatePlayerData(lineData); //evaluates line
            newLine = playerDataReaderIn.readLine();
        }
        playerDataReaderIn.close();
    }

/**
 * Evaluates player data obtained from an array of string elements.
 * Checks the validity of data and performs corresponding player operations.
 */
    private void evaluatePlayerData(String[] lineData) {
        try {
            if (lineData.length == 5 || lineData.length == 4) { //String [] must have a length of 5 for bet and a length of 4 for withdraw or deposit
                UUID playerID = UUID.fromString(lineData[0]); //line must be a valid UUID

                // Check if the operation type is valid ('BET', 'WITHDRAW', 'DEPOSIT')
                String operation;
                if (lineData[1].equals("BET") || lineData[1].equals("WITHDRAW") || lineData[1].equals("DEPOSIT")) { //only accept 3 types of operation
                    operation = lineData[1];
                } else
                    throw new RuntimeException("Wrong operation");

                // Extract match ID, amount, and bet side (if applicable) from the data
                UUID matchID = lineData[2].equals("") ? null : UUID.fromString(lineData[2]);
                Integer amount = Integer.parseInt(lineData[3]); //accepts only integers

                String betSide;
                if(lineData.length == 5) {
                    // If the data length is 5, check and store the bet side if it's valid ('A' or 'B')
                    if (lineData[4].equals("A") || lineData[4].equals("B"))  // Only 'A' or 'B' values are accepted if the player places a bet.
                        betSide = lineData[4];
                    else
                        throw new Exception("The side of the match the player places the bet on is wrong!");
                } else
                    betSide = null;

                // Get or create a player and perform corresponding player operation
                Player newPlayer = getOrCreatePlayer(playerID);
                handlePlayerOperation(newPlayer, operation, matchID, amount, betSide);
            } else throw new Exception("Wrong number of elements on the row");
        } catch (Exception e) {
            // Catch any exceptions thrown during the process and print an error message
            System.out.printf("WRONG LINE: (%s) - %s%n", Arrays.toString(lineData),e.getMessage());
        }
    }

    public Player getOrCreatePlayer(UUID playerID) {
        Player newPlayer;
        if (!players.containsKey(playerID)) {
            // If the player does not exist, adjust balances and create a new player
            this.casinoBalance += this.currentPlayerBetsBalance; // Add the current player bets balance to the casino
            this.currentPlayerBetsBalance = 0L; // Reset the current player bets balance
            newPlayer = new Player(playerID); // Create a new Player object
            players.put(playerID, newPlayer); // Add the new player to the players treeset
        } else {
            // If the player exists, retrieve the existing player
            newPlayer = players.get(playerID);
        }
        return newPlayer; // Return the existing or newly created Player object
    }

    /**
     * Writes processed player data and casino balance to an output file named "output.txt".
     * The file contains legitimate player data with their balance and win rate, illegal player operations,
     * and the final casino balance.
     *
     */
    public void writeDataOut() throws IOException {
        BufferedWriter dataOutWriter = new BufferedWriter(new FileWriter("src/output.txt"));

        // Write legitimate player data including player ID, balance, and win rate
        for (Map.Entry<UUID, Player> entry : players.entrySet()) {
            Player player = entry.getValue();
            if (player.isLegitimate()) {
                // Construct a line with player ID, balance, and win rate
                String line = String.format("%s %s %.2f\n", player.getPlayerID(), player.getBalance(), player.calculateWinRate());
                dataOutWriter.write(line); // Write the line to the file
            }
        }
        dataOutWriter.newLine(); // Add a new line between sections

        // Write information about players with illegal operations
        for (Map.Entry<UUID, Player> entry : players.entrySet()) {
            Player player = entry.getValue();
            if (!player.isLegitimate()) {
                dataOutWriter.write(player.getFirstIllegalOperation() + "\n"); // Write illegal operation details to the file
            }
        }
        dataOutWriter.newLine(); // Add a new line between sections

        // Write the final casino balance to the file
        dataOutWriter.write(String.valueOf(casinoBalance));
        dataOutWriter.close();
    }


    private void handlePlayerOperation(Player managedPlayer, String operation, UUID matchID, Integer amount, String betSide) {
        if (!managedPlayer.isLegitimate())    // If the player is not legitimate, they won't be processed further.
            return;
        // Execute a method based on the operation type using a switch statement
        switch (operation) {
            case ("DEPOSIT") -> managedPlayer.deposit(amount);
            case ("BET") -> performBet(managedPlayer, matchID, amount, betSide);
            case ("WITHDRAW") -> performWithdraw(managedPlayer, amount);
        }
    }

    /**
     * Performs a withdrawal operation for a managed player with a specified amount.
     */
    private void performWithdraw(Player managedPlayer, Integer amount) {
        if (managedPlayer.isAmountEnough(amount)) { // Check if the withdrawal amount is legitimate
            // If the amount is not enough, mark the player's operation as illegal
            managedPlayer.setLegitimate(false);
            this.currentPlayerBetsBalance = 0L; // Reset the current player's bets balance
            // Set the first illegal operation for the player
            managedPlayer.setFirstIllegalOperation(String.format("%s WITHDRAW %s %s %s",
                    managedPlayer.getPlayerID(), null, amount, null));
        } else {
            managedPlayer.withdraw(amount); // Perform the legitimate withdrawal operation
        }
    }

    /**
     * Performs a betting operation for a managed player on a specified match with a certain amount and bet side.
     */
    private void performBet(Player managedPlayer, UUID matchID, Integer amount, String betSide) {
        if (managedPlayer.isAmountEnough(amount)) { // Check if the player has enough balance for the bet
            // If the amount is not enough, mark the player's operation as illegal
            managedPlayer.setLegitimate(false);
            this.currentPlayerBetsBalance = 0L; // Reset the current player's bets balance
            // Set the first illegal operation for the player
            managedPlayer.setFirstIllegalOperation(String.format("%s BET %s %s %s",
                    managedPlayer.getPlayerID(), matchID, amount, betSide));
            return;
        }

        // Get the match details from matchSet based on matchID
        Match bettedMatch = matchSet.get(matchID);

        if (bettedMatch.getResult().equals(betSide)) {
            // Calculate the won amount based on the bet outcome and adjust balances accordingly
            Integer wonAmount = (int) Math.floor(amount * (betSide.equals("A") ? bettedMatch.getASideRate() : bettedMatch.getBSideRate()));
            this.currentPlayerBetsBalance -= wonAmount; // Subtract the won amount from current player's bets balance
            managedPlayer.incrementTotalBets(); // Increment the total bets count for the player
            managedPlayer.incrementWonBets(); // Increment the won bets count for the player
            managedPlayer.setBalance(managedPlayer.getBalance() + wonAmount); // Update player's balance
        } else if (bettedMatch.getResult().equals("DRAW")) {
            managedPlayer.incrementTotalBets(); // Increment the total bets count for the player
        } else {
            // Adjust player's balance and bets balance for a lost bet
            managedPlayer.setBalance(managedPlayer.getBalance() - amount);
            this.currentPlayerBetsBalance += amount;
            managedPlayer.incrementTotalBets(); // Increment the total bets count for the player
        }
    }

}
