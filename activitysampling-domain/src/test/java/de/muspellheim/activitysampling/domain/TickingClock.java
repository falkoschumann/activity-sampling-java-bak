package de.muspellheim.activitysampling.domain;

import java.time.*;
import java.util.*;

public class TickingClock extends Clock {
  private Instant instant;
  private final Duration tick;
  private final ZoneId zone;

  public TickingClock(Instant start, Duration tick, ZoneId zone) {
    this.instant = start;
    this.tick = tick;
    this.zone = zone;
  }

  public void tick() {
    instant = instant.plus(tick);
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return new TickingClock(instant, tick, zone);
  }

  @Override
  public Instant instant() {
    return instant;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    TickingClock that = (TickingClock) o;
    return instant.equals(that.instant) && tick.equals(that.tick) && zone.equals(that.zone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), instant, tick, zone);
  }

  @Override
  public String toString() {
    return "TickingClock{" + "instant=" + instant + ", tick=" + tick + ", zone=" + zone + '}';
  }
}
