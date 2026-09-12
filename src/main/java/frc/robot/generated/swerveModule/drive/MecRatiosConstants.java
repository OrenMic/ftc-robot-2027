package frc.robot.generated.swerveModule.drive;

import miscar.annotation.DoNotOverride;
import miscar.configs.mecs.Ratios;

@DoNotOverride
public class MecRatiosConstants {

  /** The SwerveModule's drive motor to mec ratio */
  private final double driveMotorToMecRatio =
      (1 / (((26.0 / 12.0) * (45.0 / 15.0))) * (0.1016 * Math.PI));
  // * 0.9417348967;

  // / 0.3847499125568381
  // / 1.903846153846154
  // / 0.9894736842105263
  // * 0.9717444717444717; // *
  // 0.85;

  /** The drive's ratios constructor */
  public final Ratios config = new Ratios(driveMotorToMecRatio);

  public MecRatiosConstants() {}
}
