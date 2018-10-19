import java.io.PrintStream;
import java.util.*;

public class GraphEnum {
    private static class Parser {
        String s;
        int pos;

        Parser(String s) {
            this.s = s;
            pos = 0;
        }
    }

    private int readN(Parser parser) {
        parser.pos++;
        return parser.s.charAt(0) - 63;
    }

    public boolean E(int[] g, int i, int j) {
        return ((g[i] >> j) & 1) == 1;
    }

    public String graphToString(int[] g) {
        StringBuilder sb = new StringBuilder();
        int n = g.length;
        for (int i = 0; i<n; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            for (int j = 0; j<n; j++) {
                if (E(g, i, j)) {
                    sb.append('1');
                } else {
                    sb.append('0');
                }
            }
        }
        return sb.toString();
    }

    public int[] readGraph(String s, int[] g) {
        Parser parser = new Parser(s);
        int n = readN(parser);
        if (g.length != n) {
            g = new int[n];
        } else {
            Arrays.fill(g, 0);
        }
        int len = (n*(n-1)/2 + 5)/6;
        int x = 0, y = 1;
        for (int i = 0; i<len; i++) {
            char c = s.charAt(parser.pos + i);
            int d = c - 63;
            for (int j = 0; j<6; j++) {
                int axy = (d >>> (5-j)) & 1;
                g[x] |= axy << y;
                g[y] |= axy << x;
                x++;
                if (x == y) {
                    x = 0;
                    y++;
                    if (y == n) {
                        break;
                    }
                }
            }
        }
        return g;
    }

    public void toAdjList(int[] g, int[][] adjList) {
        int n = g.length;

        for (int i = 0; i<n; i++) {
            int k = 0;
            for (int j = 0; j<n; j++) {
                if (E(g, i, j)) {
                    adjList[i][k] = j;
                    k++;
                }
            }
            for (; k<n; k++) {
                adjList[i][k] = -1;
            }
        }
    }

    private int[][] adjList = new int[0][0];
    private int[] color = new int[0];
    Queue<Integer> queue = new LinkedList<>();

    public boolean bipartite(int[] g) {
        int n = g.length;
        if (adjList.length != n) {
            adjList = new int[n][n];
        }
        toAdjList(g, adjList);
        if (color.length != n) {
            color = new int[n];
        } else {
            Arrays.fill(color, 0);
        }
        queue.clear();
        queue.add(0);
        color[0] = 1;

        while (!queue.isEmpty()) {
            int x = queue.remove();
            for (int i = 0; i<n; i++) {
                int y = adjList[x][i];
                if (y < 0) break;
                if (color[x] == color[y]) {
                    return false;
                } else if (color[y] == 0) {
                    color[y] = -color[x];
                    queue.add(y);
                }
            }
        }

        return true;
    }

    private int lastI = 0, lastJ = 0;

    public boolean notMaximal(int[] g) {
        int n = g.length;
        if (!E(g, lastI, lastJ) && (g[lastI] & g[lastJ]) == 0) {
            return true;
        }
        for (int i = 0; i<n; i++) {
            for (int j = i+1; j<n; j++) {
                if (!E(g, i, j) && (g[i] & g[j]) == 0) {
                    lastI = i;
                    lastJ = j;
                    return true;
                }
            }
        }
        return false;
    }

    public int countEdges(int[] g) {
        int n = g.length;
        int e = 0;
        for (int i = 0; i<n; i++) {
            for (int j = i+1; j<n; j++) {
                if (E(g, i, j)) {
                    e++;
                }
            }
        }
        return e;
    }

    public int maxDegree(int[] g) {
        int n = g.length;
        int max = 0;
        for (int i = 0; i<n; i++) {
            int d = Integer.bitCount(g[i]);
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GraphEnum ge = new GraphEnum();
        boolean first = true;
        int maxe = 0;
        int mine = 0;
        int minmaxd = 0;
        int count = 0;
        int[] g = new int[0];
        ArrayList<int[]> G1 = new ArrayList<>();
        ArrayList<int[]> G2 = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            g = ge.readGraph(line, g);
            if (!ge.notMaximal(g) && !ge.bipartite(g)) {
                count++;
                int e = ge.countEdges(g);
                if (first) {
                    maxe = mine = e;
                    G1.add(Arrays.copyOf(g, g.length));
                } else {
                    if (e == maxe) {
                        G1.add(Arrays.copyOf(g, g.length));
                    }
                    if (e > maxe) {
                        maxe = e;
                        G1.clear();
                        G1.add(Arrays.copyOf(g, g.length));
                    } else if (e < mine) {
                        mine = e;
                    }
                }
                int maxd = ge.maxDegree(g);
                if (first) {
                    minmaxd = maxd;
                    G2.add(Arrays.copyOf(g, g.length));
                } else {
                    if (maxd == minmaxd) {
                        G2.add(Arrays.copyOf(g, g.length));
                    } else if (maxd < minmaxd) {
                        minmaxd = maxd;
                        G2.clear();
                        G2.add(Arrays.copyOf(g, g.length));
                    }
                }
                System.out.print('#');
                if (first) {
                    first = false;
                }
            }
        }
        System.out.println();
        System.out.printf("#=%d, minE=%d, maxE=%d, minMaxDegree=%d\n", count, mine, maxe, minmaxd);
    }
}
