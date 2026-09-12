package frc.robot.subsystems.shooter.util;

import static miscar.util.AllianceUtil.isRedAlliance;
import java.util.ArrayList;
import java.util.List;
import org.littletonrobotics.junction.Logger;
import static frc.robot.Tuners.Drive.*;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.util.DriveUtil;
import frc.robot.subsystems.shooter.shootingModel.ShootingInMotion;
import frc.robot.subsystems.shooter.shootingModel.ShootingInMotion.Tof;
import frc.robot.subsystems.shooter.shootingModel.SweetSpots;
import frc.robot.util.Bounds.FlipAxis;
import frc.robot.util.FieldConstants;
import frc.robot.util.GeomUtil;
import frc.robot.util.LoggedTunableNumber;
import lombok.experimental.ExtensionMethod;
import miscar.util.AllianceUtil;

@ExtensionMethod(GeomUtil.class)
public class ShootingUtil {

    static String name = "ShootingInMotion";
    private static LoggedTunableNumber repetitions =
            new LoggedTunableNumber(name + "/Repetitions", 10, true);


    public static double getDistanceToVirtualHub() {
        return Drive.getInstance().getPose().getTranslation()
                .getDistance(getVirtualHubTargetPose());
    }

    public static double getDistanceToVirtualDeliveryLine() {

        return Drive.getInstance().getPose().getTranslation()
                .getDistance(getVirtualDeliveryTargetPose());
    }

    public static Pose2d getClosestShootingPose() {

        return getClosestShootingPoseFrom(Drive.getInstance().getPose().getTranslation());
    }

    public static Pose2d getClosestShootingPoseFrom(Translation2d pose) {
        Rotation2d angleToHub = getAngleToHubFrom(pose);
        double optimalDistance = SweetSpots.hubSweetSpots
                .getOptimalDistance(FieldConstants.getHubPose().getDistance(pose));
        Translation2d wantedSpot =
                FieldConstants.getHubPose().minus(new Translation2d(optimalDistance, angleToHub));
        // update shooting spot based on allowed areas
        wantedSpot = getClosestAllowedShootingPoseFrom(wantedSpot);
        angleToHub = getAngleToHubFrom(wantedSpot);
        return new Pose2d(wantedSpot, angleToHub);
    }

    public static Rotation2d getAngleToHubFrom(Translation2d pose) {
        Translation2d v = FieldConstants.getHubPose();
        Translation2d u = pose;
        Rotation2d angleToHub = new Rotation2d(Math.atan2(v.minus(u).getY(), v.minus(u).getX()));

        return angleToHub;
    }

    private static Translation2d getClosestAllowedShootingPoseFrom(Translation2d pose) {
        Translation2d topTrench = FieldConstants.getLeftTrench();
        Translation2d bottomTrench = FieldConstants.getRightTrench();
        Translation2d offset = new Translation2d(1, 0);
        if (AllianceUtil.isRedAlliance()
                && pose.minus(offset).getX() < FieldConstants.getDeliveryLine()) {
            Translation2d closestTrench =
                    pose.getDistance(topTrench) > pose.getDistance(bottomTrench) ? bottomTrench
                            : topTrench;
            pose = closestTrench.plus(offset);
        } else if (!AllianceUtil.isRedAlliance()
                && pose.plus(offset).getX() > FieldConstants.getDeliveryLine()) {
            Translation2d closestTrench =
                    pose.getDistance(topTrench) > pose.getDistance(bottomTrench) ? bottomTrench
                            : topTrench;
            pose = closestTrench.minus(offset);
        }

        return pose;
    }


    public static double getDistanceToHub() {
        return FieldConstants.getHubPose()
                .getDistance(Drive.getInstance().getPose().getTranslation());
    }

    public static Rotation2d getAngleToHub() {
        return getAngleToHubFrom(Drive.getInstance().getPose().getTranslation());
    }


    public static Rotation2d getAngleToDeliveryLine() {
        return AllianceUtil.isRedAlliance() ? Rotation2d.kZero : Rotation2d.k180deg;
    }

    public static double getDistanceToDeliveryLine() {
        return Math.abs(FieldConstants.getDeliveryLine() - Drive.getInstance().getPose().getX());
    }

    public static Translation2d getVirtualHubTargetPose() {
        return getVirtualTargetPose(FieldConstants.getHubPose(), ShootingInMotion.hubTof);
    }

    public static Translation2d getVirtualDeliveryTargetPose() {
        return getVirtualTargetPose(
                FieldConstants.getDeliveryLinePose(Drive.getInstance().getPose().getY()),
                ShootingInMotion.deliveryTof).withY(Drive.getInstance().getPose().getY());
    }

    public static Translation2d getVirtualTargetPose(Translation2d originalTarget,
            Tof tofCalculator) {
        ChassisSpeeds robotVelocity =
                ChassisSpeeds.fromRobotRelativeSpeeds(Drive.getInstance().getChassisSpeed(),
                        Drive.getInstance().getRotation());
        Translation2d robotVelocityTranslation =
                new Translation2d(robotVelocity.vxMetersPerSecond, robotVelocity.vyMetersPerSecond);
        double airTime;
        Translation2d virtualTarget = originalTarget;
        List<Pose2d> virtualHubs = new ArrayList<>();
        for (int i = 0; i < repetitions.getAsDouble(); i++) {
            airTime = tofCalculator
                    .get(Drive.getInstance().getPose().getTranslation().getDistance(virtualTarget));
            virtualTarget = originalTarget.minus(robotVelocityTranslation.times(airTime));
            virtualHubs.add(virtualTarget.toPose2d());
        }
        Logger.recordOutput(name + "/virtualTargets", virtualHubs.toArray(Pose2d[]::new));


        return virtualTarget;
    }

    public static boolean isRobotAlinedToShoot() {
        return getErrorToVirtualHub() < shootTolerance.getAsDouble();
    }

    public static double getErrorToVirtualHub() {
        Translation2d target = ShootingUtil.getVirtualHubTargetPose();
        double omegaError = DriveUtil.getAngleToPose(target)
                .minus(Drive.getInstance().getPose().getRotation()).getRadians();

        double error = ShootingUtil.getDistanceToVirtualHub() * Math.tan(omegaError);
        return Math.abs(error);
    }

    public static Rotation2d getAngleToVirtualHub() {
        Logger.recordOutput("ShootingInMotion" + "/angleToHUb",
                DriveUtil.getAngleToPose(getVirtualHubTargetPose()));
        return DriveUtil.getAngleToPose(getVirtualHubTargetPose());
    }

    public static Rotation2d getAngleToVirtualDeliveryLine() {
        Logger.recordOutput("ShootingInMotion" + "/angleToDeliveryLine",
                DriveUtil.getAngleToPose(getVirtualDeliveryTargetPose()));
        return DriveUtil.getAngleToPose(getVirtualDeliveryTargetPose());
    }

    public static boolean canDeliver() {

        double robotY = Drive.getInstance().getPose().getY();
        double hubY = FieldConstants.getHubPose().getY();
        double hubWidth = FieldConstants.hubWidth;
        double lowYLimit = hubY - (hubWidth / 2.0);
        double highYLimit = hubY + (hubWidth / 2.0);

        boolean isBlockedByTrench = isRedAlliance()
                ? FieldConstants.blueTrenchBound.containsFlipped(Drive.getInstance().getPose(),
                        FlipAxis.X)
                        || FieldConstants.blueTrenchBound
                                .containsFlipped(Drive.getInstance().getPose(), FlipAxis.XY)
                : FieldConstants.blueTrenchBound.contains(Drive.getInstance().getPose())
                        || FieldConstants.blueTrenchBound
                                .containsFlipped(Drive.getInstance().getPose(), FlipAxis.Y);

        return (robotY < lowYLimit || robotY > highYLimit) && !isBlockedByTrench;
    }

    public static Pose2d getClosestDeliveryPose() {
        Rotation2d angleToDeliveryLine = getAngleToDeliveryLine();
        double optimalDistance = SweetSpots.deliverySweetSpots.getOptimalDistance();
        Translation2d wantedSpot = new Translation2d(FieldConstants.getDeliveryLine(),
                Drive.getInstance().getPose().getY())
                        .minus(new Translation2d(optimalDistance, angleToDeliveryLine));

        return new Pose2d(wantedSpot, angleToDeliveryLine);
    }



}
