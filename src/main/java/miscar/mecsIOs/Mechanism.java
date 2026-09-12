package miscar.mecsIOs;

import miscar.configs.motors.MotorIOConfig;
import miscar.motorIOs.EmptyMotor;

/**
 * This is a class for mechanisms that do not have an external pose
 * encoder (encoder etc). If your mechanism has an external pose
 * encoder you would want to use {@link MechanismEncodered}
 */
public class Mechanism extends AbstractMechanism {

  /**
   * @param motorIODelegation - The motor IO class delegation, used to
   *        preform function calls for the motorIO
   */
  protected Mechanism(EmptyMotor motorIODelegation) {
    super(motorIODelegation);
  }

  protected Mechanism() {
    super();
  }

  /**
   * @param config - The motor config this mechanism will use.
   * @return A new Mechanism object.
   */
  public static Mechanism create(MotorIOConfig config) {
    return new Mechanism(config.buildMotor());
  }

  /**
   * Creates a new Mechanism with an empty motor
   *
   * @return the new {@code Mechanism}
   */
  public static Mechanism empty() {
    // used to make creating an empty mec easier
    return new Mechanism();
  }

  // @Override

}
