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

    eventStore.record(new ActivityLoggedEvent(Instant.parse("2022-10-19T21:31:00Z"), "foo"));
    eventStore.record(new ActivityLoggedEvent(Instant.parse("2022-10-19T21:32:00Z"), "bar"));

    assertThat(
        eventStore.replay(),
        contains(
            new ActivityLoggedEvent(Instant.parse("2022-10-19T21:31:00Z"), "foo"),
            new ActivityLoggedEvent(Instant.parse("2022-10-19T21:32:00Z"), "bar")));
  }
}
