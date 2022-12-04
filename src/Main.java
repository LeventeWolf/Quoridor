import game.engine.Engine;

import java.util.concurrent.ThreadLocalRandom;


public class Main {
    public static void main(String[] args) {
        String speed = "5";
        String seed = Integer.toString(ThreadLocalRandom.current().nextInt());
        String timeout = "5000";
        String blackPlayer = "Agent";
        String whitePlayer = "Agent";
        String[] configuration = {speed, "game.quoridor.QuoridorGame", seed, timeout, blackPlayer, whitePlayer};

        try {
            Engine.main(configuration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
