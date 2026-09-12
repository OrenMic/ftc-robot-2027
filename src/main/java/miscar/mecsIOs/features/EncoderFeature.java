package miscar.mecsIOs.features;

import java.util.function.DoubleConsumer;
import miscar.configs.encoder.EncoderConfig;
import miscar.mecsIOs.AbstractMechanism;
import miscar.util.encoder2.Encoder;

public class EncoderFeature implements Feature {
  private DoubleConsumer setCurrantPose;
  private final Encoder encoder;

  public EncoderFeature(EncoderConfig<?> config) {
    encoder = Encoder.create(config);
  }

  @Override
  public void connectMec(AbstractMechanism mechanism) {
    setCurrantPose = mechanism::setCurrentPose;
  }

  @Override
  public void executeBeforePeriodic() {
    setCurrantPose.accept(encoder.getMecPose());
  }

  @Override
  public void executeAfterPeriodic() {
    encoder.updateInputs();
  }
}
