public class Test {
    public static void main(String[] args) {
        short[][] kernel = {
                {0, 1, 0},
                {0, 1, 0},
                {0, 1, 0}
        };

        short[][] arr = {
                {0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0},
        };


        applyThinning(arr, 1, 1,  kernel);
    }


    public static void applyThinning(short[][] pixels, int row, int col, short[][] kernel) {
        int c = 0;
        int kL = kernel.length;
        L: for (int i = row; i < row + kL; i++) {
            for (int j = col; j < col + kL; j++) {
                if (Convo.inBound(pixels, i,  j)) {
                    short val = pixels[i][j];
                    short weight = kernel[i - row][j - col];
                    if (val == weight) c++;
                }
            }
        }

        if (c == 9) System.out.println(true);

        else System.out.println(false);
    }

}
