package frc.robot.generated.intake.extension;

import edu.wpi.first.math.util.Units;
import miscar.annotation.DoNotOverride;
import miscar.configs.mecs.Ratios;

@DoNotOverride
public class MecRatiosConstants {
  /** The Intake's extension motor to mec ratio */
  private static final double gearDiameterMeter = Units.inchesToMeters(1.5);
  private static final double extensionMotorToMecRatio = (1.0 / 5.0) * gearDiameterMeter * Math.PI;

  /** The extension's ratios constructor */
  public final Ratios config = new Ratios(extensionMotorToMecRatio);

  public MecRatiosConstants() {}
}
