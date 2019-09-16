import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

class GaussianFilter {
  static ErrorCodes apply(File sourceFile, File destinationFile, int width) {
    //noinspection SuspiciousNameCombination
    return apply(sourceFile, destinationFile, width, width);
  }

  static ErrorCodes apply(File sourceFile, File destinationFile, int width, int height) {
    // In biblioteca din Opencv trebuie indeplinita conditia:  ksize.width > 0 && ksize.width %
    // 2 == 1 && ksize.height > 0 && ksize.height % 2 == 1 in function
    // 'cv::createGaussianKernels'
    if (width % 2 == 0) width++;
    if (height % 2 == 0) height++;

    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath());
    Mat destination = new Mat(source.rows(), source.cols(), source.type());
    Imgproc.GaussianBlur(source, destination, new Size(width, height), 0);

    Imgcodecs.imwrite(destinationFile.getAbsolutePath(), destination);
    return ErrorCodes.OK;
  }
}
