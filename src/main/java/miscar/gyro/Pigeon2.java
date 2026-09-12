package miscar.gyro;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Pigeon2Configuration;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

public class Pigeon2 implements GyroIO {
  private final com.ctre.phoenix6.hardware.Pigeon2 pigeon;

  StatusSignal<Angle> yawSignal;
  StatusSignal<Angle> pitchSignal;
  StatusSignal<Angle> rollSignal;
  private final StatusSignal<AngularVelocity> yawVelocitySignal;

  public Pigeon2(int port, String CANBus) {
    pigeon = new com.ctre.phoenix6.hardware.Pigeon2(port, CANBus);
    pigeon.getConfigurator().apply(new Pigeon2Configuration());
    pigeon.getConfigurator().setYaw(0.0);
    yawSignal = pigeon.getYaw();
    pitchSignal = pigeon.getPitch();
    rollSignal = pigeon.getRoll();
    yawVelocitySignal = pigeon.getAngularVelocityZWorld();
    BaseStatusSignal
        .setUpdateFrequencyForAll(50.0, yawSignal, yawVelocitySignal, pitchSignal, rollSignal);

    pigeon.optimizeBusUtilization();
  }

  @Override
  public void updateInputs(GyroIOInputs inputs) {
    inputs.connected = BaseStatusSignal
        .refreshAll(yawSignal, yawVelocitySignal, pitchSignal, rollSignal).equals(StatusCode.OK);
    inputs.yawPosition = Rotation2d.fromDegrees(yawSignal.getValueAsDouble());
    inputs.pitchPosition = Rotation2d.fromDegrees(pitchSignal.getValueAsDouble());
    inputs.rollPosition = Rotation2d.fromDegrees(rollSignal.getValueAsDouble());
    inputs.yawVelocityRadPerSec = Units.degreesToRadians(yawVelocitySignal.getValueAsDouble());
  }
}
