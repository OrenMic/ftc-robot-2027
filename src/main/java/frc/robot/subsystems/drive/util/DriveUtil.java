package frc.robot.subsystems.drive.util;

import static frc.robot.Tuners.Drive.rotationalTolerance;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import frc.robot.subsystems.drive.Drive;

public class DriveUtil {
    public static Rotation2d getAngleToPose(Translation2d pose) {
        Translation2d v = pose;
        Translation2d u = Drive.getInstance().getPose().getTranslation();
        Rotation2d angleToHub = new Rotation2d(Math.atan2(v.minus(u).getY(), v.minus(u).getX()));

        return angleToHub;
    }

    public static boolean isRobotInPose() {
        Pose2d currentPose = Drive.getInstance().getPose(), lockPose = Drive.getLockPose();

        double xError = currentPose.minus(lockPose).getX();
        double yError = currentPose.minus(lockPose).getY();
        double omegaError = currentPose.getRotation().minus(lockPose.getRotation()).getDegrees();
        boolean isInPose =
                (Math.abs(xError) < 0.1 && Math.abs(yError) < 0.1 && Math.abs(omegaError) < 0.5);
        double val = Math.hypot(Drive.getInstance().getChassisSpeed().vxMetersPerSecond,
                Drive.getInstance().getChassisSpeed().vyMetersPerSecond);
        return isInPose && val < 0.4;
    }

    public static boolean isRobotInTransition() {
        Pose2d currentPose = Drive.getInstance().getPose(), lockPose = Drive.getLockPose();
        double xError = currentPose.minus(lockPose).getX();
        double yError = currentPose.minus(lockPose).getY();
        boolean isInTransition = (Math.abs(xError) < 0.1 && Math.abs(yError) < 0.1);
        return isInTransition;
    }

    public static boolean isRobotCloseInTransition() {
        Pose2d currentPose = Drive.getInstance().getPose(), lockPose = Drive.getLockPose();
        double xError = currentPose.minus(lockPose).getX();
        double yError = currentPose.minus(lockPose).getY();
        boolean isInTransition = (Math.abs(xError) < 0.3 && Math.abs(yError) < 0.3);
        return isInTransition;
    }

    public static boolean isRobotInAngle() {

        Pose2d currentPose = Drive.getInstance().getPose(), lockPose = Drive.getLockPose();
        double omegaError = currentPose.getRotation().minus(lockPose.getRotation()).getDegrees();

        return Math.abs(omegaError) < rotationalTolerance.getAsDouble();
    }


}
