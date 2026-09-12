package frc.robot.generated.intake.extension;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import miscar.annotation.DoNotOverride;
import miscar.configs.motors.TalonFXConfig;
import miscar.util.PhoenixControlTypes;

@DoNotOverride
public class TalonFXMotorConfig {

    /** The Intake's extension enable FOC */
    private final boolean extensionEnableFOC = false;

    /** The Intake's extension canivore */
    private final boolean extensionCanivore = false;

    /** The Intake's extension motor config */
    private final TalonFXConfiguration extensionMotorConfig = new TalonFXConfiguration()
            .withCurrentLimits(new CurrentLimitsConfigs().withSupplyCurrentLimit(15)
                    .withSupplyCurrentLowerTime(0).withSupplyCurrentLowerLimit(0))
            .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs().withKP(5).withKI(0).withKD(0))
            .withMotionMagic(new MotionMagicConfigs().withMotionMagicCruiseVelocity(0)
                    .withMotionMagicAcceleration(0).withMotionMagicJerk(0))
            .withMotorOutput(new com.ctre.phoenix6.configs.MotorOutputConfigs()
                    .withNeutralMode(com.ctre.phoenix6.signals.NeutralModeValue.Brake));

    /** The Intake's extension pose control type */
    private final PhoenixControlTypes.PoseControl extensionPoseControlType =
            PhoenixControlTypes.PoseControl.MotionMagicVoltage;

    /** The Intake's extension open loop control type */
    private final PhoenixControlTypes.OpenLoopControl extensionOpenLoopControlType =
            PhoenixControlTypes.OpenLoopControl.Regular;

    /** The Intake's extension velocity control type */
    private final PhoenixControlTypes.VelocityControl extensionVelocityControlType =
            PhoenixControlTypes.VelocityControl.VelocityVoltage;

    /** The extension's talon FX config constructor */
    public final TalonFXConfig config =
            new TalonFXConfig(frc.robot.generated.ports.Ports.extensionMotorPort,
                    extensionEnableFOC, extensionMotorConfig, extensionCanivore)
                            // .withPoseControlType(
                            // extensionPoseControlType)
                            .withInverted(true);

    public TalonFXMotorConfig() {}
}
