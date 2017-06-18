import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class MyBot {
    public static void main(String[] args) throws java.io.IOException {

        final InitPackage iPackage = Networking.getInit();
        final int myID = iPackage.myID;
        final GameMap gameMap = iPackage.map;

        Networking.sendInit("MyJavaBot");

        while (true) {

            List<Move> moves = new ArrayList<Move>();

            Networking.updateFrame(gameMap);

            // Map traverse.
            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {

                    // Current location and site.
                    final Location location = gameMap.getLocation(x, y);
                    final Site site = location.getSite();

                    // Site has MyBot's id.
                    if (site.owner == myID) {

                        // If the strenght is too low, wait to increase.
                        if (site.strength < 5) {
                            moves.add(new Move(location, Direction.STILL));

                        /* If there is a proper strength,
                         * check for best site to attack. */
                        } else {

                            // Adding all all surrounding sites in a priority queue.
                            Location north = gameMap.getLocation(
                                    location, Direction.NORTH);
                            Location south = gameMap.getLocation(
                                    location, Direction.SOUTH);
                            Location west = gameMap.getLocation(
                                    location, Direction.WEST);
                            Location east = gameMap.getLocation(
                                    location, Direction.EAST);

                            PriorityQueue<Move> bestDirection = new
                                    PriorityQueue<Move>(new Comparator<Move>() {
                                @Override
                                public int compare(Move o1, Move o2) {
                                    return o1.loc.getSite().strength - o2.loc.getSite().strength;
                                }
                            });

                            // Checking only for attack (owner is different).
                            if (north.getSite().owner != site.owner) {
                                bestDirection.add(new Move(north, Direction.NORTH));
                            }

                            if (south.getSite().owner != site.owner) {
                                bestDirection.add(new Move(south, Direction.SOUTH));
                            }

                            if (west.getSite().owner != site.owner) {
                                bestDirection.add(new Move(west, Direction.WEST));
                            }

                            if (east.getSite().owner != site.owner) {
                                bestDirection.add(new Move(east, Direction.EAST));
                            }

                            // Best attackable site is extracted from the heap.
                            if (!bestDirection.isEmpty()) {

                                Move nextDirection = bestDirection.peek();
                                // Attack only if strength is higher.
                                if (nextDirection.loc.getSite().strength >=
                                        location.getSite().strength) {
                                    moves.add(new Move(location, Direction.STILL));
                                    continue;
                                }

                                // Add the new move
                                moves.add(new Move(location, nextDirection.dir));

                                /* If all surrounding sites are mine,
                                 * find the closest enemy site that can be attacked. */
                            } else {

                                Location farNorth = gameMap.getLocation(
                                        location, Direction.NORTH);
                                Location farSouth = gameMap.getLocation(
                                        location, Direction.SOUTH);
                                Location farWest = gameMap.getLocation(
                                        location, Direction.WEST);
                                Location farEast = gameMap.getLocation(
                                        location, Direction.EAST);

                                int counter = 0;

                                // Each direction has a maximum of half of the map.
                                while (farNorth.getSite().owner == site.owner &&
                                        counter++ < gameMap.height / 2) {
                                    farNorth = gameMap.getLocation(farNorth,
                                            Direction.NORTH);
                                }

                                counter = 0;

                                while (farSouth.getSite().owner == site.owner &&
                                        counter++ < gameMap.height / 2) {
                                    farSouth = gameMap.getLocation(farSouth,
                                            Direction.SOUTH);
                                }

                                counter = 0;

                                while (farWest.getSite().owner == site.owner &&
                                        counter++ < gameMap.width / 2) {
                                    farWest = gameMap.getLocation(farWest,
                                            Direction.WEST);
                                }

                                counter = 0;

                                while (farEast.getSite().owner == site.owner &&
                                        counter++ < gameMap.width / 2) {
                                    farEast = gameMap.getLocation(farEast,
                                            Direction.EAST);
                                }

                                PriorityQueue<Move> farMoves = new
                                        PriorityQueue<Move>(new Comparator<Move>() {
                                    @Override
                                    public int compare(Move o1, Move o2) {
                                        return (int) gameMap.getDistance(location, o1.loc) -
                                                (int) gameMap.getDistance(location, o2.loc);
                                    }
                                });

                                farMoves.add(new Move(farWest, Direction.WEST));
                                farMoves.add(new Move(farNorth, Direction.NORTH));
                                farMoves.add(new Move(farSouth, Direction.SOUTH));
                                farMoves.add(new Move(farEast, Direction.EAST));

                                // Get the best move from the heap.
                                moves.add(new Move(location, farMoves.peek().dir));
                            }
                        }
                    }
                }
            }
            Networking.sendFrame(moves);
        }
    }
}
