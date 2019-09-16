import marvin.MarvinDefinitions;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

import java.io.File;

public class TransformFlip {
  static ErrorCodes apply(File sourceFile, File destinationFile, String orientation) {
    MarvinDefinitions.setImagePluginPath("libs/marvin/plugins/image/");
    MarvinImage image, backupImage;
    backupImage = MarvinImageIO.loadImage(sourceFile.getAbsolutePath());
    image = backupImage.clone();
    MarvinImagePlugin imagePlugin;
    imagePlugin = MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.transform.flip.jar");
    imagePlugin.setAttribute("flip", orientation);
    imagePlugin.process(image, image);
    image.update();
    MarvinImageIO.saveImage(image, destinationFile.getAbsolutePath());
    return ErrorCodes.OK;
  }
}
