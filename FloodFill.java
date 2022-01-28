import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class FloodFill {
    private short[] color;
    private int colorD;

    public FloodFill(short[] color, int colorD) {
        this.color = color;
        this.colorD = colorD;
    }

    public void processImage(short[][][] iChannels) {
        ArrayList<ArrayList<Location>> filledAreas = new ArrayList<>();
        for (int r = 0; r < iChannels[0].length; r++) {
            for (int c = 0; c < iChannels[0][0].length; c++) {
                if (inColorBounds(iChannels[0][r][c], iChannels[1][r][c], iChannels[2][r][c])) {
                    ArrayList<Location> a = new ArrayList<>();
                    runFloodFill(iChannels, r, c, a);
                    filledAreas.add(a);
                }
            }
        }
    }

    private void createBoundaries(short[][][] iChannels, ArrayList<Location> filledArea) {
        int minX = iChannels[0][0].length, minY = iChannels[0].length, maxX = 0, maxY = 0;

        for (Location l : filledArea) {
            if (l.y > maxY) maxY = l.y;
            if (l.y < minY) minY = l.y;

            if (l.x > maxX) maxX = l.x;
            if (l.x < minX) minX = l.x;
        }

        for (int i = minX; i <= maxX; i++) {
            iChannels[0][minY][i] = 0;
            iChannels[1][minY][i] = 0;
            iChannels[2][minY][i] = 255;

            iChannels[0][maxY][i] = 0;
            iChannels[1][maxY][i] = 0;
            iChannels[2][maxY][i] = 255;
        }

        for (int i = minY; i <= maxY; i++) {
            iChannels[0][i][minX] = 0;
            iChannels[1][i][minX] = 0;
            iChannels[2][i][minX] = 255;

            iChannels[0][i][maxX] = 0;
            iChannels[1][i][maxX] = 0;
            iChannels[2][i][maxX] = 255;
        }
    }

    private void runFloodFill(short[][][] iChannels, int row, int col, ArrayList<Location> area) {
        if (Convo.inBound(iChannels[0], row, col)) {
            Queue<Location> q = new PriorityQueue<>();
            q.add(new Location(col, row));

            while (q.size() != 0) {
                Location l = q.remove();
                area.add(l);

                iChannels[0][l.y][l.x] = 0;
                iChannels[1][l.y][l.x] = 0;
                iChannels[2][l.y][l.x] = 255;

                if (checkValidity(l.x + 1, l.y, iChannels)) q.add(new Location(l.x + 1, l.y));
                if (checkValidity(l.x - 1, l.y, iChannels)) q.add(new Location(l.x - 1, l.y));
                if (checkValidity(l.x, l.y + 1, iChannels)) q.add(new Location(l.x, l.y + 1));
                if (checkValidity(l.x, l.y - 1, iChannels)) q.add(new Location(l.x, l.y - 1));
            }
        }

    }

    private boolean checkValidity(int row, int col, short[][][] iChannels) {
        short r = iChannels[0][row][col];
        short g = iChannels[1][row][col];
        short b = iChannels[2][row][col];

        return ((r != 0 && g != 0 && b != 255) && inColorBounds(r, g, b));
    }

    private boolean inColorBounds(short r, short g, short b) {
        return ((r > color[0] - colorD && r < color[0] + colorD)
                && (g > color[1] - colorD && g < color[1] + colorD)
                && (b > color[2] - colorD && b < color[2] + colorD));
    }

    private int getCDist(short r, short g, short b) {
        int dR = r - colorD, dG = g - colorD, dB = b - colorD;
        return (int) Math.sqrt(dR * dR + dG * dG + dB + dB);
    }

}
