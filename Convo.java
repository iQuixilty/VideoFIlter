import java.util.Arrays;

public class Convo implements PixelFilter {
    private double[][] blurKernel = {
            {1.0 / 9, 1.0 / 9, 1.0 / 9},
            {1.0 / 9, 1.0 / 9, 1.0 / 9},
            {1.0 / 9, 1.0 / 9, 1.0 / 9}
    };

    private double[][] outlineKernel = {
            {-1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, 48, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1},
    };

    private double[][] embossKernel = {
            {-2, -1, 0},
            {-1, 1, 1},
            {0, 1, 2}
    };

    private double[][] sobelGX = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
    };

    private double[][] sobelGY = {
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
    };

    public double[][] gaussianKernel = {
            {1 / 256.0, 4 / 256.0, 6 / 256.0, 4 / 256.0, 1 / 256.0},
            {4 / 256.0, 16 / 256.0, 24 / 256.0, 16 / 256.0, 4 / 256.0},
            {6 / 256.0, 24 / 256.0, 36 / 256.0, 24 / 256.0, 6 / 256.0},
            {4 / 256.0, 16 / 256.0, 24 / 256.0, 16 / 256.0, 4 / 256.0},
            {1 / 256.0, 4 / 256.0, 6 / 256.0, 4 / 256.0, 1 / 256.0},
    };

    private short[][] thinningL = {
            {0, 0, 0},
            {-1, 1, -1},
            {1, 1, 1}
    };

    private short[][] thinningR = {
            {-1, 0, 0},
            {1, 1, 0},
            {-1, 1, -1}
    };

    @Override
    public DImage processImage(DImage img) {
        short[][] pixels = img.getBWPixelGrid(); // input pixels
        short[][] sobelPixels = img.getBWPixelGrid(); // sobel pixels
        short[][] outputPixels = img.getBWPixelGrid(); // thinned sobel pixels
        short[][][] oChannels = {img.getRedChannel(), img.getGreenChannel(), img.getBlueChannel()};

        edgeDetect(pixels, sobelPixels, outputPixels); // method to run all other methods
        BBox.highlightBox(outputPixels, oChannels);
        img.setPixels(outputPixels);
        img.setColorChannels(oChannels[0], oChannels[1], oChannels[2]);
        return img;
    }

    /**
     * Returns the output array of thinned sobel pixels
     *
     * @param pixels       input arr
     * @param sobelPixels  sobel arr (temporarily used)
     * @param outputPixels final array used to thin the sobel arr
     */
    private void edgeDetect(short[][] pixels, short[][] sobelPixels, short[][] outputPixels) {
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                applyKernel(pixels, pixels, r, c, gaussianKernel);
            }
        }
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                applySobelKernel(pixels, sobelPixels, r, c);
            }
        }
        applyThreshold(sobelPixels, 200);
//        AThin(sobelPixels, outputPixels);
        thin(sobelPixels, outputPixels, thinningL, thinningR);
        BBox.createBBox(outputPixels, 290, 410);
    }

    /**
     * Personal thinning algorithm
     *
     * @param pixels sobel arr
     * @param output thinned sobel arr
     */
    private void AThin(short[][] pixels, short[][] output) {
        short[][] temp = new short[pixels.length][pixels[0].length];
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                applyAThinning(pixels, output, r, c); // for each pixel in the array, apply personal thinning
            }
        }
//
//        for (int r = 0; r < pixels.length; r++) {
//            for (int c = 0; c < pixels[0].length; c++) {
//                applyAThinning(temp, output, r, c);
//            }
//        }
    }

    /**
     * Outputs the personal thinning algorithm applied to pixel
     *
     * @param pixels
     * @param output
     * @param row
     * @param col
     */
    private void applyAThinning(short[][] pixels, short[][] output, int row, int col) {
        short c = 0;
        L:
        for (int i = row; i < row + 3; i++) {
            for (int j = col; j < col + 3; j++) {
                if (inBound(pixels, i, j)) {
                    if (pixels[i][j] == 255 && pixels[row][col] == 255)
                        c++; // if the neighboring pixel is white, increment counter
                }
            }
        }

        if (c > 7)
            output[row][col] = 255; // if the pixel is surrounded by more than 7 white pixels, it remains as a white px
        else output[row][col] = 0; // else its black
    }

    /**
     * Method to thin the arr
     *
     * @param pixels input arr
     * @param output output arr
     * @param left   thinning left kernel
     * @param right  thinning right kernel
     */
    private void thin(short[][] pixels, short[][] output, short[][] left, short[][] right) {
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                if (pixels[r][c] == 255) pixels[r][c] = 1; // changes input arr to binary image (0's and 1's)
            }
        }

        short[][] lt = new short[pixels.length][pixels[0].length]; // 2 arrays for thinning (independent of each other)
        short[][] rt = new short[pixels.length][pixels[0].length];

        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                for (int deg = 0; deg < 1; deg++) { // amount of times the thinning arrays are rotated
                    applyThinning(pixels, lt, r, c, rotate(left, deg)); // applies the thinning to the px
                    applyThinning(pixels, rt, r, c, rotate(right, deg));
                }
            }
        }

        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                if (lt[r][c] == 1 || rt[r][c] == 1) output[r][c] = 255; // changes image back to grayscale (non-binary)
                else output[r][c] = 0;
            }
        }
    }

    /**
     * Superimposes thinning kernels on the image to check if it matches
     *
     * @param pixels       input arr
     * @param outputPixels output arr
     * @param row
     * @param col
     * @param kernel       thinning kernel
     */
    private void applyThinning(short[][] pixels, short[][] outputPixels, int row, int col, short[][] kernel) {
        short clr = 1;
        int kL = kernel.length;
        L:
        for (int i = row; i < row + kL; i++) {
            for (int j = col; j < col + kL; j++) {
                if (inBound(pixels, i, j)) {
                    short val = pixels[i][j]; // gets value in image
                    short weight = kernel[i - row][j - col]; // gets value in kernel
                    if (weight == -1) break; // if kernel value is -1, ignore the value (doesn't need to be matched)

                    if (weight != val) { // if the weight and val don't match, kernel doesn't fit the image
                        clr = 0;
                        break L; // break out of loop and set color to 0
                    }
                }
            }
        }

        outputPixels[row][col] = clr;
    }

    /**
     * Rotates arr n number of times
     *
     * @param arr input arr to be rotated
     * @param deg times rotated
     * @return rotated arr
     */
    private short[][] rotate(short[][] arr, int deg) {
        // Changes rows to columns
        for (int r = 0; r < deg; r++) {
            for (int i = 0; i < 3; i++) {
                for (int j = i; j < 3; j++) {
                    short temp = arr[i][j];
                    arr[i][j] = arr[j][i];
                    arr[j][i] = temp;
                }
            }

            // Vertically flips the arr
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3 / 2; j++) {
                    short temp = arr[i][j];
                    arr[i][j] = arr[i][2 - j];
                    arr[i][2 - j] = temp;
                }
            }
        }
        return arr;
    }

    /**
     * Method to apply the threshold to the input
     *
     * @param sobelPixels
     * @param t           threshold (can be set manually or mathematically)
     */
    private void applyThreshold(short[][] sobelPixels, int t) {
        for (int r = 0; r < sobelPixels.length; r++) {
            for (int c = 0; c < sobelPixels[0].length; c++) {
                if (sobelPixels[r][c] > t) sobelPixels[r][c] = 255;
                else sobelPixels[r][c] = 0;
            }
        }
    }

    /**
     * Calculates the threshold of the specified image through math
     *
     * @param sobelPixels
     * @return threshold
     */
    private int calcThreshold(short[][] sobelPixels) {
        int[] pixels = new int[256];
        for (int r = 0; r < sobelPixels.length; r++) {
            for (int c = 0; c < sobelPixels[0].length; c++) {
                pixels[sobelPixels[r][c]]++; // array of color values, every pixel color is added to the pixels arr
            }
        }

        int nonBlackPixels = (sobelPixels.length * sobelPixels[0].length) - pixels[0];
        int t = (int) (0.75 * nonBlackPixels); // finds the most concentration of pixels
        int sum = 0, threshold = 0;
        for (int i = 1; i < pixels.length; i++) {
            sum += pixels[i];
            if (sum > t) { // if the sum of color values exceeds t, then the threshold is i
                threshold = i;
                break;
            }
        }

        return threshold;
    }

    /**
     * Same as appling a kernel (loops through kernel and image and applies the kernel to it)
     *
     * @param pixels
     * @param outputPixels
     * @param row
     * @param col
     */
    private void applySobelKernel(short[][] pixels, short[][] outputPixels, int row, int col) {
        double x = 0;
        double y = 0;

        int kL = sobelGX.length;
        for (int i = row; i < row + kL; i++) {
            for (int j = col; j < col + kL; j++) {
                if (inBound(pixels, i, j)) {
                    short val = pixels[i][j];
                    double gXW = sobelGX[i - row][j - col];
                    double gYW = sobelGY[i - row][j - col];

                    x += val * gXW;
                    y += val * gYW;
                }
            }
        }

        double val = Math.sqrt(x * x + y * y);
        if (val < 0) val = 0;
        if (val > 255) val = 255;

        outputPixels[row][col] = (short) (val);
    }


    private void applyKernel(short[][] pixels, short[][] outputPixels, int row, int col, double[][] kernel) {
        double sum = 0;
        int kL = kernel.length;
        for (int i = row; i < row + kL; i++) {
            for (int j = col; j < col + kL; j++) {
                if (inBound(pixels, i, j)) {
                    short val = pixels[i][j];
                    double weight = kernel[i - row][j - col];

                    sum += val * weight;
                }
            }
        }
        if (sum < 0) sum = 0;
        if (sum > 255) sum = 255;

        outputPixels[row][col] = (short) (sum);
    }


    /**
     * Not really relevant (used to apply a kernel to a colored image (non-bw)
     *
     * @param iChannels 3D array of input colors
     * @param oChannels 3D array of output colors
     * @param row
     * @param col
     * @param kernel
     */
    private void applyKernel(short[][][] iChannels, short[][][] oChannels, int row, int col, double[][] kernel) {
        double[] sums = new double[3];
        int kL = kernel.length;
        for (int i = row; i < row + kL; i++) {
            for (int j = col; j < col + kL; j++) {
                if (inBound(iChannels[0], i, j)) {
                    short[] cVals = {iChannels[0][i][j], iChannels[1][i][j], iChannels[2][i][j]};
                    double weight = kernel[i - row][j - col];

                    sums[0] += cVals[0] * weight;
                    sums[1] += cVals[1] * weight;
                    sums[2] += cVals[2] * weight;
                }
            }
        }

        for (int i = 0; i < sums.length; i++) {
            if (sums[i] < 0) sums[i] = 0;
            if (sums[i] > 255) sums[i] = 255;

            oChannels[i][row][col] = (short) (sums[i]);
        }
    }

    /**
     * Checks if the index is in bounds of the arr
     *
     * @param arr
     * @param r
     * @param c
     * @return
     */
    public static boolean inBound(short[][] arr, int r, int c) {
        return (arr.length - 1 >= r && arr[0].length - 1 >= c && r >= 0 && c >= 0);
    }
}
