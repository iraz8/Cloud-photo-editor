import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class MainScreenController {
  public Client client;
  public Stage primaryStage;
  public Text textFilePath,
      infoText,
      intervalsAdvText,
      intervalsAdvText2,
      intervalsAdvText3,
      loadImageFromServerTextError;
  @FXML public JFXButton applyButton, saveAsButton, loadImageFromComputer, loadImageFromServer;
  public JFXRadioButton brightnessRadioButton, brightnessAdvRadioButton;
  public JFXSlider slider100_100, slider0_100;
  public ImageView imageView;
  public AnchorPane goBackAnchorPane,
      step1AnchorPane1,
      step1AnchorPane2,
      step2AnchorPane2,
      step2AnchorPane3;
  public JFXTabPane step2TabPane1;
  public ToggleGroup editOptionsGroupBasic,
      editOptionsGroupAdv,
      originalOrProcessedImageToggleGroup;
  public Tab basicTab, advancedTab;
  public JFXToggleButton toggleButton;
  public JFXTextField textFieldInputAdv, textFieldInputAdv2, textFieldInputAdv3;
  public JFXComboBox<String> comboBox3;
  // Advanced mode - input-text-combobox-text-combobox
  public JFXComboBox<String> comboBox1, comboBox2;
  String username;
  int currentStep = 0;
  String filename = null;
  ErrorCodes errorCodes = null;
  String originalImagePath;
  String processedImagePathFromServer, processedImagePathFromServerConvertedToClient;

  void setArgs(Client client, Stage primaryStage) {
    this.client = client;
    this.username = client.getUsername();
    this.primaryStage = primaryStage;
    showStep1();
  }

  void showStep1() {
    currentStep = 1;
    setInvisibleAllSliders();
    setInvisibleAllComboBoxes();
    setInvisibleAdvancedMode();
    loadImageFromServerTextError.setVisible(false);
    loadImageFromServer.setDisable(false);
    setTextFilePath("");
    goBackAnchorPane.setVisible(false);
    step2TabPane1.setVisible(false);
    step2AnchorPane2.setVisible(false);
    step2AnchorPane3.setVisible(false);
    step1AnchorPane1.setVisible(true);
    step1AnchorPane2.setVisible(true);
    originalImagePath = null;
    processedImagePathFromServer = null;
    processedImagePathFromServerConvertedToClient = null;
    filename = null;
    saveAsButton.setDisable(true);

    try {
      setImageUsingPath("images/image_background.png");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  void showStep2() {
    currentStep = 2;
    brightnessRadioButton.setSelected(true);
    setInvisibleAllSliders();
    setInvisibleAllComboBoxes();
    setInvisibleAdvancedMode();
    setVisibleSlider100_100();
    infoText.setVisible(true);
    goBackAnchorPane.setVisible(true);
    step1AnchorPane1.setVisible(false);
    step1AnchorPane2.setVisible(false);
    step2TabPane1.setVisible(true);
    step2AnchorPane2.setVisible(true);
    step2AnchorPane3.setVisible(true);
    if (filename == null) {
      try {
        setImageUsingPath("images/image_background.png");
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
  }

  @FXML
  public void exitButtonClicked(ActionEvent e) throws IOException {
    client.decisionMaker("CLOSE");
    FileUtils.deleteQuietly(new File("tmp"));
    Platform.exit();
    System.exit(0);
  }

  @FXML
  public void loadImageFromComputerButtonClicked(ActionEvent e) {
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter extFilter =
        new FileChooser.ExtensionFilter("Image files (*.jpg,*.png)", "*.jpg", "*.png");
    fileChooser.getExtensionFilters().add(extFilter);
    File file = fileChooser.showOpenDialog(primaryStage);
    if (file != null) {
      Image image = null;
      try {
        image = new Image(String.valueOf(file.toURI().toURL()));
      } catch (MalformedURLException ex) {
        ex.printStackTrace();
      }

      try {
        this.filename = file.getName();
        this.originalImagePath = file.getAbsolutePath();
        errorCodes = client.decisionMaker("SEND-FILE-TO-SERVER|" + file.getAbsolutePath());
      } catch (IOException ex) {
        setInfoTextVisibleAndShow("Image not loaded!");
        ex.printStackTrace();
      }
      toggleButton.setDisable(false);
      setToggleButtonUnselected();
      imageView.setImage(image);

      showStep2();
      setTextFilePath(originalImagePath);
    }
  }

  @FXML
  public void loadImageFromServerButtonClicked(ActionEvent e) throws IOException {

    if (checkServerIfFileListExists()) {
      Stage windowStage = new Stage();
      FXMLLoader fxmlSelectImage =
          new FXMLLoader(getClass().getResource("Others/selectImageScreen.fxml"));
      Parent selectImageParent = fxmlSelectImage.load();
      Scene selectImageScene = new Scene(selectImageParent);
      SelectImageScreenController selectImageScreenController = fxmlSelectImage.getController();

      windowStage.setScene(selectImageScene);
      windowStage.setTitle("Cloud photo editor - Select image from server");
      windowStage.getIcons().add(new Image("file:images/logo_ico_app_bar.jpg"));

      windowStage.setResizable(false);
      windowStage.initStyle(StageStyle.DECORATED);
      selectImageScreenController.setArgs(client, windowStage);
      windowStage.initModality(Modality.APPLICATION_MODAL);
      windowStage.showAndWait();

      if (client.currentSelectedFilename != null) {
        String filePath =
            "tmp/"
                + client.getUsername()
                + "/"
                + FilenameUtils.getName(client.currentSelectedFilename);
        toggleButton.setDisable(false);
        File file = new File(filePath);
        this.filename = file.getName();
        this.originalImagePath = file.getAbsolutePath();
        setToggleButtonUnselected();
        if (!file.isFile()) {
          file = new File("images/image_background.png");
          setTextFilePath("");
          toggleButton.setDisable(true);
          showStep1();
        } else {
          setImageUsingPath(filePath);
          showStep2();
          setTextFilePath(originalImagePath);
        }
      }
    } else {
      setloadImageFromServerTextError(
          "No files on server from this account. Try first to load images from computer!");
      loadImageFromServer.setDisable(true);
    }
  }

  @FXML
  public void changePasswordButtonClicked(ActionEvent e) throws IOException {
    Stage windowStage = new Stage();
    FXMLLoader fxmlChangePassword =
        new FXMLLoader(getClass().getResource("Others/changePasswordScreen.fxml"));
    Parent changePasswordParent = fxmlChangePassword.load();
    Scene changePasswordScene = new Scene(changePasswordParent);
    ChangePasswordScreenController changePasswordScreenController =
        fxmlChangePassword.getController();

    windowStage.setScene(changePasswordScene);
    windowStage.setTitle("Cloud photo editor - Change password");
    windowStage.getIcons().add(new Image("file:images/logo_ico_app_bar.jpg"));
    windowStage.setResizable(false);
    windowStage.initStyle(StageStyle.DECORATED);
    changePasswordScreenController.setArgs(client, windowStage);
    windowStage.initModality(Modality.APPLICATION_MODAL);
    windowStage.showAndWait();
  }

  @FXML
  public void basicTabClicked(Event e) {
    if (currentStep == 2) {
      setInvisibleAdvancedMode();
      setVisibleSlider100_100();
      brightnessRadioButton.setSelected(true);
      intervalsAdvText.setVisible(true);
    }
  }

  @FXML
  public void advancedTabClicked(Event e) {
    if (currentStep == 2) {
      setVisibleAdvancedMode();
      setInvisibleAllSliders();
      brightnessAdvRadioButton.setSelected(true);
      intervalsAdvText.setText("\n-250 < x < 250");
    }
  }

  @FXML
  public void applyButtonClicked(ActionEvent e) throws IOException {
    setInfoTextInvisible();
    if (basicTab.isSelected()) {
      JFXRadioButton selectedRadioButton =
          (JFXRadioButton) editOptionsGroupBasic.getSelectedToggle();
      String toggleGroupValue = selectedRadioButton.getText();
      switch (toggleGroupValue) {
        case "Brightness changer":
          errorCodes =
              client.decisionMaker(
                  "CHANGE-BRIGHTNESS|" + filename + "|" + (float) slider100_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Contrast changer":
          errorCodes =
              client.decisionMaker(
                  "CHANGE-CONTRAST|" + filename + "|" + (float) slider100_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Blur":
          errorCodes =
              client.decisionMaker("GAUSSIAN-FILTER|" + filename + "|" + slider0_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Color to gray converter":
          errorCodes = client.decisionMaker("CONVERT-COLOR-TO-GRAY|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Dilatation":
          errorCodes =
              client.decisionMaker("DILATATION|" + filename + "|" + slider0_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Erosion":
          errorCodes = client.decisionMaker("EROSION|" + filename + "|" + slider0_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Sepia":
          errorCodes = client.decisionMaker("SEPIA|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Mosaic":
          if (!comboBox1.getSelectionModel().isEmpty()
              && !comboBox2.getSelectionModel().isEmpty()) {
            String comboBox1Value = comboBox1.getValue();
            String comboBox2Value = comboBox2.getValue();
            if (comboBox1Value != null && comboBox1Value.equals("Squares"))
              comboBox1Value = "squares";
            else comboBox1Value = "triangles";
            if (comboBox2Value != null && comboBox2Value.equals("On")) comboBox2Value = "true";
            else comboBox2Value = "false";
            errorCodes =
                client.decisionMaker(
                    "MOSAIC|"
                        + filename
                        + "|"
                        + slider0_100.getValue()
                        + "|"
                        + comboBox1Value
                        + "|"
                        + comboBox2Value);
            processedImagePathFromServer = client.getLastProcessedImageFileName();
          } else {
            setInfoTextVisibleAndShow("You must select the options!");
            return;
          }
          break;
        case "Television effect":
          errorCodes = client.decisionMaker("TELEVISION-EFFECT|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Pixelize":
          errorCodes = client.decisionMaker("PIXELIZE|" + filename + "|" + slider0_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Black and white":
          errorCodes =
              client.decisionMaker(
                  "BLACK-AND-WHITE|" + filename + "|" + (float) slider100_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Emboss filter":
          errorCodes = client.decisionMaker("EMBOSS-FILTER|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Invert colors":
          errorCodes = client.decisionMaker("INVERT-COLORS|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Thresholding":
          errorCodes =
              client.decisionMaker("THRESHOLDING|" + filename + "|" + slider0_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Edge detection":
          errorCodes = client.decisionMaker("EDGE-DETECTION|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Histogram equalization":
          errorCodes = client.decisionMaker("HISTOGRAM-EQUALIZATION|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Halftone-error diffusion":
          errorCodes = client.decisionMaker("HALFTONE-ERROR-DIFFUSION|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Halftone-dithering":
          errorCodes = client.decisionMaker("HALFTONE-DITHERING|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Halftone-rylanders":
          errorCodes = client.decisionMaker("HALFTONE-RYLANDERS|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Color histogram":
          errorCodes = client.decisionMaker("COLOR-HISTOGRAM|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Gray histogram":
          errorCodes = client.decisionMaker("GRAY-HISTOGRAM|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Gray scale quantization":
          errorCodes =
              client.decisionMaker(
                  "GRAY-SCALE-QUANTIZATION|" + filename + "|" + slider0_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Noise reduction":
          errorCodes = client.decisionMaker("NOISE-REDUCTION|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Transform-flip":
          if (!comboBox3.getItems().isEmpty()) {
            String comboBox3Value = comboBox3.getValue();
            if (comboBox3Value != null && comboBox3Value.equals("Horizontal"))
              comboBox3Value = "horizontal";
            else comboBox3Value = "vertical";
            errorCodes = client.decisionMaker("TRANSFORM-FLIP|" + filename + "|" + comboBox3Value);
            processedImagePathFromServer = client.getLastProcessedImageFileName();
          } else {
            setInfoTextVisibleAndShow("You must select one of the options!");
            return;
          }
          break;
        case "Transform-rotate":
          errorCodes =
              client.decisionMaker("TRANSFORM-ROTATE|" + filename + "|" + slider100_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Compress image":
          errorCodes = client.decisionMaker("COMPRESS|" + filename + "|" + slider0_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Gamma changer":
          errorCodes =
              client.decisionMaker("GAMMA-CHANGER|" + filename + "|" + slider100_100.getValue());
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Sharpness enhancer":
          errorCodes = client.decisionMaker("SHARPNESS-ENHANCER|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;

        default:
          break;
      }
    } else {

      float field, field2, field3;

      JFXRadioButton selectedRadioButton = (JFXRadioButton) editOptionsGroupAdv.getSelectedToggle();
      String toggleGroupValue = selectedRadioButton.getText();

      switch (toggleGroupValue) {
        case "Brightness changer":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("CHANGE-BRIGHTNESS-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Contrast changer":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("CHANGE-CONTRAST-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Gaussian Filter Blur":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("GAUSSIAN-FILTER-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Color to gray converter":
          errorCodes = client.decisionMaker("CONVERT-COLOR-TO-GRAY|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Dilatation":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("DILATATION-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Erosion":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("EROSION-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Sepia":
          errorCodes = client.decisionMaker("SEPIA|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Mosaic":
          if (!comboBox1.getItems().isEmpty() && !comboBox2.getItems().isEmpty()) {
            field = getAndCheckInput(textFieldInputAdv);
            String comboBox1Value = comboBox1.getValue();
            String comboBox2Value = comboBox2.getValue();
            if (comboBox1Value != null && comboBox1Value.equals("Squares"))
              comboBox1Value = "squares";
            else comboBox1Value = "triangles";
            if (comboBox2Value != null && comboBox2Value.equals("On")) comboBox2Value = "true";
            else comboBox2Value = "false";
            errorCodes =
                client.decisionMaker(
                    "MOSAIC-ADVANCED|"
                        + filename
                        + "|"
                        + field
                        + "|"
                        + comboBox1Value
                        + "|"
                        + comboBox2Value);
            processedImagePathFromServer = client.getLastProcessedImageFileName();
          } else {
            setInfoTextVisibleAndShow("You must select the options!");
            return;
          }
          break;
        case "Television effect":
          errorCodes = client.decisionMaker("TELEVISION-EFFECT|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Pixelize":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("PIXELIZE-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Black and white":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("BLACK-AND-WHITE-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Brightness and contrast":
          field = getAndCheckInput(textFieldInputAdv);
          field2 = getAndCheckInput(textFieldInputAdv2);
          errorCodes =
              client.decisionMaker(
                  "BRIGHTNESS-AND-CONTRAST-ADVANCED|" + filename + "|" + field2 + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Color channel filter":
          field = getAndCheckInput(textFieldInputAdv);
          field2 = getAndCheckInput(textFieldInputAdv2);
          field3 = getAndCheckInput(textFieldInputAdv3);
          errorCodes =
              client.decisionMaker(
                  "COLOR-CHANNEL-FILTER-ADVANCED|"
                      + filename
                      + "|"
                      + field
                      + "|"
                      + field2
                      + "|"
                      + field3);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Emboss filter":
          errorCodes = client.decisionMaker("EMBOSS-FILTER|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Invert colors":
          errorCodes = client.decisionMaker("INVERT-COLORS|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Thresholding":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("THRESHOLDING-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Edge detection":
          errorCodes = client.decisionMaker("EDGE-DETECTION|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Histogram equalization":
          errorCodes = client.decisionMaker("HISTOGRAM-EQUALIZATION|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Halftone-circles":
          field = getAndCheckInput(textFieldInputAdv);
          field2 = getAndCheckInput(textFieldInputAdv2);
          field3 = getAndCheckInput(textFieldInputAdv3);
          errorCodes =
              client.decisionMaker(
                  "HALFTONE-CIRCLES|" + filename + "|" + field + "|" + field2 + "|" + field3);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Halftone-error diffusion":
          errorCodes = client.decisionMaker("HALFTONE-ERROR-DIFFUSION|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Halftone-dithering":
          errorCodes = client.decisionMaker("HALFTONE-DITHERING|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Halftone-rylanders":
          errorCodes = client.decisionMaker("HALFTONE-RYLANDERS|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Color histogram":
          errorCodes = client.decisionMaker("COLOR-HISTOGRAM|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Gray histogram":
          errorCodes = client.decisionMaker("GRAY-HISTOGRAM|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Gray scale quantization":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes =
              client.decisionMaker("GRAY-SCALE-QUANTIZATION-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Noise reduction":
          errorCodes = client.decisionMaker("NOISE-REDUCTION|" + filename);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Transform-flip":
          if (!comboBox3.getItems().isEmpty()) {
            String comboBox3Value = comboBox3.getValue();
            if (comboBox3Value != null && comboBox3Value.equals("Horizontal"))
              comboBox3Value = "horizontal";
            else comboBox3Value = "vertical";
            errorCodes = client.decisionMaker("TRANSFORM-FLIP|" + filename + "|" + comboBox3Value);
            processedImagePathFromServer = client.getLastProcessedImageFileName();
          } else {
            setInfoTextVisibleAndShow("You must select one of the options!");
            return;
          }
          break;
        case "Transform-scale":
          field = getAndCheckInput(textFieldInputAdv);
          field2 = getAndCheckInput(textFieldInputAdv2);
          errorCodes =
              client.decisionMaker(
                  "TRANSFORM-SCALE-ADVANCED|" + filename + "|" + field + "|" + field2);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Transform-rotate":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("TRANSFORM-ROTATE-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Compress":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("COMPRESS-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Gamma changer":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("GAMMA-CHANGER-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Median Filter":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes = client.decisionMaker("MEDIAN-FILTER-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Normalized block filter":
          field = getAndCheckInput(textFieldInputAdv);
          errorCodes =
              client.decisionMaker("NORMALIZED-BLOCK-FILTER-ADVANCED|" + filename + "|" + field);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;
        case "Sharpness enhancer":
          field = getAndCheckInput(textFieldInputAdv);
          field2 = getAndCheckInput(textFieldInputAdv2);
          errorCodes =
              client.decisionMaker(
                  "SHARPNESS-ENHANCER-ADVANCED|" + filename + "|" + field + "|" + field2);
          processedImagePathFromServer = client.getLastProcessedImageFileName();
          break;

        default:
          break;
      }
    }

    if (errorCodes.equals(ErrorCodes.NOT_A_JPEG_FILE)) {
      setInfoTextVisibleAndShow(" Not a JPEG file: starts with 0xe8 0x99");
    }
    processedImagePathFromServerConvertedToClient = convertServerPathToClientPath();
    setToggleButtonSelected();
    setImageUsingPath(processedImagePathFromServerConvertedToClient);
    saveAsButton.setDisable(false);
  }

  @FXML
  public void saveAsButtonClicked(ActionEvent e) throws IOException {
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter extFilter =
        new FileChooser.ExtensionFilter("Image files (*.jpg,*.png)", "*.jpg", "*.png");
    fileChooser.getExtensionFilters().add(extFilter);
    File file = fileChooser.showSaveDialog(primaryStage);
    if (file != null) {
      moveFileFromTMP(file);
    }
  }

  String convertServerPathToClientPath() {
    if (processedImagePathFromServer != null && !processedImagePathFromServer.isEmpty()) {
      String[] pathSplitted = processedImagePathFromServer.split("/");
      return "tmp/"
          + pathSplitted[pathSplitted.length - 2]
          + "/"
          + pathSplitted[pathSplitted.length - 1];
    }
    System.out.println("line 662: covertServerPathToClientPath returned NULL");
    return null;
  }

  void moveFileFromTMP(File file) {
    try {
      FileUtils.copyFile(new File(processedImagePathFromServerConvertedToClient), file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void brightnessChangerButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setVisibleSlider100_100();
  }

  @FXML
  public void brightnessChangerAdvButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n-250 <= x <= 250");
  }

  @FXML
  public void contrastChangerButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setVisibleSlider100_100();
  }

  @FXML
  public void contrastChangerAdvButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText(
        "0 < x < 1 to decrease contrast \n" + "1 < x < 1000 to increase contrast");
  }

  @FXML
  public void blurRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setVisibleSlider0_100();
  }

  @FXML
  public void gaussianBlurAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n 0 < x < 1000");
  }

  @FXML
  public void dilatationRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setVisibleSlider0_100();
  }

  @FXML
  public void dilatationAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n 0 < x < 1000");
  }

  @FXML
  public void erosionRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setVisibleSlider0_100();
  }

  @FXML
  public void erosionAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n 0 < x < 1000");
  }

  @FXML
  public void sepiaRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void colorToGrayConverterRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void mosaicRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
    setVisibleSlider0_100();
    comboBox1.setPromptText("Shape:");
    comboBox1.getItems().addAll("Squares", "Triangles");
    comboBox2.setPromptText("Border:");
    comboBox2.getItems().addAll("On", "Off");
    comboBox1.setVisible(true);
    comboBox2.setVisible(true);
  }

  @FXML
  public void mosaicAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();
    comboBox1.setPromptText("Shape:");
    comboBox1.getItems().addAll("Squares", "Triangles");
    comboBox2.setPromptText("Border:");
    comboBox2.getItems().addAll("On", "Off");
    comboBox1.setVisible(true);
    comboBox2.setVisible(true);

    intervalsAdvText.setVisible(true);
    intervalsAdvText.setText("\n3 < x < 1000");
  }

  @FXML
  public void televisionEffectRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void pixelizeRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
    setVisibleSlider0_100();
  }

  @FXML
  public void pixelizeAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n 1 < x < 250");
  }

  @FXML
  public void blackAndWhiteRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setVisibleSlider100_100();
  }

  @FXML
  public void blackAndWhiteAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n -100 < x < 100");
  }

  @FXML
  public void brightnessAndContrastUsingRGBAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();
    setVisibleTextField2();
    setVisibleIntervalsAdvText2();

    intervalsAdvText.setText("Contrast: \n -127 <= x <= 127");
    intervalsAdvText2.setText("Brightness: \n -127 <= x <= 127");
  }

  @FXML
  public void colorChannelFilterAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();
    setVisibleTextField2();
    setVisibleIntervalsAdvText2();
    setVisibleTextField3();
    setVisibleIntervalsAdvText3();
    intervalsAdvText.setText("Red: \n -1000 <= x <= 1000");
    intervalsAdvText2.setText("Green: \n -1000 <= x <= 1000");
    intervalsAdvText3.setText("Blue: \n -1000 <= x <= 1000");
  }

  @FXML
  public void embossFilterRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void invertColorsRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void thresholdingRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
    setVisibleSlider0_100();
  }

  @FXML
  public void thresholdingAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n 0 <= x <= 255");
  }

  @FXML
  public void edgeDetectionRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void histogramEqualizationRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void halftoneCirclesAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();
    setVisibleTextField2();
    setVisibleIntervalsAdvText2();
    setVisibleTextField3();
    setVisibleIntervalsAdvText3();
    intervalsAdvText.setText("Circle width: \n 0 < x <= 250");
    intervalsAdvText2.setText("Shift: \n -1000 <= x <= 1000");
    intervalsAdvText3.setText("Circles distances: \n 0 <= x <= 100");
  }

  @FXML
  public void halftoneErrorDiffusionRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void halftoneDitheringRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void halftoneRylandersRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void colorHistogramRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void grayHistogramRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void grayScaleQuantizationRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
    setVisibleSlider0_100();
  }

  @FXML
  public void grayScaleQuantizationAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n 1 < x <= 255");
  }

  @FXML
  public void noiseReductionRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void transformFlipRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAllComboBoxes();
    setInvisibleAdvancedMode();
    comboBox3.setPromptText("Orientation:");
    comboBox3.getItems().addAll("Horizontal", "Vertical");
    comboBox3.setVisible(true);
  }

  @FXML
  public void transformRotateRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
    setVisibleSlider100_100();
  }

  @FXML
  public void transformScaleAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();
    setVisibleTextField2();
    setVisibleIntervalsAdvText2();

    intervalsAdvText.setText("Width: \n 1 <= x");
    intervalsAdvText2.setText("Height: \n 1 <= x");
  }

  @FXML
  public void transformRotateAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("Rotation angle: \n -90 < x < 90");
  }

  @FXML
  public void compressRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
    setVisibleSlider0_100();
  }

  @FXML
  public void compressAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n 0.00 < x <= 1.00 ");
  }

  @FXML
  public void gammaRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
    setVisibleSlider100_100();
  }

  @FXML
  public void gammaAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText(
        "0 < x < 1 to decrease gamma \n" + "1 < x < 10 to increase gamma");
  }

  @FXML
  public void sharpnessEnhancerRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    setInvisibleAdvancedMode();
  }

  @FXML
  public void sharpnessEnhancerAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();
    setVisibleTextField2();
    setVisibleIntervalsAdvText2();

    intervalsAdvText.setText("\nAlpha");
    intervalsAdvText2.setText("\nBeta");
  }

  @FXML
  public void medianFilterAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n1 < x <= 100");
  }

  @FXML
  public void normalizedBlockFilterAdvRadioButtonClicked(ActionEvent e) {
    setInvisibleAllSlidersAndComboboxes();
    resetAdvancedMode();

    intervalsAdvText.setText("\n2 <= x <= 100");
  }

  @FXML
  public void goBackButtonClicked(ActionEvent e) {
    showStep1();
  }

  @FXML
  public void toggledButton(ActionEvent e) throws MalformedURLException {
    if (toggleButton.isSelected()) {
      setToggleButtonSelected();
      setImageUsingPath(processedImagePathFromServerConvertedToClient);
    } else {
      setToggleButtonUnselected();
      setImageUsingPath(originalImagePath);
    }
  }

  private void setInfoTextVisibleAndShow(String text) {
    infoText.setText(text);
    infoText.setVisible(true);
  }

  private void setInfoTextInvisible() {
    infoText.setText("");
    infoText.setVisible(false);
  }

  private void setTextFilePath(String text) {
    textFilePath.setText("");
    textFilePath.setText(text);
  }

  private void setImageUsingPath(String path) throws MalformedURLException {
    File tmpFile = new File(path);
    if (!tmpFile.isFile()) {
      tmpFile = new File("images/image_background.png");
      setTextFilePath("");
    }
    imageView.setImage(new Image(String.valueOf(tmpFile.toURI().toURL())));
  }

  private void setToggleButtonSelected() {
    toggleButton.setSelected(true);
    toggleButton.setText("Processed Image");
  }

  private void setToggleButtonUnselected() {
    toggleButton.setSelected(false);
    toggleButton.setText("Original Image");
  }

  private void setVisibleSlider100_100() {
    slider100_100.setValue(0);
    slider100_100.setVisible(true);
  }

  private void setInvisibleSlider100_100() {
    slider100_100.setVisible(false);
  }

  private void setVisibleSlider0_100() {
    slider0_100.setValue(0);
    slider0_100.setVisible(true);
  }

  private void setInvisibleSlider0_100() {
    slider0_100.setVisible(false);
  }

  private void setInvisibleAllSliders() {
    slider100_100.setVisible(false);
    slider0_100.setVisible(false);
  }

  private void setVisibleAdvancedMode() {
    textFieldInputAdv.setText("");
    textFieldInputAdv.setVisible(true);
    intervalsAdvText.setText("");
    intervalsAdvText.setVisible(true);
  }

  private void setInvisibleAdvancedMode() {
    textFieldInputAdv.setText("");
    textFieldInputAdv.setVisible(false);
    intervalsAdvText.setText("");
    intervalsAdvText.setVisible(false);
    textFieldInputAdv2.setText("");
    textFieldInputAdv2.setVisible(false);
    intervalsAdvText2.setText("");
    intervalsAdvText2.setVisible(false);
    textFieldInputAdv3.setText("");
    textFieldInputAdv3.setVisible(false);
    intervalsAdvText3.setText("");
    intervalsAdvText3.setVisible(false);
    setInvisibleAllComboBoxes();
  }

  private void resetAdvancedMode() {
    setInvisibleAdvancedMode();
    setVisibleAdvancedMode();
  }

  private void setInvisibleAllComboBoxes() {
    comboBox1.getItems().clear();
    comboBox2.getItems().clear();
    comboBox3.getItems().clear();
    comboBox1.setVisible(false);
    comboBox2.setVisible(false);
    comboBox3.setVisible(false);
  }

  private void setInvisibleAllSlidersAndComboboxes() {
    setInvisibleAllSliders();
    setInvisibleAllComboBoxes();
  }

  private void setVisibleTextField2() {
    textFieldInputAdv2.setText("");
    textFieldInputAdv2.setVisible(true);
  }

  private void setVisibleIntervalsAdvText2() {
    intervalsAdvText2.setText("");
    intervalsAdvText2.setVisible(true);
  }

  private void setVisibleTextField3() {
    textFieldInputAdv3.setText("");
    textFieldInputAdv3.setVisible(true);
  }

  private void setVisibleIntervalsAdvText3() {
    intervalsAdvText3.setText("");
    intervalsAdvText3.setVisible(true);
  }

  private void setloadImageFromServerTextError(String text) {
    loadImageFromServerTextError.setText(text);
    loadImageFromServerTextError.setVisible(true);
  }

  private void setInvisibleloadImageFromServerTextError() {
    loadImageFromServerTextError.setText("");
    loadImageFromServerTextError.setVisible(true);
  }

  private float getAndCheckInput(JFXTextField textField) {
    float field = 0;
    try {
      field = Float.parseFloat(textField.getText());
    } catch (NumberFormatException nfe) {
      setInfoTextVisibleAndShow("You must insert a valid number!");
    }
    return field;
  }

  private boolean checkServerIfFileListExists() throws IOException {
    ErrorCodes errorCodes = client.decisionMaker("GET-USERS-FILENAMES|" + username);
    List<String> listUserFiles = client.getUserFilesList(client.is);
    return !(listUserFiles == null || listUserFiles.isEmpty());
  }
}
