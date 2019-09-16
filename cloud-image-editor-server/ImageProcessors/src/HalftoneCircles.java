import marvin.MarvinDefinitions;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

import java.io.File;

public class HalftoneCircles {
  static ErrorCodes apply(
      File sourceFile, File destinationFile, int circleWidth, int shift, int circlesDistance) {
    MarvinDefinitions.setImagePluginPath("libs/marvin/plugins/image/");
    MarvinImage image, backupImage;
    backupImage = MarvinImageIO.loadImage(sourceFile.getAbsolutePath());
    image = backupImage.clone();
    MarvinImagePlugin imagePlugin;
    imagePlugin =
        MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.halftone.circles.jar");
    imagePlugin.setAttribute("circleWidth", circleWidth);
    imagePlugin.setAttribute("shift", shift);
    imagePlugin.setAttribute("circlesDistance", circlesDistance);
    imagePlugin.process(image, image);
    image.update();
    MarvinImageIO.saveImage(image, destinationFile.getAbsolutePath());
    return ErrorCodes.OK;
  }
}
