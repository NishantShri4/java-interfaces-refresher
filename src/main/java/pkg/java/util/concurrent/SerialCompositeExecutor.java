package pkg.java.util.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is an example from {@code Executor} javadocs
 * to demonstrate a composite executor which
 * and schedules execution based on polling a task queue.
 */
public class SerialCompositeExecutor {

  private final Queue<Runnable> tasks;
  private final Executor executor;
  private Runnable active;

  public SerialCompositeExecutor(final Queue<Runnable> tasks, final Executor executor) {
    this.tasks = tasks;
    this.executor = executor;
  }

  public synchronized void execute(Runnable r) {
    tasks.offer(r);

    if (active == null) {
      scheduleNext();
    }
  }

  protected synchronized void scheduleNext() {
    if ((active = tasks.poll()) != null) {
      executor.execute(active);
      active = null;
    }
  }

  public void shutdown() {
    if (executor != null && executor instanceof ExecutorService) {
      ExecutorService pool = ((ExecutorService) executor);
      pool.shutdown();
    }
  }

  public static void main(String[] args) {
    Executor executor = Executors.newSingleThreadExecutor();
    Queue<Runnable> deque = new ArrayDeque<>();

    Runnable r1 = () -> {
      try {
        Thread.sleep(2000);
        System.out.println("This is T1");
        Thread.sleep(2000);

      } catch (InterruptedException ex) {
        System.err.println(Thread.currentThread().getName() + " is Interrupted");
      }
    };
    Runnable r2 = () -> System.out.println("This is T2");
    Runnable r3 = () -> System.out.println("This is T3");

    SerialCompositeExecutor sce = new SerialCompositeExecutor(deque, executor);
    try {
      sce.execute(r1);
      sce.execute(r2);
      sce.execute(r3);
    } finally {
      sce.shutdown();
    }

  }

}