import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

class UsersSystem {
  final String usersDBPath = "usersDB/usersDB.db";
  private Connection connection = null;

  UsersSystem() {
    connectToDB();
    createTable();
  }

  private void connectToDB() {
    try {
      Class.forName("org.sqlite.JDBC");
      connection = DriverManager.getConnection("jdbc:sqlite:" + usersDBPath);
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Opened database successfully");
  }

  private void createTable() {
    Statement stmt;
    String cmd =
        "CREATE TABLE IF NOT EXISTS USERS ("
            + "ID INT PRIMARY KEY NOT NULL,"
            + "USERNAME TEXT NOT NULL UNIQUE,"
            + "HASHPASSWORD TEXT NOT NULL,"
            + "EMAIL TEXT NOT NULL UNIQUE"
            + ");";
    try {
      stmt = connection.createStatement();
      stmt.execute(cmd);
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    insertDefaultUsers();
    System.out.println("Created table successfully");
  }

  boolean checkIfUsernameExistsInDB(String username) {
    Statement stmt;

    String cmd1 =
        "SELECT COUNT(USERNAME) AS COUNT_USERNAME FROM USERS WHERE USERNAME = '" + username + "';";
    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(cmd1);
      if (rs.getInt("COUNT_USERNAME") >= 1) return true;
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return false;
  }

  boolean checkIfEmailExistsInDB(String email) {
    Statement stmt;

    String cmd1 = "SELECT COUNT(EMAIL) AS COUNT_EMAIL FROM USERS WHERE EMAIL = '" + email + "';";
    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(cmd1);
      if (rs.getInt("COUNT_EMAIL") >= 1) return true;
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return false;
  }

  ErrorCodes updatePasswordViaUsername(String username, String hashPassword) {

    Statement stmt;
    String cmd1 =
        "UPDATE USERS "
            + "SET HASHPASSWORD = '"
            + hashPassword
            + "' "
            + "WHERE USERNAME = '"
            + username
            + "';";

    try {
      stmt = connection.createStatement();
      stmt.executeUpdate(cmd1);
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return ErrorCodes.UNKNOWN;
    }
    System.out.println("Updated password successfully");
    return ErrorCodes.OK;
  }

  ErrorCodes updatePasswordViaEmail(String email, String hashPassword) {

    Statement stmt;
    String cmd1 =
        "UPDATE USERS "
            + "SET HASHPASSWORD = '"
            + hashPassword
            + "' "
            + "WHERE EMAIL = '"
            + email
            + "';";

    try {
      stmt = connection.createStatement();
      stmt.executeUpdate(cmd1);
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return ErrorCodes.UNKNOWN;
    }
    System.out.println("Updated email successfully");
    return ErrorCodes.OK;
  }

  ErrorCodes insertUser(String username, String hashPassword, String email) {

    Statement stmt;
    String cmd1 = "SELECT MAX (ID) AS MAXID FROM USERS;";
    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(cmd1);
      int newID = rs.getInt("MAXID") + 1;
      String cmd2 =
          "INSERT INTO USERS VALUES ( "
              + newID
              + " , '"
              + username
              + "' , '"
              + hashPassword
              + "' , '"
              + email
              + "' );";
      stmt.executeUpdate(cmd2);
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return ErrorCodes.UNKNOWN;
    }
    System.out.println("Inserted user successfully");
    return ErrorCodes.OK;
  }

  String getHashedPassword(String username) {
    Statement stmt;
    String hashPassword = null;
    String cmd1 =
        "SELECT HASHPASSWORD AS HASHPASSWORD FROM USERS WHERE USERNAME = '" + username + "';";

    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(cmd1);
      hashPassword = rs.getString("HASHPASSWORD");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return hashPassword;
  }

  String getEmail(String username) {
    Statement stmt;
    String email = null;
    String cmd1 = "SELECT EMAIL AS EMAIL FROM USERS WHERE USERNAME = '" + username + "';";

    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(cmd1);
      email = rs.getString("EMAIL");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return email;
  }

  private void insertDefaultUsers() {

    MainServer.pbkdf2PasswordEncoder.setAlgorithm(
        Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
    String pbkdf2CryptedRootPassword =
        MainServer.pbkdf2PasswordEncoder.encode(String.valueOf("rootpass".toCharArray()));
    String pbkdf2CryptedGuestPassword =
        MainServer.pbkdf2PasswordEncoder.encode(String.valueOf("guestpass".toCharArray()));
    Statement stmt;
    String cmd1 = "SELECT COUNT (ID) AS NR_USERS FROM USERS;";
    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(cmd1);
      int nr_users = rs.getInt("NR_USERS");
      if (nr_users == 0) {
        String cmd2 =
            "INSERT INTO USERS VALUES ( "
                + 0
                + " , '"
                + "root"
                + "' , '"
                + pbkdf2CryptedRootPassword
                + "' , '"
                + "root@email.com"
                + "' );";
        stmt.executeUpdate(cmd2);
        String cmd3 =
            "INSERT INTO USERS VALUES ( "
                + 1
                + " , '"
                + "guest"
                + "' , '"
                + pbkdf2CryptedGuestPassword
                + "' , '"
                + "guest@email.com"
                + "' );";
        stmt.executeUpdate(cmd3);
      }
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    // System.out.println("Inserted default users successfully");
  }

  /* private void printAllTable() { // DEBUG
      Statement stmt;

      String cmd1 = "SELECT * FROM USERS;";
      try {
          stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery(cmd1);
          while (rs.next()) {
              System.out.print(rs.getInt("ID") + " ");
              System.out.print(rs.getString("USERNAME") + " ");
              System.out.print(rs.getString("HASHPASSWORD") + " ");
              System.out.println(rs.getString("EMAIL") + " ");
          }
          rs.close();
      } catch (Exception e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
      }
  }*/
}
