package frc.robot.generated.swerveModule.rotation;

import miscar.annotation.DoNotOverride;
import miscar.configs.motors.SimConfig;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

@DoNotOverride
public class SimMotorConfig {
  /** The SwerveModule's rotation gear box */
  private final DCMotorUtil rotationGearBox = DCMotorUtil.from(DCMotorUtil.getKrakenX60(1));

  /** The SwerveModule's rotation PID controller */
  private final PIDControllerUtil rotationPIDController = new PIDControllerUtil(8, 0, 0);

  {
    // rotationPIDController.enableContinuousInput(-Math.PI, Math.PI);
  }

  /** The SwerveModule's rotation MOI */
  private final double rotationMOI = 1;

  /** The rotation's sim config constructor */
  public final SimConfig config =
      new SimConfig(rotationGearBox, rotationPIDController, rotationMOI);

  public SimMotorConfig() {}
}
