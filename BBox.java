public class BBox implements PixelFilter {
    @Override
    public DImage processImage(DImage img) {
        long startTime = System.currentTimeMillis();
        short[][][] iChannels = {img.getRedChannel(), img.getGreenChannel(), img.getBlueChannel()};

        short[] color = {240, 240, 240};
        int[] colorD = {40, 40, 40};

        FloodFill flooded = new FloodFill(color, colorD,  6000);
        flooded.processImage(iChannels);

        img.setColorChannels(iChannels[0], iChannels[1], iChannels[2]);

        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime + " ms");

        return img;

    }

    public static void createBBox(short[][] pixels, int width, int height) {
        int tX = 0, tY = 0;
        L:
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                if (pixels[r][c] == 255) {
                    tX = c;
                    tY = r;
                    break L;
                }
            }
        }

        int[] minXS = {pixels[0].length, pixels[0].length}, minYS = {pixels.length, pixels.length}, maxXS = new int[2], maxYS = new int[2];

        for (int i = 0; i <= 1; i++) {
            for (int r = tY - 20; r < height + tY + 40; r++) {
                for (int c = (i * width) + tX + (i * 30) - 10; c < (i * width) + width + tX + (i * 30) + 10; c++) {
                    if (Convo.inBound(pixels, r, c)) {
                        if (pixels[r][c] == 255) {
                            if (r > maxYS[i]) maxYS[i] = r;
                            if (r < minYS[i]) minYS[i] = r;

                            if (c > maxXS[i]) maxXS[i] = c;
                            if (c < minXS[i]) minXS[i] = c;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = minXS[i]; j <= maxXS[i]; j++) {
                pixels[minYS[i]][j] = 120;
                pixels[maxYS[i]][j] = 120;
            }

            for (int j = minYS[i]; j <= maxYS[i]; j++) {
                pixels[j][minXS[i]] = 120;
                pixels[j][maxXS[i]] = 120;
            }
        }
    }

    public static void highlightBox(short[][] pixels, short[][][] oChannels) {
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
//                oChannels[0][r][c] = pixels[r][c];
//                oChannels[1][r][c] = pixels[r][c];
//                oChannels[2][r][c] = pixels[r][c];
                if (pixels[r][c] == 120) oChannels[1][r][c] = 255;
            }
        }
    }
}
