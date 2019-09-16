import org.apache.commons.io.FilenameUtils;

public class FilenamesChanger {

  public static String[] getFileNameBeforeAndAfterProcessing(
      String usersHomePath,
      String clientUsername,
      String command,
      String[] parameters,
      float parameterNr) {
    String[] filenameBeforeAndAfterProcessing = new String[2];
    String fileName = parameters[0];
    String path = usersHomePath + clientUsername + "/";
    String op;
    filenameBeforeAndAfterProcessing[0] = path + fileName;
    switch (command) {
      case "CHANGE-BRIGHTNESS":
      case "CHANGE-BRIGHTNESS-ADVANCED":
        if (parameterNr > 0.0f) op = "INCREASED";
        else if (parameterNr < 0.0f) op = "DECREASED";
        else op = "NO CHANGE";
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + op
                + " BRIGHTNESS "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "CHANGE-CONTRAST":
      case "CHANGE-CONTRAST-ADVANCED":
        if (parameterNr > 1.0f) op = "INCREASED";
        else if (parameterNr < 1.0f) op = "DECREASED";
        else op = "NO CHANGE";
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + op
                + " CONTRAST "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "GAUSSIAN-FILTER":
      case "GAUSSIAN-FILTER-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "BLUR "
                + (int) parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "CONVERT-COLOR-TO-GRAY":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "COLOR-TO-GRAY"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "DILATATION":
      case "DILATATION-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "DILATATION "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "EROSION":
      case "EROSION-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "EROSION "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "SEPIA":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "SEPIA"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "MOSAIC":
      case "MOSAIC-ADVANCED":
        if (Boolean.parseBoolean(parameters[3]))
          filenameBeforeAndAfterProcessing[1] =
              path
                  + FilenameUtils.removeExtension(fileName)
                  + "["
                  + "MOSAIC "
                  + parameterNr
                  + " "
                  + parameters[2]
                  + " with border]."
                  + FilenameUtils.getExtension(fileName);
        else
          filenameBeforeAndAfterProcessing[1] =
              path
                  + FilenameUtils.removeExtension(fileName)
                  + "["
                  + "MOSAIC "
                  + parameterNr
                  + " "
                  + parameters[2]
                  + " without border]."
                  + FilenameUtils.getExtension(fileName);
        break;
      case "TELEVISION-EFFECT":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "TELEVISION EFFECT"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "PIXELIZE":
      case "PIXELIZE-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "PIXELIZE "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "BLACK-AND-WHITE":
      case "BLACK-AND-WHITE-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "BLACK-AND-WHITE "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "BRIGHTNESS-AND-CONTRAST-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "BRIGHTNESS-AND-CONTRAST "
                + Math.round(Float.parseFloat(parameters[1]))
                + " "
                + Math.round(Float.parseFloat(parameters[2]))
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "COLOR-CHANNEL-FILTER-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "COLOR-CHANNEL-FILTER "
                + Math.round(Float.parseFloat(parameters[1]))
                + " "
                + Math.round(Float.parseFloat(parameters[2]))
                + " "
                + Math.round(Float.parseFloat(parameters[3]))
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "EMBOSS-FILTER":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "EMBOSS-FILTER"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "INVERT-COLORS":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "INVERT-COLORS"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "THRESHOLDING":
      case "THRESHOLDING-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "THRESHOLDING "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "EDGE-DETECTION":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "EDGE-DETECTION"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "HISTOGRAM-EQUALIZATION":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "HISTOGRAM-EQUALIZATION"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "HALFTONE-CIRCLES":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "HALFTONE-CIRCLES"
                + Math.round(Float.parseFloat(parameters[1]))
                + " "
                + Math.round(Float.parseFloat(parameters[2]))
                + " "
                + Math.round(Float.parseFloat(parameters[3]))
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "HALFTONE-ERROR-DIFFUSION":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "HALFTONE-ERROR-DIFFUSION"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "HALFTONE-DITHERING":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "HALFTONE-DITHERING"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "HALFTONE-RYLANDERS":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "HALFTONE-RYLANDERS"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "COLOR-HISTOGRAM":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "COLOR-HISTOGRAM"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "GRAY-HISTOGRAM":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "GRAY-HISTOGRAM"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;

      case "GRAY-SCALE-QUANTIZATION":
      case "GRAY-SCALE-QUANTIZATION-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "GRAY-SCALE-QUANTIZATION "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "NOISE-REDUCTION":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "NOISE-REDUCTION"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "TRANSFORM-FLIP":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "TRANSFORM-FLIP"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "TRANSFORM-ROTATE":
      case "TRANSFORM-ROTATE-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "TRANSFORM-ROTATE "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "TRANSFORM-SCALE-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "TRANSFORM-SCALE "
                + Math.round(Float.parseFloat(parameters[1]))
                + " "
                + Math.round(Float.parseFloat(parameters[2]))
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "COMPRESS":
      case "COMPRESS-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "COMPRESS "
                + Float.valueOf(parameters[1])
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "GAMMA-CHANGER":
      case "GAMMA-CHANGER-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "GAMMA-CHANGER "
                + parameterNr
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "SHARPNESS-ENHANCER":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "SHARPNESS-ENHANCER"
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
      case "SHARPNESS-ENHANCER-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "SHARPNESS-ENHANCER "
                + Float.valueOf(parameters[1])
                + " "
                + Float.valueOf(parameters[2])
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;

      case "MEDIAN-FILTER-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "MEDIAN-FILTER "
                + Math.round(parameterNr)
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;

      case "NORMALIZED-BLOCK-FILTER-ADVANCED":
        filenameBeforeAndAfterProcessing[1] =
            path
                + FilenameUtils.removeExtension(fileName)
                + "["
                + "NORMALIZED-BLOCK-FILTER "
                + Math.round(parameterNr)
                + "]."
                + FilenameUtils.getExtension(fileName);
        break;
    }
    return filenameBeforeAndAfterProcessing;
  }
}
