package frc.robot.generated.shooterMec.indexer;

import miscar.annotation.DoNotOverride;
import miscar.configs.mecs.Ratios;

@DoNotOverride
public class MecRatiosConstants {
  /** The ShooterMec's indexer motor to mec ratio */
  private final double indexerMotorToMecRatio = 1.0 / ((1.5 + 1.6) / 2.0);
  /** The indexer's ratios constructor */
  public final Ratios config = new Ratios(indexerMotorToMecRatio);

  public MecRatiosConstants() {}
}
