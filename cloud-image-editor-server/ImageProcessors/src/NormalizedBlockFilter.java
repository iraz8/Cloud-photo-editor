import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

class NormalizedBlockFilter {
  static ErrorCodes apply(File sourceFile, File destinationFile, int maxKernelLength) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath());
    Mat destination = new Mat(source.rows(), source.cols(), source.type());
    for (int i = 1; i < maxKernelLength; i = i + 2) {
      Imgproc.blur(source, destination, new Size(i, i), new Point(-1, -1));
    }
    Imgcodecs.imwrite(destinationFile.getAbsolutePath(), destination);
    return ErrorCodes.OK;
  }
}
