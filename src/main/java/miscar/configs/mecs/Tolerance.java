package miscar.configs.mecs;

import java.util.OptionalDouble;
import miscar.annotation.ConstantsName;
import miscar.configs.Config;

@ConstantsName(constantsName = "mecTolerance")
public class Tolerance extends Config {

  private OptionalDouble allowedMecPositionError = OptionalDouble.empty();
  private OptionalDouble allowedMecVelocityError = OptionalDouble.empty();

  /**
   * @param allowedMecPositionError - The new
   *        {@code allowedMecPositionError}
   * @return - This object with the new {@code allowedMecPositionError}
   */
  public Tolerance withAllowedMecPositionError(double allowedMecPositionError) {
    this.allowedMecPositionError = OptionalDouble.of(allowedMecPositionError);
    return this;
  }

  /**
   * @param allowedMecPositionError - The new
   *        {@code allowedMecPositionError}
   * @return - This object with the new {@code allowedMecPositionError}
   */
  public Tolerance withAllowedMecPositionError(OptionalDouble allowedMecPositionError) {
    this.allowedMecPositionError = allowedMecPositionError;
    return this;
  }

  /**
   * @param allowedMecVelocityError - The new
   *        {@code allowedMecVelocityError}
   * @return - This object with the new {@code allowedMecVelocityError}
   */
  public Tolerance withAllowedMecVelocityError(double allowedMecVelocityError) {
    this.allowedMecVelocityError = OptionalDouble.of(allowedMecVelocityError);
    return this;
  }

  /**
   * @param allowedMecVelocityError - The new
   *        {@code allowedMecVelocityError}
   * @return - This object with the new {@code allowedMecVelocityError}
   */
  public Tolerance withAllowedMecVelocityError(OptionalDouble allowedMecVelocityError) {
    this.allowedMecVelocityError = allowedMecVelocityError;
    return this;
  }

  /**
   * @return The {@code allowedMecPositionError}
   */
  public OptionalDouble getAllowedMecPositionError() {
    return allowedMecPositionError;
  }

  /**
   * @return The {@code allowedMecVelocityError}
   */
  public OptionalDouble getAllowedMecVelocityError() {
    return allowedMecVelocityError;
  }

  public Tolerance clone() {
    Tolerance newConfig = new Tolerance();
    newConfig.withAllowedMecPositionError(getAllowedMecPositionError());
    newConfig.withAllowedMecVelocityError(getAllowedMecVelocityError());
    return newConfig;
  }
}
