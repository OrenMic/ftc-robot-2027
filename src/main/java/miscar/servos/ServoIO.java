package miscar.servos;

import org.littletonrobotics.junction.AutoLog;

public interface ServoIO {

  @AutoLog
  public class ServoIOInputs {
    /**
     * The servo's normalized position, ranging from 0.0 (minimum) to 1.0
     * (maximum).
     */
    double rangedPose = -1;
    /** The servo's position as a raw pulse width value. */
    double pulseWidthPose = -1;
    /**
     * The servo's position in degrees (for {@link RevServoIO}s only if
     * {@link #setAngleRange(double)} has been previously called).
     */
    double anglePose = -1;
    /**
     * The current draw of the servo in amps. (will be -1 if using
     * {@link WpiServoIO} because there is no way to access the servo's
     * current draw data for servos connected directly to the RoboRIO)
     */
    double current = -1;
  }

  /**
   * Updates the provided ServoInputs object with the latest sensor
   * values from the servo.
   *
   * @param inputs The ServoInputs object to populate with current
   *        values.
   */
  public default void updateInputs(ServoIOInputs inputs) {}

  /**
   * Sets the servo's position in degrees. Only works if setAngleRange()
   * has been called.
   *
   * @param angle The desired angle in degrees.
   */
  public default void setAngle(double angle) {}

  /**
   * Sets the servo's position as a normalized value from 0 to 1.
   *
   * @param pose The desired position, where 0 is minimum and 1 is
   *        maximum.
   */
  public default void setRangedPose(double pose) {}

  /**
   * Sets the servo's position using a raw pulse width value.
   *
   * @param pulseWidthPose The desired pulse width value.
   */
  public default void setPulseWidthPose(int pulseWidthPose) {}

  /**
   * Sets the minimum and maximum pulse width range for the servo.
   *
   * <p>
   *
   * @param min The minimum pulse width.
   * @param max The maximum pulse width.
   * @param deadband The deadband of the servo.
   * @implNote The default values are 500 for min and 2500 for max.
   */
  public default void setPulseWidthRange(int min, int max, int deadband) {}

  /**
   * Sets the maximum angle for the servo. (for {@link RevServoIO} Must
   * be called before using {@link #setAngle(double)}, for
   * {@link WpiServoIO} calling this function will simply limit the
   * servo's range and is unnecessary).
   *
   * @param maxAngle The maximum angle in degrees.
   */
  public default void setAngleRange(double maxAngle) {}

  /** Turns the servo on, enabling output. */
  public default void turnOn() {}

  /** Turns the servo off, disabling output. */
  public default void turnOff() {}

  /**
   * Returns whether the servo is currently on.
   *
   * @return true if the servo is on, false otherwise.
   */
  public default boolean isOn() {
    return false;
  }
}
