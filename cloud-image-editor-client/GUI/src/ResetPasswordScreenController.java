import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;

public class ResetPasswordScreenController {
  public Client client;
  public Scene loginScene;
  public Stage primaryStage;
  @FXML public JFXTextField username, email;
  public JFXButton resetPasswordViaUsername, resetPasswordViaEmail, goBack;
  public Text infoText;
  public JFXSpinner spinner;
  ErrorCodes errorCodes = null;

  void setArgs(Client client, Stage primaryStage, Scene loginScene) {
    this.client = client;
    this.primaryStage = primaryStage;
    this.loginScene = loginScene;
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
  public void resetPasswordViaUsernameButtonClicked(ActionEvent e) throws IOException {
    setSpinnerEnabled();
    String usernameText = username.getText();
    if (!checkIfInputExists(usernameText)) {
      setSpinnerDisabled();
      setInfoText("Please insert your username!");
      return;
    }

    setSpinnerEnabled();
    try {
      errorCodes = client.decisionMaker("RESET-PASSWORD-VIA-USERNAME|" + usernameText);
    } catch (ConnectException exception) {
      System.out.println("Server offline");
      setInfoText("Server offline!");
    }

    if (errorCodes.equals(ErrorCodes.OK)) {
      setInfoText("The password was reset! Please check your email!");
    } else {
      if (errorCodes.equals(ErrorCodes.USERNAME_NOT_EXIST))
        setInfoText("The username you’ve entered doesn’t match any account!");
    }
    setSpinnerDisabled();
  }

  @FXML
  public void resetPasswordViaEmailButtonClicked(ActionEvent e) throws IOException {
    setSpinnerEnabled();
    String emailText = email.getText();
    if (!checkIfInputExists(emailText)) {
      setSpinnerDisabled();
      setInfoText("Please insert your email!");
      return;
    }

    setSpinnerEnabled();
    try {
      errorCodes = client.decisionMaker("RESET-PASSWORD-VIA-EMAIL|" + emailText);
    } catch (ConnectException exception) {
      System.out.println("Server offline");
      setInfoText("Server offline!");
    }

    if (errorCodes.equals(ErrorCodes.OK)) {
      setInfoText("The password was reset! Please check your email!");
    } else {
      if (errorCodes.equals(ErrorCodes.EMAIL_NOT_EXIST))
        setInfoText("The email you’ve entered doesn’t match any account!");
    }

    setSpinnerDisabled();
  }

  @FXML
  public void goBackButtonClicked() {
    primaryStage.setTitle("Cloud Photo Editor - Login");
    primaryStage.setScene(loginScene);
    primaryStage.setResizable(false);
    primaryStage.show();
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
