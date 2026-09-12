package frc.robot.generated.intake.intake;

import miscar.annotation.DoNotOverride;
import miscar.configs.mecs.Ratios;

@DoNotOverride
public class MecRatiosConstants {
  /** The Intake's intake motor to mec ratio */
  private static final double intakeMotorToMecRatio = 36.0 / 48.0;

  /** The intake's ratios constructor */
  public final Ratios config = new Ratios(intakeMotorToMecRatio);

  public MecRatiosConstants() {}
}
