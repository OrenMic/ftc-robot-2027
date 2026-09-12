package frc.robot.generated.intake.extension;

import miscar.annotation.DoNotOverride;
import miscar.configs.motors.SimConfig;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

@DoNotOverride
public class SimMotorConfig {
  /** The Intake's extension gear box */
  private final DCMotorUtil extensionGearBox = DCMotorUtil.from(DCMotorUtil.getKrakenX60(1));

  /** The Intake's extension PID controller */
  private final PIDControllerUtil extensionPIDController = new PIDControllerUtil(8, 0, 0);

  /** The Intake's extension MOI */
  private final double extensionMOI = 0.01;

  /** The extension's sim config constructor */
  public final SimConfig config =
      new SimConfig(extensionGearBox, extensionPIDController, extensionMOI);

  public SimMotorConfig() {}
}
