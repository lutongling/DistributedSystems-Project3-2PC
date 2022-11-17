package utils;

import java.sql.Timestamp;
import java.util.logging.Logger;

/**
 * This class represents the logger. It can log info, error or warning message.
 * The log is time-stamped with the current system time maintaining milliseconds precision.
 */
public class KVLogger {
  private Logger logger;

  public KVLogger(String className) {
    this.logger = Logger.getLogger(className);
  }

  public void logInfoMessage(String errorMsg) {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    logger.info(timestamp + " " + errorMsg);
  }

  public void logErrorMessage(String errorMsg) {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    logger.severe(timestamp + " " + errorMsg);
  }

  public void logWarningMessage(String warningMsg) {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    logger.warning(timestamp + " " + warningMsg);
  }

}
