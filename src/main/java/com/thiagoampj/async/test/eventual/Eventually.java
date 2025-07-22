package com.thiagoampj.async.test.eventual;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Class meant to be used during asynchronous assertions for stuff running in a background.
 *
 * E.G
 * Eventually ( () ->
 *   assert(statusHasChanged(PROCESSED)) // this happens in a different thread or process.
 * );
 *
 */
public class Eventually {

  private static final Duration DEFAULT_INTERVAL = Duration.ofMillis(100);
  private static final Duration DEFAULTMAX_DURATION = Duration.ofSeconds(3);

  public static <T> T eventually(EventualInterface<T> command) throws TimeoutException {
    return eventually(DEFAULT_INTERVAL, DEFAULTMAX_DURATION, command);
  }

  public static <T> T eventually(Duration interval, Duration maxDuration, EventualInterface<T> command)
          throws TimeoutException {
    if (interval.toMillis() > maxDuration.toMillis()) {
      throw new IllegalArgumentException("Eventually: Interval cannot be bigger than maxDuration");
    }
    final Instant max = Instant.now()
            .plus(maxDuration);

    do {
      try {
        return command.checkNow();
      } catch (Throwable t) {
        if (isStillInTime(max)) {
          try {
            Thread.sleep(interval.toMillis());
          } catch (InterruptedException interruptedException) {
            throw new RuntimeException("Do not interrupt this Thread!", interruptedException);
          }
        } else {
          throw t;
        }
      }
    } while (isStillInTime(max));
    throw new TimeoutException(
            String.format("Could not eventually verify within maxDuration=%s", maxDuration));
  }

  private static boolean isStillInTime(Instant max) {
    return Instant.now()
            .isBefore(max);
  }

  @FunctionalInterface
  public interface EventualInterface<T> {
    T checkNow();
  }
}
