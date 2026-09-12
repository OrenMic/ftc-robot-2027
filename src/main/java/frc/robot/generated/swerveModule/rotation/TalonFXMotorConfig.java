package frc.robot.generated.swerveModule.rotation;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.VoltageConfigs;
import miscar.annotation.DoNotOverride;
import miscar.configs.motors.TalonFXConfig;
import miscar.util.PhoenixControlTypes;

@DoNotOverride
public class TalonFXMotorConfig {
  /** The SwerveModule's rotation enable FOC */
  private final boolean rotationEnableFOC = false;

  /** The SwerveModule's rotation canivore */
  private final boolean rotationCanivore = false;

  /** The SwerveModule's rotation motor config */
  /** The SimpleDriveModule's rotation motor config */
  private final TalonFXConfiguration rotationMotorConfig = new TalonFXConfiguration()
      .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs().withKP(5).withKI(0).withKD(0))
      .withMotorOutput(new com.ctre.phoenix6.configs.MotorOutputConfigs()
          .withNeutralMode(com.ctre.phoenix6.signals.NeutralModeValue.Brake))
      .withVoltage(new VoltageConfigs().withPeakForwardVoltage(10).withPeakReverseVoltage(-10));

  /** The SwerveModule's rotation pose control type */
  private final PhoenixControlTypes.PoseControl rotationPoseControlType =
      PhoenixControlTypes.PoseControl.PositionVoltage;

  /** The SwerveModule's rotation open loop control type */
  private final PhoenixControlTypes.OpenLoopControl rotationOpenLoopControlType =
      PhoenixControlTypes.OpenLoopControl.Regular;

  /** The SwerveModule's rotation velocity control type */
  private final PhoenixControlTypes.VelocityControl rotationVelocityControlType =
      PhoenixControlTypes.VelocityControl.VelocityVoltage;

  /** The rotation's talon FX config constructor */
  public final TalonFXConfig config =
      new TalonFXConfig(frc.robot.generated.ports.Ports.rotationMotorPort, rotationEnableFOC,
          rotationMotorConfig, rotationCanivore).withInverted(true);

  public TalonFXMotorConfig() {}
}
