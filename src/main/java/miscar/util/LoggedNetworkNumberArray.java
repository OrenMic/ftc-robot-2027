package miscar.util;

import edu.wpi.first.networktables.DoubleArrayEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;
import org.littletonrobotics.junction.networktables.LoggedNetworkInput;

/** Manages a number value published to the root table of NT. */
public class LoggedNetworkNumberArray extends LoggedNetworkInput {
  private final String key;
  private final DoubleArrayEntry entry;
  private double[] defaultValue = new double[0];
  private double[] value;

  /**
   * Creates a new LoggedNetworkNumber, for handling a number input sent
   * via NetworkTables.
   *
   * @param key The key for the number, published to the root table of
   *        NT or "/DashboardInputs/{key}" when logged.
   */
  public LoggedNetworkNumberArray(String key) {
    this.key = key;
    this.entry =
        NetworkTableInstance.getDefault().getDoubleArrayTopic(key).getEntry(new double[] {});
    this.value = defaultValue;
    Logger.registerDashboardInput(this);
  }

  /**
   * Creates a new LoggedNetworkNumber, for handling a number input sent
   * via NetworkTables.
   *
   * @param key The key for the number, published to the root table of
   *        NT or "/DashboardInputs/{key}" when logged.
   * @param defaultValue The default value if no value in NT is found.
   */
  public LoggedNetworkNumberArray(String key, double[] defaultValue) {
    this(key);
    setDefault(defaultValue);
    this.value = defaultValue;
  }

  /**
   * Updates the default value, which is used if no value in NT is
   * found.
   */
  public void setDefault(double[] defaultValue) {
    this.defaultValue = defaultValue;
    entry.set(entry.get(defaultValue));
  }

  /**
   * Publishes a new value. Note that the value will not be returned by
   * {@link #get()} until the next cycle.
   */
  public void set(double[] value) {
    entry.set(value);
  }

  /** Returns the current value. */
  public double[] get() {
    return value;
  }

  private final LoggableInputs inputs = new LoggableInputs() {
    public void toLog(LogTable table) {
      table.put(removeSlash(key), value);
    }

    public void fromLog(LogTable table) {
      value = table.get(removeSlash(key), defaultValue);
    }
  };

  public void periodic() {
    if (!Logger.hasReplaySource()) {
      value = entry.get(defaultValue);
    }
    Logger.processInputs(prefix, inputs);
  }
}
