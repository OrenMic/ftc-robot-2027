package frc.robot.generated.swerveModule.drive;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.NeutralModeValue;
import miscar.annotation.DoNotOverride;
import miscar.configs.motors.TalonFXConfig;
import miscar.util.PhoenixControlTypes;

@DoNotOverride
public class TalonFXMotorConfig {
    /** The SwerveModule's drive enable FOC */
    private final boolean driveEnableFOC = false;
    /** The SwerveModule's drive canivore */
    private final boolean driveCanivore = false;

    /** The SwerveModule's drive motor config */
    private final TalonFXConfiguration driveMotorConfig = new TalonFXConfiguration()
            .withCurrentLimits(new CurrentLimitsConfigs().withStatorCurrentLimit(60)
                    .withStatorCurrentLimitEnable(true).withSupplyCurrentLimit(30)
                    .withSupplyCurrentLowerLimit(30).withSupplyCurrentLowerTime(1)
                    .withSupplyCurrentLimitEnable(true))
            // .withCurrentLimits(new
            // CurrentLimitsConfigs().withStatorCurrentLimit(80)
            // // .withStatorCurrentLimit(4).withSupplyCurrentLowerLimit(70)
            // .withSupplyCurrentLimitEnable(false)
            // .withStatorCurrentLimitEnable(true))
            // .withCurrentLimits(new
            // CurrentLimitsConfigs().withSupplyCurrentLimit(70)
            // .withSupplyCurrentLowerTime(0.2).withSupplyCurrentLowerLimit(35)).
            .withMotorOutput(new MotorOutputConfigs().withNeutralMode(NeutralModeValue.Brake))
            .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs().withKP(0.12).withKI(0)
                    .withKD(0.0).withKS(0.17).withKV(0.12).withKA(0.1));

    /** The SwerveModule's drive pose control type */
    private final PhoenixControlTypes.PoseControl drivePoseControlType =
            PhoenixControlTypes.PoseControl.PositionVoltage;

    /** The SwerveModule's drive open loop control type */
    private final PhoenixControlTypes.OpenLoopControl driveOpenLoopControlType =
            PhoenixControlTypes.OpenLoopControl.Regular;

    /** The SwerveModule's drive velocity control type */
    private final PhoenixControlTypes.VelocityControl driveVelocityControlType =
            PhoenixControlTypes.VelocityControl.VelocityVoltage;

    /** The drive's talon FX config constructor */
    public final TalonFXConfig config =
            new TalonFXConfig(frc.robot.generated.ports.Ports.driveMotorPort, driveEnableFOC,
                    driveMotorConfig, driveCanivore).withInverted(true)
                            .withVelocityControlType(driveVelocityControlType);

    public TalonFXMotorConfig() {}
}
