package miscar.configs.motors;

import miscar.configs.Config;
import miscar.motorIOs.EmptyMotor;

/**
 * A config class to unify all miscar-motor-config classes, used
 * mainly for making sure the config passed to
 * {@link miscar.mecsIOs.MechanismIO} is for a motor
 */
public abstract class MotorIOConfig extends Config {

  protected boolean inverted = false;
  public int motorPort = -1;

  public MotorIOConfig withInverted(boolean inverted) {
    this.inverted = inverted;
    return this;
  }

  /**
   * @param motorPort - The new port this motor shall be configured to
   * @return This config with the new {@code motorPort}
   */
  public MotorIOConfig withMotorPort(int motorPort) {
    this.motorPort = motorPort;
    return this;
  }

  public boolean getInverted() {
    return inverted;
  }

  public EmptyMotor buildMotor() {
    return new EmptyMotor(-1);
  }
}
