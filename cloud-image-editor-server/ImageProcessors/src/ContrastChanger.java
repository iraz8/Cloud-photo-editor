import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

class ContrastChanger {

  static ErrorCodes change(File sourceFile, File destinationFile, float alpha) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath());
    Mat destination = new Mat(source.rows(), source.cols(), source.type());
    source.convertTo(destination, -1, alpha, 0);
    Imgcodecs.imwrite(destinationFile.getAbsolutePath(), destination);
    return ErrorCodes.OK;
  }
}
