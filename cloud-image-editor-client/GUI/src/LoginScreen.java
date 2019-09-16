import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class LoginScreen extends Application {

  public void start(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    Client client = new Client();
    if (client.initialize("localhost") == ErrorCodes.CONNECTION_TO_SERVER_ERROR) {
      System.out.println("SERVER OFFLINE");
      Platform.exit();
      System.exit(0);
    }

    FXMLLoader fxmlLoaderLoginScreen =
        new FXMLLoader(getClass().getResource("Others/loginScreen.fxml"));
    Parent loginScreen = fxmlLoaderLoginScreen.load();
    Scene loginScene = new Scene(loginScreen);
    LoginScreenController loginScreenController = fxmlLoaderLoginScreen.getController();

    FXMLLoader fxmlLoaderCreationAccount =
        new FXMLLoader(getClass().getResource("Others/createAccountScreen.fxml"));
    Parent creationAccountParent = fxmlLoaderCreationAccount.load();
    Scene creationAccountScene = new Scene(creationAccountParent);
    CreateAccountScreenController createAccountScreenController =
        fxmlLoaderCreationAccount.getController();

    FXMLLoader fxmlLoaderResetPassword =
        new FXMLLoader(getClass().getResource("Others/resetPasswordScreen.fxml"));
    Parent resetPasswordParent = fxmlLoaderResetPassword.load();
    Scene resetPasswordScene = new Scene(resetPasswordParent);
    ResetPasswordScreenController resetPasswordScreenController =
        fxmlLoaderResetPassword.getController();

    loginScreenController.setArgs(client, primaryStage, creationAccountScene, resetPasswordScene);
    createAccountScreenController.setArgs(client, primaryStage, loginScene);
    resetPasswordScreenController.setArgs(client, primaryStage, loginScene);

    primaryStage.initStyle(StageStyle.DECORATED);
    primaryStage.setMaxHeight(604);
    primaryStage.setMaxWidth(1030);
    primaryStage.setTitle("Cloud photo editor - Login");
    primaryStage.getIcons().add(new Image("file:images/logo_ico_app_bar.jpg"));
    primaryStage.setScene(loginScene);
    primaryStage.setResizable(false);

    primaryStage.show();
    primaryStage.setOnCloseRequest(
        new EventHandler<WindowEvent>() {
          public void handle(WindowEvent we) {
            try {
              client.decisionMaker("CLOSE");
              FileUtils.deleteQuietly(new File("/tmp"));
              Platform.exit();
              System.exit(0);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
    final SplashScreen splash = SplashScreen.getSplashScreen();
    if (splash != null) {
      splash.close();
    }
  }
}
