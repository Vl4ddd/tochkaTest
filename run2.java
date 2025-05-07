import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    private static final int[] DX = { -1, 1, 0, 0 };
    private static final int[] DY = { 0, 0, -1, 1 };
    private static final char[] KEYS_CHAR = new char[26];
    private static final char[] DOORS_CHAR = new char[26];

    static {
        for (int i = 0; i < 26; i++) {
            KEYS_CHAR[i] = (char) ('a' + i);
            DOORS_CHAR[i] = (char) ('A' + i);
        }
    }

    private static class Connection {
        int targetNode;
        int distance;
        Set<Character> doorsNeeded;

        Connection(int targetNode, int distance, Set<Character> doorsNeeded) {
            this.targetNode = targetNode;
            this.distance = distance;
            this.doorsNeeded = doorsNeeded;
        }
    }

    private static class Position {
        int row;
        int col;
        int steps;
        Set<Character> doorsOnPath;

        Position(int row, int col, int steps, Set<Character> doorsOnPath) {
            this.row = row;
            this.col = col;
            this.steps = steps;
            this.doorsOnPath = doorsOnPath;
        }
    }

    private static class State implements Comparable<State> {
        int[] robotNodes;
        Set<Character> collectedKeys;
        int totalSteps;

        State(int[] robotNodes, Set<Character> collectedKeys, int totalSteps) {
            this.robotNodes = robotNodes.clone();
            this.collectedKeys = new HashSet<>(collectedKeys);
            this.totalSteps = totalSteps;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof State)) {
                return false;
            }
            State other = (State) obj;
            return Arrays.equals(robotNodes, other.robotNodes) && collectedKeys.equals(other.collectedKeys);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(robotNodes);
            result = 31 * result + collectedKeys.hashCode();
            return result;
        }

        @Override
        public int compareTo(State other) {
            return Integer.compare(this.totalSteps, other.totalSteps);
        }
    }

    private static char[][] getInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }

        char[][] maze = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            maze[i] = lines.get(i).toCharArray();
        }

        return maze;
    }

    public static void main(String[] args) throws IOException {
        char[][] data = getInput();
        int steps = solve(data);

        if (steps == Integer.MAX_VALUE) {
            System.out.println("No solution found");
        } else {
            System.out.println(steps);
        }
    }

    private static int solve(char[][] data) {
        int height = data.length;
        int width = data[0].length;

        List<int[]> startPositions = new ArrayList<>();
        Map<Character, int[]> keyPositions = new HashMap<>();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                char ch = data[r][c];
                if (ch == '@') {
                    startPositions.add(new int[] { r, c });
                } else if (ch >= 'a' && ch <= 'z') {
                    keyPositions.put(ch, new int[] { r, c });
                }
            }
        }

        List<int[]> nodes = new ArrayList<>(startPositions);
        List<Character> sortedKeys = new ArrayList<>(keyPositions.keySet());
        Collections.sort(sortedKeys);

        Map<Character, Integer> keyIndexMap = new HashMap<>();
        for (int i = 0; i < sortedKeys.size(); i++) {
            char key = sortedKeys.get(i);
            nodes.add(keyPositions.get(key));
            keyIndexMap.put(key, i + startPositions.size());
        }

        List<List<Connection>> graph = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            graph.add(new ArrayList<>());
        }

        for (int i = 0; i < nodes.size(); i++) {
            boolean[][] visited = new boolean[height][width];
            Queue<Position> queue = new ArrayDeque<>();

            int sr = nodes.get(i)[0];
            int sc = nodes.get(i)[1];

            visited[sr][sc] = true;
            queue.add(new Position(sr, sc, 0, new HashSet<>()));

            while (!queue.isEmpty()) {
                Position curr = queue.poll();
                char cell = data[curr.row][curr.col];
                Set<Character> doorsSoFar = new HashSet<>(curr.doorsOnPath);

                if (cell >= 'A' && cell <= 'Z') {
                    doorsSoFar.add(cell);
                }

                if (cell >= 'a' && cell <= 'z' && keyIndexMap.containsKey(cell)) {
                    int targetNodeId = keyIndexMap.get(cell);
                    if (targetNodeId != i) {
                        graph.get(i).add(new Connection(targetNodeId, curr.steps, doorsSoFar));
                    }
                }

                for (int d = 0; d < 4; d++) {
                    int nr = curr.row + DX[d];
                    int nc = curr.col + DY[d];
                    if (nr >= 0 && nr < height && nc >= 0 && nc < width &&
                            data[nr][nc] != '#' && !visited[nr][nc]) {
                        visited[nr][nc] = true;
                        queue.add(new Position(nr, nc, curr.steps + 1, doorsSoFar));
                    }
                }
            }
        }

        PriorityQueue<State> queue = new PriorityQueue<>();
        Map<State, Integer> minStepsMap = new HashMap<>();

        int[] initialPositions = new int[startPositions.size()];
        for (int i = 0; i < initialPositions.length; i++) {
            initialPositions[i] = i;
        }

        State initialState = new State(initialPositions, Collections.emptySet(), 0);
        queue.add(initialState);
        minStepsMap.put(initialState, 0);

        while (!queue.isEmpty()) {
            State current = queue.poll();

            if (minStepsMap.getOrDefault(current, Integer.MAX_VALUE) < current.totalSteps)
                continue;

            if (current.collectedKeys.size() == sortedKeys.size()) {
                return current.totalSteps;
            }

            for (int robotIdx = 0; robotIdx < current.robotNodes.length; robotIdx++) {
                int posNode = current.robotNodes[robotIdx];

                for (Connection conn : graph.get(posNode)) {
                    char keyHere = sortedKeys.get(conn.targetNode - startPositions.size());
                    if (current.collectedKeys.contains(keyHere))
                        continue;

                    boolean canPass = true;
                    for (char door : conn.doorsNeeded) {
                        if (!current.collectedKeys.contains(Character.toLowerCase(door))) {
                            canPass = false;
                            break;
                        }
                    }
                    if (!canPass)
                        continue;

                    Set<Character> newKeys = new HashSet<>(current.collectedKeys);
                    newKeys.add(keyHere);

                    int[] newRobotPositions = current.robotNodes.clone();
                    newRobotPositions[robotIdx] = conn.targetNode;

                    State newState = new State(newRobotPositions, newKeys, current.totalSteps + conn.distance);

                    int recordedSteps = minStepsMap.getOrDefault(newState, Integer.MAX_VALUE);
                    if (newState.totalSteps < recordedSteps) {
                        minStepsMap.put(newState, newState.totalSteps);
                        queue.add(newState);
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }
}
