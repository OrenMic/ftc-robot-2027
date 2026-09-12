package frc.robot.generated.shooterMec.front;

import miscar.annotation.DoNotOverride;
import miscar.configs.mecs.Ratios;

@DoNotOverride
public class MecRatiosConstants {
  /** The ShooterMec's front motor to mec ratio */
  private final double frontMotorToMecRatio = 24.0 / 36.0;

  /** The front's ratios constructor */
  public final Ratios config = new Ratios(frontMotorToMecRatio);

  public MecRatiosConstants() {}
}
