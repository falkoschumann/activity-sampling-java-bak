package de.muspellheim.activitysampling.infrastructure;

import de.muspellheim.activitysampling.domain.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import org.apache.commons.csv.*;

public class CsvEventStore implements EventStore {
  public static final String COLUMN_TIMESTAMP = "Timestamp";
  public static final String COLUMN_CLIENT = "Client";
  public static final String COLUMN_PROJECT = "Project";
  public static final String COLUMN_TASK = "Task";
  public static final String COLUMN_NOTES = "Notes";
  private final Path file;

  public CsvEventStore(Path file) {
    this.file = file;
  }

  @Override
  public void record(Event event) {
    var formatBuilder =
        CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader(COLUMN_TIMESTAMP, COLUMN_CLIENT, COLUMN_PROJECT, COLUMN_TASK, COLUMN_NOTES);
    if (Files.exists(file)) {
      formatBuilder.setSkipHeaderRecord(true);
    }
    var format = formatBuilder.build();
    try (var printer =
        new CSVPrinter(
            Files.newBufferedWriter(
                file,
                StandardOpenOption.APPEND,
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE),
            format)) {
      var customEvent = (ActivityLoggedEvent) event;
      printer.printRecord(
          customEvent.timestamp().truncatedTo(ChronoUnit.SECONDS),
          customEvent.client(),
          customEvent.project(),
          customEvent.task(),
          customEvent.notes());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to record event into store.", e);
    }
  }

  @Override
  public Iterable<? extends Event> replay() {
    var format =
        CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader(COLUMN_TIMESTAMP, COLUMN_CLIENT, COLUMN_PROJECT, COLUMN_TASK, COLUMN_NOTES)
            .build();
    try (var parser = new CSVParser(Files.newBufferedReader(file), format)) {
      return parser.stream()
          .skip(1) // skip header
          .map(
              record ->
                  new ActivityLoggedEvent(
                      Instant.parse(record.get(COLUMN_TIMESTAMP)),
                      record.get(COLUMN_CLIENT),
                      record.get(COLUMN_PROJECT),
                      record.get(COLUMN_TASK),
                      record.get(COLUMN_NOTES)))
          .toList();
    } catch (NoSuchFileException e) {
      return List.of();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to replay events from store.", e);
    }
  }
}
