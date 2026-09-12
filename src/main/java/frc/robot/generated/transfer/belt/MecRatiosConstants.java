package frc.robot.generated.transfer.belt;

import miscar.annotation.DoNotOverride;
import miscar.configs.mecs.Ratios;

@DoNotOverride
public class MecRatiosConstants {
  /** The Transfer's belt motor to mec ratio */
  private final double beltMotorToMecRatio = 1.0;// 26.0 / 34.0;

  /** The belt's ratios constructor */
  public final Ratios config = new Ratios(beltMotorToMecRatio);

  public MecRatiosConstants() {}
}
