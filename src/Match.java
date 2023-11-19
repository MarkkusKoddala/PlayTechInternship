import java.util.UUID;

public class Match {

    private final Double ASideRate;
    private final Double BSideRate;
    private final String result;

    // Constructor initializes a Match with a unique matchID, ASideRate, BSideRate, and result
    public Match(UUID matchID, double ASideRate, double BSideRate, String result) {
        this.ASideRate = ASideRate;
        this.BSideRate = BSideRate;
        this.result = result;
    }

    // Getters to retrieve ASideRate, BSideRate, and result of the match
    public double getASideRate() {
        return ASideRate;
    }

    public double getBSideRate() {
        return BSideRate;
    }

    public String getResult() {
        return result;
    }
}
