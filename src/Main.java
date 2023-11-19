import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        BettingProcessor bp = new BettingProcessor();
        bp.readMatchDataIn("resource/match_data.txt");
        bp.readPlayerDataIn("resource/player_data.txt");
        bp.writeDataOut();
    }
}