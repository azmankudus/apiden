package com.example.apiden.shared.infrastructure;

import ch.qos.logback.core.rolling.RollingPolicyBase;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A custom rolling policy for Logback that supports file size limits,
 * time-scheduled rolling, and duration-based rolling intervals.
 * It also handles automatic compression of archived log files.
 *
 * @param <E> The type of the log event.
 */
public final class CustomRollingPolicy<E> extends RollingPolicyBase implements TriggeringPolicy<E> {

  private static final Logger log = LoggerFactory.getLogger(CustomRollingPolicy.class);

  private FileSize maxFileSize;
  private FileSize totalSizeCap;
  private int uncompressedFileCount = 3;
  private boolean isFirst = true;

  private String rollSchedule;
  private String rollInterval;

  private List<LocalTime> scheduledTimes;
  private Duration intervalDuration;
  private long appStartMillis;
  private long lastTimeRollMillis;

  /**
   * Default constructor for CustomRollingPolicy.
   */
  public CustomRollingPolicy() {
    log.debug("CustomRollingPolicy instance created.");
  }

  /**
   * Starts the rolling policy, initializing schedules and durations.
   */
  @Override
  public void start() {
    log.info("Starting CustomRollingPolicy...");
    super.start();

    if (maxFileSize == null) {
      log.error("maxFileSize property is not set; rolling might not function as expected.");
      addError("maxFileSize property is not set");
    }

    appStartMillis = System.currentTimeMillis();
    lastTimeRollMillis = appStartMillis;
    log.trace("Initial reference time set: {}", appStartMillis);

    // Initialize time-based rolling schedule
    if (rollSchedule != null && !rollSchedule.isBlank()) {
      log.debug("Initializing time-based roll schedule: {}", rollSchedule);
      scheduledTimes = new ArrayList<>();
      final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
      for (final String part : rollSchedule.split(",")) {
        try {
          scheduledTimes.add(LocalTime.parse(part.trim(), fmt));
        } catch (final Exception e) {
          log.error("Invalid rollSchedule time format: '{}'. Expected HH:mm.", part.trim());
          addError("Invalid rollSchedule time: '" + part.trim() + "'. Expected HH:mm format.", e);
        }
      }
      if (!scheduledTimes.isEmpty()) {
        Collections.sort(scheduledTimes);
        log.info("Time-scheduled rolling enabled at: {}", scheduledTimes);
        addInfo("Time-scheduled rolling enabled at: " + scheduledTimes);
      }
    }

    // Initialize duration-based rolling interval
    if (rollInterval != null && !rollInterval.isBlank()) {
      try {
        intervalDuration = Duration.parse(rollInterval.trim());
        log.info("Duration-based rolling enabled every: {}", intervalDuration);
        addInfo("Duration-based rolling enabled every: " + intervalDuration);
      } catch (final Exception e) {
        log.error("Invalid rollInterval duration: '{}'. Expected ISO-8601 format.", rollInterval);
        addError("Invalid rollInterval: '" + rollInterval + "'. Expected ISO-8601 duration (e.g. PT12H).", e);
      }
    }
  }

  /**
   * Determines if a rollover should be triggered based on size or time.
   *
   * @param activeFile The currently active log file.
   * @param event The log event.
   * @return true if rollover should occur, false otherwise.
   */
  @Override
  public boolean isTriggeringEvent(final File activeFile, final E event) {
    // Handle first run logic (e.g., rollover on startup if file exists)
    if (isFirst) {
      log.trace("Performing first-run check for file: {}", activeFile.getName());
      isFirst = false;
      if (activeFile.exists() && activeFile.length() > 0) {
        log.debug("Non-empty active file found on startup; triggering initial roll.");
        lastTimeRollMillis = System.currentTimeMillis();
        return true;
      }
    }

    // Check size-based trigger
    if (activeFile.exists() && activeFile.length() >= maxFileSize.getSize()) {
      log.info("Rolling file due to size limit: {}/{}", activeFile.length(), maxFileSize.getSize());
      lastTimeRollMillis = System.currentTimeMillis();
      return true;
    }

    // Check time-based trigger
    if (shouldRollByTime()) {
      log.info("Rolling file due to time/interval schedule.");
      lastTimeRollMillis = System.currentTimeMillis();
      return true;
    }

    return false;
  }

  /**
   * Evaluates if the current time matches a scheduled roll time or exceeds the interval.
   *
   * @return true if time-based roll is triggered.
   */
  private boolean shouldRollByTime() {
    final long now = System.currentTimeMillis();

    // Check scheduled clock times
    if (scheduledTimes != null && !scheduledTimes.isEmpty()) {
      final LocalDateTime lastRollDt = new Date(lastTimeRollMillis).toInstant()
          .atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime();
      final LocalDateTime nowDt = LocalDateTime.now();

      for (final LocalTime scheduledTime : scheduledTimes) {
        final LocalDateTime scheduledDt = nowDt.toLocalDate().atTime(scheduledTime);

        if (scheduledDt.isAfter(nowDt)) {
          continue; // Future scheduled time
        }

        if (scheduledDt.isAfter(lastRollDt)) {
          log.debug("Time-scheduled roll triggered for time: {}", scheduledTime);
          addInfo("Time-scheduled roll triggered at: " + scheduledTime);
          return true;
        }
      }
    }

    // Check duration-based intervals
    if (intervalDuration != null) {
      final long elapsedSinceLastRoll = now - lastTimeRollMillis;
      if (elapsedSinceLastRoll >= intervalDuration.toMillis()) {
        log.debug("Duration-based roll triggered after elapsed: {}ms", elapsedSinceLastRoll);
        addInfo("Duration-based roll triggered after: " + intervalDuration);
        return true;
      }
    }

    return false;
  }

  /**
   * Performs the actual file rollover, renaming the active file and managing archives.
   *
   * @throws RolloverFailure if the rollover process fails.
   */
  @Override
  public void rollover() throws RolloverFailure {
    log.info("Executing rollover process...");
    String activeFileName = getActiveFileName();
    if (activeFileName == null) {
      activeFileName = getParentsRawFileProperty();
    }

    final File activeFile = new File(activeFileName);
    if (!activeFile.exists() || activeFile.length() == 0) {
      log.debug("Active file does not exist or is empty; skipping rollover.");
      return;
    }

    try {
      final long lastModified = activeFile.lastModified();
      final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
      final String timestamp = sdf.format(new Date(lastModified));

      String basePath = activeFileName;
      if (basePath.endsWith(".log")) {
        basePath = basePath.substring(0, basePath.length() - 4);
      }

      String newFileName = basePath + "-" + timestamp + ".log";
      File newFile = new File(newFileName);

      // Handle file name collisions
      int counter = 1;
      while (newFile.exists()) {
        newFileName = basePath + "-" + timestamp + "-" + String.format("%04d", counter) + ".log";
        newFile = new File(newFileName);
        counter++;
      }

      log.debug("Renaming {} to {}", activeFile.getName(), newFile.getName());
      if (activeFile.renameTo(newFile)) {
        log.trace("Successfully renamed log file.");
      } else {
        log.warn("Failed to rename log file using renameTo; might be a file lock issue.");
      }

      // Cleanup and maintenance of old archives
      manageArchivedFiles(activeFileName);

    } catch (final Exception e) {
      log.error("Critical failure during file rollover: {}", e.getMessage(), e);
      addError("Failed to rollover", e);
      throw new RolloverFailure("Failed to rollover");
    }
  }

  /**
   * Manages archived log files, compressing older ones and enforcing total storage limits.
   *
   * @param activeFileName The path to the active log file (used for identifying relatives).
   */
  private void manageArchivedFiles(final String activeFileName) {
    log.trace("Managing archived log files for: {}", activeFileName);
    final File activeFile = new File(activeFileName);
    final File parentDir = activeFile.getParentFile();
    if (parentDir == null || !parentDir.exists()) {
      log.warn("Parent directory for log files does not exist: {}", parentDir);
      return;
    }

    String baseName = activeFile.getName();
    if (baseName.endsWith(".log")) {
      baseName = baseName.substring(0, baseName.length() - 4);
    }

    final String finalBaseName = baseName;
    final String finalActiveName = activeFile.getName();

    final File[] files = parentDir
        .listFiles((dir, name) -> name.startsWith(finalBaseName) && !name.equals(finalActiveName));
    if (files == null) {
      return;
    }

    final List<File> logFiles = new ArrayList<>();
    final List<File> zipFiles = new ArrayList<>();

    for (final File f : files) {
      if (f.getName().endsWith(".log")) {
        logFiles.add(f);
      } else if (f.getName().endsWith(".zip")) {
        zipFiles.add(f);
      }
    }

    // Sort by modification date (newest first)
    logFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
    log.trace("Found {} archived .log files and {} .zip files.", logFiles.size(), zipFiles.size());

    // Compress logs exceeding the uncompressed limit
    for (int i = uncompressedFileCount; i < logFiles.size(); i++) {
      final File toZip = logFiles.get(i);
      log.debug("Compressing old log file: {}", toZip.getName());
      if (zipFile(toZip)) {
        if (toZip.delete()) {
          log.trace("Deleted original file after compression.");
        }
        zipFiles.add(new File(toZip.getAbsolutePath() + ".zip"));
      }
    }

    // Enforce total size cap
    if (totalSizeCap != null) {
      log.trace("Enforcing total size cap of: {}", totalSizeCap);
      final List<File> allFiles = new ArrayList<>();
      for (int i = 0; i < Math.min(logFiles.size(), uncompressedFileCount); i++) {
        allFiles.add(logFiles.get(i));
      }
      allFiles.addAll(zipFiles);
      allFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

      long currentSize = 0;
      for (final File f : allFiles) {
        if (f.exists()) {
          currentSize += f.length();
          if (currentSize > totalSizeCap.getSize()) {
            log.info("Deleting archive exceeding total size cap: {}", f.getName());
            f.delete();
          }
        }
      }
    }
  }

  /**
   * Compresses a file into a ZIP archive.
   *
   * @param file The file to compress.
   * @return true if compression was successful.
   */
  private boolean zipFile(final File file) {
    try (final FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".zip");
        final ZipOutputStream zos = new ZipOutputStream(fos);
        final FileInputStream fis = new FileInputStream(file)) {

      final ZipEntry zipEntry = new ZipEntry(file.getName());
      zos.putNextEntry(zipEntry);

      final byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        zos.write(bytes, 0, length);
      }
      zos.closeEntry();
      log.trace("File successfully zipped: {}", file.getName());
      return true;
    } catch (final Exception e) {
      log.error("Failed to zip file {}: {}", file.getName(), e.getMessage());
      addError("Failed to zip file " + file.getName(), e);
      return false;
    }
  }

  /**
   * Retrieves the currently active file name.
   *
   * @return The active file name path.
   */
  @Override
  public String getActiveFileName() {
    return getParentsRawFileProperty();
  }

  /**
   * Sets the maximum allowed size before a rollover is triggered.
   *
   * @param maxFileSize The maximum file size.
   */
  public void setMaxFileSize(final FileSize maxFileSize) {
    log.trace("Setting maxFileSize to: {}", maxFileSize);
    this.maxFileSize = maxFileSize;
  }

  /**
   * Sets the total storage cap for all archived log files.
   *
   * @param totalSizeCap The total size cap.
   */
  public void setTotalSizeCap(final FileSize totalSizeCap) {
    log.trace("Setting totalSizeCap to: {}", totalSizeCap);
    this.totalSizeCap = totalSizeCap;
  }

  /**
   * Sets the number of log files to keep in uncompressed form.
   *
   * @param uncompressedFileCount The number of uncompressed files.
   */
  public void setUncompressedFileCount(final int uncompressedFileCount) {
    log.trace("Setting uncompressedFileCount to: {}", uncompressedFileCount);
    this.uncompressedFileCount = uncompressedFileCount;
  }

  /**
   * Sets the schedule for periodic rollovers.
   *
   * @param rollSchedule A comma-separated list of times in HH:mm format.
   */
  public void setRollSchedule(final String rollSchedule) {
    log.trace("Setting rollSchedule to: {}", rollSchedule);
    this.rollSchedule = rollSchedule;
  }

  /**
   * Sets the interval duration for periodic rollovers.
   *
   * @param rollInterval The interval in ISO-8601 duration format.
   */
  public void setRollInterval(final String rollInterval) {
    log.trace("Setting rollInterval to: {}", rollInterval);
    this.rollInterval = rollInterval;
  }
}
