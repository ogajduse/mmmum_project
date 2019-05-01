package cz.vutbr.feec.utko.mmum.project;

import Jama.Matrix;
import ij.ImagePlus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sun.java2d.cmm.ColorTransform;

public class Controller {
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int BLUE = 3;
    public static final int Y = 4;
    public static final int CB = 5;
    public static final int CR = 6;
    public static final int S444 = 7;
    public static final int S422 = 8;
    public static final int S420 = 9;
    public static final int S411 = 10;
    public int macroblock_size;
    private int vzorkovani = S444;

    private Matrix quantizationMatrix8Y;
    private Matrix quantizationMatrix8C;

    // private Quality quality;

    private ImagePlus imagePlus;
    private ColorTransform colorTransform;
    private ColorTransform colorTransform_stvorec1;
    private ColorTransform colorTransform_stvorec2;
    private ColorTransform colorTransformOrig;

    String block_size;

    @FXML
    private ChoiceBox choice_box_n;

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
    private Label sad_label1;
    @FXML
    private Label sad_label2;
    @FXML
    private Label sad_label3;
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

    final ToggleGroup transformBlockSizeGroup = new ToggleGroup();
    private RadioButton selectedTransformBlockSizeRadioButton;

    @FXML
    private RadioButton radioButtonDCT;
    @FXML
    private RadioButton radioButtonWHT;
    final ToggleGroup transformTypeGroup = new ToggleGroup();
    private RadioButton selectedTransformRadioButton;
    private ImagePlus imagePlus_ctverec1;
    private ImagePlus imagePlus_ctverec2;

    public void yButtonPressed(ActionEvent event){

    }

    public void cbButtonPressed(ActionEvent event) {

    }

    public void crButtonPressed(ActionEvent event) {

    }

    public void rButtonPressed(ActionEvent event) {

    }

    public void gButtonPressed(ActionEvent event) {

    }

    public void bButtonPressed(ActionEvent event) {

    }

    public void dS444ButtonPressed(ActionEvent event) {
        vzorkovani = S444;
    }

    public void dS422ButtonPressed(ActionEvent event) {
        vzorkovani = S422;
    }

    public void dS420ButtonPressed(ActionEvent event) {
        vzorkovani = S420;
    }

    public void dS411ButtonPressed(ActionEvent event) {
        vzorkovani = S411;
    }

    public void overSampleButtonPressed(ActionEvent event) {

    }

    public void qualityButtonPressed(ActionEvent event) {

    }

    public void showResult(){

    }

    public void transformAndQuantize(ActionEvent event) {

    }

    public void iTransformAndIQuantize(ActionEvent event) {

    }

    public void DPCM_1(ActionEvent event) {

    }

    public void DPCM_2(ActionEvent event) {

    }

    public void FULL_s(ActionEvent event) {

    }

    public void threeStepSearch(ActionEvent event) {

    }

    public void oneattime(ActionEvent event) {

    }

    public void SAD_pred_po(ActionEvent event) {

    }
}
