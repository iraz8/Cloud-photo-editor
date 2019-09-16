import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.io.IOException;
import java.net.ServerSocket;

class MainServer {
  private static final int ECHOPORT = 57000;
  private static final int NUM_THREADS = 5;
  static UsersSystem usersSystem;
  static Pbkdf2PasswordEncoder pbkdf2PasswordEncoder;
  TransferFilesServer transferFilesServer;

  private MainServer() {

    ServerSocket serverSocket;

    try {
      serverSocket = new ServerSocket(MainServer.ECHOPORT);

    } catch (IOException e) {
      throw new RuntimeException("Could not create ServerSocket ", e);
    }

    pbkdf2PasswordEncoder = new Pbkdf2PasswordEncoder();
    pbkdf2PasswordEncoder.setAlgorithm(
        Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);

    usersSystem = new UsersSystem();
    transferFilesServer = new TransferFilesServer();
    for (int i = 0; i < MainServer.NUM_THREADS; i++) {
      new MainServerHandler(serverSocket, i).start();
    }
  }

  public static void main(String[] av) {
    new MainServer();
  }
}
