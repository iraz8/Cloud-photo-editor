import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class SelectImageScreenController {
  public Client client;
  public Stage windowStage;
  public String username;
  public List<String> listUserFiles;
  @FXML public JFXListView<String> listView;
  public ImageView imageView;
  public JFXButton goBackButton, loadImageButton, deleteFileButton;
  public Text infoText;
  String selectedItem;

  void setArgs(Client client, Stage windowStage) throws IOException {
    this.client = client;
    this.windowStage = windowStage;
    this.username = client.getUsername();
    listUserFiles = null;
    getUsersFileFromServer();
    if (listUserFiles.size() > 0) selectedItem = listUserFiles.get(0);
  }

  @FXML
  public void goBackButtonClicked(ActionEvent e) {
    windowStage.close();
  }

  @FXML
  public void loadImageButtonClicked(ActionEvent e) throws IOException {
    client.setCurrentSelectedFilename(selectedItem);
    client.getFileFromServer(selectedItem);

    windowStage.close();
  }

  @FXML
  public void deleteFileButtonClicked(ActionEvent e) throws IOException {
    String oldSelectedItem = selectedItem;
    ErrorCodes errorCodes = client.decisionMaker("DELETE-FILE|" + oldSelectedItem);
    if (listUserFiles.size() == 1) listUserFiles = null;
    File file = new File("tmp/" + client.getUsername() + "/" + oldSelectedItem);
    FileUtils.forceDelete(file);

    reloadUsersFilesView();
  }

  @FXML
  public void listViewClicked(MouseEvent e) throws IOException {
    this.selectedItem = listView.getSelectionModel().getSelectedItem();
    setImage();
  }

  private void getUsersFileFromServer() throws IOException {
    ErrorCodes errorCodes = client.decisionMaker("GET-USERS-FILENAMES|" + username);
    if (listUserFiles != null && !listUserFiles.isEmpty()) {
      listView.getItems().clear();
      listView.getSelectionModel().clearSelection();
      listView.getItems().removeAll(listUserFiles);
      setVisiblelistView();
    }
    listUserFiles = client.getUserFilesList(client.is);
    if (listUserFiles == null || listUserFiles.isEmpty()) {
      noFiles();
    } else {
      addItemsFromListToListView();
    }
  }

  private void reloadUsersFilesView() throws IOException {
    setSelectedNextItem();
    getUsersFileFromServer();
    setImage();
  }

  private void addItemsFromListToListView() {
    listView.getItems().addAll(listUserFiles);
  }

  private void setSelectedNextItem() {
    int nextItemIndex = 0;
    if (listUserFiles == null || listUserFiles.size() == 1) {
      selectedItem = null;
    } else {
      for (int i = 0; i < listUserFiles.size(); i++) {

        if (listUserFiles.get(i).equals(selectedItem)) {

          if (i == listUserFiles.size() - 1) {

            selectedItem = listUserFiles.get(0);

          } else {

            selectedItem = listUserFiles.get(i + 1);
            nextItemIndex = i + 1;
          }

          listView.scrollTo(nextItemIndex);
          listView.getSelectionModel().select(nextItemIndex);

          break;
        }
      }
    }
  }

  private void setImage() throws IOException {

    if (selectedItem != null) {
      client.getFileFromServer(selectedItem);
      String filePath = "tmp/" + client.getUsername() + "/" + selectedItem;
      File file = new File(filePath);
      Image image = null;
      try {
        image = new Image(String.valueOf(file.toURI().toURL()));
      } catch (MalformedURLException ex) {
        ex.printStackTrace();
      }
      imageView.setImage(image);
    } else {
      File file = new File("images/image_background.png");
      Image image = null;
      try {
        image = new Image(String.valueOf(file.toURI().toURL()));
      } catch (MalformedURLException ex) {
        ex.printStackTrace();
      }
      imageView.setImage(image);
    }
  }

  private void setInvisibleinfoText() {
    infoText.setText("");
    infoText.setVisible(false);
  }

  private void setInfoText(String text) {
    infoText.setText(text);
    infoText.setVisible(true);
  }

  private void setInvisiblelistView() {
    this.listView.setVisible(false);
    this.listView.setDisable(true);
  }

  private void setVisiblelistView() {
    this.listView.setVisible(true);
    this.listView.setDisable(false);
  }

  private void noFiles() {

    setInfoText(
        "There are no files on server! Try to go back and upload some files from your computer!");
    setInvisiblelistView();
    deleteFileButton.setDisable(true);
    loadImageButton.setDisable(true);
    File file = new File("images/image_background.png");
    Image image = null;
    try {
      image = new Image(String.valueOf(file.toURI().toURL()));
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
    }
    imageView.setImage(image);
  }
}
