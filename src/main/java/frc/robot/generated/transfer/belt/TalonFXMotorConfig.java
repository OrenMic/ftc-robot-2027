package frc.robot.generated.transfer.belt;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import miscar.annotation.DoNotOverride;
import miscar.configs.motors.TalonFXConfig;
import miscar.util.PhoenixControlTypes;

@DoNotOverride
public class TalonFXMotorConfig {
    /** The Transfer's belt enable FOC */
    private final boolean beltEnableFOC = false;

    /** The Transfer's belt canivore */
    private final boolean beltCanivore = false;

    /** The Transfer's belt motor config */
    private final TalonFXConfiguration beltMotorConfig = new TalonFXConfiguration()
            .withCurrentLimits(new CurrentLimitsConfigs().withSupplyCurrentLimit(70)
                    .withSupplyCurrentLowerTime(1.5).withSupplyCurrentLowerLimit(50)
                    .withSupplyCurrentLimitEnable(true))
            .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs().withKP(0.3).withKI(0).withKD(0)
                    .withKS(0.4).withKV(0.14))
            .withMotorOutput(new com.ctre.phoenix6.configs.MotorOutputConfigs()
                    .withNeutralMode(com.ctre.phoenix6.signals.NeutralModeValue.Coast));

    /** The Transfer's belt pose control type */
    private final PhoenixControlTypes.PoseControl beltPoseControlType =
            PhoenixControlTypes.PoseControl.PositionVoltage;

    /** The Transfer's belt open loop control type */
    private final PhoenixControlTypes.OpenLoopControl beltOpenLoopControlType =
            PhoenixControlTypes.OpenLoopControl.Regular;

    /** The Transfer's belt velocity control type */
    private final PhoenixControlTypes.VelocityControl beltVelocityControlType =
            PhoenixControlTypes.VelocityControl.VelocityVoltage;

    /** The belt's talon FX config constructor */
    public final TalonFXConfig config =
            new TalonFXConfig(frc.robot.generated.ports.Ports.beltMotorPort, beltEnableFOC,
                    beltMotorConfig, beltCanivore).withInverted(true);

    public TalonFXMotorConfig() {}
}
