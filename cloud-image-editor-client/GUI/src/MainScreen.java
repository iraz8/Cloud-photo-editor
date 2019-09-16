import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class MainScreen extends Application {
  Client client = null;

  public void setClient(Client client) {
    this.client = client;
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    FXMLLoader fxmlLoaderMainScreen =
        new FXMLLoader(getClass().getResource("Others/mainScreen.fxml"));
    Parent mainScreen = fxmlLoaderMainScreen.load();
    Scene mainScene = new Scene(mainScreen);
    MainScreenController mainScreenController = fxmlLoaderMainScreen.getController();

    mainScreenController.setArgs(client, primaryStage);
    primaryStage.setMaxHeight(604);
    primaryStage.setMaxWidth(1030);

    primaryStage.setTitle("Cloud photo editor");
    primaryStage.getIcons().add(new Image("file:images/logo_ico_small.png"));
    primaryStage.setResizable(false);
    primaryStage.setScene(mainScene);

    primaryStage.show();
    primaryStage.setOnCloseRequest(
        new EventHandler<WindowEvent>() {
          public void handle(WindowEvent we) {
            try {
              client.decisionMaker("CLOSE");
              FileUtils.deleteQuietly(new File("tmp"));
              Platform.exit();
              System.exit(0);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
  }
}
