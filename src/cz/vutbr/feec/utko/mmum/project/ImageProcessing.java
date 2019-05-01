package cz.vutbr.feec.utko.mmum.project;

import Jama.Matrix;

import java.util.LinkedList;

public class ImageProcessing {
    public static Matrix upsizeMatrix(int size, Matrix inputMatrix) {

        int height = inputMatrix.getRowDimension();
        int width = inputMatrix.getColumnDimension();

        int heightUp = height + (2 * size);
        int widthUp = width + (2 * size);

        double leftUpCorner = inputMatrix.get(0, 0);
        double rightUpCorner = inputMatrix.get(0, (width - 1));
        double leftDownCorner = inputMatrix.get((height - 1), 0);
        double rightDownCorner = inputMatrix.get((height - 1), (width - 1));

        Matrix leftUpCornerMatrix = new Matrix(size, size);
        Matrix rightUpCornerMatrix = new Matrix(size, size);
        Matrix leftDownCornerMatrix = new Matrix(size, size);
        Matrix rightDownCornerMatrix = new Matrix(size, size);

        Matrix up = new Matrix(size, width);
        Matrix down = new Matrix(size, width);
        Matrix left = new Matrix(width, size);
        Matrix right = new Matrix(width, size);

        Matrix tmpMatrix = new Matrix(heightUp, widthUp);

        // set corners matrix
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                leftUpCornerMatrix.set(i, j, leftUpCorner);
                rightUpCornerMatrix.set(i, j, rightUpCorner);
                leftDownCornerMatrix.set(i, j, leftDownCorner);
                rightDownCornerMatrix.set(i, j, rightDownCorner);
            }
        }

        // set up matrix
        for (int j = 0; j < width; j++) {
            for (int i = 0; i < size; i++) {
                up.set(i, j, inputMatrix.get(0, j));
            }
        }

        // set down matrix
        for (int j = 0; j < width; j++) {
            for (int i = 0; i < size; i++) {
                down.set(i, j, inputMatrix.get((height - 1), j));
            }
        }

        // set left matrix
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < size; j++) {
                left.set(i, j, inputMatrix.get(i, 0));
            }
        }

        // set right matrix
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < size; j++) {
                right.set(i, j, inputMatrix.get(i, (width - 1)));
            }
        }

        // set left up corner
        tmpMatrix.setMatrix(0, (size - 1), 0, (size - 1), leftUpCornerMatrix);
        // set left down corner
        tmpMatrix.setMatrix((heightUp - size), (heightUp - 1), 0, (size - 1), leftDownCornerMatrix);
        // set right up corner
        tmpMatrix.setMatrix(0, (size - 1), (widthUp - size), (widthUp - 1), rightUpCornerMatrix);
        // set right down corner
        tmpMatrix.setMatrix((heightUp - size), (heightUp - 1), (widthUp - size), (widthUp - 1), rightDownCornerMatrix);
        // set left side
        tmpMatrix.setMatrix(size, (heightUp - size - 1), 0, (size - 1), left);
        // set right side
        tmpMatrix.setMatrix(size, (heightUp - size - 1), (widthUp - size), (widthUp - 1), right);
        // set up side
        tmpMatrix.setMatrix(0, (size - 1), size, (widthUp - size - 1), up);
        // set down side
        tmpMatrix.setMatrix((heightUp - size), (heightUp - 1), size, (widthUp - size - 1), down);
        // set base
        tmpMatrix.setMatrix(size, (heightUp - size - 1), size, (widthUp - size - 1), inputMatrix);

        return tmpMatrix;
    }

    // fce for printing matrix in readable form
    public static void printMatrix(Matrix inputMatrix) {
        int height = inputMatrix.getRowDimension();
        int width = inputMatrix.getColumnDimension();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(inputMatrix.get(i, j) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // fce which compute SAD
    public static double SAD(Matrix inputMatrixA, Matrix inputMatrixB) {
        int height = inputMatrixA.getRowDimension();
        int width = inputMatrixA.getColumnDimension();
        double tmp = 0.0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tmp += Math.abs(inputMatrixA.get(i, j) - inputMatrixB.get(i, j));
            }
        }

        return tmp;
    }

    public static Matrix DPCM(Matrix inputMatrix1, Matrix inputMatrix2) {
        int height = inputMatrix1.getRowDimension();
        int width = inputMatrix1.getColumnDimension();

//		System.out.println("DPCM:");

        Matrix tmpMatrix = new Matrix(height, width);

        double tmpValue = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tmpValue = (((inputMatrix2.get(i, j) + 255) - inputMatrix1.get(i, j)) / 2);
                tmpMatrix.set(i, j, tmpValue);
//				System.out.print(tmpValue + " ");

            }
//			System.out.println();
        }
        return tmpMatrix;
    }

    public static LinkedList<int[]> fullSearchVectors(Matrix inputMatrix1, Matrix inputMatrix2, int macroblockSize) {
        int height = inputMatrix2.getRowDimension();
        int width = inputMatrix2.getColumnDimension();

        int startHeight;
        int startWidth;

        LinkedList<int[]> tmpList = new LinkedList<>();

        Matrix upsizedMatrixA = upsizeMatrix((macroblockSize / 2), inputMatrix1);

        Matrix macroblok;

        double tmpSad;
        double tmpSadBlock = 0;

        int heightV = 0;
        int widthV = 0;

        for (int i = 0; i < (height / macroblockSize); i++) {

            for (int j = 0; j < (width / macroblockSize); j++) {

                tmpSad = 100000.0;

                macroblok = inputMatrix2.getMatrix((i * macroblockSize), (((i + 1) * macroblockSize) - 1), (j * macroblockSize), (((j + 1) * macroblockSize) - 1));

                startHeight = i * macroblockSize;
                startWidth = j * macroblockSize;

                for (int m = 0; m <= macroblockSize; m++) {
                    for (int n = 0; n <= macroblockSize; n++) {

                        tmpSadBlock = SAD(macroblok, upsizedMatrixA.getMatrix((startHeight + m), ((startHeight + m + macroblockSize) - 1), (startWidth + n), ((startWidth + n + macroblockSize) - 1)));

                        if (tmpSad > tmpSadBlock) {
                            tmpSad = tmpSadBlock;
                            heightV = startHeight + m;
                            widthV = startWidth + n;
                        }

                    }
                }

//				System.out.print("( " + heightV + " , " + widthV + " ) ; ");
                tmpList.add(new int[]{heightV, widthV});

            }

//			System.out.println();

        }

        return tmpList;
    }

    public static Matrix fullSearchError(LinkedList<int[]> vectors, Matrix inputMatrix1, int macroblockSize, Matrix inputMatrix2) {
        int height = inputMatrix2.getRowDimension();
        int width = inputMatrix2.getColumnDimension();

        Matrix tmpMatrix = new Matrix(height, width);

        Matrix matrix2Recovered = fullSearchI(vectors, inputMatrix1, macroblockSize);

        tmpMatrix = inputMatrix2.minus(matrix2Recovered);

        return tmpMatrix;
    }

    public static Matrix fullSearchI(LinkedList<int[]> vectors, Matrix inputMatrix1, int macroblockSize) {
        int height = inputMatrix1.getRowDimension();
        int width = inputMatrix1.getColumnDimension();

        int heightV;
        int widthV;

        int[] tmpArray;

        Matrix tmpMatrix = new Matrix(height, width);
        Matrix upsizedMatrixB;

        for (int i = 0; i < (height / macroblockSize); i++) {
            for (int j = 0; j < (width / macroblockSize); j++) {

                Matrix macroblok;

                tmpArray = vectors.poll();
                heightV = tmpArray[0];
                widthV = tmpArray[1];

                upsizedMatrixB = upsizeMatrix((macroblockSize / 2), inputMatrix1);

                macroblok = upsizedMatrixB.getMatrix(heightV, ((heightV + macroblockSize) - 1), widthV, ((widthV + macroblockSize) - 1));

                tmpMatrix.setMatrix((i * macroblockSize), (((i + 1) * macroblockSize) - 1), (j * macroblockSize), (((j + 1) * macroblockSize) - 1), macroblok);
            }
        }
        return tmpMatrix;
    }


    public static Matrix fullSearchErrorI(Matrix matrix2Recovered, Matrix error) {
        int height = matrix2Recovered.getRowDimension();
        int width = matrix2Recovered.getColumnDimension();

        Matrix tmpMatrix = new Matrix(height, width);

        tmpMatrix = matrix2Recovered.plus(error);

        return tmpMatrix;
    }

    public static LinkedList<int[]> nStepSearchVectors(Matrix inputMatrix1, Matrix inputMatrix2, int macroblockSize, int n) {
        int height = inputMatrix2.getRowDimension();
        int width = inputMatrix2.getColumnDimension();

        int step = (int) Math.pow(2, (n - 1));

        int startHeight;
        int startWidth;

        LinkedList<int[]> tmpList = new LinkedList<>();

        Matrix upsizedMatrixA = upsizeMatrix((macroblockSize / 2), inputMatrix1);

        Matrix macroblock;

        int tmpHeight;
        int tmpWidth;

        double tmpSAD;
        double tmpSADBlock = 0;

        int heightV = 0;
        int widthV = 0;

        for (int i = 0; i < (height / macroblockSize); i++) {

            for (int j = 0; j < (width / macroblockSize); j++) {

                tmpSAD = 100000.0;

                macroblock = inputMatrix2.getMatrix((i * macroblockSize), (((i + 1) * macroblockSize) - 1), (j * macroblockSize), (((j + 1) * macroblockSize) - 1));

                startHeight = (i + 1) * macroblockSize;
                startWidth = (j + 1) * macroblockSize;

                for (int m = 0; m < n; m++) {

                    tmpHeight = startHeight - (macroblockSize / 2);
                    tmpWidth = startWidth - (macroblockSize / 2);

                    tmpSADBlock = SAD(macroblock, upsizedMatrixA.getMatrix(tmpHeight, ((tmpHeight + macroblockSize) - 1), tmpWidth, (tmpWidth + macroblockSize) - 1));

                    if (tmpSAD > tmpSADBlock) {
                        tmpSAD = tmpSADBlock;
                        heightV = tmpHeight;
                        widthV = tmpWidth;
                    }

                    for (int k = 0; k < 3; k++) {
                        for (int l = 0; l < 3; l++) {

                            tmpHeight = (((k * step) + startHeight) - step) - (macroblockSize / 2);
                            tmpWidth = (((l * step) + startHeight) - step) - (macroblockSize / 2);

                            tmpSADBlock = SAD(macroblock, upsizedMatrixA.getMatrix(tmpHeight, ((tmpHeight + macroblockSize) - 1), tmpWidth, (tmpWidth + macroblockSize) - 1));

                            if (tmpSAD > tmpSADBlock) {
                                tmpSAD = tmpSADBlock;
                                heightV = tmpHeight;
                                widthV = tmpWidth;
                            }
                        }
                    }

                    step = step / 2;

                    startHeight = heightV + (macroblockSize / 2);
                    startWidth = tmpWidth + (macroblockSize / 2);

                }

//				System.out.print("( " + heightV + " , " + widthV + " ) ; ");
                tmpList.add(new int[]{heightV, widthV});

            }

//			System.out.println();

        }

        return tmpList;
    }

    public static LinkedList<int[]> oneAtSearch(Matrix inputMatrix1, Matrix inputMatrix2, int macroblockSize) {
        int height = inputMatrix2.getRowDimension();
        int width = inputMatrix2.getColumnDimension();

        int startHeight;
        int startWidth;

        LinkedList<int[]> tmpList = new LinkedList<>();

        Matrix upsizedMatrixA = upsizeMatrix((macroblockSize / 2), inputMatrix1);

        Matrix macroblock;

        double tmpSADL;
        double tmpSADR;
        double tmpSADBlock = 0;

        int tmpHeight;
        int tmpWidth;

        int heightV = 0;
        int widthV = 0;


        int tmpL = 0;
        int tmpR = 0;

        for (int i = 0; i < (height / macroblockSize); i++) {

            for (int j = 0; j < (width / macroblockSize); j++) {

                macroblock = inputMatrix2.getMatrix((i * macroblockSize), (((i + 1) * macroblockSize) - 1), (j * macroblockSize), (((j + 1) * macroblockSize) - 1));


                startHeight = (i + 1) * macroblockSize;
                startWidth = (j + 1) * macroblockSize;

                tmpL = 1;
                tmpR = 1;

                while (true) {

                    tmpHeight = startHeight - (macroblockSize / 2);
                    tmpWidth = startWidth - (macroblockSize / 2);

                    tmpSADBlock = SAD(macroblock, upsizedMatrixA.getMatrix(tmpHeight, ((tmpHeight + macroblockSize) - 1), tmpWidth, (tmpWidth + macroblockSize) - 1));
                    tmpSADL = SAD(macroblock, upsizedMatrixA.getMatrix(tmpHeight, ((tmpHeight + macroblockSize) - 1), tmpWidth - 1, (tmpWidth + macroblockSize) - 2));
                    tmpSADR = SAD(macroblock, upsizedMatrixA.getMatrix(tmpHeight, ((tmpHeight + macroblockSize) - 1), tmpWidth + 1, (tmpWidth + macroblockSize)));

                    if (tmpSADBlock <= tmpSADL && tmpSADBlock <= tmpSADR) {
                        break;
                    } else if (tmpSADL <= tmpSADR) {
                        startWidth--;
                        tmpL++;
                    } else {
                        startWidth++;
                        tmpR++;
                    }

                    if (tmpL == (macroblockSize / 2) || tmpR == (macroblockSize / 2)) {
                        break;
                    }
                }

                tmpL = 1;
                tmpR = 1;

                while (true) {

                    tmpHeight = startHeight - (macroblockSize / 2);
                    tmpWidth = startWidth - (macroblockSize / 2);

                    tmpSADBlock = SAD(macroblock, upsizedMatrixA.getMatrix(tmpHeight, ((tmpHeight + macroblockSize) - 1), tmpWidth, (tmpWidth + macroblockSize) - 1));
                    tmpSADL = SAD(macroblock, upsizedMatrixA.getMatrix(tmpHeight - 1, ((tmpHeight + macroblockSize) - 2), tmpWidth, (tmpWidth + macroblockSize) - 1));
                    tmpSADR = SAD(macroblock, upsizedMatrixA.getMatrix(tmpHeight + 1, ((tmpHeight + macroblockSize)), tmpWidth, (tmpWidth + macroblockSize) - 1));

                    if (tmpSADBlock <= tmpSADL && tmpSADBlock <= tmpSADR) {
                        break;
                    } else if (tmpSADL <= tmpSADR) {
                        startHeight--;
                        tmpL++;
                    } else {
                        startHeight++;
                        tmpR++;
                    }

                    if (tmpL == (macroblockSize / 2) || tmpR == (macroblockSize / 2)) {
                        break;
                    }
                }


                heightV = startHeight - (macroblockSize / 2);
                widthV = startWidth - (macroblockSize / 2);

//				System.out.print("( " + heightV + " , " + widthV + " ) ; ");
                tmpList.add(new int[]{heightV, widthV});

            }

//			System.out.println();

        }

        return tmpList;
    }

}
