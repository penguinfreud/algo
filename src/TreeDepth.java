import java.util.*;
import java.util.stream.Stream;

public class TreeDepth {
    public static class Graph {
        public int n, Esize;
        public boolean[] adj;

        public Graph(int n) {
            this.n = n;
            Esize = nChoose2(n);
            adj = new boolean[Esize];
        }

        public static int nChoose2(int n) {
            return n * (n-1) / 2;
        }

        public int edgeIndex(int i, int j) {
            if (j > i) {
                int t = i;
                i = j;
                j = t;
            }
            return nChoose2(i) + j;
        }

        public boolean adjacent(int i, int j) {
            if (i == j) return false;
            return adj[edgeIndex(i, j)];
        }

        public void setAdjacent(int i, int j, boolean adjacent) {
            adj[edgeIndex(i, j)] = adjacent;
        }

        public Graph removeVertex(int i) {
            Graph g = new Graph(n-1);
            System.arraycopy(adj, 0, g.adj, 0, nChoose2(i));
            for (int k = i+1; k<n; k++) {
                for (int l = 0; l<i; l++) {
                    g.setAdjacent(k-1, l, adj[edgeIndex(k, l)]);
                }
                for (int l = i+1; l<k; l++) {
                    g.setAdjacent(k-1, l-1, adj[edgeIndex(k, l)]);
                }
            }
            return g;
        }

        public boolean isNull() {
            for (int i = 0; i<Esize; i++) {
                if (adj[i])
                    return false;
            }
            return true;
        }

        public boolean isComplete() {
            for (int i = 0; i<Esize; i++) {
                if (!adj[i])
                    return false;
            }
            return true;
        }

        public List<Graph> connectedComponentDecompose() {
            boolean[] cc = Arrays.copyOf(adj, Esize);
            for (int k = 0; k<n; k++) {
                for (int i = 0; i<n; i++) {
                    if (i == k) continue;
                    for (int j = 0; j<i; j++) {
                        if (j == k) continue;
                        cc[edgeIndex(i, j)] |= cc[edgeIndex(i, k)] && cc[edgeIndex(k, j)];
                    }
                }
            }
            List<Graph> glist = new ArrayList<>();
            loop:
            for (int i = 0; i<n; i++) {
                for (int j = 0; j<i; j++) {
                    if (cc[edgeIndex(i, j)])
                        continue loop;
                }
                List<Integer> V = new ArrayList<>();
                V.add(i);
                for (int j = i+1; j<n; j++) {
                    if (cc[edgeIndex(j, i)])
                        V.add(j);
                }
                int ng = V.size();
                Graph g = new Graph(ng);
                for (int k = 0; k<ng; k++) {
                    for (int l = 0; l<k; l++) {
                        g.setAdjacent(k, l, adjacent(V.get(k), V.get(l)));
                    }
                }
                glist.add(g);
            }
            return glist;
        }

        public int treeDepth() {
            if (n == 1)
                return 1;
            if (n == 2)
                return adj[0]? 2: 1;
            List<Graph> cc = connectedComponentDecompose();
            return cc.stream().map(Graph::connectedTreeDepth).max(Integer::compare).orElse(0);
        }

        public int connectedTreeDepth() {
            if (n == 1)
                return 1;
            if (n == 2)
                return 2;
            return Stream.iterate(0, x -> x + 1).limit(n)
                    .map(this::removeVertex)
                    .map(Graph::treeDepth)
                    .min(Integer::compare).orElse(0) + 1;
        }

        public void complement() {
            for (int i = 0; i<Esize; i++) {
                adj[i] = !adj[i];
            }
        }

        public void random(Random random) {
            for (int i = 0; i<Esize; i++) {
                adj[i] = random.nextBoolean();
            }
        }

        public void setNull() {
            Arrays.fill(adj, false);
        }

        public void setComplete() {
            Arrays.fill(adj, true);
        }

        public static Graph path(int n) {
            Graph g = new Graph(n);
            for (int i = 0; i<n-1; i++) {
                g.setAdjacent(i, i+1, true);
            }
            return g;
        }

        public static Graph cycle(int n) {
            Graph g = path(n);
            g.setAdjacent(0, n-1, true);
            return g;
        }

        public static Graph completeBipartite(int m, int n) {
            Graph g = new Graph(m + n);
            for (int i = 0; i<m; i++) {
                for (int j = 0; j<n; j++) {
                    g.setAdjacent(i, m+j, true);
                }
            }
            return g;
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        Graph g = new Graph(11);
        g.random(random);
        System.out.println(Arrays.toString(g.adj));
        System.out.println(g.treeDepth());
        g.complement();
        System.out.println(g.treeDepth());
    }
}
