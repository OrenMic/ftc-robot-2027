package miscar.configs.motors;

import com.revrobotics.spark.config.SparkMaxConfig;
import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;
import miscar.motorIOs.MotorIOSpark;

/** A generic, class for Spark motorIO configs. */
@ConstantsName(constantsName = "sparkMotorConfig")
public class SparkConfig extends MotorIOConfig {
  public final SparkMaxConfig motorConfig;

  /**
   * @param encoderPort - The port the motor is registered to
   * @param motorConfig - The config for this Spark motor,staff like
   *        gear ratio and max currant consumption
   */
  @ConstantsConstructor
  public SparkConfig(int motorPort, SparkMaxConfig motorConfig) {
    super();
    this.motorPort = motorPort;
    this.motorConfig = motorConfig;
  }

  public SparkConfig(SparkConfig config) {
    super();
    this.motorPort = config.motorPort;
    this.motorConfig = new SparkMaxConfig().apply(config.motorConfig);
    this.withInverted(config.inverted).withMotorPort(config.motorPort);
  }

  @Override
  public SparkConfig withInverted(boolean inverted) {
    motorConfig.inverted(inverted);

    super.withInverted(inverted);
    return this;
  }

  @Override
  public SparkConfig withMotorPort(int port) {
    this.motorPort = port;
    return this;
  }

  /**
   * @return The {@code port} this motor is configured to
   */
  public int getPort() {
    return motorPort;
  }

  @Override
  public MotorIOSpark buildMotor() {
    return new MotorIOSpark(this);
  }

  @Override
  public SparkConfig clone() {
    SparkConfig newConfig = new SparkConfig(this);
    return newConfig;
  }
}
