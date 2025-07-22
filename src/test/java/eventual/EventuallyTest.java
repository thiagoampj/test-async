package eventual;

import com.thiagoampj.async.test.eventual.Eventually;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class EventuallyTest {

  @Test
  public void itShouldEventuallyGetExpectedDataTest() throws TimeoutException {
    var thread = new MyThread();
    thread.start();
    Eventually.eventually(() -> {
      Assertions.assertEquals(1, thread.count());
      return true;
    });
  }

  @Test
  public void itShouldTimeoutTest() {
    var thread = new MyThread();
    thread.start();
    Assertions.assertThrows(TimeoutException.class, () -> Eventually.eventually(Duration.ofMillis(100), Duration.ofMillis(500), () -> {
      Assertions.assertEquals(2, thread.count());
      return true;
    }));
  }

  class MyThread extends Thread {

    private AtomicInteger counter = new AtomicInteger();

    public int count() {
      return counter.get();
    }

    @Override
    public void run() {
      try {
        Thread.sleep(300);
        counter.incrementAndGet();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
