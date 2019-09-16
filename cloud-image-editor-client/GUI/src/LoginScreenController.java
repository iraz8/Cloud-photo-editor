import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;

public class LoginScreenController {
  public Client client;
  public Scene creationAccountScene;
  public Scene resetPasswordScene;
  public Stage primaryStage;

  @FXML public JFXTextField username;
  public JFXPasswordField password;
  public JFXButton login, createAccount, exitButton;
  public Text infoText;
  public JFXSpinner spinner;
  public JFXButton forgotPassword;
  ErrorCodes errorCode = null;

  void setArgs(
      Client client, Stage primaryStage, Scene creationAccountScene, Scene resetPasswordScene) {
    this.client = client;
    this.primaryStage = primaryStage;
    this.creationAccountScene = creationAccountScene;
    this.resetPasswordScene = resetPasswordScene;
    spinner.setVisible(false);
  }

  @FXML
  public void usernameTextFieldClicked(ActionEvent e) {
    setInfoTextInvisible();
  }

  @FXML
  public void passwordFieldClicked(ActionEvent e) {
    setInfoTextInvisible();
  }

  @FXML
  public void loginButtonClicked(ActionEvent e) throws IOException {
    setSpinnerEnabled();

    String usernameText, passwordText;
    usernameText = username.getText();
    passwordText = password.getText();

    if (!checkIfInputExists(usernameText)) {
      setInfoText("Insert a valid username!");
      setSpinnerDisabled();
      return;
    }

    if (!checkIfInputExists(passwordText)) {
      setInfoText("Insert a valid password!");
      setSpinnerDisabled();
      return;
    }

    try {
      errorCode = client.decisionMaker("LOGIN|" + usernameText + "|" + passwordText);
    } catch (ConnectException exception) {
      System.out.println("Server offline");
      setInfoText("The server is offline!");
    }
    setSpinnerDisabled();

    if (!errorCode.equals(ErrorCodes.OK)) {
      if (errorCode.equals(ErrorCodes.USERNAME_NOT_EXIST))
        setInfoText("The username you’ve entered doesn’t match any account!");
      if (errorCode.equals(ErrorCodes.WRONG_PASSWORD))
        setInfoText("The password you’ve entered is incorrect!");
    } else {
      setInfoTextInvisible();
      MainScreen mainScreen = new MainScreen();
      mainScreen.setClient(this.client);
      mainScreen.start(primaryStage);
    }
  }

  @FXML
  public void createAccountButtonClicked(ActionEvent e) {
    setSpinnerDisabled();
    primaryStage.setTitle("Cloud photo editor - Create account");
    primaryStage.setScene(creationAccountScene);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  @FXML
  public void forgotPasswordButtonClicked(ActionEvent e) {
    setSpinnerDisabled();
    primaryStage.setTitle("Cloud photo editor - Reset password");
    primaryStage.setScene(resetPasswordScene);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  @FXML
  public void exitButtonClicked(ActionEvent e) throws IOException {
    client.decisionMaker("CLOSE");
    FileUtils.deleteQuietly(new File("tmp"));
    Platform.exit();
    System.exit(0);
  }

  private void setInfoText(String info) {
    infoText.setText(info);
    infoText.setVisible(true);
  }

  private void setInfoTextInvisible() {
    infoText.setText("");
    infoText.setVisible(false);
  }

  private void setSpinnerEnabled() {
    spinner.setDisable(false);
    spinner.setVisible(true);
  }

  private void setSpinnerDisabled() {
    spinner.setDisable(true);
    spinner.setVisible(false);
  }

  private boolean checkIfInputExists(String input) {
    return input != null && !input.isEmpty();
  }
}
