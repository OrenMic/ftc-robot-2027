package miscar.util;

import java.util.ArrayList;
import java.util.List;

public abstract class BackgroundProcess {
  private static List<Runnable> processes = new ArrayList<>();

  public BackgroundProcess() {
    processes.add(this::periodic);
  }

  public static void add(Runnable runnable) {
    processes.add(runnable);
  }

  public static void periodicAll() {
    for (Runnable process : processes) {
      process.run();
    }
  }

  protected abstract void periodic();
}
