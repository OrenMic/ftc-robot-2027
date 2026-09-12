package frc.robot.generated;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;

public class SeasonConfigs {

  public static class VisionConstants {
    public static AprilTagFieldLayout aprilTagLayout =
        AprilTagFieldLayout.loadField(AprilTagFields.kDefaultField);

    public static double maxAmbiguity = 0.3;
    public static double maxZError = 0.75;

    public static double linearStdDevBaseline = 1; // 0.02; // Meters
    public static double angularStdDevBaseline = 1; // 0.06; // Radians

    // Multipliers to apply for MegaTag 2 observations
    // More stable than full 3D solve
    public static double linearStdDevMegatag2Factor = 0.3; // 0.5;

    // No rotation data available
    public static double angularStdDevMegatag2Factor = Double.POSITIVE_INFINITY;
  }
}
