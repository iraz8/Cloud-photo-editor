import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

class SharpnessEnhancer {
  static ErrorCodes enhance(
      File sourceFile, File destinationFile, float parameter1, float parameter2) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath());
    Mat destination = new Mat(source.rows(), source.cols(), source.type());
    Imgproc.GaussianBlur(source, destination, new Size(0, 0), 10);
    Core.addWeighted(source, parameter1, destination, parameter2, 0, destination);
    Imgcodecs.imwrite(destinationFile.getAbsolutePath(), destination);
    return ErrorCodes.OK;
  }
}
