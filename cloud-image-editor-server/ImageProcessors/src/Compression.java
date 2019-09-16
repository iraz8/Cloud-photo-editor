import org.apache.commons.io.FilenameUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class Compression {
  static ErrorCodes compressImage(
      File initialSourceFile, File destinationFile, float compressionQuality) throws IOException {
    File sourceFile, tempFile = null;
    if (FilenameUtils.getExtension(initialSourceFile.getAbsolutePath()).equals("png")) {
      tempFile =
          new File(
              FilenameUtils.getPath(initialSourceFile.getPath())
                  + FilenameUtils.getBaseName(initialSourceFile.getAbsolutePath())
                  + "[tmp].jpg");
      tempFile.createNewFile();
      PNGtoJPGConverter.convert(initialSourceFile, tempFile);
      sourceFile = tempFile;
    } else sourceFile = initialSourceFile;

    BufferedImage image = ImageIO.read(sourceFile);
    OutputStream os = new FileOutputStream(destinationFile);
    Iterator<ImageWriter> writers;
    writers = ImageIO.getImageWritersByFormatName("jpg");
    ImageWriter writer = writers.next();
    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
    writer.setOutput(ios);
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(compressionQuality);
    writer.write(null, new IIOImage(image, null, null), param);

    os.close();
    ios.close();
    writer.dispose();

    if (FilenameUtils.getExtension(initialSourceFile.getAbsolutePath()).equals("png")) {
      JPGtoPNGConverter.convert(destinationFile, destinationFile);
      tempFile.delete();
    }

    return ErrorCodes.OK;
  }
}
