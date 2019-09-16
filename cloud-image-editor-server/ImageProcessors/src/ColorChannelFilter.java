import marvin.MarvinDefinitions;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

import java.io.File;

public class ColorChannelFilter {

  static ErrorCodes apply(
      File sourceFile, File destinationFile, int redLevel, int greenLevel, int blueLevel) {
    MarvinDefinitions.setImagePluginPath("libs/marvin/plugins/image/");
    MarvinImage image, backupImage;
    backupImage = MarvinImageIO.loadImage(sourceFile.getAbsolutePath());
    image = backupImage.clone();
    MarvinImagePlugin imagePlugin;
    imagePlugin =
        MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.color.colorChannel.jar");
    imagePlugin.setAttribute("red", redLevel);
    imagePlugin.setAttribute("green", greenLevel);
    imagePlugin.setAttribute("blue", blueLevel);
    imagePlugin.process(image, image);
    image.update();
    MarvinImageIO.saveImage(image, destinationFile.getAbsolutePath());
    return ErrorCodes.OK;
  }
}
