package miscar.configs.encoder;

import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;
import miscar.util.encoder2.BoreEncoderIO;

@ConstantsName(constantsName = "throughBoreEncoderConfig")
public class BoreEncoderConfig extends EncoderConfig<BoreEncoderConfig> {

  @ConstantsConstructor
  public BoreEncoderConfig(int encoderPort) {
    super(encoderPort);
  }

  @Override
  public BoreEncoderIO buildEncoder() {
    return new BoreEncoderIO(this);
  }
}
