import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateAccountScreenController {
  public Client client;
  public Scene loginScene;
  public Stage primaryStage;

  @FXML public JFXTextField username, email;
  public JFXPasswordField password, confirmPassword;
  public JFXButton createAccount, goBack;
  public JFXSpinner spinner;
  public Text infoText;
  ErrorCodes errorCodes = null;

  void setArgs(Client client, Stage primaryStage, Scene loginScene) {
    this.client = client;
    this.primaryStage = primaryStage;
    this.loginScene = loginScene;
  }

  private boolean checkConfirmedPassword(String password, String confirmPassword) {
    return password.equals(confirmPassword);
  }

  @FXML
  public void createAccountButtonClicked() throws IOException {
    String usernameText = username.getText();
    String passwordText = password.getText();
    String confirmPasswordText = confirmPassword.getText();
    String emailText = email.getText();
    setSpinnerEnabled();
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

    if (!checkIfInputExists(confirmPasswordText)) {
      setInfoText("The passwords are different!");
      setSpinnerDisabled();
      return;
    }

    if (!checkIfInputExists(emailText)) {
      setInfoText("Insert a valid email!");
      setSpinnerDisabled();
      return;
    }

    if (!checkConfirmedPassword(passwordText, confirmPasswordText)) {
      setInfoText("The passwords are different!");
      setSpinnerDisabled();
      return;
    }

    errorCodes =
        client.decisionMaker(
            "NEWUSER|" + username.getText() + "|" + password.getText() + "|" + email.getText());
    setSpinnerDisabled();
    if (errorCodes != ErrorCodes.OK) {
      if (errorCodes.equals(ErrorCodes.WRONG_USERNAME_PATTERN))
        setInfoText("The username must have minimum 4 characters and to not contain spaces!");
      if (errorCodes.equals(ErrorCodes.WRONG_PASSWORD_PATTERN))
        setInfoText("The password must have minimum 6 characters and to not contain spaces!");
      if (errorCodes.equals(ErrorCodes.WRONG_EMAIL_PATTERN))
        setInfoText("Please insert a valid email address");
      if (errorCodes.equals(ErrorCodes.USERNAME_EXISTS))
        setInfoText("An account with this username already exists!");
      if (errorCodes.equals(ErrorCodes.EMAIL_EXISTS))
        setInfoText("An account with this email already exists!");
    } else {
      setInfoText("The account was created successfully!");
    }
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
