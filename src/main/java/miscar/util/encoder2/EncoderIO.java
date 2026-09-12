package miscar.util.encoder2;

import org.littletonrobotics.junction.AutoLog;

public interface EncoderIO {

  @AutoLog
  public class EncoderInputs {
    public boolean connected = false;
    public double pose = 0;
    public double offset = 0;
  }

  public default void updateInputs(EncoderInputs inputs) {}

  public default double get() {
    return 0;
  }
}
