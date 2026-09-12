package frc.robot.generated.shooterMec.backRight;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import miscar.annotation.DoNotOverride;
import miscar.configs.motors.TalonFXConfig;
import miscar.util.PhoenixControlTypes;

@DoNotOverride
public class TalonFXMotorConfig {
    /** The ShooterMec's back right enable FOC */
    private final boolean backRightEnableFOC = false;

    /** The ShooterMec's back right canivore */
    private final boolean backRightCanivore = true;

    /** The ShooterMec's back right motor config */
    private final TalonFXConfiguration backRightMotorConfig = new TalonFXConfiguration()
            .withCurrentLimits(new CurrentLimitsConfigs().withSupplyCurrentLimit(30)
                    .withSupplyCurrentLowerTime(1).withSupplyCurrentLowerLimit(20)
                    .withSupplyCurrentLimitEnable(true))
            .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs().withKP(0.35).withKI(0)
                    .withKD(0.01).withKV(0.121))
            .withMotorOutput(new com.ctre.phoenix6.configs.MotorOutputConfigs()
                    .withNeutralMode(com.ctre.phoenix6.signals.NeutralModeValue.Coast));

    /** The ShooterMec's back right pose control type */
    private final PhoenixControlTypes.PoseControl backRightPoseControlType =
            PhoenixControlTypes.PoseControl.PositionVoltage;

    /** The ShooterMec's back right open loop control type */
    private final PhoenixControlTypes.OpenLoopControl backRightOpenLoopControlType =
            PhoenixControlTypes.OpenLoopControl.Regular;

    /** The ShooterMec's back right velocity control type */
    private final PhoenixControlTypes.VelocityControl backRightVelocityControlType =
            PhoenixControlTypes.VelocityControl.VelocityVoltage;

    /** The back right's talon FX config constructor */
    public final TalonFXConfig config =
            new TalonFXConfig(frc.robot.generated.ports.Ports.backRightMotorPort,
                    backRightEnableFOC, backRightMotorConfig, backRightCanivore)
                            .withInverted(false);

    public TalonFXMotorConfig() {}
}
