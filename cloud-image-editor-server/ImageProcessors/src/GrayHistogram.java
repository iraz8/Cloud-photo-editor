import marvin.MarvinDefinitions;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

import java.io.File;

public class GrayHistogram {
  static ErrorCodes get(File sourceFile, File destinationFile) {
    MarvinDefinitions.setImagePluginPath("libs/marvin/plugins/image/");
    MarvinImage image, backupImage;
    backupImage = MarvinImageIO.loadImage(sourceFile.getAbsolutePath());
    image = backupImage.clone();
    MarvinImagePlugin imagePlugin;
    imagePlugin =
        MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.histogram.grayHistogram.jar");
    imagePlugin.process(image, image);
    image.update();
    MarvinImageIO.saveImage(image, destinationFile.getAbsolutePath());
    return ErrorCodes.OK;
  }
}
