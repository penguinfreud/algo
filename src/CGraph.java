import java.util.ArrayList;

public class CGraph {
    int n;
    boolean[][] adjMatrix;
    ArrayList<Integer>[] adjList;
    int[] colors;

    @SuppressWarnings("unchecked")
    public CGraph(boolean[][] adjMatrix, int[] colors) {
        this.adjMatrix = adjMatrix;
        this.colors = colors;
        this.n = adjMatrix.length;
        adjList = (ArrayList<Integer>[]) new ArrayList[n];
        for (int i = 0; i<n; i++) {
            adjList[i] = new ArrayList<>();
        }
        for (int i = 0; i<n; i++) {
            for (int j = i+1; j<n; j++) {
                if (adjMatrix[i][j]) {
                    adjList[i].add(j);
                    adjList[j].add(i);
                }
            }
        }
    }

    public void print() {
        for (int i = 0; i<n; i++) {
            System.out.printf("vertex %d color=%d neighbors=%s\n", i, colors[i], adjList[i].toString());
        }
    }
}
