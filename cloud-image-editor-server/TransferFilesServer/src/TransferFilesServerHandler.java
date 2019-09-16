import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

class TransferFilesServerHandler extends Thread {
  static final String usersHomePath = "home/";
  static int nrTotalThreads = 0;
  private final ServerSocket serverSocket;
  BufferedReader is;
  PrintStream os;
  String username;
  private Socket clientSocket;

  TransferFilesServerHandler(ServerSocket s, int i) {
    addNrTotalThreads();
    serverSocket = s;
    setName("Thread " + i);
  }

  synchronized void addNrTotalThreads() {
    nrTotalThreads++;
  }

  public void run() {
    try {
      System.out.println("[TransferFilesServer] " + getName() + " waiting");

      synchronized (serverSocket) {
        clientSocket = serverSocket.accept();
      }
      is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      os = new PrintStream(clientSocket.getOutputStream(), true);

      System.out.println(
          "[TransferFilesServer] " + getName() + " starting, IP=" + clientSocket.getInetAddress());
      decisionMaker();
      if (!clientSocket.isClosed()) {
        clientSocket.close();
      }

      System.out.println("[TransferFilesServer] " + getName() + " Closed ");

    } catch (IOException ex) {
      System.out.println(getName() + ": IO Error on socket " + ex);
    }
    new TransferFilesServerHandler(serverSocket, nrTotalThreads).start();
  }

  public void decisionMaker() throws IOException {
    String msgFromClient = getMsgFromClient(is);
    this.username = msgFromClient;
    msgFromClient = getMsgFromClient(is);
    String command = getCommandFromMsg(msgFromClient);
    String[] parameters = getParametersFromMsg(msgFromClient);
    System.out.println("TRANSFER SERVER:" + msgFromClient);
    switch (command) {
      case "SEND-FILE-TO-SERVER":
        receiveFileFromServer(parameters[0]);
        break;
      case "GET-FILE-FROM-SERVER":
        getFileFromServer(parameters[0]);
        break;
    }
    is.close();
    os.close();
    clientSocket.close();
    //  closeConnection(clientSocket);
  }

  private String getMsgFromClient(BufferedReader is) throws IOException {
    return is.readLine();
  }

  private String getCommandFromMsg(String msg) {
    String[] msgSplitted = msg.split("\\|");
    return msgSplitted[0];
  }

  private String[] getParametersFromMsg(String msg) {
    String[] msgSplitted = msg.split("\\|");
    return Arrays.copyOfRange(msgSplitted, 1, msgSplitted.length);
  }

  private void getFileFromServer(String fileName) throws IOException {
    File file = new File(usersHomePath + username + "/" + fileName);
    FileTransfer fileTransfer = new FileTransfer(clientSocket);
    fileTransfer.send(file);
  }

  private void sendFileToServer(String path) throws IOException {
    File file = new File(path);
    FileTransfer fileTransfer = new FileTransfer(clientSocket);
    fileTransfer.send(file);
  }

  private void receiveFileFromServer(String fileName) throws IOException {
    new File(usersHomePath + username + "/" + fileName).delete();
    File file = new File(usersHomePath + username + "/" + fileName);
    file.getParentFile().mkdirs();
    file.createNewFile();
    FileTransfer fileTransfer = new FileTransfer(clientSocket);
    fileTransfer.receive(file);
  }

  private void closeConnection(Socket clientSocket) {
    String clientInetAddress = clientSocket.getInetAddress().toString();
    try {
      clientSocket.close();
    } catch (IOException e) {
      System.err.println("ERROR! method void closeConnection(Socket clientSocket)");
      e.printStackTrace();
    }

    if (clientSocket.isClosed())
      System.out.println(
          "Connection closed! The socket with IP " + clientInetAddress + " is closed!");
    else
      System.err.println(
          "ERROR! method void closeConnection(Socket clientSocket). Connection with "
              + clientInetAddress
              + "is still open!");
  }
}
