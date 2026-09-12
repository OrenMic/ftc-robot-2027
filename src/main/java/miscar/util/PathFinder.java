package miscar.util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import frc.robot.util.FieldConstants;

public class PathFinder {
  public static boolean willCollide(Translation2d start, Translation2d end) {
    return isInAllianceZone(start) != isInAllianceZone(end);
  }

  public static boolean isInAllianceZone(Translation2d pose) {
    return AllianceUtil.isRedAlliance() ? FieldConstants.redDeliveryLine < pose.getX()
        : FieldConstants.blueDeliveryLine > pose.getX();
  }

  public static boolean isInAllianceZone(Pose2d pose) {
    return isInAllianceZone(pose.getTranslation());
  }
}
