import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangePasswordScreenController {
  public Client client;
  public Stage windowStage;

  @FXML public JFXPasswordField currentPassword, newPassword, confirmNewPassword;
  public Text infoText;
  public JFXSpinner spinner;

  void setArgs(Client client, Stage windowStage) {
    this.client = client;
    this.windowStage = windowStage;
  }

  @FXML
  public void changePasswordButtonClicked(ActionEvent e) throws IOException {
    String currentPasswordText = currentPassword.getText();
    String newPasswordText = newPassword.getText();
    String confirmNewPasswordText = confirmNewPassword.getText();
    ErrorCodes errorCodes;
    setSpinnerEnabled();
    if (!checkIfInputExists(currentPasswordText)) {
      setInfoText("Insert your current password!");
      setSpinnerDisabled();
      return;
    }

    if (!checkIfInputExists(newPasswordText)) {
      setInfoText("Insert a valid new password!");
      setSpinnerDisabled();
      return;
    }

    if (!checkIfInputExists(newPasswordText)) {
      setInfoText("Insert the new password again in \"Confirm new password\"!");
      setSpinnerDisabled();
      return;
    }

    if (!checkConfirmedPassword(newPasswordText, confirmNewPasswordText)) {
      setInfoText("The new passwords are different!");
      setSpinnerDisabled();
      return;
    }

    errorCodes = client.decisionMaker("CHECK-IF-PASSWORD-IS-CORRECT|" + currentPasswordText);

    if (!errorCodes.equals(ErrorCodes.OK)) {
      setInfoText("Wrong password! Try again!");
      setSpinnerDisabled();
      return;
    }

    errorCodes =
        client.decisionMaker("CHANGE-PASSWORD|" + newPasswordText + "|" + currentPasswordText);

    if (errorCodes.equals(ErrorCodes.OK)) {
      setInfoText("Password changed successfully!");
      setSpinnerDisabled();
    }
  }

  @FXML
  public void goBackButtonClicked(ActionEvent e) {
    windowStage.close();
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

  private boolean checkConfirmedPassword(String password, String confirmPassword) {
    return password.equals(confirmPassword);
  }
}
