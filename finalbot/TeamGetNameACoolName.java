import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class TeamGetNameACoolName {

    private final static InitPackage iPackage = Networking.getInit();
    private final static int myID = iPackage.myID;
    private final static GameMap gameMap = iPackage.map;
    private static boolean[][] wasMoved = new boolean[gameMap.width][gameMap.height];

	// Resets wasMoved after each round
    private static void resetMoves() {
        for (int i = 0; i < gameMap.width; i++) {
            Arrays.fill(wasMoved[i], false);
        }
    }

	// Checks if site decribed by location is at the edge of my area
    private static boolean isEdge(Location loc) {
        return loc.getSite().owner != myID &&
                (gameMap.getLocation(loc, Direction.NORTH).getSite().owner == myID ||
                        gameMap.getLocation(loc, Direction.SOUTH).getSite().owner == myID ||
                        gameMap.getLocation(loc, Direction.WEST).getSite().owner == myID ||
                        gameMap.getLocation(loc, Direction.EAST).getSite().owner == myID);
    }

    private static boolean isCloseToEdge(Location loc) {
        return gameMap.getLocation(loc, Direction.NORTH).getSite().owner != myID ||
                gameMap.getLocation(loc, Direction.SOUTH).getSite().owner != myID ||
                gameMap.getLocation(loc, Direction.WEST).getSite().owner != myID ||
                gameMap.getLocation(loc, Direction.EAST).getSite().owner != myID;
    }

	// Return score for location
    private static double getScore(Location loc) {
        return 5 * loc.getSite().production - 0.7 * loc.getSite().strength + 50;
    }

    private static boolean isMine(Location loc) {
        return loc.getSite().owner == myID;
    }

    private static int calculateStrengthAfter(Location loc, int steps) {
        return loc.getSite().strength + steps * loc.getSite().production;
    }

	// Get the closest site that is not mine
    private static Move getClosestEdge(Location location) {

        Move[] moves = new Move[]{new Move(location, Direction.NORTH),
                new Move(location, Direction.SOUTH),
                new Move(location, Direction.WEST),
                new Move(location, Direction.EAST)};

        int[] distances = new int[]{0, 0, 0, 0};

        int[] margins = {gameMap.height / 2, gameMap.width / 2};

        for (int i = 0; i < 4; i++) {
            while (moves[i].loc.getSite().owner == myID &&
                    distances[i] < margins[i / 2]) {

                Location newLocation;
                newLocation = gameMap.getLocation(moves[i].loc, moves[i].dir);
                moves[i].loc = newLocation;
                distances[i]++;
            }
        }

        int[] distancesToEnemies = new int[]{0, 0, 0, 0};
        int maxDifference = 8;

        for (int i = 0; i < 4; i++) {
            if (distances[i] != margins[i / 2]) {
                if (moves[i].loc.getSite().owner == 0) {
                    while (distancesToEnemies[i] < maxDifference && moves[i].loc.getSite().owner == 0) {
                        Location newLocation;
                        newLocation = gameMap.getLocation(moves[i].loc, moves[i].dir);
                        moves[i].loc = newLocation;
                        distancesToEnemies[i]++;
                    }
                }
            } else {
                distancesToEnemies[i] = maxDifference;
            }
        }

        /* Sort the possible moves in order to get the move
         * with the smallest distance */
        int minDistance = distances[0] + distancesToEnemies[0];
        int minIndex = 0;
        for (int i = 1; i < 4; i++) {
            if (minDistance > distances[i] + distancesToEnemies[i]) {
                minDistance = distances[i] + distancesToEnemies[i];
                minIndex = i;
            }
        }
        return moves[minIndex];
    }

    /**
     * Returns the neighbours that are mine of a location sorted by strength
     *
     * @param loc The location
     * @return A list of the neighbours
     */
    private static ArrayList<Move> getLocsAround(Location loc, int steps) {

        ArrayList<Move> arrayList = new ArrayList<>();

        Move[] moves = new Move[]{new Move(gameMap.getLocation(loc, Direction.NORTH), Direction.SOUTH),
                new Move(gameMap.getLocation(loc, Direction.SOUTH), Direction.NORTH),
                new Move(gameMap.getLocation(loc, Direction.WEST), Direction.EAST),
                new Move(gameMap.getLocation(loc, Direction.EAST), Direction.WEST)};

        for (Move m : moves) {
            if (isMine(m.loc) && !wasMoved[m.loc.getX()][m.loc.getY()]) {
                arrayList.add(m);
            }
        }

        arrayList.sort((o1, o2) -> calculateStrengthAfter(o2.loc, steps) -
                calculateStrengthAfter(o1.loc, steps));

        // The sorted list of neighbours
        return arrayList;
    }

    private static ArrayList<Move> tryToTakeOver(Location loc, int steps) {

        ArrayList<Move> moves = new ArrayList<>();
        ArrayList<Move> locs = getLocsAround(loc, steps);
        if (locs.size() == 0) {
            return moves;
        }
        Move biggest = locs.get(0);

        // Try one move
        if (calculateStrengthAfter(biggest.loc, steps) > loc.getSite().strength) {
            if (steps != 0) {
                moves.add(new Move(biggest.loc, Direction.STILL));
            } else {
                moves.add(biggest);
            }
        } else {

            // Try two moves
            int last = locs.size() - 1;
            while (last > 0) {
                if (calculateStrengthAfter(biggest.loc, steps) +
                        calculateStrengthAfter(locs.get(last).loc, steps) > loc.getSite().strength) {
                    if (steps != 0) {
                        moves.add(new Move(biggest.loc, Direction.STILL));
                    } else {
                        moves.add(biggest);
                    }
                    moves.add(locs.get(last));
                    break;
                }
                last--;
            }

            // Try three moves
            if (moves.size() == 0) {
                last = locs.size() - 1;
                while (last > 1) {
                    if (calculateStrengthAfter(biggest.loc, steps) + calculateStrengthAfter(locs.get(1).loc, steps) +
                            calculateStrengthAfter(locs.get(last).loc, steps) > loc.getSite().strength) {
                        if (steps != 0) {
                            moves.add(new Move(biggest.loc, Direction.STILL));
                        } else {
                            moves.add(biggest);
                        }
                        moves.add(locs.get(1));
                        moves.add(locs.get(last));
                    }
                    last--;
                }
            }
        }

        setWasMoved(moves);
        return moves;
    }

    private static void setWasMoved(ArrayList<Move> moves) {
        for (Move move : moves) {
            wasMoved[move.loc.getX()][move.loc.getY()] = true;
        }
    }

    private static void setWasMoved(Move move) {
        wasMoved[move.loc.getX()][move.loc.getY()] = true;
    }

    public static void main(String[] args) throws java.io.IOException {

        Networking.sendInit("team.getName(aCoolName)");

        while (true) {

            List<Move> moves = new ArrayList<>();

            Networking.updateFrame(gameMap);
            resetMoves();

            PriorityQueue<Location> scores = new PriorityQueue<>((Location o1, Location o2) -> {

                Location[] locations = {o1, o2};
                double[] scores1 = {0, 0};
                int[] count = {0, 0};

                for (int i = 0; i < 2; i++) {
                    Location loc = locations[i];
                    for (Direction dir : Direction.DIRECTIONS) {
                        Location newLocation = gameMap.getLocation(loc, dir);
                        if (newLocation.getSite().owner != myID) {
                            count[i]++;
                            scores1[i] += getScore(newLocation);
                        }
                    }
                }

                return (int) (scores1[1] - scores1[0]);
            });
			
			// First map traverse
            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {

                    // Current location and site
                    final Location location = gameMap.getLocation(x, y);

                    if (isEdge(location)) {
                        scores.add(location);
                    }
                }
            }

            while (!scores.isEmpty()) {

                // Get the location with the best score
                Location loc = scores.poll();
                ArrayList<Move> movesForEdge;

                if (!((movesForEdge = tryToTakeOver(loc, 0)).size() == 0)) {
                    moves.addAll(movesForEdge);
                } else if (!((movesForEdge = tryToTakeOver(loc, 1)).size() == 0)) {
                    moves.addAll(movesForEdge);
                } else {

                    ArrayList<Move> bestMoves = getLocsAround(loc, 1);
                    if (bestMoves.size() == 0) {
                        continue;
                    }
                    Move bestMove = bestMoves.get(0);

                    ArrayList<Move> bestNeighbours = getLocsAround(bestMove.loc, 0);
                    if (bestNeighbours.size() == 0) {
                        continue;
                    }
                    Move bestNeighbour = bestNeighbours.get(0);

                    if (calculateStrengthAfter(bestMove.loc, 1) + bestNeighbour.loc.getSite().strength
                            > loc.getSite().strength) {
                        moves.add(new Move(bestMove.loc, Direction.STILL));
                        moves.add(bestNeighbour);
                        setWasMoved(bestMove);
                        setWasMoved(bestNeighbour);
                    }

                }
            }

            // Map traverse.
            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {

                    // Current location and site.
                    final Location location = gameMap.getLocation(x, y);
                    final Site site = location.getSite();

                    // Site has MyBot's id.
                    if (isMine(location)) {

                        if (isCloseToEdge((location))) {
                            if (!wasMoved[location.getX()][location.getY()]) {
                                moves.add(new Move(location, Direction.STILL));
                            }
                        } else {
                            if (site.strength > 6 * site.production) {
                                moves.add(new Move(location, getClosestEdge(location).dir));
                            } else {
                                moves.add(new Move(location, Direction.STILL));
                            }
                        }

                        wasMoved[location.getX()][location.getY()] = true;
                    }
                }
            }
            Networking.sendFrame(moves);
        }
    }
}
