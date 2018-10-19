import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class ImgToGraph {
    private static class Component {
        BitSet pixels = new BitSet();
        int id = 0;
        double r, g, b;
    }

    private static int root(int[] link, int v) {
        while (link[v] != v)
            v = link[v];
        return v;
    }

    private static void union(BufferedImage img, int[] link, int x1, int y1, int x2, int y2, int threshold) {
        int rgb1 = img.getRGB(x1, y1), rgb2 = img.getRGB(x2, y2);
        if (rgbDist(rgb1, rgb2) <= threshold) {
            int w = img.getWidth();
            int index1 = y1 * w + x1, index2 = y2 * w + x2;
            if (index1 < index2) {
                link[root(link, index2)] = root(link, index1);
            } else {
                link[root(link, index1)] = root(link, index2);
            }
        }
    }

    private static int rgbDist(int rgb1, int rgb2) {
        return Math.abs(((rgb1 >> 16) & 0xff) - ((rgb2 >> 16) & 0xff)) +
                Math.abs(((rgb1 >> 8) & 0xff) - ((rgb2 >> 8) & 0xff)) +
                Math.abs((rgb1 & 0xff) - (rgb2 & 0xff));
    }

    private static BitSet dilate(BitSet pixels, int w) {
        BitSet result = new BitSet();
        pixels.stream().forEach(i -> {
            int x = i%w;
            if (x >= 2) {
                result.set(i-2);
                result.set(i-1);
                if (i >= w) {
                    result.set(i-1-w);
                }
                result.set(i-1+w);
            }
            if (x < w-2) {
                result.set(i+2);
                result.set(i+1);
                if (i >= w) {
                    result.set(i+1-w);
                }
                result.set(i+1+w);
            }
            if (i >= 2*w) {
                result.set(i-2*w);
                result.set(i-w);
            }
            result.set(i+2*w);
            result.set(i+w);
            result.set(i);
        });
        return result;
    }

    private static boolean intersect(Component c, Component d, int w) {
        BitSet rc = dilate(c.pixels, w), rd = dilate(d.pixels, w);
        rc.and(rd);
        if (rc.cardinality() <= 5) return false;
        return rc.stream().map(i -> i%w).max().orElse(0) - rc.stream().map(i -> i%w).min().orElse(0) > 10 ||
                rc.stream().map(i -> i/w).max().orElse(0) - rc.stream().map(i -> i/w).min().orElse(0) > 10;
    }

    public static CGraph imgToGraph(File imgFile, int threshold) throws IOException {
        BufferedImage img = ImageIO.read(imgFile);
        int w = img.getWidth();
        int h = img.getHeight();
        int wh = w*h;

        int[] link = new int[wh];
        for (int i = 0; i<wh; i++) {
            link[i] = i;
        }

        System.out.println("step 1");

        for (int x = 0; x<w; x++) {
            for (int y = 0; y<h; y++) {
                if (x > 0) {
                    union(img, link, x, y, x-1, y, threshold);
                }
                if (x < w-1) {
                    union(img, link, x, y, x+1, y, threshold);
                }
                if (y > 0) {
                    union(img, link, x, y, x, y-1, threshold);
                }
                if (y < h-1) {
                    union(img, link, x, y, x, y+1, threshold);
                }
            }
        }

        System.out.println("step 2");

        HashMap<Integer, Component> cc = new HashMap<>();
        for (int i =  0; i<wh; i++) {
            int r = root(link, i);
            Component comp = cc.computeIfAbsent(r, k -> new Component());
            comp.pixels.set(i);
            int rgb = img.getRGB(i % w, i / w);
            comp.r += (rgb >> 16) & 0xff;
            comp.g += (rgb >> 8) & 0xff;
            comp.b += rgb & 0xff;
        }

        System.out.printf("before removal #cc=%d\n", cc.size());

        System.out.println("step 3");

        link = null;
        Random random = new Random();
        ArrayList<Component> ccList = new ArrayList<>();
        cc.forEach((r, comp) -> {
            if (random.nextInt(100) == 0) {
                System.out.printf("card %d\n", comp.pixels.cardinality());
            }
            if (comp.pixels.cardinality() >= 40) {
                comp.id = ccList.size();
                ccList.add(comp);
            }
        });

        System.out.println("step 4");

        System.out.printf("before removal #cc=%d\n", ccList.size());
        cc = null;
        int n = ccList.size();
        boolean[][] adjMatrix = new boolean[n][n];
        int[] colors = new int[n];
        int[] colorLink = new int[n];

        for (int i = 0; i<n; i++) {
            colorLink[i] = i;
            Component c = ccList.get(i);
            int card = c.pixels.cardinality();
            c.r /= card;
            c.g /= card;
            c.b /= card;
            System.out.printf("vertex %d %e %e %e\n", i, c.r, c.g, c.b);
        }
        for (int i = 0; i<n; i++) {
            Component c = ccList.get(i);
            for (int j = i+1; j<n; j++) {
                Component d = ccList.get(j);
                if (Math.abs(c.r - d.r) + Math.abs(c.g - d.g) + Math.abs(c.b - d.b) <= threshold) {
                    colorLink[root(colorLink, j)] = root(colorLink, i);
                }
                if (intersect(c, d, w)) {
                    adjMatrix[i][j] = adjMatrix[j][i] = true;
                }
            }
        }

        System.out.println("step 5");

        int colorCount = 0;
        for (int i = 0; i<n; i++) {
            if (colorLink[i] == i) {
                colors[i] = colorCount;
                colorCount++;
            }
        }
        for (int i = 0; i<n; i++) {
            colors[i] = colors[root(colorLink, i)];
        }

        System.out.println("step 6");

        for (int i = 0; i<n; i++) {
            int index = ccList.get(i).pixels.nextSetBit(0);
            int x = index % w, y = index / w;
            System.out.printf("vertex %d x=%d y=%d\n", i, x, y);
        }
        return new CGraph(adjMatrix, colors);
    }
}
