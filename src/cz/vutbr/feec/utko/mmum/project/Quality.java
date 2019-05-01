package cz.vutbr.feec.utko.mmum.project;

class Quality {
    static double getMse(int[][] original, int[][] edited) {
        int width = original.length;
        int height = original[0].length;
        double mse = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                mse += Math.pow(original[i][j] - edited[i][j], 2);
            }
        }
        mse = mse / (width * height);
        return mse;
    }

    static double getPsnr(int[][] original, int[][] edited) {
        return 10 * Math.log10(Math.pow(255, 2) / getMse(original, edited));
    }
}
