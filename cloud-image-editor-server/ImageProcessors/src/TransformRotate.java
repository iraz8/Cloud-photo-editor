import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class TransformRotate {
  static ErrorCodes apply(File sourceFile, File destinationFile, int rotateAngle) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat source = Imgcodecs.imread(sourceFile.getAbsolutePath());
    Mat destination = new Mat(source.rows(), source.cols(), source.type());
    Point center = new Point(destination.cols() / 2, destination.rows() / 2);
    Mat rotMat;
    rotMat = Imgproc.getRotationMatrix2D(center, rotateAngle, 1);
    Imgproc.warpAffine(source, destination, rotMat, destination.size());
    Imgcodecs.imwrite(destinationFile.getAbsolutePath(), destination);
    return ErrorCodes.OK;
  }
}
