package frc.robot.subsystems.shooter.shootingModel;

import edu.wpi.first.math.interpolation.Interpolatable;
import frc.robot.util.MisCarInterpolatingTreeMap;
import frc.robot.util.MisMath;
import java.util.function.DoubleSupplier;
import static frc.robot.util.MisMath.*;

public class DataTypes {

  public static record SweetSpot(double backVelocity, double frontVelocity, double indexerVelocity)
      implements Interpolatable<SweetSpot> {

    @Override
    public SweetSpot interpolate(SweetSpot endValue, double t) {
      return new SweetSpot(lerp(backVelocity, endValue.backVelocity, t),
          lerp(frontVelocity, endValue.frontVelocity, t),
          lerp(indexerVelocity, endValue.indexerVelocity, t));
    }

    public SweetSpot(double backVelocity, double frontVelocity) {
      this(backVelocity, frontVelocity, 1935.4838709677419355);
    }

    public SweetSpot addOffsets(double backVelocityOffset, double frontVelocityOffset,
        double indexerVelocityOffset) {
      return new SweetSpot(backVelocity + backVelocityOffset, frontVelocity + frontVelocityOffset,
          indexerVelocity + indexerVelocityOffset);
    }

    public boolean isKZero() {
      return this.equals(kZero);
    }

    public static SweetSpot kZero = new SweetSpot(0, 0);
  }



  public static class ShootingVelocities extends MisCarInterpolatingTreeMap<Double, SweetSpot> {

    // private static LoggedTunableNumber distanceTuner =
    // new LoggedTunableNumber("ShootingVelocities/distance", 2, false);

    public final DoubleSupplier distanceSupplier;

    public ShootingVelocities(DoubleSupplier distanceSupplier) {
      super(MisMath::doubleInverseLerp, // Double inverse interpolation
          (start, end, t) -> start.interpolate(end, t) // SweetSpot interpolation
      );
      this.distanceSupplier = distanceSupplier;
    }

    public SweetSpot InterpolateOptimalVelocities() {
      return super.get(distanceSupplier.getAsDouble());
    }

    public SweetSpot getOptimalVelocities() {
      return get(getOptimalDistance());
    }

    public double getOptimalDistance() {
      return getOptimalDistance(distanceSupplier.getAsDouble());
    }

    /**
     * Returns the closest key in the map (i.e if the map as only the keys
     * {1,4,9} and you search 8, it will return 9)
     *
     * @param wantedKey - The wanted key
     * @return The closest key found in the map
     */
    public double getOptimalDistance(double wantedKey) {
      double smallestDifference = Double.POSITIVE_INFINITY;
      double closestKey = Double.NaN;

      for (Double key : m_map.keySet()) {
        double diff = Math.abs(wantedKey - key);
        if (diff < smallestDifference) {
          smallestDifference = diff;
          closestKey = key;
        }
      }
      return closestKey;
    }

  }
}
