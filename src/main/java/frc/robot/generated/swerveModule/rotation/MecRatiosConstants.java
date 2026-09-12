package frc.robot.generated.swerveModule.rotation;

import miscar.annotation.DoNotOverride;
import miscar.configs.mecs.Ratios;

@DoNotOverride
public class MecRatiosConstants {

  /** The SwerveModule's rotation motor to mec ratio */
  private final double rotationMotorToMecRatio = (1 / 10.0) * 360;
  /** The rotation's ratios constructor */
  public final Ratios config = new Ratios(rotationMotorToMecRatio);

  public MecRatiosConstants() {}
}
