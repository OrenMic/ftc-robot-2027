package frc.robot.generated.shooterMec.backRight;

import miscar.annotation.DoNotOverride;
import miscar.configs.motors.SimConfig;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

@DoNotOverride
public class SimMotorConfig {
  /** The ShooterMec's back right gear box */
  private final DCMotorUtil backRightGearBox = DCMotorUtil.from(DCMotorUtil.getKrakenX60(1));

  /** The ShooterMec's back right PID controller */
  private final PIDControllerUtil backRightPIDController = new PIDControllerUtil(8, 0, 0);

  /** The ShooterMec's back right MOI */
  private final double backRightMOI = 1;

  /** The back right's sim config constructor */
  public final SimConfig config =
      new SimConfig(backRightGearBox, backRightPIDController, backRightMOI);

  public SimMotorConfig() {}
}
