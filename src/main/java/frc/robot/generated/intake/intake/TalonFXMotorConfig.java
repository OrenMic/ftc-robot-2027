package frc.robot.generated.intake.intake;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import miscar.annotation.DoNotOverride;
import miscar.configs.motors.TalonFXConfig;
import miscar.util.PhoenixControlTypes;

@DoNotOverride
public class TalonFXMotorConfig {
        /** The Intake's intake enable FOC */
        private final boolean intakeEnableFOC = false;

        /** The Intake's intake canivore */
        private final boolean intakeCanivore = false;

        /** The Intake's intake motor config */
        private final TalonFXConfiguration intakeMotorConfig = new TalonFXConfiguration()
                        .withCurrentLimits(new CurrentLimitsConfigs()
                                        .withSupplyCurrentLowerLimit(40)
                                        .withSupplyCurrentLowerTime(0.5).withSupplyCurrentLimit(45)
                                        .withStatorCurrentLimitEnable(true))
                        .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs().withKP(0.45)
                                        .withKI(0).withKD(0).withKV(0.135))
                        .withSlot1(new com.ctre.phoenix6.configs.Slot1Configs().withKP(0.3)
                                        .withKI(0).withKD(0).withKV(0.135))
                        .withMotorOutput(new com.ctre.phoenix6.configs.MotorOutputConfigs()
                                        .withNeutralMode(
                                                        com.ctre.phoenix6.signals.NeutralModeValue.Coast));

        /** The Intake's intake pose control type */
        private final PhoenixControlTypes.PoseControl intakePoseControlType =
                        PhoenixControlTypes.PoseControl.PositionVoltage;

        /** The Intake's intake open loop control type */
        private final PhoenixControlTypes.OpenLoopControl intakeOpenLoopControlType =
                        PhoenixControlTypes.OpenLoopControl.Regular;

        /** The Intake's intake velocity control type */
        private final PhoenixControlTypes.VelocityControl intakeVelocityControlType =
                        PhoenixControlTypes.VelocityControl.VelocityVoltage;

        /** The intake's talon FX config constructor */
        public final TalonFXConfig config =
                        new TalonFXConfig(frc.robot.generated.ports.Ports.intakeMotorPort,
                                        intakeEnableFOC, intakeMotorConfig, intakeCanivore)
                                                        .withInverted(true);

        public TalonFXMotorConfig() {}
}
