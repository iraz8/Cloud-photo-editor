import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GammaCorrection {
  static ErrorCodes change(File sourceFile, File destinationFile, float gamma) throws IOException {

    BufferedImage original = ImageIO.read(sourceFile);
    BufferedImage gamma_corrected = gammaCorrection(original, gamma);
    ImageIO.write(gamma_corrected, "jpg", destinationFile);
    return ErrorCodes.OK;
  }

  private static BufferedImage gammaCorrection(BufferedImage original, double gamma) {

    int alpha, red, green, blue;
    int newPixel;

    double gamma_new = 1 / gamma;
    int[] gamma_LUT = gamma_LUT(gamma_new);

    BufferedImage gamma_cor =
        new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

    for (int i = 0; i < original.getWidth(); i++) {
      for (int j = 0; j < original.getHeight(); j++) {

        alpha = new Color(original.getRGB(i, j)).getAlpha();
        red = new Color(original.getRGB(i, j)).getRed();
        green = new Color(original.getRGB(i, j)).getGreen();
        blue = new Color(original.getRGB(i, j)).getBlue();

        red = gamma_LUT[red];
        green = gamma_LUT[green];
        blue = gamma_LUT[blue];

        newPixel = colorToRGB(alpha, red, green, blue);

        gamma_cor.setRGB(i, j, newPixel);
      }
    }

    return gamma_cor;
  }

  private static int[] gamma_LUT(double gamma_new) {
    int[] gamma_LUT = new int[256];

    for (int i = 0; i < gamma_LUT.length; i++) {
      gamma_LUT[i] = (int) (255 * (Math.pow((double) i / (double) 255, gamma_new)));
    }

    return gamma_LUT;
  }

  private static int colorToRGB(int alpha, int red, int green, int blue) {

    int newPixel = 0;
    newPixel += alpha;
    newPixel = newPixel << 8;
    newPixel += red;
    newPixel = newPixel << 8;
    newPixel += green;
    newPixel = newPixel << 8;
    newPixel += blue;

    return newPixel;
  }
}
