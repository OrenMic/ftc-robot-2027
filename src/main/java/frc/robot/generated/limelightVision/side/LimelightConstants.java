package frc.robot.generated.limelightVision.side;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.util.Units;
import miscar.annotation.DoNotOverride;
import miscar.configs.vision.LimelightConfig;

@DoNotOverride
public class LimelightConstants {
  /**
   * The LimelightVision's side use mega bot pose2
   */
  private final boolean sideUseMegaBotPose2 = true;

  /**
   * The LimelightVision's side use mega bot pose1
   */
  private final boolean sideUseMegaBotPose1 = true;

  /**
   * The LimelightVision's side name
   */
  private final String sideName = "limelight-side";

  /**
   * The LimelightVision's side std dev factors
   */
  private final double sideStdDevFactors = 1;

  /**
   * /** The LimelightVision's back left robot to camera
   */
  public final Pose3d backLeftRobotToCamera =
      new Pose3d(0.0631, -0.264, 0.3312, new Rotation3d(Units.degreesToRadians(0),
          Units.degreesToRadians(22.49), Units.degreesToRadians(115.73)));

  /**
   * The LimelightVision's back right robot to camera
   */
  public final Pose3d backRightRobotToCamera = new Pose3d(0.2694, 0.257777, 0.33,
      new Rotation3d(0, Units.degreesToRadians(22.49), Units.degreesToRadians(-115.73)));



  /**
   * The side's limelight config constructor
   */
  public final LimelightConfig config =
      new LimelightConfig(sideName, sideStdDevFactors, backLeftRobotToCamera)
          .enableMegaBotPose1(sideUseMegaBotPose1).enableMegaBotPose2(sideUseMegaBotPose2);

  public LimelightConstants() {}
}
