package miscar.util.encoder2;

import org.littletonrobotics.junction.Logger;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Timer;
import miscar.configs.encoder.BoreEncoderConfig;

public class BoreEncoderIO extends DutyCycleEncoder implements EncoderIO {

  private String freqyencyLogPath = "Encoders/Frequency/" + getSourceChannel();

  public BoreEncoderIO(BoreEncoderConfig config) {
    super(config.encoderPort);
    setInverted(config.inverted);
    double start = Timer.getFPGATimestamp();

    while (getFrequency() < 900 && Timer.getFPGATimestamp() - start < 3) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public double get() {
    return super.get() * 360;
  }

  @Override
  public void updateInputs(EncoderInputs inputs) {
    inputs.connected = isConnected();
    inputs.pose = get();
    Logger.recordOutput(freqyencyLogPath, getFrequency());
  }
}
