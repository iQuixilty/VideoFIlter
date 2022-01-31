import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class FloodFill {
    private final short[] COLOR;
    private final int[] COLOR_D;
    private final int AREA_THRESHOLD;

    private final short[] BACKGROUND_COLOR = {70, 40, 0};


    public FloodFill(short[] color, int[] colorD, int areaThreshold) {
        this.COLOR = color;
        this.COLOR_D = colorD;
        this.AREA_THRESHOLD = areaThreshold;
    }

    public void processImage(short[][][] iChannels) {
        ArrayList<ArrayList<Location>> filledAreas = new ArrayList<>();
        // ArrayList<ArrayList<int[]>> filledAreas = new ArrayList<>();
        int h = iChannels[0].length;
        int w = iChannels[0][0].length;
        short[][][] tempChannels = {new short[h][w], new short[h][w], new short[h][w]};

        copyTo(iChannels, tempChannels);
        boolean[][] colorBounds = genColorBounds(iChannels);


        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (inColorBounds(tempChannels[0][r][c], tempChannels[1][r][c], tempChannels[2][r][c], COLOR)) {
                    ArrayList<Location> a = new ArrayList<>();
                    runFloodFill(tempChannels, r, c, a, colorBounds);
                    if (a.size() > AREA_THRESHOLD) filledAreas.add(a);
                }
            }
        }

        // copyTo(tempChannels, iChannels);

        createBoundaries(iChannels, filledAreas);


    }

    private void createBoundaries(short[][][] iChannels, ArrayList<ArrayList<Location>> filledAreas) {
        for (ArrayList<Location> area : filledAreas) {
            int minX = iChannels[0].length, minY = iChannels[0][0].length, maxX = 0, maxY = 0;
            for (Location l : area) {
                if (l.y > maxY) maxY = l.y;
                if (l.y < minY) minY = l.y;

                if (l.x > maxX) maxX = l.x;
                if (l.x < minX) minX = l.x;
            }


            int[] boxColor = colorDetect(minX, maxX, minY, maxY, iChannels);
            System.out.println(shapeDetect(minX, maxX, minY, maxY, iChannels));


            for (int c = 0; c < boxColor.length; c++) {
                for (int i = minX; i <= maxX; i++) {
                    iChannels[c][i][minY] = (short) boxColor[c];
                    iChannels[c][i][minY + ((maxY - minY) / 2)] = (short) boxColor[c];
                    iChannels[c][i][maxY] = (short) boxColor[c];
                }

                for (int i = minY; i <= maxY; i++) {
                    iChannels[c][minX][i] = (short) boxColor[c];
                    iChannels[c][minX + ((maxX - minX) / 2)][i] = (short) boxColor[c];
                    iChannels[c][maxX][i] = (short) boxColor[c];
                }

            }
        }
    }

    private String shapeDetect(int minX, int maxX, int minY, int maxY, short[][][] iChannels) {
        int count = 0;
        int fillSize = (maxX - minX) * (maxY - minY);
        for (int row = minX; row < maxX; row++) {
            for (int col = minY; col < maxY; col++) {
                short r = iChannels[0][row][col], g = iChannels[1][row][col], b = iChannels[2][row][col];
                if (!inColorBounds(r, g, b, COLOR) && !inColorBounds(r, g, b, BACKGROUND_COLOR)) count++;
            }
        }

        int numOfShapes = calcNumOfShapes(minX, maxX, minY, iChannels);
        double ratio = (((count * 100.0) / fillSize)) / numOfShapes;

        if (ratio > 12) return "Circle " + ratio;
        else if (ratio < 12 && ratio > 6.1) return "Squiggly " + ratio;
        else return "Diamond " + ratio;
    }

    private int calcNumOfShapes(int minX, int maxX, int minY, short[][][] iChannels) {
        int start = minY, midline = minX + ((maxX - minX) / 2);

        while (!inColorBounds(iChannels[0][midline][start], iChannels[1][midline][start], iChannels[2][midline][start], COLOR))
            start++;
        while (inColorBounds(iChannels[0][midline][start], iChannels[1][midline][start], iChannels[2][midline][start], COLOR))
            start++;

        int diff = start - minY;
        if (diff > 70) return 1;
        else if (diff > 45 && diff < 70) return 2;
        else return 3;
    }

    private int[] colorDetect(int minX, int maxX, int minY, int maxY, short[][][] iChannels) {
        int[] borderC = {0, 0, 0};
        int sR = 0, sG = 0, sB = 0, c = 0;

        for (int row = minX; row < maxX; row++) {
            for (int col = minY; col < maxY; col++) {
                short r = iChannels[0][row][col], g = iChannels[1][row][col], b = iChannels[2][row][col];
                if (!inColorBounds(r, g, b, COLOR)) {
                    sR += r;
                    sG += g;
                    sB += b;
                    c++;
                }
            }
        }

        int[] avgs = {sR / c, sG / c, sB / c};
        int maxColor = Math.max(Math.max(avgs[0], avgs[1]), avgs[2]);

        if ((maxColor == avgs[0]) && (Math.abs(avgs[0] - avgs[2]) > 70)) borderC[0] = 255;
        else if (maxColor == avgs[1]) borderC[1] = 255;
        else borderC[2] = 255;

        return borderC;
    }

    private void runFloodFill(short[][][] iChannels, int row, int col, ArrayList<Location> area, boolean[][] colorBounds) {
        boolean[][] visited = new boolean[iChannels[0].length][iChannels[0][0].length];
        HashMap<Location, Integer> temp = new HashMap<>();
        if (Convo.inBound(iChannels[0], row, col)) {
            ArrayDeque<Location> q = new ArrayDeque<>();
            q.add(new Location(row, col));

            while (q.size() > 0) {
                Location l = q.pop();
                if (visited[l.x][l.y]) {
                    if (q.size() == 0) return;
                    else continue;
                }

                if (!temp.containsKey(l)) {
                    area.add(l);
                    temp.put(l, 1);
                }
                visited[l.x][l.y] = true;
                int[] boxColor = {0, 255, 0};

                iChannels[0][l.x][l.y] = (short) boxColor[0];
                iChannels[1][l.x][l.y] = (short) boxColor[1];
                iChannels[2][l.x][l.y] = (short) boxColor[2];

                if (checkValidity(l.x + 1, l.y, iChannels, visited, colorBounds)) q.push(new Location(l.x + 1, l.y));
                if (checkValidity(l.x - 1, l.y, iChannels, visited, colorBounds)) q.push(new Location(l.x - 1, l.y));
                if (checkValidity(l.x, l.y + 1, iChannels, visited, colorBounds)) q.push(new Location(l.x, l.y + 1));
                if (checkValidity(l.x, l.y - 1, iChannels, visited, colorBounds)) q.push(new Location(l.x, l.y - 1));
            }
        }
    }

    private void runIntFloodFill(short[][][] iChannels, int row, int col, ArrayList<int[]> area, boolean[][] colorBounds) {
        boolean[][] visited = new boolean[iChannels[0].length][iChannels[0][0].length];
        HashMap<int[], Integer> temp = new HashMap<>();
        if (Convo.inBound(iChannels[0], row, col)) {
            ArrayDeque<int[]> q = new ArrayDeque<>();
            q.add(new int[]{row, col});

            while (q.size() > 0) {
                int[] l = q.pop();
                if (visited[l[0]][l[1]]) {
                    if (q.size() == 0) return;
                    else continue;
                }

                if (!temp.containsKey(l)) {
                    area.add(l);
                    temp.put(l, 1);
                }
                visited[l[0]][l[1]] = true;
                int[] boxColor = {255, 0, 0};

                iChannels[0][l[0]][l[1]] = (short) boxColor[0];
                iChannels[1][l[0]][l[1]] = (short) boxColor[1];
                iChannels[2][l[0]][l[1]] = (short) boxColor[2];

                if (checkValidity(l[0] + 1, l[1], iChannels, visited, colorBounds)) q.push(new int[]{l[0] + 1, l[1]});
                if (checkValidity(l[0] - 1, l[1], iChannels, visited, colorBounds)) q.push(new int[]{l[0] - 1, l[1]});
                if (checkValidity(l[0], l[1] + 1, iChannels, visited, colorBounds)) q.push(new int[]{l[0], l[1] + 1});
                if (checkValidity(l[0], l[1] - 1, iChannels, visited, colorBounds)) q.push(new int[]{l[0], l[1] - 1});
            }
        }
    }

    private void createIntBoundaries(short[][][] iChannels, ArrayList<ArrayList<int[]>> filledAreas) {
        for (ArrayList<int[]> area : filledAreas) {
            int minX = iChannels[0].length, minY = iChannels[0][0].length, maxX = 0, maxY = 0;
            for (int[] l : area) {
                if (l[1] > maxY) maxY = l[1];
                if (l[1] < minY) minY = l[1];

                if (l[0] > maxX) maxX = l[0];
                if (l[0] < minX) minX = l[0];
            }

            int[] boxColor = {0, 255, 0};


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

    private void copyTo(short[][][] input, short[][][] output) {
        for (int r = 0; r < input[0].length; r++) {
            for (int c = 0; c < input[0][0].length; c++) {
                output[0][r][c] = input[0][r][c];
                output[1][r][c] = input[1][r][c];
                output[2][r][c] = input[2][r][c];
            }
        }
    }

    private boolean checkValidity(int row, int col, short[][][] iChannels, boolean[][] visited, boolean[][] colorBounds) {
        if (!Convo.inBound(iChannels[0], row, col)) return false;
        if (visited[row][col]) return false;
        short r = iChannels[0][row][col];
        short g = iChannels[1][row][col];
        short b = iChannels[2][row][col];

        return ((r != 0 && g != 0 && b != 255) && colorBounds[row][col]);
    }

    private boolean[][] genColorBounds(short[][][] iChannels) {
        boolean[][] colorBounds = new boolean[iChannels[0].length][iChannels[0][0].length];

        for (int row = 0; row < iChannels[0].length; row++) {
            for (int col = 0; col < iChannels[0][0].length; col++) {
                short r = iChannels[0][row][col];
                short g = iChannels[1][row][col];
                short b = iChannels[2][row][col];

                colorBounds[row][col] = ((r > COLOR[0] - COLOR_D[0] && r < COLOR[0] + COLOR_D[0])
                        && (g > COLOR[1] - COLOR_D[1] && g < COLOR[1] + COLOR_D[1])
                        && (b > COLOR[2] - COLOR_D[2] && b < COLOR[2] + COLOR_D[2]));
            }
        }

        return colorBounds;
    }

    private boolean inColorBounds(short r, short g, short b, short[] color) {
        return ((r > color[0] - COLOR_D[0] && r < color[0] + COLOR_D[0])
                && (g > color[1] - COLOR_D[1] && g < color[1] + COLOR_D[1])
                && (b > color[2] - COLOR_D[2] && b < color[2] + COLOR_D[2]));
    }

    private int getCDist(short r, short g, short b) {
        int dR = r - COLOR_D[0], dG = g - COLOR_D[1], dB = b - COLOR_D[2];
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