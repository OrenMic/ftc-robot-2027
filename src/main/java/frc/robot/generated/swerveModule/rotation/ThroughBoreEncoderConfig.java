package frc.robot.generated.swerveModule.rotation;

import miscar.annotation.DoNotOverride;
import miscar.configs.encoder.BoreEncoderConfig;

@DoNotOverride
public class ThroughBoreEncoderConfig {
  /** The SwerveModule's rotation encoder to mec ratio */
  public final double rotationEncoderToMecRatio = 1;

  /** The SwerveModule's rotation inverted */
  public final boolean rotationInverted = true;

  /** The rotation's bore encoder config constructor */
  public final BoreEncoderConfig config =
      new BoreEncoderConfig(frc.robot.generated.ports.RoboRIOPorts.DIO.rotationEncoderPort)
          .withInverted(rotationInverted);

  public ThroughBoreEncoderConfig() {}
}
