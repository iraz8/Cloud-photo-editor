import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class TransformScale {
  static ErrorCodes apply(File sourceFile, File destinationFile, int newWidth, int newHeight) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath());
    Mat destination = new Mat(newHeight, newWidth, source.type());
    if (newHeight + newWidth > source.rows() + source.cols())
      Imgproc.resize(source, destination, new Size(newWidth, newHeight), Imgproc.INTER_CUBIC);
    else Imgproc.resize(source, destination, new Size(newWidth, newHeight), Imgproc.INTER_AREA);
    Imgcodecs.imwrite(destinationFile.getAbsolutePath(), destination);
    return ErrorCodes.OK;
  }
}
