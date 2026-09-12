package miscar.configs.encoder;

import miscar.configs.Config;
import miscar.util.encoder2.EncoderIO;

public class EncoderConfig<T> extends Config {

  public int encoderPort = 1;
  public double encoderToMecRatio = 1;
  public boolean inverted = false;

  public EncoderConfig(int encoderPort) {
    this.encoderPort = encoderPort;
  }

  @SuppressWarnings("unchecked")
  public T withInverted(boolean inverted) {
    this.inverted = inverted;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T withEncoderToMecRatio(double ratio) {
    this.encoderToMecRatio = ratio;
    return (T) this;
  }

  public EncoderIO buildEncoder() {
    return new EncoderIO() {};
  }
}
