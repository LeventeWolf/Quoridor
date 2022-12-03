///Wolf,Wolf.Levente@stud.u-szeged.hu

import game.quoridor.MoveAction;
import game.quoridor.QuoridorGame;
import game.quoridor.QuoridorPlayer;
import game.quoridor.WallAction;
import game.quoridor.players.DummyPlayer;
import game.quoridor.utils.PlaceObject;
import game.quoridor.utils.QuoridorAction;
import game.quoridor.utils.WallObject;

import java.util.*;

public class Agent extends QuoridorPlayer {

    private final List<WallObject> walls = new LinkedList<>();
    private final QuoridorPlayer[] players = new QuoridorPlayer[2];
    private final ArrayList<PlaceObject> endPositions = new ArrayList<>();
    private int numWalls;

    public Agent(int i, int j, int color, Random random) {
        super(i, j, color, random);
        players[color] = this;
        players[1-color] = new DummyPlayer((1-color) * (QuoridorGame.HEIGHT - 1), j, 1-color, null);
        numWalls = 0;

        initEndPositions(color);
    }

    private void initEndPositions(int color) {
        for (int k = 0; k < QuoridorGame.HEIGHT - 1; k++) {
            endPositions.add(new PlaceObject(color == 1 ? 0 : QuoridorGame.HEIGHT - 1, k));
        }
    }

    @Override
    public QuoridorAction getAction(QuoridorAction prevAction, long[] remainingTimes) {
        saveEnemyAction(prevAction);

        int di = (color * (QuoridorGame.HEIGHT - 1)) - players[1-color].i < 0 ? -1 : 0;
        List<WallObject> wallObjects = new LinkedList<>();
        wallObjects.add(new WallObject(players[1-color].i + di, players[1-color].j - color, true));
        wallObjects.add(new WallObject(players[1-color].i + di, players[1-color].j - 1 + color, true));
        wallObjects.add(new WallObject(players[1-color].i + di, players[1-color].j - color, false));
        wallObjects.add(new WallObject(players[1-color].i + di, players[1-color].j - 1 + color, false));
        for (WallObject wall : wallObjects) {
            if (numWalls < QuoridorGame.MAX_WALLS && QuoridorGame.checkWall(wall, walls, players)) {
                numWalls ++;
                walls.add(wall);
                return wall.toWallAction();
            }
        }

        ArrayList<PlaceObject> shortestPath = getShortestPath();
        PlaceObject nextStep = shortestPath.get(1);
        return new MoveAction(i, j, nextStep.i, nextStep.j);
    }

    /**
     * Megadja a lehetséges célpontok közül azt, amelyet a legkevesebb lépésegben érjük el (min keresés)
     * Szükséges, mert a falak befolyásolják a célpontokhoz való útvonal hosszát
     * @return Lista amelyben megtalálható az útvonal koordinátái
     */
    private ArrayList<PlaceObject> getShortestPath() {
        ArrayList<PlaceObject> shortestPath = new ArrayList<>();
        int shortestPathLength = 100000;
        for (PlaceObject endPosition : endPositions) {
            ArrayList<PlaceObject> path = astar(new PlaceObject(this.i, this.j), endPosition);
            if (path.size() != 0 && path.size() < shortestPathLength) {
                shortestPathLength = path.size();
                shortestPath = path;
            }
        }
        return shortestPath;
    }

    /**
     * Node osztály ami egy csúcsot-t reprezentál az a* keresési algoritmusban
     */
    static class Node {
        Node parent;
        PlaceObject position;
        double g, h, f = 0.0;

        public Node(Node parent, PlaceObject position) {
            this.parent = parent;
            this.position = position;
        }


        @Override
        public boolean equals(Object obj) {
            Node other = (Node) obj;
            return this.position.i == other.position.i && this.position.j == other.position.j;
        }
    }

    /**
     * A* keresési algoritmus
     * @param start Játékos pozíciója
     * @param end Célpont pozíciója
     * @return Lista amely tartalmazza a legrövidebb útvonalat a célponthoz
     */
    private ArrayList<PlaceObject> astar(PlaceObject start, PlaceObject end){
        // Create start and end node
        Node start_node = new Node(null, new PlaceObject(start.i, start.j));
        Node end_node = new Node(null, end);

        // Initialize both open and closed list
        ArrayList<Node> open_list = new ArrayList<>();
        ArrayList<Node> closed_list = new ArrayList<>();

        open_list.add(start_node);

        while (open_list.size() > 0) {
            Node current = nodeInOpenWithTheLowestFCost(open_list);
            open_list.remove(current);
            closed_list.add(current);

            if (current.equals(end_node)) {
                ArrayList<PlaceObject> path = new ArrayList<>();
                while (current != null) {
                    path.add(current.position);
                    current = current.parent;
                }

                Collections.reverse(path);
                return path;
            }

            for (Node neighbour : neighbours(current)) {
                if (closed_list.contains(neighbour))
                    continue;

                if (newPathIsShorterToNeighbour(neighbour, current) || !open_list.contains(neighbour)) {
                    setFCost(end_node, current, neighbour);
                    if (!open_list.contains(neighbour)) {
                        open_list.add(neighbour);
                    }
                }
            }
        }

        return new ArrayList<PlaceObject>();
    }

    /**
     * Megnézi, hogyha a szomszédos csúcsra lépnénk akkor közelebb kerülünk-e a célponthoz.
     * @param neighbour Szomszédos csúcs
     * @param current Jelenlegi csúcs
     * @return Igaz ha a szomszéd távolsága közelebb van a célpont távolságához, különben hamis
     */
    private boolean newPathIsShorterToNeighbour(Node neighbour, Node current) {
        return current.g + 1 < neighbour.g;
    }

    /**
     * Beállítja a szomszéd csúcs értékeit az A* algoritmus alapján
     * Heurisztika: Manhattan
     * @param end_node Célpont csúcs
     * @param current Jelenlegi csúcs
     * @param neighbour Szomszéd csúcs
     */
    private void setFCost(Node end_node, Node current, Node neighbour) {
        neighbour.g = current.g + 1;
        neighbour.h = manhattanDistance(end_node, neighbour);
        neighbour.f = neighbour.g + neighbour.h;
    }

    private double manhattanDistance(Node end_node, Node neighbour) {
        return Math.abs(neighbour.position.i - end_node.position.i) + Math.abs(neighbour.position.j - end_node.position.j);
    }

//    private double eukledianDistance(Node end_node, Node neighbour) {
//        return Math.pow(neighbour.position.i - end_node.position.i, 2) + Math.pow(neighbour.position.j - end_node.position.j, 2);
//    }


    /**
     * Megadja a jelenlegi csúcsból a további lehetséges lépéseket
     * @param node Jelenlegi csúcs
     * @return Lista amelyben szerepel a jelenlegi csúcsból léphető csúcsok
     */
    private List<Node> neighbours(Node node) {
        ArrayList<Node> result = new ArrayList<>();

        for (PlaceObject neighbour : node.position.getNeighbors(walls, players)) {
            result.add(new Node(node, neighbour));
        }

        return result;
    }

    /**
     * Visszaadja a nyílt listából a lekisebb F értékkel rendelkező csúcsot
     * @param open_list Nyílt lista
     * @return Csúcs amelynek a legkevesebb az F értéke
     */
    private Node nodeInOpenWithTheLowestFCost(ArrayList<Node> open_list) {
        Node candidate = open_list.get(0);
        for (Node item : open_list) {
            if (item.f < candidate.f) {
                candidate = item;
            }
        }
        return candidate;
    }

    /**
     * Elmenti az ellenfél lépését ami lehet fal lerakás vagy lépés
     * @param prevAction Előző akció
     */
    private void saveEnemyAction(QuoridorAction prevAction) {
        if (prevAction instanceof WallAction a) {
            walls.add(new WallObject(a.i, a.j, a.horizontal));
        } else if (prevAction instanceof MoveAction a) {
            players[1 - color].i = a.to_i;
            players[1 - color].j = a.to_j;
        }
    }
}


