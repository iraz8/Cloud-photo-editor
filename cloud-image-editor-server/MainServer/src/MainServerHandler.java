import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;

public class MainServerHandler extends Thread {
  static int nrTotalThreads = 0;
  final String usersHomePath = "home/";
  private final ServerSocket serverSocket;

  String clientUsername;
  BufferedReader is;
  PrintStream os;
  private Socket clientSocket;

  MainServerHandler(ServerSocket s, int i) {
    addNrTotalThreads();
    serverSocket = s;
    setName("Thread " + i);
  }

  public static char[] generatePassword() {
    SecureRandom r = new SecureRandom();
    String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$&@?<>~!%#";
    while (true) {
      char[] password = new char[r.nextBoolean() ? 10 : 30];
      boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
      for (int i = 0; i < password.length; i++) {
        char ch = symbols.charAt(r.nextInt(symbols.length()));
        if (Character.isUpperCase(ch)) hasUpper = true;
        else if (Character.isLowerCase(ch)) hasLower = true;
        else if (Character.isDigit(ch)) hasDigit = true;
        else hasSpecial = true;
        password[i] = ch;
      }
      if (hasUpper && hasLower && hasDigit && hasSpecial) {
        return password;
      }
    }
  }

  public static float findValueFromPercentage(
      float left, float right, float totalPercents, float percentage) {
    return left + percentage / totalPercents * (right - left);
  }

  static synchronized void addNrTotalThreads() {
    nrTotalThreads++;
  }

  public void run() {

    try {
      System.out.println(getName() + " waiting");

      synchronized (serverSocket) {
        clientSocket = serverSocket.accept();
      }

      System.out.println(getName() + " starting, IP=" + clientSocket.getInetAddress());
      is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      os = new PrintStream(clientSocket.getOutputStream(), true);
      decisionMaker();

      if (!clientSocket.isClosed()) {
        clientSocket.close();
      }

      System.out.println(getName() + " Closed ");

    } catch (IOException ex) {
      System.out.println(getName() + ": IO Error on socket " + ex);
    }
    new MainServerHandler(serverSocket, nrTotalThreads).start();
  }

  private void sendMessageToServer(String msg, PrintWriter os) {
    os.println(msg);
  }

  private String getMsgFromClient(BufferedReader is) throws IOException {
    return is.readLine();
  }

  private String getCommandFromMsg(String msg) {
    if (msg != null && !msg.isEmpty()) {
      String[] msgSplitted = msg.split("\\|");
      return msgSplitted[0];
    }
    System.out.println("getCommandFromMsg EMPTY. ERROR!");
    return null;
  }

  private String[] getParametersFromMsg(String msg) {
    if (msg != null && !msg.isEmpty()) {
      String[] msgSplitted = msg.split("\\|");
      return Arrays.copyOfRange(msgSplitted, 1, msgSplitted.length);
    }
    return null;
  }

  private ErrorCodes logout() {
    return closeConnection(clientSocket);
  }

  private ErrorCodes getUsersFiles(String username) {
    File folder = new File("home/" + username);
    File[] listOfFiles = folder.listFiles();
    if (listOfFiles != null) {
      for (File listOfFile : listOfFiles) {
        if (listOfFile.isFile()) {
          os.println(listOfFile.getName());
        }
      }
    }
    os.println(ErrorCodes.END_READING_FILENAMES);

    if (listOfFiles == null || listOfFiles.length == 0) return ErrorCodes.OK_ListOfFilesEmpty;
    return ErrorCodes.OK;
  }

  private ErrorCodes checkUsername(String username) {
    String regexUsernamePattern = "(?=\\S+$).{4,}";
    if (username.matches(regexUsernamePattern)) return ErrorCodes.OK;
    else return ErrorCodes.WRONG_USERNAME_PATTERN;
  }

  private ErrorCodes closeConnection(Socket clientSocket) {
    String clientInetAddress = clientSocket.getInetAddress().toString();
    try {
      clientSocket.close();
      return ErrorCodes.OK;
    } catch (IOException e) {
      System.err.println(
          "ERROR! method void closeConnection(Socket clientSocket)\n "
              + "Connection: "
              + clientInetAddress
              + " still open!");
      e.printStackTrace();
      return ErrorCodes.CONNECTION_NOT_CLOSED;
    }
  }

  private ErrorCodes checkPassword(char[] password) {
    String regexPasswordPattern = "(?=\\S+$).{6,}";
    if (String.valueOf(password).matches(regexPasswordPattern)) return ErrorCodes.OK;
    else return ErrorCodes.WRONG_PASSWORD_PATTERN;
  }

  private ErrorCodes createFolderNewUser(String username) {
    try {
      FileUtils.forceMkdir(new File(usersHomePath + username));
    } catch (IOException e) {
      e.printStackTrace();

      return ErrorCodes.DIRECTORY_NOT_CREATED;
    }
    return ErrorCodes.OK;
  }

  private ErrorCodes checkEmail(String email) {
    String regexEmailPattern =
        "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    if (email.matches(regexEmailPattern)) return ErrorCodes.OK;
    else return ErrorCodes.WRONG_EMAIL_PATTERN;
  }

  private ErrorCodes newUser(String username, char[] password, String email) {

    if (checkUsername(username) == ErrorCodes.WRONG_USERNAME_PATTERN)
      return ErrorCodes.WRONG_USERNAME_PATTERN;
    if (checkPassword(password) == ErrorCodes.WRONG_PASSWORD_PATTERN)
      return ErrorCodes.WRONG_PASSWORD_PATTERN;
    if (checkEmail(email) == ErrorCodes.WRONG_EMAIL_PATTERN) return ErrorCodes.WRONG_EMAIL_PATTERN;

    if (MainServer.usersSystem.checkIfUsernameExistsInDB(username)) {
      return ErrorCodes.USERNAME_EXISTS;
    }
    if (MainServer.usersSystem.checkIfEmailExistsInDB(email)) {
      return ErrorCodes.EMAIL_EXISTS;
    }

    createFolderNewUser(username);

    String pbkdf2CryptedPassword =
        MainServer.pbkdf2PasswordEncoder.encode(String.valueOf(password));

    return MainServer.usersSystem.insertUser(username, pbkdf2CryptedPassword, email);
  }

  public ErrorCodes updatePasswordViaUsername(String username, char[] password) {
    return MainServer.usersSystem.updatePasswordViaUsername(username, String.valueOf(password));
  }

  public ErrorCodes updatePasswordViaEmail(String email, char[] password) {
    return MainServer.usersSystem.updatePasswordViaEmail(email, String.valueOf(password));
  }

  public ErrorCodes checkIfPasswordIsCorrect(String username, char[] password) {
    String hashPassword = MainServer.usersSystem.getHashedPassword(username);
    boolean passwordIsValid;

    if (hashPassword.isEmpty()) passwordIsValid = false;
    else
      passwordIsValid =
          MainServer.pbkdf2PasswordEncoder.matches(String.valueOf(password), hashPassword);
    if (passwordIsValid) {
      this.clientUsername = username;
      return ErrorCodes.OK;
    } else return ErrorCodes.WRONG_PASSWORD;
  }

  private ErrorCodes login(String username, char[] password) {
    if (!MainServer.usersSystem.checkIfUsernameExistsInDB(username)) {
      return ErrorCodes.USERNAME_NOT_EXIST;
    }
    return checkIfPasswordIsCorrect(username, password);
  }

  public ErrorCodes resetPasswordViaUsername(String username) {
    if (!MainServer.usersSystem.checkIfUsernameExistsInDB(username)) {
      return ErrorCodes.USERNAME_NOT_EXIST;
    }
    char[] newPassword = generatePassword();
    ErrorCodes errorCodes = changePassword(username, newPassword);
    if (!errorCodes.equals(ErrorCodes.OK)) return ErrorCodes.UNKNOWN;

    EmailSender.send(
        "cloudimageeditor@gmail.com",
        "cloudimageeditorpass",
        MainServer.usersSystem.getEmail(username),
        "Reset Password",
        "Hello! Your password was reset. For your security, please log in and change the new password as soon as you can. Your new password is: "
            + String.valueOf(newPassword));
    return ErrorCodes.OK;
  }

  public ErrorCodes changePassword(String username, char[] newPassword) {
    String pbkdf2CryptedPassword =
        MainServer.pbkdf2PasswordEncoder.encode(String.valueOf(newPassword));
    ErrorCodes errorCodes =
        updatePasswordViaUsername(username, pbkdf2CryptedPassword.toCharArray());
    if (errorCodes.equals(ErrorCodes.OK))
      EmailSender.send(
          "cloudimageeditor@gmail.com",
          "cloudimageeditorpass",
          MainServer.usersSystem.getEmail(username),
          "Changed Password",
          "Hello "
              + clientUsername
                  + "! Your password was changed successfully!");
    return errorCodes;
  }

  public ErrorCodes resetPasswordViaEmail(String email) {
    if (!MainServer.usersSystem.checkIfEmailExistsInDB(email)) {
      return ErrorCodes.EMAIL_NOT_EXIST;
    }
    char[] newPassword = generatePassword();
    String pbkdf2CryptedPassword =
        MainServer.pbkdf2PasswordEncoder.encode(String.valueOf(newPassword));
    updatePasswordViaEmail(email, pbkdf2CryptedPassword.toCharArray());
    EmailSender.send(
        "cloudimageeditor@gmail.com",
        "cloudimageeditorpass",
        email,
        "Reset Password",
        "Hello! Your password was reset. For your security, please log in and change the new password as soon as you can. Your new password is: "
            + String.valueOf(newPassword));
    return ErrorCodes.OK;
  }

  private String changeBrightness(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = UsefulMethods.round(Float.parseFloat(parameters[1]), 2);
      parameter = findValueFromPercentage(0, 510, 200, parameter);
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        BrightnessChanger.change(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            parameter);
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String changeBrightness(String command, String[] parameters)");
    return null;
  }

  private String applyGaussianFilter(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(0, 500, 100, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        GaussianFilter.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyGaussianFilter(String command, String[] parameters)");
    return null;
  }

  private String convertColorToGray(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        ColorToGrayConverter.convert(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String convertColorToGray(String command, String[] parameters)");
    return null;
  }

  private String changeContrast(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;

    float parameter;
    if (!advancedMode) {
      parameter = UsefulMethods.round(Float.parseFloat(parameters[1]), 2);
      if (parameter < 0) parameter = 1 + findValueFromPercentage(0, 1, 100, parameter);
      else parameter = 1 + findValueFromPercentage(0, 10, 100, parameter);
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }

    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        ContrastChanger.change(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            parameter);
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String changeContrast(String command, String[] parameters)");
    return null;
  }

  private String applyDilatation(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(0, 40, 100, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        DilatationApplicator.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyDilatation(String command, String[] parameters)");
    return null;
  }

  private String applyErosion(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(0, 40, 100, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        ErosionApplicator.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyErosion(String command, String[] parameters)");
    return null;
  }

  private String applySepia(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        SepiaConverter.convert(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applySepia(String command, String[] parameters)");
    return null;
  }

  private String applyMosaic(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(3, 250, 250, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        Mosaic.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter),
            parameters[2],
            Boolean.parseBoolean(parameters[3]));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyMosaic(String command, String[] parameters)");
    return null;
  }

  private String applyTelevisionEffect(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        TelevisionEffect.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyTelevisionEffect(String command, String[] parameters)");
    return null;
  }

  private String applyPixelize(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(1, 100, 100, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        Pixelize.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyPixelize(String command, String[] parameters)");
    return null;
  }

  private String applyBlackAndWhite(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(0, 200, 200, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        BlackAndWhite.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyBlackAndWhite(String command, String[] parameters)");
    return null;
  }

  private String applyBrightnessAndContrast(
      String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter, parameter2;
    // ONLY ADVANCED MODE IMPLEMENTED!
    parameter = Float.parseFloat(parameters[1]);
    parameter2 = Float.parseFloat(parameters[2]);
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        BrightnessAndContrastChanger.change(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter),
            Math.round(parameter2));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyBrightnessAndContrast(String command, String[] parameters)");
    return null;
  }

  private String applyColorChannelFilter(
      String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter, parameter2, parameter3;
    // ONLY ADVANCED MODE IMPLEMENTED!
    parameter = Float.parseFloat(parameters[1]);
    parameter2 = Float.parseFloat(parameters[2]);
    parameter3 = Float.parseFloat(parameters[3]);
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        ColorChannelFilter.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter),
            Math.round(parameter2),
            Math.round(parameter3));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyColorChannelFilter(String command, String[] parameters)");
    return null;
  }

  private String applyEmbossFilter(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        EmbossFilter.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyEmbossFilter(String command, String[] parameters)");
    return null;
  }

  private String applyInvertColors(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        InvertColors.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyInvertColors(String command, String[] parameters)");
    return null;
  }

  private String applyThresholding(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(0, 255, 100, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        Thresholding.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyThresholding(String command, String[] parameters)");
    return null;
  }

  private String applyEdgeDetection(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        EdgeDetection.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyEdgeDetection(String command, String[] parameters)");
    return null;
  }

  private String applyHistogramEqualization(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        HistogramEqualization.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyHistogramEqualization(String command, String[] parameters)");
    return null;
  }

  private String applyHalftoneCircles(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter, parameter2, parameter3;
    // ONLY ADVANCED MODE IMPLEMENTED!
    parameter = Float.parseFloat(parameters[1]);
    parameter2 = Float.parseFloat(parameters[2]);
    parameter3 = Float.parseFloat(parameters[3]);
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        HalftoneCircles.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter),
            Math.round(parameter2),
            Math.round(parameter3));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyHalftoneCircles(String command, String[] parameters)");
    return null;
  }

  private String applyHalftoneErrorDiffusion(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        HalftoneErrorDiffusion.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyHalftoneErrorDiffusion(String command, String[] parameters)");
    return null;
  }

  private String applyHalftoneDithering(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        HalftoneDithering.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyHalftoneDithering(String command, String[] parameters)");
    return null;
  }

  private String applyHalftoneRylanders(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        HalftoneRylanders.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyHalftoneRylanders(String command, String[] parameters)");
    return null;
  }

  private String getColorHistogram(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        ColorHistogram.get(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String getColorHistogram(String command, String[] parameters)");
    return null;
  }

  private String getGrayHistogram(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        GrayHistogram.get(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String getGrayHistogram(String command, String[] parameters)");
    return null;
  }

  private String applyGrayScaleQuantization(
      String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(1, 255, 100, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        GrayScaleQuantization.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyGrayScaleQuantization(String command, String[] parameters)");
    return null;
  }

  private String applyNoiseReduction(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        NoiseReduction.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]));

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyNoiseReduction(String command, String[] parameters)");
    return null;
  }

  private String applyTransformFlip(String command, String[] parameters) {
    ErrorCodes errorCode;
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, 0);
    errorCode =
        TransformFlip.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            parameters[1]);

    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyTransformFlip(String command, String[] parameters)");
    return null;
  }

  private String applyTransformRotate(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(0, 180, 100, Float.parseFloat(parameters[1]));
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        TransformRotate.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyTransformRotate(String command, String[] parameters)");
    return null;
  }

  private String applyTransformScale(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter, parameter2;
    // ONLY ADVANCED MODE IMPLEMENTED!
    parameter = Float.parseFloat(parameters[1]);
    parameter2 = Float.parseFloat(parameters[2]);
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        TransformScale.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter),
            Math.round(parameter2));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyTransformScale(String command, String[] parameters)");
    return null;
  }

  private String compressImage(String command, String[] parameters, boolean advancedMode)
      throws IOException {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = findValueFromPercentage(0, 100, 100, Float.parseFloat(parameters[1]));
      parameter = parameter / 100f;
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }

    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        Compression.compressImage(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            parameter);
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String compressImage(String command, String[] parameters)");
    return null;
  }

  private String changeGamma(String command, String[] parameters, boolean advancedMode)
      throws IOException {
    ErrorCodes errorCode;
    float parameter;
    if (!advancedMode) {
      parameter = UsefulMethods.round(Float.parseFloat(parameters[1]), 2);
      if (parameter < 0) parameter = 1 + findValueFromPercentage(0, 1, 100, parameter);
      else parameter = 1 + findValueFromPercentage(0, 10, 100, parameter);
    } else {
      parameter = Float.parseFloat(parameters[1]);
    }

    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        GammaCorrection.change(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            parameter);
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String changeGamma(String command, String[] parameters)");
    return null;
  }

  private String applySharpnessEnhancer(String command, String[] parameters, boolean advancedMode) {
    ErrorCodes errorCode;
    float parameter, parameter2;
    if (!advancedMode) {
      parameter = 1.5f;
      parameter2 = -0.5f;
    } else {
      parameter = Float.parseFloat(parameters[1]);
      parameter2 = Float.parseFloat(parameters[2]);
    }
    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        SharpnessEnhancer.enhance(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            parameter,
            parameter2);
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applySharpnessEnhancer(String command, String[] parameters)");
    return null;
  }

  private String applyMedianFilter(String command, String[] parameters, boolean advancedMode)
      throws IOException {
    ErrorCodes errorCode;
    float parameter = Float.parseFloat(parameters[1]);

    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        MedianFilter.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyMedianFilter(String command, String[] parameters)");
    return null;
  }

  private String applyNormalizedBlockFilter(
      String command, String[] parameters, boolean advancedMode) throws IOException {
    ErrorCodes errorCode;
    float parameter = Float.parseFloat(parameters[1]);

    String[] fileNameBeforeAndAfterProcessing =
        FilenamesChanger.getFileNameBeforeAndAfterProcessing(
            usersHomePath, clientUsername, command, parameters, parameter);
    errorCode =
        NormalizedBlockFilter.apply(
            new File(fileNameBeforeAndAfterProcessing[0]),
            new File(fileNameBeforeAndAfterProcessing[1]),
            Math.round(parameter));
    if (errorCode.equals(ErrorCodes.OK)) return fileNameBeforeAndAfterProcessing[1];
    System.out.println(
        "ERROR! MainServerHandler private String applyNormalizedBlockFilter(String command, String[] parameters)");
    return null;
  }

  private ErrorCodes deleteFile(String[] parameters) {
    String fileName = parameters[0];
    File file = new File(usersHomePath + clientUsername + "/" + fileName);
    try {
      FileUtils.forceDelete(file);
    } catch (IOException e) {
      e.printStackTrace();
      return ErrorCodes.FILE_NOT_FOUND;
    }
    return ErrorCodes.OK;
  }

  private void decisionMaker() throws IOException {
    while (!clientSocket.isClosed()) {
      String msgFromClient = getMsgFromClient(is);
      String command = getCommandFromMsg(msgFromClient);
      String[] parameters = getParametersFromMsg(msgFromClient);
      String fileNameAfterProcessing;
      System.out.println("[SERVER-Message received]: " + command);
      ErrorCodes errorCode = ErrorCodes.OK;
      switch (command) {
        case "CHANGE-BRIGHTNESS":
          fileNameAfterProcessing = changeBrightness(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "CHANGE-BRIGHTNESS-ADVANCED":
          fileNameAfterProcessing = changeBrightness(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "CHANGE-CONTRAST":
          fileNameAfterProcessing = changeContrast(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "CHANGE-CONTRAST-ADVANCED":
          fileNameAfterProcessing = changeContrast(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "GAUSSIAN-FILTER":
          fileNameAfterProcessing = applyGaussianFilter(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "GAUSSIAN-FILTER-ADVANCED":
          fileNameAfterProcessing = applyGaussianFilter(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "CONVERT-COLOR-TO-GRAY":
          fileNameAfterProcessing = convertColorToGray(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "DILATATION":
          fileNameAfterProcessing = applyDilatation(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "DILATATION-ADVANCED":
          fileNameAfterProcessing = applyDilatation(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "EROSION":
          fileNameAfterProcessing = applyErosion(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "EROSION-ADVANCED":
          fileNameAfterProcessing = applyErosion(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "SEPIA":
          fileNameAfterProcessing = applySepia(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "MOSAIC":
          fileNameAfterProcessing = applyMosaic(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "MOSAIC-ADVANCED":
          fileNameAfterProcessing = applyMosaic(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "TELEVISION-EFFECT":
          fileNameAfterProcessing = applyTelevisionEffect(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "PIXELIZE":
          fileNameAfterProcessing = applyPixelize(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "PIXELIZE-ADVANCED":
          fileNameAfterProcessing = applyPixelize(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "BLACK-AND-WHITE":
          fileNameAfterProcessing = applyBlackAndWhite(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "BLACK-AND-WHITE-ADVANCED":
          fileNameAfterProcessing = applyBlackAndWhite(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "BRIGHTNESS-AND-CONTRAST-ADVANCED":
          fileNameAfterProcessing = applyBrightnessAndContrast(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "COLOR-CHANNEL-FILTER-ADVANCED":
          fileNameAfterProcessing = applyColorChannelFilter(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "EMBOSS-FILTER":
          fileNameAfterProcessing = applyEmbossFilter(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "INVERT-COLORS":
          fileNameAfterProcessing = applyInvertColors(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "THRESHOLDING":
          fileNameAfterProcessing = applyThresholding(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "THRESHOLDING-ADVANCED":
          fileNameAfterProcessing = applyThresholding(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "EDGE-DETECTION":
          fileNameAfterProcessing = applyEdgeDetection(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "HISTOGRAM-EQUALIZATION":
          fileNameAfterProcessing = applyHistogramEqualization(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "HALFTONE-CIRCLES":
          fileNameAfterProcessing = applyHalftoneCircles(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "HALFTONE-ERROR-DIFFUSION":
          fileNameAfterProcessing = applyHalftoneErrorDiffusion(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "HALFTONE-DITHERING":
          fileNameAfterProcessing = applyHalftoneDithering(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "HALFTONE-RYLANDERS":
          fileNameAfterProcessing = applyHalftoneRylanders(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "COLOR-HISTOGRAM":
          fileNameAfterProcessing = getColorHistogram(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "GRAY-HISTOGRAM":
          fileNameAfterProcessing = getGrayHistogram(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "GRAY-SCALE-QUANTIZATION":
          fileNameAfterProcessing = applyGrayScaleQuantization(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "GRAY-SCALE-QUANTIZATION-ADVANCED":
          fileNameAfterProcessing = applyGrayScaleQuantization(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "NOISE-REDUCTION":
          fileNameAfterProcessing = applyNoiseReduction(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "TRANSFORM-FLIP":
          fileNameAfterProcessing = applyTransformFlip(command, parameters);
          os.println(fileNameAfterProcessing);
          break;
        case "TRANSFORM-ROTATE":
          fileNameAfterProcessing = applyTransformRotate(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "TRANSFORM-ROTATE-ADVANCED":
          fileNameAfterProcessing = applyTransformRotate(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "TRANSFORM-SCALE-ADVANCED":
          fileNameAfterProcessing = applyTransformScale(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "COMPRESS":
          fileNameAfterProcessing = compressImage(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "COMPRESS-ADVANCED":
          fileNameAfterProcessing = compressImage(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "GAMMA-CHANGER":
          fileNameAfterProcessing = changeGamma(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "GAMMA-CHANGER-ADVANCED":
          fileNameAfterProcessing = changeGamma(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "SHARPNESS-ENHANCER":
          fileNameAfterProcessing = applySharpnessEnhancer(command, parameters, false);
          os.println(fileNameAfterProcessing);
          break;
        case "SHARPNESS-ENHANCER-ADVANCED":
          fileNameAfterProcessing = applySharpnessEnhancer(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "MEDIAN-FILTER-ADVANCED":
          fileNameAfterProcessing = applyMedianFilter(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "NORMALIZED-BLOCK-FILTER-ADVANCED":
          fileNameAfterProcessing = applyNormalizedBlockFilter(command, parameters, true);
          os.println(fileNameAfterProcessing);
          break;
        case "DELETE-FILE":
          errorCode = deleteFile(parameters);
          break;
        case "SEND-FILE-TO-SERVER":
          break;
        case "CLOSE":
        case "LOGOUT":
          errorCode = logout();
          if (errorCode.equals(ErrorCodes.OK)) return;
          break;
        case "NEWUSER":
          errorCode = newUser(parameters[0], parameters[1].toCharArray(), parameters[2]);
          //    Server.usersSystem.printAllTable(); // DEBUG
          break;
        case "LOGIN":
          errorCode = login(parameters[0], parameters[1].toCharArray());
          break;
        case "RESET-PASSWORD-VIA-USERNAME":
          errorCode = resetPasswordViaUsername(parameters[0]);
          break;
        case "RESET-PASSWORD-VIA-EMAIL":
          errorCode = resetPasswordViaEmail(parameters[0]);
          break;
        case "CHECK-IF-PASSWORD-IS-CORRECT":
          errorCode = checkIfPasswordIsCorrect(clientUsername, parameters[0].toCharArray());
          break;
        case "CHANGE-PASSWORD":
          errorCode = changePassword(clientUsername, parameters[0].toCharArray());
          break;
        case "GET-USERS-FILENAMES":
          errorCode = getUsersFiles(clientUsername);
          break;
        default:
          System.err.println(
              "ERROR! Method: int decisionMaker(String line)! Unknown command! Command:" + command);
          break;
      }

      System.out.println("[ERRORCODE-SERVER] " + errorCode);
      os.println(errorCode);
    }
  }
}
