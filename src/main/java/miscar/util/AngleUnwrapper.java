package miscar.util;

import java.util.function.DoubleSupplier;

public class AngleUnwrapper {
  private double lastAngle = 0.0; // last raw encoder reading
  private double continuousAngle = 0.0; // unwrapped -inf to +inf
  private DoubleSupplier poseSupplier;

  /**
   * @param poseSupplier - the angle's pose supplier [0,360)
   */
  public AngleUnwrapper(DoubleSupplier poseSupplier) {
    this.poseSupplier = poseSupplier;
    // lastAngle = poseSupplier.getAsDouble();
    // init();
  }

  public double calc() {
    // currentAngle is the raw encoder reading (0–360)
    double currentAngle = poseSupplier.getAsDouble();

    double delta = currentAngle - lastAngle;
    // delta *= -1;
    // Wrap delta into range (-180, 180]
    if (delta > 180) {
      delta -= 360;
    } else if (delta < -180) {
      delta += 360;
    }

    // Add the adjusted delta to the continuous angle
    continuousAngle += delta;
    lastAngle = currentAngle;

    return continuousAngle;
  }

  public double getLastGoodAngle() {
    return continuousAngle;
  }

  private void init() {
    double currentAngle = poseSupplier.getAsDouble();
    lastAngle = currentAngle;

    // double delta = currentAngle - lastAngle;

    // Add the adjusted delta to the continuous angle
    // continuousAngle += delta;
  }
}
