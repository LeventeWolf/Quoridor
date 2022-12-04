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

/**
 * Ágens osztály ami a játékos lehetséges akcióit kezeli le <br>
 * Egy akció lehet fal lerakás vagy mozgási lépés
 * */
public class Agent extends QuoridorPlayer {

    /**
     * Lerakott falak poziciójai
     */
    private final List<WallObject> walls = new LinkedList<>();
    /**
     * Tömb amibne a játékosok szerepelenk
     */
    private final QuoridorPlayer[] players = new QuoridorPlayer[2];
    /**
     * Célpontok a játék megnyeréséhez
     */
    private final ArrayList<PlaceObject> endPositions = new ArrayList<>();
    /**
     * Ellenfél célpontjai a játék megnyeréséhez
     */
    private final ArrayList<PlaceObject> endPositionsEnemy = new ArrayList<>();
    /**
     * Legrövidebb útvonal a játék megnyeréséhez
     */
    private ArrayList<PlaceObject> shortestPath;
    /**
     * Az általunk lerakott falak száma
     */
    private int numWalls;

    /**
     * Konstruktor amely a következőket inicializálja: <br>
     * - Játékos <br>
     * - Ellenfél játékos <br>
     * - Lerakott falak száma <br>
     * @param i a játékos kezdő oszlopának koordinátája
     * @param j a játékos kezdő sorának koordinátája
     * @param color A játékos színe ami lehet 0 vagy 1 (0: fekete, 1: fehér)
     * @param random Véletlen szám generálásra szolgál
     */
    public Agent(int i, int j, int color, Random random) {
        super(i, j, color, random);
        players[color] = this;
        players[1-color] = new DummyPlayer((1-color) * (QuoridorGame.HEIGHT - 1), j, 1-color, null);
        numWalls = 0;

        initEndPositions(color);
    }

    /**
     * Célpontok inicializálása mind a két játékos számára, attól függően, hogy épp hol kezdenek.
     * Szükséges az akciók végrehajtásának kiszámításához.
     * @param color játékos színe, lehet 0 vagy 1
     */
    private void initEndPositions(int color) {
        for (int k = 0; k < QuoridorGame.HEIGHT - 1; k++) {
            endPositions.add(new PlaceObject(color == 1 ? 0 : QuoridorGame.HEIGHT - 1, k));
        }

        for (int k = 0; k < QuoridorGame.HEIGHT - 1; k++) {
            endPositionsEnemy.add(new PlaceObject(color != 1 ? 0 : QuoridorGame.HEIGHT - 1, k));
        }
    }

    /**
     * Ha az ellenfél hamarabb érne célbe oda akkor próbáljuk meg ezt egy fal lerakásával befolyásolni,
     * hogy mi érjünk a célba hamarabb
     * Ha már nem sikerül fal lerakásával se kevesebb lépésszer eljutni, akkor csak menjünk a legrövidebb útvonalon
     * @param prevAction
     * @param remainingTimes
     * @return
     */
    @Override
    public QuoridorAction getAction(QuoridorAction prevAction, long[] remainingTimes) {
        saveEnemyAction(prevAction);

        shortestPath = getShortestPath(new PlaceObject(i, j), endPositions);
        int enemyShortestPathLength = getShortestPath(new PlaceObject(players[1-color].i, players[1-color].j), endPositionsEnemy).size();

        if (shortestPath.size() > enemyShortestPathLength && numWalls < QuoridorGame.MAX_WALLS) {
            WallObject wall = getWallStep(shortestPath.size() - enemyShortestPathLength, enemyShortestPathLength);
            if (wall != null) {
                numWalls++;
                walls.add(wall);
                return wall.toWallAction();
            } else {
                return getMyNextStep();
            }
        } else {
            return getMyNextStep();
        }
    }

    /**
     * Fal lépés kiszámolása:
     * Keressük azt a fal lerakási lépést ahová leraknánk ott az ellenfél legtávolabb lenne a végponttol,
     * mi pedig a leközelebb a célpontunkhoz. <br>
     * @param diff különbség amely megadja a két játékos közötti játékkos célba jutási távolságot
     * @return fal lerakás pozicója, null ha nem tudjuk megfordítani az "állást"
     */
    private WallObject getWallStep(int diff, int enemyShortestPathLength) {
        int max = enemyShortestPathLength;
        int pathDiff = diff;
        WallObject candidateWall = null;

        for (int k = 0; k < QuoridorGame.HEIGHT - 1; k++) {
            for (int l = 0; l < QuoridorGame.HEIGHT - 1; l++) {
                for (int m = 0; m <= 1; m++) {
                    WallObject wall = new WallObject(k, l, m == 0);

                    boolean canPut = QuoridorGame.checkWall(wall, walls, players);
                    if (canPut) {
                        walls.add(wall);
                        int enemyShortestPath = getShortestPath(new PlaceObject(players[1-color].i, players[1-color].j), endPositionsEnemy).size();
                        walls.remove(wall);

                        if (shortestPath.size() - enemyShortestPath < pathDiff) {
                            if (enemyShortestPath > max) {
                                candidateWall = wall;
                                pathDiff = shortestPath.size() - enemyShortestPath;
                                max = enemyShortestPath;
                            }
                        }
                    }
                }
            }
        }

        return candidateWall;
    }

    /**
     * @return a legrövidebb útvonal szerinti következő lépés
     */
    private MoveAction getMyNextStep() {
        PlaceObject nextStep = shortestPath.get(1);
        return new MoveAction(i, j, nextStep.i, nextStep.j);
    }

    /**
     * Megadja a lehetséges célpontok közül azt, amelyet a legkevesebb lépésegben érjük el (min keresés)
     * Szükséges, mert a falak befolyásolják a célpontokhoz való útvonal hosszát
     * @return Lista amelyben megtalálható az útvonal koordinátái
     */
    private ArrayList<PlaceObject> getShortestPath(PlaceObject start, ArrayList<PlaceObject> endPositions) {
        ArrayList<PlaceObject> shortestPath = new ArrayList<>();
        int shortestPathLength = Integer.MAX_VALUE;
        for (PlaceObject endPosition : endPositions) {
            ArrayList<PlaceObject> path = astar(start, endPosition);
            if (path.size() != 0 && path.size() < shortestPathLength) {
                shortestPathLength = path.size();
                shortestPath = path;
            }
        }
        return shortestPath;
    }

    /**
     * Node osztály ami egy csúcsot-t reprezentál az a* keresési algoritmusban. <br>
     * Attribútumai: <br>
     * - Parent: a csúcs szülője <br>
     * - Position: a csúcs pozíciója <br>
     * - g: lépési költség a csúcshoz való eljutásig <br>
     * - h: a csúcstól a célpontig történő mozgás becsült költsége (heurisztika) <br>
     * - f: g és h összege <br>
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
     * A* keresési algoritmus, ami visszadja a legrövidebb útvonalat a kezdő és a célpont között.
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

    /**
     * Kiszámolja a manhattan távolságot a megadott két csúcs között
     * @param end_node Csúcs ahová el akarunk jutni
     * @param neighbour Kiindulási csúcs ahonnan indulunk
     * @return Manhattan távolság a csúcs pont között
     */
    private double manhattanDistance(Node end_node, Node neighbour) {
        return Math.abs(neighbour.position.i - end_node.position.i) + Math.abs(neighbour.position.j - end_node.position.j);
    }

    /**
     * Kiszámolja az eukleidészi távolságot a megadott két csúcs között
     * @param end_node Csúcs ahová el akarunk jutni
     * @param neighbour Kiindulási csúcs ahonnan indulunk
     * @return Eukleidészi távolság a csúcs pont között
     */
    private double euclideanDistance(Node end_node, Node neighbour) {
        return Math.pow(neighbour.position.i - end_node.position.i, 2) + Math.pow(neighbour.position.j - end_node.position.j, 2);
    }

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


