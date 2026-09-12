package frc.robot.util;

import java.util.ArrayList;
import java.util.List;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import lombok.experimental.ExtensionMethod;
import miscar.util.AllianceUtil;

@ExtensionMethod(value = GeomUtil.class)
public class FieldConstants {
  public static final double width = Units.inchesToMeters(144);
  public static final double length = width;
  private static double hiveHight = 1.369;

  public static Translation3d blueRightHive = new Translation3d(1.494, 1.510, hiveHight);
  public static Translation3d redRightHive = new Translation3d(2.142, 2.127, hiveHight);
  public static Translation3d blueLeftHive = new Translation3d(1.494, 2.127, hiveHight);
  public static Translation3d redLeftHive = new Translation3d(2.142, 1.510, hiveHight);
  public static List<Translation2d> redHives = new ArrayList<>();
  public static List<Translation2d> blueHives = new ArrayList<>();

  static {
    redHives.add(redRightHive.toTranslation2d());
    redHives.add(redLeftHive.toTranslation2d());

    blueHives.add(blueRightHive.toTranslation2d());
    blueHives.add(blueLeftHive.toTranslation2d());
  }

  public static Translation2d getHivePose(Translation2d robotPose) {
    return robotPose.nearest(AllianceUtil.isRedAlliance() ? redHives : blueHives);
  }

  public static Translation3d getHivePose3d(Translation2d robotPose) {
    var pose = getHivePose(robotPose);
    return new Translation3d(pose.getX(), pose.getY(), hiveHight);
  }
}
