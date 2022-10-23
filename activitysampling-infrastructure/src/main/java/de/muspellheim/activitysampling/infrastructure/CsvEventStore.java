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
  public static final String COLUMN_ACTIVITY = "Activity";
  private final Path file;

  public CsvEventStore(Path file) {
    this.file = file;
  }

  @Override
  public void record(Event event) {
    var formatBuilder =
        CSVFormat.Builder.create(CSVFormat.RFC4180).setHeader(COLUMN_TIMESTAMP, COLUMN_ACTIVITY);
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
      var e = (ActivityLoggedEvent) event;
      printer.printRecord(e.timestamp().truncatedTo(ChronoUnit.SECONDS), e.activity());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to record event into store.", e);
    }
  }

  @Override
  public Iterable<? extends Event> replay() {
    var format =
        CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader(COLUMN_TIMESTAMP, COLUMN_ACTIVITY)
            .build();
    try (var parser = new CSVParser(Files.newBufferedReader(file), format)) {
      return parser.stream()
          .skip(1) // skip header
          .map(
              record ->
                  new ActivityLoggedEvent(
                      Instant.parse(record.get(COLUMN_TIMESTAMP)), record.get(COLUMN_ACTIVITY)))
          .toList();
    } catch (NoSuchFileException e) {
      return List.of();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to replay events from store.", e);
    }
  }
}
