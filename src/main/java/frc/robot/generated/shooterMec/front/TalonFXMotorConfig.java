package frc.robot.generated.shooterMec.front;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import miscar.annotation.DoNotOverride;
import miscar.configs.motors.TalonFXConfig;
import miscar.util.PhoenixControlTypes;

@DoNotOverride
public class TalonFXMotorConfig {
        /** The ShooterMec's front enable FOC */
        private final boolean frontEnableFOC = false;

        /** The ShooterMec's front canivore */
        private final boolean frontCanivore = true;

        /** The ShooterMec's front motor config */
        private final TalonFXConfiguration frontMotorConfig =
                        new TalonFXConfiguration()
                                        .withCurrentLimits(new CurrentLimitsConfigs()
                                                        .withSupplyCurrentLimit(30)
                                                        .withSupplyCurrentLowerTime(1)
                                                        .withSupplyCurrentLowerLimit(
                                                                        20)
                                                        .withSupplyCurrentLimitEnable(true))
                                        .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs()
                                                        .withKP(0.6).withKI(0).withKD(0.0)
                                                        .withKV(0.121))
                                        .withMotorOutput(
                                                        new com.ctre.phoenix6.configs.MotorOutputConfigs()
                                                                        .withNeutralMode(
                                                                                        com.ctre.phoenix6.signals.NeutralModeValue.Coast));

        /** The ShooterMec's front pose control type */
        private final PhoenixControlTypes.PoseControl frontPoseControlType =
                        PhoenixControlTypes.PoseControl.PositionVoltage;

        /** The ShooterMec's front open loop control type */
        private final PhoenixControlTypes.OpenLoopControl frontOpenLoopControlType =
                        PhoenixControlTypes.OpenLoopControl.Regular;

        /** The ShooterMec's front velocity control type */
        private final PhoenixControlTypes.VelocityControl frontVelocityControlType =
                        PhoenixControlTypes.VelocityControl.VelocityVoltage;

        /** The front's talon FX config constructor */
        public final TalonFXConfig config =
                        new TalonFXConfig(frc.robot.generated.ports.Ports.frontMotorPort,
                                        frontEnableFOC, frontMotorConfig, frontCanivore)
                                                        .withInverted(false);

        public TalonFXMotorConfig() {}
}
