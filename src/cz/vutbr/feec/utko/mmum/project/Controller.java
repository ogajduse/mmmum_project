package cz.vutbr.feec.utko.mmum.project;

import Jama.Matrix;
import ij.ImagePlus;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final int RED = 1;
    private static final int GREEN = 2;
    private static final int BLUE = 3;
    private static final int Y = 4;
    private static final int CB = 5;
    private static final int CR = 6;
    private static final int S444 = 7;
    private static final int S422 = 8;
    private static final int S420 = 9;
    private static final int S411 = 10;
    private int macroBlockSize;
    private int sampling = S444;

    private Matrix quantizationMatrix8Y;
    private Matrix quantizationMatrix8C;

    private ImagePlus imagePlus;
    private ColorTransform colorTransform;
    private ColorTransform colorTransformSquare1;
    private ColorTransform colorTransformSquare2;
    private ColorTransform colorTransformOrig;

    private String blockSize;

    @FXML
    private ChoiceBox<String> choiceBoxN;

    @FXML
    private Button redButton;
    @FXML
    private Button greenButton;
    @FXML
    private Button blueButton;
    @FXML
    private Button yButton;
    @FXML
    private Button cbButton;
    @FXML
    private Button crButton;
    @FXML
    private Label psnrLabel;
    @FXML
    private Label sadLabel1;
    @FXML
    private Label sadLabel2;
    @FXML
    private Label sadLabel3;
    @FXML
    private Label mseLabel;
    @FXML
    private Slider qualitySlider;

    @FXML
    private RadioButton radioButton8x8;
    @FXML
    private RadioButton radioButton4x4;
    @FXML
    private RadioButton radioButton2x2;

    private final ToggleGroup transformBlockSizeGroup = new ToggleGroup();
    private RadioButton selectedTransformBlockSizeRadioButton;

    @FXML
    private RadioButton radioButtonDCT;
    @FXML
    private RadioButton radioButtonWHT;
    private final ToggleGroup transformTypeGroup = new ToggleGroup();
    private RadioButton selectedTransformRadioButton;
    private ImagePlus imagePlusSquare1;
    private ImagePlus imagePlusSquare2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioButton2x2.setToggleGroup(transformBlockSizeGroup);
        radioButton4x4.setToggleGroup(transformBlockSizeGroup);
        radioButton8x8.setToggleGroup(transformBlockSizeGroup);

        radioButtonDCT.setToggleGroup(transformTypeGroup);
        radioButtonWHT.setToggleGroup(transformTypeGroup);

        loadOrigImageSquares();
        this.colorTransformSquare1.convertRgbToYcbcr();
        imagePlusSquare1.setTitle("Square 1");
        imagePlusSquare1.show("Square 1");

        this.colorTransformSquare2.convertRgbToYcbcr();
        imagePlusSquare2.setTitle("Square 2");
        imagePlusSquare2.show("Square 2");


        loadOrigImage();
        this.colorTransform.convertRgbToYcbcr();
        imagePlus.setTitle("Original Image");
        imagePlus.show("Original Image");

        choiceBoxN.setItems(FXCollections.observableArrayList("2 x 2", "4 x 4", "8 x 8", "16 x 16"));
        choiceBoxN.getSelectionModel().select(3);
        blockSize = choiceBoxN.getValue();
        macroBlockSize = 16;
        choiceBoxN.setTooltip(new Tooltip("Choose block size"));
        choiceBoxN.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                blockSize = newValue;
                switch (blockSize) {
                    case "2 x 2":
                        macroBlockSize = 2;
                        break;
                    case "4 x 4":
                        macroBlockSize = 4;
                        break;
                    case "8 x 8":
                        macroBlockSize = 8;
                        break;
                    case "16 x 16":
                        macroBlockSize = 16;
                        break;
                }
                System.out.println("Block size set to: " + blockSize);
            }
        });
    }

    public void yButtonPressed(ActionEvent event) {
        getComponent(Y).show("Y Component");
    }

    public void cbButtonPressed(ActionEvent event) {
        getComponent(CB).show("Cb Component");
    }

    public void crButtonPressed(ActionEvent event) {
        getComponent(CR).show("Cr Component");
    }

    public void rButtonPressed(ActionEvent event) {
        getComponent(RED).show("Red Component");
    }

    public void gButtonPressed(ActionEvent event) {
        getComponent(GREEN).show("Green Component");
    }

    public void bButtonPressed(ActionEvent event) {
        getComponent(BLUE).show("Blue Component");
    }

    public void dS444ButtonPressed(ActionEvent event) {
        loadOrigImage();
        sampling = S444;
        downsample(S444);
    }

    public void dS422ButtonPressed(ActionEvent event) {
        loadOrigImage();
        sampling = S422;
        downsample(S422);
    }

    public void dS420ButtonPressed(ActionEvent event) {
        loadOrigImage();
        sampling = S420;
        downsample(S420);
    }

    public void dS411ButtonPressed(ActionEvent event) {
        loadOrigImage();
        sampling = S411;
        downsample(S411);
    }

    public void overSampleButtonPressed(ActionEvent event) {
        overSample(sampling);
    }

    public void qualityButtonPressed(ActionEvent event) {
        psnrLabel.setText("PSNR = " + getPsnr());
        mseLabel.setText("MSE = " + getMse());
    }

    public void transformAndQuantize(ActionEvent event) {
        // choose block size for transformation
        int blockSize = 0;
        selectedTransformBlockSizeRadioButton = (RadioButton) transformBlockSizeGroup.getSelectedToggle();

        if (selectedTransformBlockSizeRadioButton.getText().equalsIgnoreCase("8x8"))
            blockSize = 8;
        if (selectedTransformBlockSizeRadioButton.getText().equalsIgnoreCase("4x4"))
            blockSize = 4;
        if (selectedTransformBlockSizeRadioButton.getText().equalsIgnoreCase("2x2"))
            blockSize = 2;

        // choose transformation
        Matrix dct = null;
        selectedTransformRadioButton = (RadioButton) transformTypeGroup.getSelectedToggle();
        if (selectedTransformRadioButton.getText().equalsIgnoreCase("dct"))
            dct = new TransformMatrix().getDctMatrix(blockSize);
        if (selectedTransformRadioButton.getText().equalsIgnoreCase("wht"))
            dct = new TransformMatrix().getWhtMatrix(blockSize);

        // set quantization matrices
        this.quantizationMatrix8Y = new Matrix(
                colorTransform.getQuantizationMatrix8Y());
        this.quantizationMatrix8C = new Matrix(
                colorTransform.getQuantizationMatrix8C());
        double alpha = 0;
        double quality = (int) qualitySlider.getValue();
        System.out.println(quality);
        if (quality < 50) {
            alpha = 50.0 / quality;
            this.quantizationMatrix8Y.timesEquals(alpha);
            this.quantizationMatrix8C.timesEquals(alpha);
        } else if (quality >= 50 && quality < 99) {

            alpha = 2 - (2 * quality) / 100.0;
            this.quantizationMatrix8Y.timesEquals(alpha);
            this.quantizationMatrix8C.timesEquals(alpha);
        } else if (quality == 100) {
            this.quantizationMatrix8Y = new Matrix(8, 8, 1);
            this.quantizationMatrix8C = new Matrix(8, 8, 1);
        }

        // transformation
        Matrix yPom = new Matrix(colorTransform.getY().getRowDimension(), colorTransform.getY().getColumnDimension());
        Matrix cbPom = new Matrix(colorTransform.getcB().getRowDimension(), colorTransform.getcB().getColumnDimension());
        Matrix crPom = new Matrix(colorTransform.getcR().getRowDimension(), colorTransform.getcR().getColumnDimension());

        for (int i = 0; i < colorTransform.getY().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getY().getColumnDimension() - 1; j = j + blockSize) {
                yPom.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1,
                        colorTransform.quantize(blockSize, colorTransform.transform(blockSize, dct, colorTransform.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1)), this.quantizationMatrix8Y));
            }
        }

        for (int i = 0; i < colorTransform.getcB().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getcB().getColumnDimension() - 1; j = j + blockSize) {
                //cbPom.setMatrix(i, i + blockSize-1, j, j + blockSize-1, colorTransform.transform(blockSize,dct, colorTransform.getcB().getMatrix(i, i + blockSize-1, j, j + blockSize-1)));
                //crPom.setMatrix(i, i + blockSize-1, j, j + blockSize-1, colorTransform.transform(blockSize,dct, colorTransform.getcR().getMatrix(i, i + blockSize-1, j, j + blockSize-1)));
                cbPom.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1,
                        colorTransform.quantize(blockSize, colorTransform.transform(blockSize, dct, colorTransform.getcB().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1)), this.quantizationMatrix8C));
                crPom.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1,
                        colorTransform.quantize(blockSize, colorTransform.transform(blockSize, dct, colorTransform.getcR().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1)), this.quantizationMatrix8C));
            }
        }

        colorTransform.setY(yPom);
        colorTransform.setcB(cbPom);
        colorTransform.setcR(crPom);
    }

    public void iTransformAndIQuantize(ActionEvent event) {
        // choose block size for transformation
        int blockSize = 0;
        selectedTransformBlockSizeRadioButton = (RadioButton) transformBlockSizeGroup.getSelectedToggle();

        if (selectedTransformBlockSizeRadioButton.getText().equalsIgnoreCase("8x8"))
            blockSize = 8;
        if (selectedTransformBlockSizeRadioButton.getText().equalsIgnoreCase("4x4"))
            blockSize = 4;
        if (selectedTransformBlockSizeRadioButton.getText().equalsIgnoreCase("2x2"))
            blockSize = 2;

        // choose transformation
        Matrix dct = null;
        if (selectedTransformRadioButton.getText().equalsIgnoreCase("dct"))
            dct = new TransformMatrix().getDctMatrix(blockSize);
        if (selectedTransformRadioButton.getText().equalsIgnoreCase("wht"))
            dct = new TransformMatrix().getWhtMatrix(blockSize);

        // transformation
        Matrix yPom = new Matrix(colorTransform.getY().getRowDimension(), colorTransform.getY().getColumnDimension());
        Matrix cbPom = new Matrix(colorTransform.getcB().getRowDimension(), colorTransform.getcB().getColumnDimension());
        Matrix crPom = new Matrix(colorTransform.getcR().getRowDimension(), colorTransform.getcR().getColumnDimension());

        for (int i = 0; i < colorTransform.getY().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getY().getColumnDimension() - 1; j = j + blockSize) {
                yPom.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.inverseTransform(blockSize, dct, colorTransform.inverseQuantize(blockSize, colorTransform.getY().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1), this.quantizationMatrix8Y)));
                //yPom.setMatrix(i, i + blockSize-1, j, j + blockSize-1,
//						colorTransform.iKvantizace(blockSize, colorTransform.inverseTransform(blockSize,dct, colorTransform.getY().getMatrix(i, i + blockSize-1, j, j + blockSize-1)), this.quantizationMatrix8Y));
            }
        }

        for (int i = 0; i < colorTransform.getcB().getRowDimension() - 1; i = i + blockSize) {
            for (int j = 0; j < colorTransform.getcB().getColumnDimension() - 1; j = j + blockSize) {
                //cbPom.setMatrix(i, i + blockSize-1, j, j + blockSize-1, colorTransform.inverseTransform(blockSize,dct, colorTransform.getcB().getMatrix(i, i + blockSize-1, j, j + blockSize-1)));
                //crPom.setMatrix(i, i + blockSize-1, j, j + blockSize-1, colorTransform.inverseTransform(blockSize,dct, colorTransform.getcR().getMatrix(i, i + blockSize-1, j, j + blockSize-1)));
                cbPom.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.inverseTransform(blockSize, dct, colorTransform.inverseQuantize(blockSize, colorTransform.getcB().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1), this.quantizationMatrix8C)));
                crPom.setMatrix(i, i + blockSize - 1, j, j + blockSize - 1, colorTransform.inverseTransform(blockSize, dct, colorTransform.inverseQuantize(blockSize, colorTransform.getcR().getMatrix(i, i + blockSize - 1, j, j + blockSize - 1), this.quantizationMatrix8C)));

            }
        }

        colorTransform.setY(yPom);
        colorTransform.setcB(cbPom);
        colorTransform.setcR(crPom);
    }

    public void DPCM1(ActionEvent event) {
        colorTransform.setY(ImageProcessing.DPCM(colorTransformSquare2.getY(), colorTransformSquare1.getY()));
        getComponent(Y).show();
        colorTransform.setcB(ImageProcessing.DPCM(colorTransformSquare2.getcB(), colorTransformSquare1.getcB()));
        getComponent(CB).show();
        colorTransform.setcR(ImageProcessing.DPCM(colorTransformSquare2.getcR(), colorTransformSquare1.getcR()));
        getComponent(CR).show();
    }

    public void DPCM2(ActionEvent event) {
        colorTransform.setY(
                ImageProcessing.DPCM(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getY(),
                                macroBlockSize
                        ),
                        colorTransformSquare2.getY()
                )
        );

        getComponent(Y).show();

        colorTransform.setcB(
                ImageProcessing.DPCM(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcB(),
                                macroBlockSize
                        ),
                        colorTransformSquare2.getcB()
                )
        );

        getComponent(CB).show();

        colorTransform.setcR(
                ImageProcessing.DPCM(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcR(),
                                macroBlockSize
                        ),
                        colorTransformSquare2.getcR()
                )
        );

        getComponent(CR).show();
    }

    public void FULLSearch(ActionEvent event) {
        colorTransform.setY(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getY(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getY(),
                                macroBlockSize,
                                colorTransformSquare2.getY())
                )
        );

        getComponent(Y).show();

        colorTransform.setcB(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcB(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcB(),
                                macroBlockSize,
                                colorTransformSquare2.getcB())
                )
        );

        getComponent(CB).show();

        colorTransform.setcR(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcR(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcR(),
                                macroBlockSize,
                                colorTransformSquare2.getcR())
                )
        );

        getComponent(CR).show();

        showResult();
    }

    public void threeStepSearch(ActionEvent event) {
        colorTransform.setY(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.nStepSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize, 3),
                                colorTransformSquare1.getY(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.nStepSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize, 3),
                                colorTransformSquare1.getY(),
                                macroBlockSize,
                                colorTransformSquare2.getY())
                )
        );

        getComponent(Y).show();

        colorTransform.setcB(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.nStepSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize, 3),
                                colorTransformSquare1.getcB(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.nStepSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize, 3),
                                colorTransformSquare1.getcB(),
                                macroBlockSize,
                                colorTransformSquare2.getcB())
                )
        );

        getComponent(CB).show();

        colorTransform.setcR(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.nStepSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize, 3),
                                colorTransformSquare1.getcR(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.nStepSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize, 3),
                                colorTransformSquare1.getcR(),
                                macroBlockSize,
                                colorTransformSquare2.getcR())
                )
        );

        getComponent(CR).show();

        showResult();
    }

    public void oneAtTime(ActionEvent event) {
        colorTransform.setY(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.oneAtSearch(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getY(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.oneAtSearch(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getY(),
                                macroBlockSize,
                                colorTransformSquare2.getY())
                )
        );

        getComponent(Y).show();

        colorTransform.setcB(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.oneAtSearch(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcB(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.oneAtSearch(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcB(),
                                macroBlockSize,
                                colorTransformSquare2.getcB())
                )
        );

        getComponent(CB).show();

        colorTransform.setcR(
                ImageProcessing.fullSearchErrorI(
                        ImageProcessing.fullSearchI(
                                ImageProcessing.oneAtSearch(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcR(),
                                macroBlockSize
                        ),
                        ImageProcessing.fullSearchError(ImageProcessing.oneAtSearch(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                                colorTransformSquare1.getcR(),
                                macroBlockSize,
                                colorTransformSquare2.getcR())
                )
        );

        getComponent(CR).show();

        showResult();
    }

    public void SADBeforeAfter(ActionEvent event) {
        int height = colorTransformSquare2.getY().getRowDimension();
        int width = colorTransformSquare2.getY().getColumnDimension();

        double before;
        double after;
        double po3step;
        double poone;
        double difference;
        double percentage;

        before = ImageProcessing.SAD(colorTransformSquare2.getY(), colorTransformSquare1.getY());

        after = ImageProcessing.SAD(
                ImageProcessing.fullSearchI(
                        ImageProcessing.fullSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                        colorTransformSquare1.getY(),
                        macroBlockSize
                ),
                colorTransformSquare2.getY()
        );

        po3step = ImageProcessing.SAD(
                ImageProcessing.fullSearchI(
                        ImageProcessing.nStepSearchVectors(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize, 3),
                        colorTransformSquare2.getY(),
                        macroBlockSize
                ),
                colorTransformSquare1.getY()
        );

        poone = ImageProcessing.SAD(
                ImageProcessing.fullSearchI(
                        ImageProcessing.oneAtSearch(colorTransformSquare1.getY(), colorTransformSquare2.getY(), macroBlockSize),
                        colorTransformSquare1.getY(),
                        macroBlockSize
                ),
                colorTransformSquare2.getY()
        );

        difference = Math.abs(before - after);

        sadLabel1.setText("Pred: " + (int)before);
        sadLabel2.setText("Po: " + (int)after + ";3: "+ (int)po3step + "; 1: " + (int)poone);

        sadLabel3.setText("Dif: " + (int)difference);
    }

    private void loadOrigImage() {
        this.imagePlus = new ImagePlus("img/pomaly.jpg");
        this.colorTransformOrig = new ColorTransform(imagePlus.getBufferedImage());
        this.colorTransform = new ColorTransform(imagePlus.getBufferedImage());
        this.colorTransform.getRGB();
        this.colorTransformOrig.getRGB();
    }

    private void loadOrigImageSquares() {
        this.imagePlusSquare1 = new ImagePlus("img/pomaly.jpg");
        this.colorTransformSquare1 = new ColorTransform(imagePlusSquare1.getBufferedImage());
        this.colorTransformSquare1.getRGB();

        this.imagePlusSquare2 = new ImagePlus("img/pomaly2.jpg");
        this.colorTransformSquare2 = new ColorTransform(imagePlusSquare2.getBufferedImage());
        this.colorTransformSquare2.getRGB();
    }

    private ImagePlus getComponent(int component) {
        ImagePlus imagePlus = null;
        switch (component) {
            case RED:
                imagePlus = colorTransform.setImageFromRGB(
                        colorTransform.getImageWidth(),
                        colorTransform.getImageHeight(), colorTransform.getRed(),
                        "RED");
                break;
            case GREEN:
                imagePlus = colorTransform.setImageFromRGB(
                        colorTransform.getImageWidth(),
                        colorTransform.getImageHeight(), colorTransform.getGreen(),
                        "GREEN");
                break;
            case BLUE:
                imagePlus = colorTransform.setImageFromRGB(
                        colorTransform.getImageWidth(),
                        colorTransform.getImageHeight(), colorTransform.getBlue(),
                        "BLUE");
                break;
            case Y:
                imagePlus = colorTransform.setImageFromRGB(colorTransform.getY()
                        .getColumnDimension(), colorTransform.getY()
                        .getRowDimension(), colorTransform.getY(), "Y");
                break;
            case CB:
                imagePlus = colorTransform.setImageFromRGB(colorTransform.getcB()
                        .getColumnDimension(), colorTransform.getcB()
                        .getRowDimension(), colorTransform.getcB(), "Cb");
                break;
            case CR:
                imagePlus = colorTransform.setImageFromRGB(colorTransform.getcR()
                        .getColumnDimension(), colorTransform.getcR()
                        .getRowDimension(), colorTransform.getcR(), "Cr");
            default:
                break;
        }
        return imagePlus;
    }

    private void downsample(int downsampleType) {
        colorTransform.convertRgbToYcbcr();
        Matrix cB = new Matrix(colorTransform.getcB().getArray());
        Matrix cR = new Matrix(colorTransform.getcR().getArray());
        switch (downsampleType) {
            case S444:
                break;
            case S422:
                cB = colorTransform.downsample(cB);
                colorTransform.setcB(cB);

                cR = colorTransform.downsample(cR);
                colorTransform.setcR(cR);
                break;

            case S420:
                cB = colorTransform.downsample(cB);
                cB = cB.transpose();
                cB = colorTransform.downsample(cB);
                cB = cB.transpose();
                colorTransform.setcB(cB);

                cR = colorTransform.downsample(cR);
                cR = cR.transpose();
                cR = colorTransform.downsample(cR);
                cR = cR.transpose();
                colorTransform.setcR(cR);
                break;

            case S411:
                cB = colorTransform.downsample(cB);
                colorTransform.setcB(cB);
                cB = new Matrix(colorTransform.getcB().getArray());
                cB = colorTransform.downsample(cB);
                colorTransform.setcB(cB);

                cR = colorTransform.downsample(cR);
                colorTransform.setcR(cR);
                cR = new Matrix(colorTransform.getcR().getArray());
                cR = colorTransform.downsample(cR);
                colorTransform.setcR(cR);
                break;
        }
    }

    private void overSample(int overSample) {
        Matrix cB;
        Matrix cR;
        switch (overSample) {
            case S444:
                break;
            case S422:
                cB = new Matrix(colorTransform.getcB().getArray());
                cB = colorTransform.oversample(cB);
                colorTransform.setcB(cB);
                cR = new Matrix(colorTransform.getcR().getArray());
                cR = colorTransform.oversample(cR);
                colorTransform.setcR(cR);
                break;

            case S420:
                cB = new Matrix(colorTransform.getcB().getArray()).transpose();
                cB = colorTransform.oversample(cB);
                colorTransform.setcB(cB);
                cB = new Matrix(colorTransform.getcB().getArray()).transpose();
                cB = colorTransform.oversample(cB);
                colorTransform.setcB(cB);

                cR = new Matrix(colorTransform.getcR().getArray()).transpose();
                cR = colorTransform.oversample(cR);
                colorTransform.setcR(cR);
                cR = new Matrix(colorTransform.getcR().getArray()).transpose();
                cR = colorTransform.oversample(cR);
                colorTransform.setcR(cR);
                break;

            case S411:
                cB = new Matrix(colorTransform.getcB().getArray());
                cB = colorTransform.oversample(cB);
                colorTransform.setcB(cB);
                cB = new Matrix(colorTransform.getcB().getArray());
                cB = colorTransform.oversample(cB);
                colorTransform.setcB(cB);

                cR = new Matrix(colorTransform.getcR().getArray());
                cR = colorTransform.oversample(cR);
                colorTransform.setcR(cR);
                cR = new Matrix(colorTransform.getcR().getArray());
                cR = colorTransform.oversample(cR);
                colorTransform.setcR(cR);
                break;

            default:
                break;
        }
        colorTransform.convertYcbcrToRgb();
    }

    private double getMse() {
        colorTransform.convertYcbcrToRgb();
        double a = Quality.getMse(colorTransformOrig.getRed(),
                colorTransform.getRed());
        double b = Quality.getMse(colorTransformOrig.getGreen(),
                colorTransform.getGreen());
        double c = Quality.getMse(colorTransformOrig.getBlue(),
                colorTransform.getBlue());
        return ((a + b + c) / 3.0);

    }

    private double getPsnr() {
        colorTransform.convertYcbcrToRgb();
        double result = (Quality.getPsnr(colorTransformOrig.getRed(),
                colorTransform.getRed()) + Quality.getPsnr(colorTransformOrig.getGreen(),
                colorTransform.getGreen()) + Quality.getPsnr(colorTransformOrig.getBlue(),
                colorTransform.getBlue())) / 3.0;
        return result;
    }

    public void showResult() {
        colorTransform.convertYcbcrToRgb();
        colorTransform.setImageFromRGB(colorTransform.getRed().length,
                colorTransform.getRed()[0].length, colorTransform.getRed(),
                colorTransform.getGreen(), colorTransform.getBlue()).show(
                "Transformed Image");
    }
}
