package miscar.util.digitalInput;

import org.littletonrobotics.junction.AutoLog;

public interface DigitalInputIO {

  @AutoLog
  public class DigitalInputs {
    public boolean connected = false;
    public boolean value = false;
  }

  public default void updateInputs(DigitalInputs inputs) {}

  public default boolean get() {
    return false;
  }
}
