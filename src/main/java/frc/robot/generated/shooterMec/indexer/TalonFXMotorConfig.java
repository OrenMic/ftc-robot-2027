package frc.robot.generated.shooterMec.indexer;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import miscar.annotation.DoNotOverride;
import miscar.configs.motors.TalonFXConfig;
import miscar.util.PhoenixControlTypes;

@DoNotOverride
public class TalonFXMotorConfig {
        /** The ShooterMec's indexer enable FOC */
        private final boolean indexerEnableFOC = false;

        /** The ShooterMec's indexer canivore */
        private final boolean indexerCanivore = true;

        /** The ShooterMec's indexer motor config */
        private final TalonFXConfiguration indexerMotorConfig =
                        new TalonFXConfiguration()
                                        .withCurrentLimits(new CurrentLimitsConfigs()
                                                        .withSupplyCurrentLimit(45)
                                                        .withSupplyCurrentLowerTime(1)
                                                        .withSupplyCurrentLowerLimit(
                                                                        30)
                                                        .withSupplyCurrentLimitEnable(true))
                                        .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs()
                                                        .withKP(0.4).withKI(0).withKD(0.01)
                                                        .withKV(0.1215).withKS(0.33984375))
                                        .withMotorOutput(
                                                        new com.ctre.phoenix6.configs.MotorOutputConfigs()
                                                                        .withNeutralMode(
                                                                                        com.ctre.phoenix6.signals.NeutralModeValue.Coast));

        /** The ShooterMec's indexer pose control type */
        private final PhoenixControlTypes.PoseControl indexerPoseControlType =
                        PhoenixControlTypes.PoseControl.PositionVoltage;

        /** The ShooterMec's indexer open loop control type */
        private final PhoenixControlTypes.OpenLoopControl indexerOpenLoopControlType =
                        PhoenixControlTypes.OpenLoopControl.Regular;

        /** The ShooterMec's indexer velocity control type */
        private final PhoenixControlTypes.VelocityControl indexerVelocityControlType =
                        PhoenixControlTypes.VelocityControl.VelocityVoltage;

        /** The indexer's talon FX config constructor */
        public final TalonFXConfig config =
                        new TalonFXConfig(frc.robot.generated.ports.Ports.indexerMotorPort,
                                        indexerEnableFOC, indexerMotorConfig, indexerCanivore)
                                                        .withInverted(true);

        public TalonFXMotorConfig() {}
}
