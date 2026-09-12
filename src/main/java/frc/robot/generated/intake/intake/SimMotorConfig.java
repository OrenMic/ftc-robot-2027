package frc.robot.generated.intake.intake;

import miscar.annotation.DoNotOverride;
import miscar.configs.motors.SimConfig;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

@DoNotOverride
public class SimMotorConfig {
  /** The Intake's intake gear box */
  private final DCMotorUtil intakeGearBox = DCMotorUtil.from(DCMotorUtil.getKrakenX60(1));

  /** The Intake's intake PID controller */
  private final PIDControllerUtil intakePIDController = new PIDControllerUtil(8, 0, 0);

  /** The Intake's intake MOI */
  private final double intakeMOI = 1;

  /** The intake's sim config constructor */
  public final SimConfig config = new SimConfig(intakeGearBox, intakePIDController, intakeMOI);

  public SimMotorConfig() {}
}
