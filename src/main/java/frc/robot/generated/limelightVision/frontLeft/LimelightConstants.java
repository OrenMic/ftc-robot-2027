package frc.robot.generated.limelightVision.frontLeft;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.util.Units;
import miscar.annotation.DoNotOverride;
import miscar.configs.vision.LimelightConfig;

@DoNotOverride
public class LimelightConstants {
  /**
   * The LimelightVision's front left use mega bot pose2
   */
  private final boolean frontLeftUseMegaBotPose2 = true;

  /**
   * The LimelightVision's front left use mega bot pose1
   */
  private final boolean frontLeftUseMegaBotPose1 = true;

  /**
   * The LimelightVision's front left name
   */
  private final String frontLeftName = "limelight-left";

  /**
   * The LimelightVision's front left std dev factors
   */
  private final double frontLeftStdDevFactors = 1;

  /**
   * The LimelightVision's front left robot to camera
   */
  private final Pose3d frontLeftRobotToCamera =
      new Pose3d(0.241261091189, 0.261995412655, 0.426621600226, new Rotation3d(
          Units.degreesToRadians(0), Units.degreesToRadians(30), Units.degreesToRadians(15)));


  /**
   * The front left's limelight config constructor
   */
  public final LimelightConfig config =
      new LimelightConfig(frontLeftName, frontLeftStdDevFactors, frontLeftRobotToCamera)
          .enableMegaBotPose1(frontLeftUseMegaBotPose1)
          .enableMegaBotPose2(frontLeftUseMegaBotPose2);

  public LimelightConstants() {}
}
