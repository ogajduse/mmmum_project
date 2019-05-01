package cz.vutbr.feec.utko.mmum.project;

import Jama.Matrix;
import ij.ImagePlus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

class ColorTransform {
    private BufferedImage bImage;
    private ColorModel colorModel;
    private int imageHeight;
    private int imageWidth;

    // color components
    private int[][] red;
    private int[][] green;
    private int[][] blue;

    private Matrix y;
    private Matrix cB;
    private Matrix cR;

    public int[][] getRed() {
        return red;
    }

    public int[][] getGreen() {
        return green;
    }

    public int[][] getBlue() {
        return blue;
    }

    public Matrix getY() {
        return y;
    }

    public Matrix getcB() {
        return cB;
    }

    public Matrix getcR() {
        return cR;
    }

    public void setY(Matrix y) {
        this.y = y;
    }

    public void setcB(Matrix cB) {
        this.cB = cB;
    }

    public void setcR(Matrix cR) {
        this.cR = cR;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public static final double[][] quantizationMatrix8Y = {
            {16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}};


    public double[][] getQuantizationMatrix8Y() {
        return quantizationMatrix8Y;
    }

    public static final double[][] quantizationMatrix8C = {
            {17, 18, 24, 47, 99, 99, 99, 99},
            {18, 21, 26, 66, 99, 99, 99, 99},
            {24, 26, 56, 99, 99, 99, 99, 99},
            {47, 66, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99}};

    public double[][] getQuantizationMatrix8C() {
        return quantizationMatrix8C;
    }

    public ColorTransform(BufferedImage bImage) {
        this.bImage = bImage;
        this.colorModel = bImage.getColorModel();
        this.imageHeight = bImage.getHeight();
        this.imageWidth = bImage.getWidth();
        red = new int[this.imageHeight][this.imageWidth];
        green = new int[this.imageHeight][this.imageWidth];
        blue = new int[this.imageHeight][this.imageWidth];
        y = new Matrix(this.imageHeight, this.imageWidth);
        cB = new Matrix(this.imageHeight, this.imageWidth);
        cR = new Matrix(this.imageHeight, this.imageWidth);
    }

    public void getRGB() {
        for (int i = 0; i < this.imageHeight; i++) {
            for (int j = 0; j < this.imageWidth; j++) {
                red[i][j] = colorModel.getRed(this.bImage.getRGB(j, i));
                green[i][j] = colorModel.getGreen(this.bImage.getRGB(j, i));
                blue[i][j] = colorModel.getBlue(this.bImage.getRGB(j, i));
            }
        }
    }

    public ImagePlus setImageFromRGB(int width, int height, int[][] r, int[][] g, int[][] b) {
        BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[][] rgb = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rgb[i][j] = new Color(r[i][j], g[i][j], b[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus("", bImage));
    }

    public ImagePlus setImageFromRGB(int width, int height, int[][] x, String component) {
        BufferedImage bImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int[][] rgb = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rgb[i][j] = new Color(x[i][j], x[i][j], x[i][j]).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus(component, bImage));
    }

    public ImagePlus setImageFromRGB(int width, int height, Matrix x,
                                     String component) {
        BufferedImage bImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int[][] rgb = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                rgb[i][j] = new Color((int) x.get(i, j), (int) x.get(i, j),
                        (int) x.get(i, j)).getRGB();
                bImage.setRGB(j, i, rgb[i][j]);
            }
        }
        return (new ImagePlus(component, bImage));
    }

    public void convertRgbToYcbcr() {
        for (int i = 0; i < this.imageHeight; i++) {
            for (int j = 0; j < this.imageWidth; j++) {
                y.set(i, j, 0.257 * red[i][j] + 0.504 * green[i][j] + 0.098 * blue[i][j] + 16);
                cB.set(i, j, -0.148 * red[i][j] - 0.291 * green[i][j] + 0.439 * blue[i][j] + 128);
                cR.set(i, j, 0.439 * red[i][j] - 0.368 * green[i][j] - 0.071 * blue[i][j] + 128);
            }
        }
    }

    public void convertYcbcrToRgb() {
        for (int i = 0; i < this.imageHeight; i++) {
            for (int j = 0; j < this.imageWidth; j++) {
                red[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16) + 1.596 * (cR.get(i, j) - 128));
                if (red[i][j] > 255) red[i][j] = 255;
                if (red[i][j] < 0) red[i][j] = 0;
                green[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16) - 0.813 * (cR.get(i, j) - 128) - 0.391 * (cB.get(i, j) - 128));
                if (green[i][j] > 255) green[i][j] = 255;
                if (green[i][j] < 0) green[i][j] = 0;
                blue[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16) + 2.018 * (cB.get(i, j) - 128));
                if (blue[i][j] > 255) blue[i][j] = 255;
                if (blue[i][j] < 0) blue[i][j] = 0;
            }
        }
    }

    public Matrix downsample(Matrix mat) {
        Matrix newMat = new Matrix(mat.getRowDimension(), mat.getColumnDimension() / 2);
        for (int i = 0; i < mat.getColumnDimension(); i = i + 2) {
            newMat.setMatrix(0, mat.getRowDimension() - 1, i / 2, i / 2, mat.getMatrix(0, mat.getRowDimension() - 1, i, i));
        }
        return newMat;
    }

    public Matrix oversample(Matrix mat) {
        Matrix newMat = new Matrix(mat.getRowDimension(), mat.getColumnDimension() * 2);
        for (int i = 0; i < mat.getColumnDimension(); i++) {
            newMat.setMatrix(0, mat.getRowDimension() - 1, 2 * i, 2 * i, mat.getMatrix(0, mat.getRowDimension() - 1, i, i));
            newMat.setMatrix(0, mat.getRowDimension() - 1, 2 * i + 1, 2 * i + 1, mat.getMatrix(0, mat.getRowDimension() - 1, i, i));
        }
        return newMat;

    }

    public Matrix transform(int size, Matrix transformMatrix, Matrix inputMatrix) {
        Matrix out = transformMatrix.times(inputMatrix);
        out = out.times(transformMatrix.transpose());
        return (out);
    }

    public Matrix inverseTransform(int size, Matrix transformMatrix, Matrix inputMatrix) {
        Matrix out = transformMatrix.transpose().times(inputMatrix);
        out = out.times(transformMatrix);
        return (out);
    }

    public Matrix quantize(int size, Matrix inputMatrix, Matrix quantizeMatrix) {
        double pom;
        double[][] result = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                pom = inputMatrix.get(i, j) / quantizeMatrix.get(i, j);
                if (pom > 0)
                    result[i][j] = Math.floor(pom);
                else
                    result[i][j] = Math.ceil(pom);
            }
        }
        return (new Matrix(result));
    }

    public Matrix inverseQuantize(int size, Matrix inputMatrix, Matrix quantizeMatrix) {

        double[][] result = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = inputMatrix.get(i, j) * quantizeMatrix.get(i, j);
            }
        }
        return (new Matrix(result));
    }
}
