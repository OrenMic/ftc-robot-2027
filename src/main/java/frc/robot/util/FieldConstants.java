package frc.robot.util;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;

public class FieldConstants {
  public static final double width = Units.inchesToMeters(144);
  public static final double length = width;
  private static double hiveHight = 1.369;

  public static Translation3d blueRightHive = new Translation3d(1.456, 1.472, hiveHight);
  public static Translation3d redRightHive = new Translation3d(2.103, 2.089, hiveHight);
  public static Translation3d blueLeftHive = new Translation3d(1.456, 2.089, hiveHight);
  public static Translation3d redLeftHive = new Translation3d(2.103, 1.472, hiveHight);

  public static Translation2d getHivePose() {
    return blueRightHive.toTranslation2d();
  }
}
