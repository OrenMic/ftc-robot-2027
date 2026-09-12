package frc.robot.generated.limelightVision.frontRight;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.util.Units;
import miscar.annotation.DoNotOverride;
import miscar.configs.vision.LimelightConfig;

@DoNotOverride
public class LimelightConstants {
  /**
   * The LimelightVision's front right use mega bot pose2
   */
  private final boolean frontRightUseMegaBotPose2 = true;

  /**
   * The LimelightVision's front right use mega bot pose1
   */
  private final boolean frontRightUseMegaBotPose1 = true;

  /**
   * The LimelightVision's front right name
   */
  private final String frontRightName = "limelight-right";

  /**
   * The LimelightVision's front right std dev factors
   */
  private final double frontRightStdDevFactors = 1;

  /**
   * The LimelightVision's front right robot to camera
   */
  private final Pose3d frontRightRobotToCamera =
      new Pose3d(0.241617143969, 0.261995412655, 0.426621600226, new Rotation3d(
          Units.degreesToRadians(0), Units.degreesToRadians(30), Units.degreesToRadians(15)));


  /**
   * The front right's limelight config constructor
   */
  public final LimelightConfig config =
      new LimelightConfig(frontRightName, frontRightStdDevFactors, frontRightRobotToCamera)
          .enableMegaBotPose1(frontRightUseMegaBotPose1)
          .enableMegaBotPose2(frontRightUseMegaBotPose2);

  public LimelightConstants() {}
}
