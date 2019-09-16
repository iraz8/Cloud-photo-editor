import java.io.*;
import java.net.URL;

class ImageDownloader {
  static ErrorCodes downloadFromURL(File file, URL url) throws IOException {
    InputStream inputStream = url.openStream();
    OutputStream outputStream = new FileOutputStream(file);
    byte[] buffer = new byte[4096];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, length);
    }

    inputStream.close();
    outputStream.close();
    return ErrorCodes.OK;
  }
}
