package miscar.configs.encoder;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;
import miscar.util.encoder2.CanCoderIO;

@ConstantsName(constantsName = "canEncoderConfig")
public class CanCoderConfig extends EncoderConfig<CanCoderConfig> {
  private final boolean canivore;
  public final CANcoderConfiguration encoderConfig;

  @ConstantsConstructor
  public CanCoderConfig(int encoderPort, CANcoderConfiguration encoderConfig, boolean canivore) {
    super(encoderPort);
    this.canivore = canivore;
    this.encoderConfig = encoderConfig;
  }

  /**
   * @return The {@code CANBus} this encoder is configured to
   */
  public String getCANBus() {
    return canivore ? "canivore" : "rio";
  }

  @Override
  public CanCoderIO buildEncoder() {
    return new CanCoderIO(this);
  }
}
