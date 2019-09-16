import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

class BrightnessChanger {

  static ErrorCodes change(File sourceFile, File destinationFile, float beta) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
    Mat destination = new Mat(source.rows(), source.cols(), source.type());
    source.convertTo(destination, -1, 1, beta);
    Imgcodecs.imwrite(
        destinationFile.getAbsolutePath(),
        destination,
        new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 100));
    return ErrorCodes.OK;
  }
}
