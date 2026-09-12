package frc.robot.generated.swerveModule.drive;

import miscar.annotation.DoNotOverride;
import miscar.configs.motors.SimConfig;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

@DoNotOverride
public class SimMotorConfig {
  /** The SwerveModule's drive gear box */
  private final DCMotorUtil driveGearBox = DCMotorUtil.from(DCMotorUtil.getKrakenX60(1));

  /** The SwerveModule's drive PID controller */
  private final PIDControllerUtil drivePIDController = new PIDControllerUtil(8, 0, 0);

  /** The SwerveModule's drive MOI */
  private final double driveMOI = 1;

  /** The drive's sim config constructor */
  public final SimConfig config = new SimConfig(driveGearBox, drivePIDController, driveMOI);

  public SimMotorConfig() {}
}
