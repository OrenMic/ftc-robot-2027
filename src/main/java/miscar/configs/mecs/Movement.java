package miscar.configs.mecs;

import miscar.annotation.ConstantsName;
import miscar.configs.Config;

@ConstantsName(constantsName = "mecMovement")
public class Movement extends Config {
  /** The maximum movement we allow to the mec in the mec's units */
  private double maxMovement = Double.POSITIVE_INFINITY;
  /** The minimum movement we allow to the mec in the mec's units */
  private double minMovement = Double.NEGATIVE_INFINITY;
  /** The maximum velocity we allow to the mec in the mec's units */
  private double maxVelocity = Double.POSITIVE_INFINITY;

  // @ConstantsConstructor
  public Movement() {}

  public Movement withMaxMovement(double maxMovement) {
    this.maxMovement = maxMovement;
    return this;
  }

  public Movement withMinMovement(double minMovement) {
    this.minMovement = minMovement;
    return this;
  }

  public Movement withMaxVelocity(double maxVelocity) {
    this.maxVelocity = maxVelocity;
    return this;
  }

  @Override
  public Movement clone() {
    return new Movement().withMaxMovement(maxMovement).withMinMovement(minMovement)
        .withMaxVelocity(maxVelocity);
  }
}
