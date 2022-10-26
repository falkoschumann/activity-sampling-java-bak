package de.muspellheim.activitysampling.infrastructure;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import de.muspellheim.activitysampling.domain.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import org.junit.jupiter.api.*;

class CsvEventStoreTests {
  private final Path file = Paths.get("build/event-store.csv");

  @BeforeEach
  void init() throws IOException {
    Files.deleteIfExists(file);
  }

  @Test
  void storeNotExists_ReturnEmptyIterable() {
    var eventStore = new CsvEventStore(file);

    var result = eventStore.replay();

    assertThat(result, emptyIterable());
  }

  @Test
  void recordAndReplay() {
    var eventStore = new CsvEventStore(file);

    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-19T21:31:00Z"),
            Duration.ofMinutes(5),
            "client 1",
            "project 1",
            "task 1",
            "notes 1"));
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-19T21:32:00Z"),
            Duration.ofMinutes(10),
            "client 2",
            "project 2",
            "task 2",
            "notes 2"));

    assertThat(
        eventStore.replay(),
        contains(
            new ActivityLoggedEvent(
                Instant.parse("2022-10-19T21:31:00Z"),
                Duration.ofMinutes(5),
                "client 1",
                "project 1",
                "task 1",
                "notes 1"),
            new ActivityLoggedEvent(
                Instant.parse("2022-10-19T21:32:00Z"),
                Duration.ofMinutes(10),
                "client 2",
                "project 2",
                "task 2",
                "notes 2")));
  }
}
