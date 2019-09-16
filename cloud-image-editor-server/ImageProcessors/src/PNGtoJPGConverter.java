import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PNGtoJPGConverter {
  static void convert(File sourceFile, File destinationFile) throws IOException {
    BufferedImage img = ImageIO.read(sourceFile);
    ImageIO.write(img, "jpg", destinationFile);
  }
}
