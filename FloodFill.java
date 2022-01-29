import java.util.ArrayDeque;
import java.util.ArrayList;

public class FloodFill {
    private short[] color;
    private int colorD;

    public FloodFill(short[] color, int colorD) {
        this.color = color;
        this.colorD = colorD;
    }

    public void processImage(short[][][] iChannels) {
        ArrayList<ArrayList<Location>> filledAreas = new ArrayList<>();
        int h = iChannels[0].length;
        int w = iChannels[0][0].length;
        short[][][] tempChannels = {new short[h][w], new short[h][w], new short[h][w]};

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                tempChannels[0][r][c] = iChannels[0][r][c];
                tempChannels[1][r][c] = iChannels[1][r][c];
                tempChannels[2][r][c] = iChannels[2][r][c];
            }
        }

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (inColorBounds(tempChannels[0][r][c], tempChannels[1][r][c], tempChannels[2][r][c])) {
                    ArrayList<Location> a = new ArrayList<>();
                    runFloodFill(tempChannels, r, c, a);
                    filledAreas.add(a);
                }
            }
        }

        createBoundaries(iChannels, filledAreas, 5000);
    }

    private void createBoundaries(short[][][] iChannels, ArrayList<ArrayList<Location>> filledAreas, int areaThreshold) {
        for (ArrayList<Location> area : filledAreas) {
            if (area.size() < areaThreshold) continue;
            int minX = iChannels[0].length, minY = iChannels[0][0].length, maxX = 0, maxY = 0;
            for (Location l : area) {
                if (l.y > maxY) maxY = l.y;
                if (l.y < minY) minY = l.y;

                if (l.x > maxX) maxX = l.x;
                if (l.x < minX) minX = l.x;
            }

            int[] boxColor = {0,255,0};


            for (int c = 0; c < boxColor.length; c++) {
                for (int i = minX; i <= maxX; i++) {
                    iChannels[c][i][minY] = (short) boxColor[c];
                    iChannels[c][i][maxY] = (short) boxColor[c];
                }

                for (int i = minY; i <= maxY; i++) {
                    iChannels[c][minX][i] = (short) boxColor[c];
                    iChannels[c][maxX][i] = (short) boxColor[c];
                }
            }


        }
    }

    private void runFloodFill(short[][][] iChannels, int row, int col, ArrayList<Location> area) {
        boolean[][] visited = new boolean[iChannels[0].length][iChannels[0][0].length];
        if (Convo.inBound(iChannels[0], row, col)) {
            ArrayDeque<Location> q = new ArrayDeque<>();
            q.add(new Location(row, col));

            while (q.size() > 0) {
                Location l = q.pop();
                if (visited[l.x][l.y]) {
                    if (q.size() == 0) return;
                    else l = q.pop();
                }

                if (!area.contains(l)) area.add(l);
                visited[l.x][l.y] = true;
                int[] boxColor = {255, 0, 0};

                iChannels[0][l.x][l.y] = (short) boxColor[0];
                iChannels[1][l.x][l.y] = (short) boxColor[1];
                iChannels[2][l.x][l.y] = (short) boxColor[2];

                if (checkValidity(l.x + 1, l.y, iChannels, visited)) q.push(new Location(l.x + 1, l.y));
                if (checkValidity(l.x - 1, l.y, iChannels, visited)) q.push(new Location(l.x - 1, l.y));
                if (checkValidity(l.x, l.y + 1, iChannels, visited)) q.push(new Location(l.x, l.y + 1));
                if (checkValidity(l.x, l.y - 1, iChannels, visited)) q.push(new Location(l.x, l.y - 1));
            }
        }
    }

    private boolean checkValidity(int row, int col, short[][][] iChannels, boolean[][] visited) {
        if (!Convo.inBound(iChannels[0], row, col)) return false;
        if (visited[row][col]) return false;
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

    private boolean hasLoc(ArrayList<Location> area, int x, int y) {
        for (int i = 0; i < area.size(); i++) {
            Location l = area.get(i);
            if (l.x == x && l.y == y) return true;
        }
        return false;
    }

}