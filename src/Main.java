import game.engine.Engine;

import java.util.concurrent.ThreadLocalRandom;


public class Main {
    public static void main(String[] args) {
        String speed = "10";
        String seed = Integer.toString(ThreadLocalRandom.current().nextInt());
        String timeout = "5000";
        String blackPlayer = "Agent";
        String whitePlayer = "game.quoridor.players.BlockRandomPlayer";
        String[] asBlack = {speed, "game.quoridor.QuoridorGame", seed, timeout, blackPlayer, whitePlayer};

        try {
            Engine.main(asBlack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
