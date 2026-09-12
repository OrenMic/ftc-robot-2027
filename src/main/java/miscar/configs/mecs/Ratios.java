package miscar.configs.mecs;

import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;
import miscar.configs.Config;
import miscar.util.MountedMec;

@ConstantsName(constantsName = "mecRatios")
public class Ratios extends Config {

  /** See {@link MountedMec} for more */
  private final double motorToMecRatio;

  /**
   * @param motorToMecRatio - See {@link MountedMec} for more
   */
  @ConstantsConstructor
  public Ratios(double motorToMecRatio) {
    if (motorToMecRatio <= 0) {
      throw new IllegalArgumentException("motorToMecRatio must be positive and non-zero");
    }
    this.motorToMecRatio = motorToMecRatio;
  }

  /**
   * @return The {@code motorToMecRatio}, see {@link MountedMec} for
   *         more
   */
  public double getMotorToMecRatio() {
    return motorToMecRatio;
  }

  @Override
  public Ratios clone() {
    return new Ratios(getMotorToMecRatio());
  }
}
