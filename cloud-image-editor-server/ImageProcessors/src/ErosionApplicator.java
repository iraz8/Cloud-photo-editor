import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

class ErosionApplicator {
  static ErrorCodes apply(File sourceFile, File destinationFile, float erosion_size) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath());
    Mat destination;
    destination = source;
    Mat element =
        Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT, new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
    Imgproc.erode(source, destination, element);
    Imgcodecs.imwrite(destinationFile.getAbsolutePath(), destination);
    return ErrorCodes.OK;
  }
}
