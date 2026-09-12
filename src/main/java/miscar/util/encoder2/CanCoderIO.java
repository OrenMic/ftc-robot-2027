package miscar.util.encoder2;

import static miscar.util.PhoenixUtil.tryUntilOk;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.hardware.CANcoder;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.units.measure.Angle;
import miscar.configs.encoder.CanCoderConfig;

public class CanCoderIO extends CANcoder implements EncoderIO {

  StatusSignal<Angle> positionRotations;

  public CanCoderIO(CanCoderConfig config) {
    super(config.encoderPort, config.getCANBus());
    tryUntilOk(5,
        () -> this.getConfigurator().apply(config.encoderConfig, 0.25),
        config.encoderPort,
        "Encoder");

    positionRotations = getAbsolutePosition();

    BaseStatusSignal.setUpdateFrequencyForAll(50.0, positionRotations);
    optimizeBusUtilization();
  }

  @Override
  public double get() {
    return MathUtil.inputModulus(positionRotations.getValueAsDouble() * 360, 0, 360);
  }

  @Override
  public void updateInputs(EncoderInputs inputs) {
    // Refresh all signals
    var encoderStatus = BaseStatusSignal.refreshAll(positionRotations);

    inputs.connected = encoderStatus.isOK();
    inputs.pose = get();
  }
}
