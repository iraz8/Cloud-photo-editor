import java.io.IOException;
import java.net.ServerSocket;

class TransferFilesServer {
  private static final int ECHOPORT = 57001;
  private static final int NUM_THREADS = 5;

  TransferFilesServer() {

    ServerSocket serverSocket;

    try {
      serverSocket = new ServerSocket(TransferFilesServer.ECHOPORT);
      System.out.println("TransferFilesServer booted");
    } catch (IOException e) {
      throw new RuntimeException("Could not create ServerSocket ", e);
    }
    for (int i = 0; i < TransferFilesServer.NUM_THREADS; i++) {
      new TransferFilesServerHandler(serverSocket, i).start();
    }
  }
}
