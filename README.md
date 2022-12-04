# Quoridor
Quoridor two player game

## Insights

## Setup and install

The JavaFX and Game_Engine is located in the /libraries folder.

1. Add libraries/JavaFX (unzip libraries/javafx-sdk1.2.zip)
2. Add JavaFX SDK to Project libraries
   - -> File -> Project Structures -> Libraries
   - "+" Java
   - Select the downloaded javafx-sdk/lib folder
   ![](documentation/add-javafx-sdk.png)

3. Add libraries/game_engine.jar the same way (-> 2.)
4. Create new Run configuration <br>
   - -> Edit configurations
   ![](documentation/edit-configurations.png) <br>
   - Select Main class <br>
   - Add your javaFX-sdk lib path to new VM options with the --module-path flag
   ![](documentation/edit-configurations.png) <br>

You successuly setup the project. <br>
Now you can configure the game parameters in Main.java.


## Players

Built in players: (located in game_engine/game/quoridor/players)
- HumanPlayer
- DummyPlayer
- RandomPlayer
- BlockRandomPlayer

Advanced AI: (located in src/)
- Agent (A* path finding with min-max wall blocking)