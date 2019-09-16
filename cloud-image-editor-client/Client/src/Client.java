import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class Client {

  BufferedReader is;
  PrintWriter os;
  String username;
  String currentSelectedFilename;
  private Socket serverSocket;
  private String lastProcessedImageFileName;

  public static void main(String[] argv) {
    if (argv.length == 0) {
      new Client().initialize("localhost");
    } else {
      new Client().initialize(argv[0]);
    }
  }

  public ErrorCodes initialize(String hostName) {
    try {
      serverSocket = new Socket(hostName, 57000);

      os = new PrintWriter(serverSocket.getOutputStream(), true);
      is = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
    } catch (IOException e) {
      e.printStackTrace();
      return ErrorCodes.CONNECTION_TO_SERVER_ERROR;
    }
    return ErrorCodes.OK;
  }

  public ErrorCodes decisionMaker(String msg) throws IOException {
    ErrorCodes errorCode = ErrorCodes.OK;
    String command = getCommandFromMsg(msg).toUpperCase();
    String[] parameters = getParametersFromMsg(msg);
    sendMessageToServer(msg, os);
    switch (command) {
      case "CLOSE":
      case "LOGOUT":
        errorCode = logout();
        break;
      case "LOGIN":
        errorCode = ErrorCodes.valueOf(getMsgFromServer(is));
        if (errorCode.equals(ErrorCodes.OK)) this.username = parameters[0];
        break;
      case "NEWUSER":
      case "CHECK-IF-PASSWORD-IS-CORRECT":
      case "CHANGE-PASSWORD":
      case "RESET-PASSWORD-VIA-USERNAME":
      case "RESET-PASSWORD-VIA-EMAIL":
      case "DELETE-FILE":
        errorCode = ErrorCodes.valueOf(getMsgFromServer(is));
        break;
      case "GET-USERS-FILENAMES":
        break;
      case "CHANGE-BRIGHTNESS":
      case "GAUSSIAN-FILTER":
      case "CONVERT-COLOR-TO-GRAY":
      case "DILATATION":
      case "EROSION":
      case "SEPIA":
      case "CHANGE-CONTRAST":
      case "CHANGE-BRIGHTNESS-ADVANCED":
      case "CHANGE-CONTRAST-ADVANCED":
      case "GAUSSIAN-FILTER-ADVANCED":
      case "DILATATION-ADVANCED":
      case "EROSION-ADVANCED":
      case "MOSAIC":
      case "MOSAIC-ADVANCED":
      case "TELEVISION-EFFECT":
      case "PIXELIZE":
      case "PIXELIZE-ADVANCED":
      case "BLACK-AND-WHITE":
      case "BLACK-AND-WHITE-ADVANCED":
      case "BRIGHTNESS-AND-CONTRAST-ADVANCED":
      case "COLOR-CHANNEL-FILTER-ADVANCED":
      case "EMBOSS-FILTER":
      case "INVERT-COLORS":
      case "THRESHOLDING":
      case "THRESHOLDING-ADVANCED":
      case "EDGE-DETECTION":
      case "HISTOGRAM-EQUALIZATION":
      case "HALFTONE-CIRCLES":
      case "HALFTONE-ERROR-DIFFUSION":
      case "HALFTONE-DITHERING":
      case "HALFTONE-RYLANDERS":
      case "COLOR-HISTOGRAM":
      case "GRAY-HISTOGRAM":
      case "GRAY-SCALE-QUANTIZATION":
      case "GRAY-SCALE-QUANTIZATION-ADVANCED":
      case "NOISE-REDUCTION":
      case "TRANSFORM-FLIP":
      case "TRANSFORM-ROTATE":
      case "TRANSFORM-ROTATE-ADVANCED":
      case "TRANSFORM-SCALE-ADVANCED":
      case "COMPRESS":
      case "COMPRESS-ADVANCED":
      case "GAMMA-CHANGER":
      case "GAMMA-CHANGER-ADVANCED":
      case "SHARPNESS-ENHANCER":
      case "SHARPNESS-ENHANCER-ADVANCED":
      case "MEDIAN-FILTER-ADVANCED":
      case "NORMALIZED-BLOCK-FILTER-ADVANCED":
        String filenameAfterProcessing = is.readLine();
        getFileFromServer(filenameAfterProcessing);
        lastProcessedImageFileName = filenameAfterProcessing;
        errorCode = ErrorCodes.valueOf(getMsgFromServer(is));
        break;

      case "SEND-FILE-TO-SERVER":
        sendFileToServer(parameters[0]);
        this.currentSelectedFilename = parameters[0];
        errorCode = ErrorCodes.valueOf(getMsgFromServer(is));
        break;
      default:
        System.err.println(
            "ERROR! Method: int decisionMaker(String line)! Unknown command! Command:" + command);
        break;
    }
    System.out.println("[ERRORCODE-CLIENT] " + errorCode);
    return errorCode;
  }

  private String getMsgFromServer(BufferedReader is) throws IOException {
    return is.readLine();
  }

  public String getLastProcessedImageFileName() {
    return this.lastProcessedImageFileName;
  }

  private String getCommandFromMsg(String msg) {
    String[] msgSplitted = msg.split("\\|");
    return msgSplitted[0];
  }

  private String[] getParametersFromMsg(String msg) {
    String[] msgSplitted = msg.split("\\|");
    return Arrays.copyOfRange(msgSplitted, 1, msgSplitted.length);
  }

  private void sendMessageToServer(String msg, PrintWriter os) {
    os.println(msg);
  }

  private String getInput() {
    return (new Scanner(System.in)).nextLine();
  }

  private ErrorCodes closeConnection(Socket serverSocket) {
    String clientInetAddress = serverSocket.getInetAddress().toString();
    try {
      serverSocket.close();
      return ErrorCodes.OK;
    } catch (IOException e) {
      System.err.println(
          "ERROR! method void closeConnection(Socket clientSocket)\n "
              + "Connection: "
              + clientInetAddress
              + " still open!");
      e.printStackTrace();
      return ErrorCodes.CONNECTION_NOT_CLOSED;
    }
  }

  private ErrorCodes logout() {
    return closeConnection(serverSocket);
  }

  private void sendFileToServer(String path) throws IOException {
    Socket transferFilesServerSocket = new Socket("localhost", 57001);
    FileTransfer fileTransfer = new FileTransfer(transferFilesServerSocket);
    PrintWriter osTransferFile = new PrintWriter(transferFilesServerSocket.getOutputStream(), true);
    sendMessageToServer(username, osTransferFile);

    File file = new File(path);

    sendMessageToServer("SEND-FILE-TO-SERVER|" + file.getName(), osTransferFile);
    fileTransfer.send(file);
  }

  private void receiveFileFromServer(String path) throws IOException {
    Socket transferFilesServerSocket = new Socket("localhost", 57001);
    File file = new File(path);
    FileTransfer fileTransfer = new FileTransfer(transferFilesServerSocket);
    fileTransfer.receive(file);
  }

  public void getFileFromServer(String path) throws IOException {
    String[] pathSplitted = path.split("/");
    String fileName;
    if (pathSplitted.length > 1) {
      fileName =
          pathSplitted[pathSplitted.length - 2] + "/" + pathSplitted[pathSplitted.length - 1];
    } else {
      fileName = username + "/" + pathSplitted[0];
    }
    new File("tmp/" + fileName).delete();
    File file = new File("tmp/" + fileName);
    file.getParentFile().mkdirs();
    file.createNewFile();

    Socket transferFilesServerSocket = new Socket("localhost", 57001);
    FileTransfer fileTransfer = new FileTransfer(transferFilesServerSocket);

    PrintWriter osTransferFilePW =
        new PrintWriter(transferFilesServerSocket.getOutputStream(), true);
    sendMessageToServer(username, osTransferFilePW);
    sendMessageToServer("GET-FILE-FROM-SERVER|" + file.getName(), osTransferFilePW);

    fileTransfer.receive(file);
  }

  public String getUsername() {
    return this.username;
  }

  public List<String> getUserFilesList(BufferedReader is) throws IOException {
    List<String> list = new ArrayList<>();
    boolean endReadFlag = false;
    while (!endReadFlag) {
      String readedLine = getMsgFromServer(is);
      if (readedLine.equals(String.valueOf(ErrorCodes.END_READING_FILENAMES))) {
        endReadFlag = true;

      } else {
        list.add(readedLine);
      }
    }
    String readedLine = getMsgFromServer(is);

    return list;
  }

  public String getCurrentSelectedFilename() {
    return this.currentSelectedFilename;
  }

  public void setCurrentSelectedFilename(String filename) {
    this.currentSelectedFilename = filename;
  }
}
