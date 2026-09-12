package frc.robot.util;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import miscar.util.AllianceUtil;

public class FieldConstants {
  public static final double width = 8.07;
  public static final double length = 16.54;
  private static final double rightBlueTrenchY = 0.665988;

  public static final Translation2d redHub =
      new Translation2d(length - Units.inchesToMeters(182), width - Units.inchesToMeters(158));
  public static final Translation2d blueHub =
      new Translation2d(Units.inchesToMeters(182), Units.inchesToMeters(158));

  public static final double hubWidth = Units.inchesToMeters(47);

  public static final double redDeliveryLine = length - (4.622);
  public static final double blueDeliveryLine = 4.622;

  public static final Bounds blueTrenchBound = new Bounds(4.650, 6.250, 0, 1.200);

  public static final Translation2d getLeftTrench() {
    return getDeliveryLinePose(width - rightBlueTrenchY);
  }

  public static final Translation2d getRightTrench() {
    return getDeliveryLinePose(rightBlueTrenchY);
  }

  public static Translation2d getHubPose() {
    return AllianceUtil.isRedAlliance() ? redHub : blueHub;
  }

  public static double getDeliveryLine() {
    return AllianceUtil.isRedAlliance() ? redDeliveryLine : blueDeliveryLine;
  }

  public static Translation2d getDeliveryLinePose(double inY) {
    return new Translation2d(AllianceUtil.isRedAlliance() ? redDeliveryLine : blueDeliveryLine,
        inY);
  }
}
