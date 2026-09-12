package miscar.configs.motors;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.InvertedValue;
import frc.robot.Constants;
import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;
import miscar.motorIOs.MotorIOTalonFX;
import miscar.util.PhoenixControlTypes.OpenLoopControl;
import miscar.util.PhoenixControlTypes.PoseControl;
import miscar.util.PhoenixControlTypes.VelocityControl;

/**
 * A generic, abstract class for TalonFX motorIO configs. you can't
 * construct this class directly as it's abstract, see
 * {@link TalonFXMotorIOConfig} If you want to use this in your code
 */
@ConstantsName(constantsName = "talonFXMotorConfig")
public class TalonFXConfig extends MotorIOConfig {
  public final boolean enableFOC;
  public final boolean canivore;
  public final TalonFXConfiguration motorConfig;

  public PoseControl poseControlType = PoseControl.PositionVoltage;
  public OpenLoopControl openLoopControlType = OpenLoopControl.Regular;
  public VelocityControl velocityControlType = VelocityControl.VelocityVoltage;

  /**
   * @param motorPort - The port the motor is registered to
   * @param enableFOC - If we want to use FOC for this motor
   * @param motorConfig - The config for this talonFX motor,staff like
   *        gear ratio and max currant consumption
   * @param canivore - Whether or not the motor sits on the canivore net
   *        work
   * @param closedLoopControlType - The closed loop control type we want
   *        To use
   * @param openLoopControlType - The open loop control type we want to
   *        use
   */
  @ConstantsConstructor
  public TalonFXConfig(int motorPort, boolean enableFOC, TalonFXConfiguration motorConfig,
      boolean canivore) {
    super();
    this.motorPort = motorPort;
    this.enableFOC = Constants.boughtFOC ? enableFOC : false;
    this.motorConfig = motorConfig;
    this.canivore = canivore;

    this.withInverted(inverted);
  }


  public TalonFXConfig(TalonFXConfig config) {
    this(config.motorPort, config.enableFOC, config.motorConfig.clone(), config.canivore);
    this.withInverted(config.inverted).withMotorPort(config.motorPort);
  }

  @Override
  public TalonFXConfig withInverted(boolean inverted) {
    motorConfig.MotorOutput.Inverted =
        inverted ? InvertedValue.CounterClockwise_Positive : InvertedValue.Clockwise_Positive;

    super.withInverted(inverted);

    return this;
  }

  public TalonFXConfig withPoseControlType(PoseControl poseControlType) {
    this.poseControlType = poseControlType;
    return this;
  }

  public TalonFXConfig withVelocityControlType(VelocityControl velocityControlType) {
    this.velocityControlType = velocityControlType;
    return this;
  }

  public TalonFXConfig withOpenLoopControlType(OpenLoopControl openLoopControlType) {
    this.openLoopControlType = openLoopControlType;
    return this;
  }

  /**
   * @param motorPort - The new port this motor shall be configured to
   * @return This config with the new {@code motorPort}
   */
  @Override
  public TalonFXConfig withMotorPort(int motorPort) {
    this.motorPort = motorPort;
    return this;
  }

  /**
   * @return The {@code motorPort} this motor is configured to
   */
  public int getMotorPort() {
    return motorPort;
  }

  /**
   * @return The {@code CANBus} this motor is configured to
   */
  public String getCANBus() {
    return canivore ? "canivore" : "rio";
  }

  @Override
  public MotorIOTalonFX buildMotor() {
    return new MotorIOTalonFX(this);
  }

  @Override
  public TalonFXConfig clone() {
    // create a new empty TalonFXConfiguration object
    // TalonFXConfiguration newMotorConfig = new TalonFXConfiguration();

    // deep copy the motorConfig into the new motor config
    // newMotorConfig.deserialize(motorConfig.serialize());

    // instant a new config
    TalonFXConfig newConfig = new TalonFXConfig(this);

    // copy the control types
    newConfig.withPoseControlType(poseControlType);
    newConfig.withVelocityControlType(velocityControlType);
    newConfig.withOpenLoopControlType(openLoopControlType);

    return newConfig;
  }
}
