package frc.robot.subsystems.shooter.util;

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

    public static Pose2d getClosestShootingPose() {

        return getClosestShootingPoseFrom(Drive.getInstance().getPose().getTranslation());
    }

    public static Pose2d getClosestShootingPoseFrom(Translation2d pose) {
        Rotation2d angleToHub = getAngleToHubFrom(pose);
        double optimalDistance = SweetSpots.hubSweetSpots
                .getOptimalDistance(FieldConstants.getHivePose(pose).getDistance(pose));
        Translation2d wantedSpot = FieldConstants.getHivePose(pose)
                .minus(new Translation2d(optimalDistance, angleToHub));
        angleToHub = getAngleToHubFrom(wantedSpot);
        return new Pose2d(wantedSpot, angleToHub);
    }

    public static Rotation2d getAngleToHubFrom(Translation2d pose) {
        Translation2d v = FieldConstants.getHivePose(pose);
        Translation2d u = pose;
        Rotation2d angleToHub = new Rotation2d(Math.atan2(v.minus(u).getY(), v.minus(u).getX()));

        return angleToHub;
    }


    public static double getDistanceToHub() {
        return FieldConstants.getHivePose(Drive.getInstance().getPose().getTranslation())
                .getDistance(Drive.getInstance().getPose().getTranslation());
    }

    public static Rotation2d getAngleToHub() {
        return getAngleToHubFrom(Drive.getInstance().getPose().getTranslation());
    }


    public static Rotation2d getAngleToDeliveryLine() {
        return AllianceUtil.isRedAlliance() ? Rotation2d.kZero : Rotation2d.k180deg;
    }

    public static Translation2d getVirtualHubTargetPose() {
        return getVirtualTargetPose(
                FieldConstants.getHivePose(Drive.getInstance().getPose().getTranslation()),
                ShootingInMotion.hubTof);
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
}
