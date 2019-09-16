import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

class ColorToGrayConverter {
  static ErrorCodes convert(File sourceFile, File destinationFile) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath());
    Mat destination = new Mat();
    Imgproc.cvtColor(source, destination, Imgproc.COLOR_RGB2GRAY);
    Imgcodecs.imwrite(destinationFile.getAbsolutePath(), destination);
    return ErrorCodes.OK;
  }
}
