import marvin.MarvinDefinitions;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

import java.io.File;

public class Mosaic {
  /*
  Input Attributes:

  width - tile´s width.
  shape - accepted values: "squares" or "triangles".
  border - true or false.
   */
  static ErrorCodes apply(
      File sourceFile, File destinationFile, int width, String shape, boolean border) {
    MarvinDefinitions.setImagePluginPath("libs/marvin/plugins/image/");
    MarvinImage image, backupImage;
    backupImage = MarvinImageIO.loadImage(sourceFile.getAbsolutePath());
    image = backupImage.clone();
    MarvinImagePlugin imagePlugin;
    imagePlugin = MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.artistic.mosaic.jar");

    imagePlugin.setAttribute("width", width);
    if (shape.equals("squares")) imagePlugin.setAttribute("shape", "squares");
    else imagePlugin.setAttribute("shape", "triangles");
    imagePlugin.setAttribute("border", border);
    imagePlugin.process(image, image);
    image.update();
    MarvinImageIO.saveImage(image, destinationFile.getAbsolutePath());
    return ErrorCodes.OK;
  }
}
