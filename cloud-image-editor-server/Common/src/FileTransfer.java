import java.io.*;
import java.net.Socket;

public class FileTransfer {
  private final Socket socket;

  FileTransfer(Socket socket) {
    this.socket = socket;
  }

  public void send(File fileToBeSend) throws IOException {
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    DataInputStream in = new DataInputStream(new FileInputStream(fileToBeSend.getAbsoluteFile()));
    int count;
    byte[] buffer = new byte[8192];
    while ((count = in.read(buffer)) > 0) {
      out.write(buffer, 0, count);
    }

    in.close();
    out.flush();
    out.close();
  }

  public void receive(File fileToBeReceived) throws IOException {
    DataOutputStream out =
        new DataOutputStream(new FileOutputStream(fileToBeReceived.getAbsoluteFile()));
    DataInputStream in = new DataInputStream(socket.getInputStream());
    int count;
    byte[] buffer = new byte[8192];
    while ((count = in.read(buffer)) > 0) {
      out.write(buffer, 0, count);
    }
    in.close();
    out.flush();
    out.close();
  }
}
