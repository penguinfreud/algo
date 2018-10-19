import java.util.*;
import java.util.function.*;

public final class Test {
    static final class Node {
        boolean forbidden = false;
        boolean shouldCover = false;
        int depth = 0;
        Node parent;
        Node[] child = new Node[2];
    }

    private Node root = new Node();

    {
        root.forbidden = true;
        root.shouldCover = true;
    }

    private int forbidCount = 0;
    private void forbid(int term) {
        Node node = root;
        for (int i = 0; i<30; i++) {
            int c = (term >> i) & 1;
            if (node.child[c] == null) {
                node.child[c] = new Node();
                node.child[c].depth = node.depth + 1;
                node.child[c].parent = node;
            }
            node = node.child[c];
            node.forbidden = true;
        }
        ++forbidCount;
    }

    private int coverCount = 0;
    private void shouldCover(int term) {
        Node node = root;
        for (int i = 0; i<30; i++) {
            int c = (term >> i) & 1;
            if (node.child[c] == null) {
                node.child[c] = new Node();
                node.child[c].depth = node.depth + 1;
                node.child[c].parent = node;
            }
            node = node.child[c];
            node.shouldCover = true;
        }
        ++coverCount;
    }

    private int edge(int x, int y) {
        if (x > y) return edge(y, x);
        else if (x == 0) return y-1;
        else if (x == 1) return y+3;
        else if (x == 2) return y+6;
        else if (x == 3) return y+8;
        else if (x == 4) return y+9;
        return -1;
    }

    private final int S6_order = 720;
    private int[][] S6_edge = new int[S6_order][15];

    private void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    private void initPerm() {
        int[] perm = new int[6];
        int permIndex = 0;
        for (int i = 0; i<6; i++) perm[i] = i;
        for (int i1 = 0; i1<6; i1++) {
            swap(perm, 0, i1);
            for (int i2 = 1; i2<6; i2++) {
                swap(perm, 1, i2);
                for (int i3 = 2; i3<6; i3++) {
                    swap(perm, 2, i3);
                    for (int i4 = 3; i4<6; i4++) {
                        swap(perm, 3, i4);
                        for (int i5 = 4; i5<6; i5++) {
                            swap(perm, 4, i5);
                            for (int i = 0; i<6; i++) {
                                for (int j = i+1; j<6; j++) {
                                    S6_edge[permIndex][edge(i, j)] = edge(perm[i], perm[j]);
                                }
                            }
                            permIndex++;
                            swap(perm, 4, i5);
                        }
                        swap(perm, 3, i4);
                    }
                    swap(perm, 2, i3);
                }
                swap(perm, 1, i2);
            }
            swap(perm, 0, i1);
        }
    }

    private List<Integer> td2 = new ArrayList<>();
    private void addTd(String adj) {
        td2.add(Integer.parseInt(adj.replace(" ", ""), 2));
    }

    private void init() {
        addTd("0 00 000 0000 00000");
        addTd("0 00 000 0000 00001");
        addTd("0 00 000 0000 00011");
        addTd("0 00 001 0000 00001");
        addTd("0 00 000 0000 00111");
        addTd("0 01 000 0000 00011");
        addTd("1 00 001 0000 00001");
        addTd("0 00 000 0000 01111");
        addTd("1 00 000 0000 00111");
        addTd("0 11 000 0000 00011");
        addTd("0 00 000 0000 11111");
        for (int term: td2) {
            forAllIso(term | (term << 15), this::forbid);
            for (int term2: td2) {
                if (term != term2) {
                    forAllIso(term | (term2 << 15), this::shouldCover);
                }
            }
        }
    }

    private void forAllIso(int term, Consumer<Integer> cb) {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i<S6_order; i++) {
            int newTerm1 = 0;
            for (int e = 0; e<15; e++) {
                if (((term >> S6_edge[i][e]) & 1) == 1) {
                    newTerm1 |= 1 << e;
                }
            }
            for (int j = 0; j<S6_order; j++) {
                int newTerm2 = newTerm1;
                for (int e = 0; e<15; e++) {
                    if (((term >> 15 >> S6_edge[j][e]) & 1) == 1) {
                        newTerm2 |= 1 << 15 << e;
                    }
                }
                set.add(newTerm2);
            }
        }
        set.forEach(cb);
    }

    private List<int[]> cnf() {
        List<Node> clauses = new ArrayList<>();
        Deque<Node> deque = new ArrayDeque<>();
        deque.push(root);
        while (!deque.isEmpty()) {
            Node node = deque.pop();
            if (!node.forbidden && node.shouldCover) {
                clauses.add(node);
            } else if (node.shouldCover) {
                if (node.child[1] != null) {
                    deque.push(node.child[1]);
                }
                if (node.child[0] != null) {
                    deque.push(node.child[0]);
                }
            }
        }

        clauses.sort(new Comparator<Node>() {
            public int compare(Node a, Node b) {
                return Integer.compare(a.depth, b.depth);
            }

            public boolean equals(Object object) {
                return object == this;
            }
        });
        List<int[]> clauses2 = new ArrayList<>();
        for (Node node: clauses) {
            clauses2.add(node2Clause(node));
        }
        System.out.println(clauses2.size());
        return clauses2;
    }

    private int[] node2Clause(Node node) {
        int[] res = new int[node.depth];
        int j = node.depth-1;
        while (node != root) {
            if (node == node.parent.child[0]) {
                res[j] = ~(node.depth-1);
            } else {
                res[j] = node.depth-1;
            }
            j--;
            node = node.parent;
        }
        return res;
    }

    private List<int[]> primeClauses = new ArrayList<>();
    private BitSet subsumed = new BitSet();

    private BitSet smallClauseVisited = new BitSet();
    private HashSet<String> bigCluaseVisited = new HashSet<>();
    private int threshold = 0x10000;
    private Random random = new Random();

    int clause2int(int[] clause) {
        int key = 0;
        for (int i = 0; i<clause.length; i++) {
            key = key * 62 + clause[i] + 32;
        }
        return key;
    }

    private boolean checkVisited(int[] clause) {
        if (clause.length <= 5) {
            int key = clause2int(clause);
            if (smallClauseVisited.get(key)) {
                return true;
            } else {
                smallClauseVisited.set(key);
                return false;
            }
        } else if (random.nextInt(100) > 95) {
            String strClause = Arrays.toString(clause);
            if (bigCluaseVisited.contains(strClause)) {
                return true;
            } else {
                if (bigCluaseVisited.size() >= threshold) {
                    bigCluaseVisited.clear();
                }
                bigCluaseVisited.add(strClause);
                return false;
            }
        }
        return false;
    }

    private final int[][] filledPool = new int[31][];

    {
        for (int i = 0; i<31; i++) {
            filledPool[i] = new int[i];
        }
    }

    private BitSet vars = new BitSet();

    private boolean checkNotForbidden(int[] clause) {
        vars.clear();
        int len = clause.length;
        int max = clause[len - 1];
        if (max < 0) max = ~max;
        for (int lit: clause) {
            if (lit < 0) {
                vars.set(~lit);
            } else {
                vars.set(lit);
            }
        }
        int[] filled = filledPool[max+1];
        Arrays.fill(filled, 0);
        for (int j = 0; j<len; j++) {
            if (clause[j] >= 0) {
                filled[clause[j]] = 1;
            }
        }
        int depth = max+1;
        Node node = root;
        int i = 0;
        while (true) {
            boolean noChild;
            if (vars.get(i)) {
                noChild = node.child[filled[i]] == null;
            } else {
                noChild = node.child[0] == null && node.child[1] == null;
            }
            if (i == depth || noChild) {
                int d = 0;
                while (node != root && (node == node.parent.child[1] || node.parent.child[1] == null || vars.get(i-d-1))) {
                    node = node.parent;
                    d++;
                }
                if (node == root) {
                    return true;
                }
                node = node.parent.child[1];
                i -= d;
                if (i == depth && node.forbidden) {
                    return false;
                }
            } else {
                if (vars.get(i)) {
                    node = node.child[filled[i]];
                } else if (node.child[0] != null) {
                    node = node.child[0];
                } else {
                    node = node.child[1];
                }
                i++;
                if (i == depth && node.forbidden) {
                    return false;
                }
            }
        }

        /*
        vars.clear();
        int len = clause.length;
        int max = clause[len - 1];
        if (max < 0) max = ~max;
        for (int lit: clause) {
            if (lit < 0) {
                vars.set(~lit);
            } else {
                vars.set(lit);
            }
        }
        int rem = max - len + 1;
        int[] filled = filledPool[max+1];
        Arrays.fill(filled, 0);
        for (int j = 0; j<len; j++) {
            if (clause[j] >= 0) {
                filled[clause[j]] = 1;
            }
        }
        if (!checkNotForbiddenFilled(filled)) return false;
        for (int i = 1; i<(1<<rem); i++) {
            int j = 0;
            while (true) {
                if (!vars.get(j)) {
                    if (filled[j] == 0)
                        break;
                    filled[j] = 0;
                }
                j++;
            }
            filled[j] = 1;
            if (!checkNotForbiddenFilled(filled)) return false;
        }
        */
    }

    boolean checkNotForbiddenFilled(int[] clause) {
        Node node = root;
        for (int aClause : clause) {
            if (node == null)
                return true;
            node = node.child[aClause];
        }
        if (node == null)
            return true;
        return !node.forbidden;
    }

    private boolean arrayContains(int[] a, int key) {
        for (int anA : a) {
            if (anA == key)
                return true;
        }
        return false;
    }

    private boolean subclause(int[] a, int[] b) {
        for (int anA : a) {
            if (!arrayContains(b, anA))
                return false;
        }
        return true;
    }

    private void cnfReduce(List<int[]> clauses) {
        int nclauses = clauses.size();
        for (int _i = 0; _i<nclauses; _i++) {
            final int i = _i;
            if (subsumed.get(i)) continue;
            int[] clause = clauses.get(i);
            if (!allSubset(clause, sub -> {
                if (!checkVisited(sub)) {
                    if (checkNotForbidden(sub)) {
                        //System.out.println("not forbidden");
                        primeClauses.add(sub);
                        printClause(sub);
                        for (int j = i+1; j<nclauses; j++) {
                            if (subclause(sub, clauses.get(j))) {
                                subsumed.set(j);
                            }
                        }
                        return true;
                    } else {
                        //System.out.println("forbidden");
                    }
                }
                return false;
            })) {
                primeClauses.add(clause);
                printClause(clause);
            }
        }
        System.out.println(primeClauses.size());
    }

    private void printClause(int[] clause) {
        for (int aClause : clause) {
            if (aClause < 0) {
                System.out.printf("-%d ", ~aClause);
            } else {
                System.out.printf("+%d ", aClause);
            }
        }
        System.out.println();
    }

    private boolean allSubset(int[] clause, Function<int[], Boolean> cb) {
        for (int size = 1; size<clause.length; size++) {
            if (allSubsetOfSize(clause, size, cb))
                return true;
        }
        return false;
    }

    private void fromIndices(int[] values, int[] indices, int[] res) {
        for (int i = 0; i<indices.length; i++) {
            res[i] = values[indices[i]];
        }
    }

    private final int[][] subIndPool = new int[31][];
    private final int[][] subPool = new int[31][];

    {
        for (int i = 0; i<31; i++) {
            subIndPool[i] = new int[i];
            subPool[i] = new int[i];
        }
    }

    private boolean allSubsetOfSize(int[] clause, int size, Function<int[], Boolean> cb) {
        int len = clause.length;
        int[] subInd = subIndPool[size];
        int[] sub = subPool[size];
        for (int i = 0; i<size; i++) {
            subInd[i] = i;
        }
        fromIndices(clause, subInd, sub);
        if (cb.apply(sub)) {
            return true;
        }
        while (true) {
            int k = size-1;
            while (k >= 0 && subInd[k] - k == len - size) {
                k--;
            }
            if (k < 0) break;
            int ak = subInd[k];
            for (int j = k; j<size; j++) {
                subInd[j] = ak+j-k+1;
            }
            fromIndices(clause, subInd, sub);
            if (cb.apply(sub)) return true;
        }
        return false;
    }

    private void run() {
        initPerm();
        init();
        System.out.printf("%d %d\n", forbidCount, coverCount);
        List<int[]> clauses = cnf();
        //System.out.println(checkNotForbidden(new int[]{0, 1, 2, 15, 16, ~17, ~19}));
        cnfReduce(clauses);
    }

    public static void main(String[] args) {
        Test t = new Test();
        //System.out.printf("5 choose 2 is %d\n", t.binomCoef(5, 2));
        //System.out.printf("8 choose 3 is %d\n", t.binomCoef(8, 3));
        t.run();
        //t.allSubsetOfSize(new int[]{ 5, 10, 15, 20, 25 }, 3, a -> {System.out.println(Arrays.toString(a)); return false;});
    }
}