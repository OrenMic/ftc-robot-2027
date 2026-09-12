package frc.robot.generated.shooterMec.front;

import miscar.annotation.DoNotOverride;
import miscar.configs.motors.SimConfig;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

@DoNotOverride
public class SimMotorConfig {
  /** The ShooterMec's front gear box */
  private final DCMotorUtil frontGearBox = DCMotorUtil.from(DCMotorUtil.getKrakenX60(1));

  /** The ShooterMec's front PID controller */
  private final PIDControllerUtil frontPIDController = new PIDControllerUtil(8, 0, 0);

  /** The ShooterMec's front MOI */
  private final double frontMOI = 1;

  /** The front's sim config constructor */
  public final SimConfig config = new SimConfig(frontGearBox, frontPIDController, frontMOI);

  public SimMotorConfig() {}
}
