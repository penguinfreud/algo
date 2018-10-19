import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

public class KamiSolver {
    static class BitVector {
        int len;
        int[] bits;

        public BitVector(int len) {
            this.len = len;
            bits = new int[(len+31) / 32];
        }

        public void set(int i) {
            bits[i / 32] |= 1 << (i % 32);
        }

        public void setAll(BitVector v) {
            if (v.len != len)
                throw new RuntimeException("vector lengths do not match");
            for (int i = 0; i<bits.length; i++) {
                bits[i] |= v.bits[i];
            }
        }

        public void unset(int i) {
            bits[i / 32] &= ~(1 << (i % 32));
        }

        public boolean test(int i) {
            return ((bits[i/32] >> (i % 32)) & 1) == 1;
        }
    }

    class MaximalKamiGraph {
        ArrayList<MaximalKamiGraph> children = new ArrayList<>();

        BitSet vertices = new BitSet();

        int color, depth = 1;

        public MaximalKamiGraph(int color) {
            this.color = color;
        }

        public void addChild(MaximalKamiGraph child) {
            children.add(child);
            depth += child.depth;
        }
    }

    class VertexData {
        ArrayList<MaximalKamiGraph>[] dp;

        @SuppressWarnings("unchecked")
        public VertexData() {
            dp = (ArrayList<MaximalKamiGraph>[]) new ArrayList[steps+1];
        }
    }

    CGraph g;
    int steps;
    int colors;
    ArrayList<MaximalKamiGraph>[][] dp;

    public KamiSolver(CGraph g, int steps, int colors) {
        this.g = g;
        this.steps = steps;
        this.colors = colors;
    }

    private static BitSet tmp = new BitSet();
    static int subsetCompare(BitSet a, BitSet b) {
        int ca = a.cardinality(), cb = b.cardinality();
        if (ca == 0)
            return -1;
        if (cb == 0)
            return 1;
        tmp.clear();
        tmp.or(a);
        tmp.and(b);
        int ct = tmp.cardinality();
        if (ca == ct) {
            return -1;
        } else if (ct == cb) {
            return 1;
        }
        return 0;
    }

    void findNeighbors(BitSet vertices, BitSet neighbors) {
        neighbors.clear();
        vertices.stream().forEach(i -> {
            for (int j: g.adjList[i]) {
                neighbors.set(j);
            }
        });
        neighbors.andNot(vertices);
    }

    boolean shouldAdd(BitSet vertices, int step, int color) {
        for (MaximalKamiGraph mkg2: dp[step][color]) {
            int rel = subsetCompare(vertices, mkg2.vertices);
            if (rel == 1) {
                dp[step][color].remove(mkg2);
                return true;
            } else if (rel == -1) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    void solve() {
        dp = (ArrayList<MaximalKamiGraph>[][]) new ArrayList[steps+1][colors];
        for (int i = 0; i<steps+1; i++) {
            for (int c = 0; c<colors; c++) {
                dp[i][c] = new ArrayList<>();
            }
        }
        for (int i = 0; i<g.n; i++) {
            int c = g.colors[i];
            MaximalKamiGraph mkg = new MaximalKamiGraph(c);
            mkg.vertices.set(i);
            mkg.depth = 0;
            dp[0][c].add(mkg);
        }
        for (int cc = 0; cc<colors; cc++) {
            System.out.printf("#dp[0][%d]=%d\n", cc, dp[0][cc].size());
        }
        for (int step = 0; step<steps; step++) {
            BitSet neighbors = new BitSet();
            BitSet newVx = new BitSet();
            for (int c = 0; c<colors; c++) {
                for (MaximalKamiGraph mkg : dp[step][c]) {
                    findNeighbors(mkg.vertices, neighbors);
                    for (int c2 = 0; c2 < colors; c2++) {
                        if (c2 == c) continue;
                        final int _c2 = c2;
                        newVx.clear();
                        newVx.or(mkg.vertices);
                        neighbors.stream().filter(i -> g.colors[i] == _c2).forEach(newVx::set);

                        if (newVx.cardinality() > mkg.vertices.cardinality() &&
                                shouldAdd(newVx, step+1, c2)) {
                            MaximalKamiGraph mkg2 = new MaximalKamiGraph(c2);
                            mkg2.addChild(mkg);
                            mkg2.vertices.or(newVx);
                            dp[step+1][c2].add(mkg2);
                        }
                    }
                    if (step == 0) continue;
                    for (MaximalKamiGraph adjoint: dp[1][c]) {
                        if (neighbors.intersects(adjoint.vertices)) {
                            newVx.clear();
                            newVx.or(mkg.vertices);
                            newVx.or(adjoint.vertices);
                            if (shouldAdd(newVx, step+1, c)) {
                                MaximalKamiGraph mkg2 = new MaximalKamiGraph(c);
                                mkg2.addChild(mkg);
                                mkg2.addChild(adjoint);
                                mkg2.depth = step+1;
                                mkg2.vertices.or(newVx);
                                dp[step+1][c].add(mkg2);
                            }
                        }
                    }
                }
                int maxK1 = 2;
                for (int k1 = 1; k1 <= Math.min(maxK1, step/2); k1++) {
                    for (MaximalKamiGraph mkg : dp[k1][c]) {
                        findNeighbors(mkg.vertices, neighbors);
                        for (int c2 = 0; c2 < colors; c2++) {
                            if (c2 == c) continue;
                            for (MaximalKamiGraph adjoint : dp[step - k1][c2]) {
                                if (neighbors.intersects(adjoint.vertices)) {
                                    newVx.clear();
                                    newVx.or(mkg.vertices);
                                    newVx.or(adjoint.vertices);
                                    if (shouldAdd(newVx, step + 1, c2)) {
                                        MaximalKamiGraph mkg2 = new MaximalKamiGraph(c2);
                                        mkg2.addChild(mkg);
                                        mkg2.addChild(adjoint);
                                        mkg2.vertices.or(newVx);
                                        dp[step+1][c2].add(mkg2);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int cc = 0; cc<colors; cc++) {
                System.out.printf("#dp[%d][%d]=%d\n", step+1, cc, dp[step+1][cc].size());
            }
        }
        boolean hasSolution = false;
        for (int c = 0; c<colors; c++) {
            for (MaximalKamiGraph mkg: dp[steps][c]) {
                if (mkg.vertices.cardinality() == g.n) {
                    hasSolution = true;
                    ArrayList<Integer> solVx = new ArrayList<>();
                    ArrayList<Integer> solCol = new ArrayList<>();
                    genSol(mkg, solVx, solCol);
                    for (int i = 0; i<solVx.size(); i++) {
                        System.out.printf("%d %d\n", solVx.get(i), solCol.get(i));
                    }
                    MaximalKamiGraph mkg2 = mkg;
                    while (mkg2.depth >= 1) {
                        System.out.println(mkg2.vertices.toString());
                        mkg2 = mkg2.children.get(0);
                    }
                    break;
                }
            }
        }
        if (!hasSolution) {
            System.out.println("no solution");
        }
    }

    private void genSol(MaximalKamiGraph mkg, ArrayList<Integer> solVx, ArrayList<Integer> solCol) {
        if (mkg.depth > 1) {
            for (MaximalKamiGraph child : mkg.children) {
                genSol(child, solVx, solCol);
            }
        }
        solVx.add(mkg.children.get(0).vertices.nextSetBit(0));
        solCol.add(mkg.color);
    }

    private static void setColumns(boolean[] arr, boolean val, int ... indices) {
        for (int i: indices) {
            arr[i] = val;
        }
    }

    private static void setColumns(int[] arr, int val, int ... indices) {
        for (int i: indices) {
            if (arr[i] != 0) {
                throw new RuntimeException("dup color " + Integer.toString(i));
            }
            arr[i] = val;
        }
    }

    public static void main(String[] args) throws IOException {
        int n = 72;
        boolean[][] adjMatrix = new boolean[n][n];
        int[] colors = new int[n];

        setColumns(adjMatrix[0], true, 1);
        setColumns(adjMatrix[1], true, 0,24);
        setColumns(adjMatrix[2], true, 3,7);
        setColumns(adjMatrix[3], true, 2,4);
        setColumns(adjMatrix[4], true, 3,5);
        setColumns(adjMatrix[5], true, 4,6);
        setColumns(adjMatrix[6], true,  5,7,26);
        setColumns(adjMatrix[7], true, 2,6);
        setColumns(adjMatrix[8], true, 9,13);
        setColumns(adjMatrix[9], true, 8,10);
        setColumns(adjMatrix[10], true, 9,11);
        setColumns(adjMatrix[11], true, 10,12);
        setColumns(adjMatrix[12], true, 11,13,28);
        setColumns(adjMatrix[13], true, 8,12);
        setColumns(adjMatrix[14], true, 15);
        setColumns(adjMatrix[15], true, 14,30);
        setColumns(adjMatrix[16], true, 17,19);
        setColumns(adjMatrix[17], true, 16,18);
        setColumns(adjMatrix[18], true, 17,19);
        setColumns(adjMatrix[19], true, 16,18,32);
        setColumns(adjMatrix[20], true, 21,23);
        setColumns(adjMatrix[21], true, 20,22);
        setColumns(adjMatrix[22], true, 21,23);
        setColumns(adjMatrix[23], true, 20,22,33);
        setColumns(adjMatrix[24], true, 1,25,34);
        setColumns(adjMatrix[25], true, 24,26);
        setColumns(adjMatrix[26], true, 6,25,27);
        setColumns(adjMatrix[27], true, 26,28);
        setColumns(adjMatrix[28], true, 12,27,29);
        setColumns(adjMatrix[29], true, 28,30);
        setColumns(adjMatrix[30], true, 15,29,31);
        setColumns(adjMatrix[31], true, 30,32);
        setColumns(adjMatrix[32], true, 19,31,33);
        setColumns(adjMatrix[33], true, 23,32);
        setColumns(adjMatrix[34], true, 24,35,44);
        setColumns(adjMatrix[35], true, 34,36);
        setColumns(adjMatrix[36], true, 35,37);
        setColumns(adjMatrix[37], true, 36,38);
        setColumns(adjMatrix[38], true, 37,39);
        setColumns(adjMatrix[39], true, 38,40);
        setColumns(adjMatrix[40], true, 39,41);
        setColumns(adjMatrix[41], true, 40,42);
        setColumns(adjMatrix[42], true, 41,43);
        setColumns(adjMatrix[43], true, 42);
        setColumns(adjMatrix[44], true, 34,45,54);
        setColumns(adjMatrix[45], true, 44,46);
        setColumns(adjMatrix[46], true, 45,47,60);
        setColumns(adjMatrix[47], true, 46,48);
        setColumns(adjMatrix[48], true, 47,49,66);
        setColumns(adjMatrix[49], true, 48,50);
        setColumns(adjMatrix[50], true, 49,51,68);
        setColumns(adjMatrix[51], true, 50,52);
        setColumns(adjMatrix[52], true, 51,53,70);
        setColumns(adjMatrix[53], true, 52,71);
        setColumns(adjMatrix[54], true, 44,55,59);
        setColumns(adjMatrix[55], true, 54,56);
        setColumns(adjMatrix[56], true, 55,57);
        setColumns(adjMatrix[57], true, 56,58);
        setColumns(adjMatrix[58], true, 57,59);
        setColumns(adjMatrix[59], true, 54,58);
        setColumns(adjMatrix[60], true, 46,61,65);
        setColumns(adjMatrix[61], true, 60,62);
        setColumns(adjMatrix[62], true, 61,63);
        setColumns(adjMatrix[63], true, 62,64);
        setColumns(adjMatrix[64], true, 63,65);
        setColumns(adjMatrix[65], true, 60, 64);
        setColumns(adjMatrix[66], true, 48,67);
        setColumns(adjMatrix[67], true, 66);
        setColumns(adjMatrix[68], true, 50,69);
        setColumns(adjMatrix[69], true, 68);
        setColumns(adjMatrix[70], true, 52);
        setColumns(adjMatrix[71], true, 53);
        setColumns(colors, 1, 2,4,24,32,35,41,44,50,62,64);
        setColumns(colors, 2, 1,6,8,10,12,15,17,21,54,56,58,60,66,68,70,71);
        setColumns(colors, 3, 3,19,23,28,37,39,46,52,63);
        setColumns(colors, 4, 0,5,7,9,11,13,16,18,20,22,26,30,43,48,53,55,57,59,61,65,67);

        for (int i = 0; i<n; i++) {
            if (adjMatrix[i][i])
                throw new RuntimeException("reflexive " + Integer.toString(i));
            for (int j = i+1; j<n; j++) {
                if (adjMatrix[i][j] != adjMatrix[j][i])
                    throw new RuntimeException("not symmetric " + Integer.toString(i) + " " + Integer.toString(j));
                if (adjMatrix[i][j] && colors[i] == colors[j]) {
                    throw new RuntimeException("adj same color " + Integer.toString(i) + " " + Integer.toString(j));
                }
            }
        }

        CGraph g = new CGraph(adjMatrix, colors);
        KamiSolver solver = new KamiSolver(g, 15, 5);
        solver.solve();
    }
}
