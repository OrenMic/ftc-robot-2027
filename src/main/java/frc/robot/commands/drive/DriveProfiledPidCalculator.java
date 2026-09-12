package frc.robot.commands.drive;

import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.math.util.Units;
import frc.robot.subsystems.drive.Drive;
import frc.robot.util.GeomUtil;
import frc.robot.util.LoggedTunableNumber;
import miscar.util.ProfiledPIDControllerUtil;

public class DriveProfiledPidCalculator {
  private static boolean tune = true;

  private static LoggedTunableNumber omegaKFF =
      new LoggedTunableNumber("DriveProfiledPidCalculator/omegaFF", 0, tune);
  private static LoggedTunableNumber kp, ki, kd;
  private static LoggedTunableNumber xPowerTolorance =
      new LoggedTunableNumber("DriveProfiledPidCalculator/xPowerTolorance", 0.02, tune),
      yPowerTolorance =
          new LoggedTunableNumber("DriveProfiledPidCalculator/yPowerTolorance", 0.02, tune),
      omegaPowerTolorance =
          new LoggedTunableNumber("DriveProfiledPidCalculator/omegaPowerTolorance", 0.05, tune);
  private static LoggedNetworkBoolean tuneTranslational;
  private static LoggedNetworkBoolean tuneRotational;
  private static double prevTargetAngle;

  private static LoggedNetworkBoolean tuneConstants =
      new LoggedNetworkBoolean("DriveProfiledPidCalculator/Constraints/tuneConstants", false);
  private static LoggedTunableNumber maxVelConstraints_x =
      new LoggedTunableNumber("DriveProfiledPidCalculator/Constraints/maxVel/x", 0.5, tune);
  private static LoggedTunableNumber maxAccelConstraints_x =
      new LoggedTunableNumber("DriveProfiledPidCalculator/Constraints/maxAcc/x", 0.5, tune);
  private static LoggedTunableNumber maxVelConstraints_y =
      new LoggedTunableNumber("DriveProfiledPidCalculator/Constraints/maxVel/y", 2, tune);
  private static LoggedTunableNumber maxAccelConstraints_y =
      new LoggedTunableNumber("DriveProfiledPidCalculator/Constraints/maxAcc/y", 0.5, tune);

  static {
    kp = new LoggedTunableNumber("DriveProfiledPidCalculator/pid/kp", 0, tune);
    ki = new LoggedTunableNumber("DriveProfiledPidCalculator/pid/ki", 0, tune);
    kd = new LoggedTunableNumber("DriveProfiledPidCalculator/pid/kd", 0, tune);


    tuneTranslational =
        new LoggedNetworkBoolean("DriveProfiledPidCalculator/pid/tuneTranslational", false);
    tuneRotational =
        new LoggedNetworkBoolean("DriveProfiledPidCalculator/pid/tuneRotational", false);


    prevTargetAngle = 0;
  }

  // @Getter
  // private static PidState pidState = PidState.NORMAL;

  private static ProfiledPIDControllerUtil translationalController_x =
      new ProfiledPIDControllerUtil(2.1, 0, 0, null);
  private static ProfiledPIDControllerUtil translationalController_y =
      translationalController_x.clone();

  static {
    translationalController_y.setConstraints(
        new Constraints(maxVelConstraints_y.getAsDouble(), maxAccelConstraints_y.getAsDouble()));
    translationalController_x.setConstraints(
        new Constraints(maxVelConstraints_x.getAsDouble(), maxAccelConstraints_x.getAsDouble()));
  }

  private static PIDController rotationalController = new PIDController(2.8, 0.06, 0.0);
  static {
    rotationalController.enableContinuousInput(-Math.PI, Math.PI);
  }

  public static void initialize() {
    resetTranslation();
    resetRotational();
  }

  private static void resetTranslation() {
    Translation2d translation2d = Drive.getInstance().getPose().getTranslation();
    translationalController_x.reset(translation2d.getX());
    translationalController_y.reset(translation2d.getY());
  }

  private static void resetRotational() {
    rotationalController.reset();
  }


  private static void updateTranslationalPid() {
    translationalController_x.setP(kp.getAsDouble());
    translationalController_x.setI(ki.getAsDouble());
    translationalController_x.setD(kd.getAsDouble());
    translationalController_y.setP(kp.getAsDouble());
    translationalController_y.setI(ki.getAsDouble());
    translationalController_y.setD(kd.getAsDouble());

    resetTranslation();
  }

  private static void updateRotationalPid() {
    rotationalController.setP(kp.getAsDouble());
    rotationalController.setI(ki.getAsDouble());
    rotationalController.setD(kd.getAsDouble());

    resetRotational();
  }

  private static void updateConstraints() {
    translationalController_x.setConstraints(
        new Constraints(maxVelConstraints_x.getAsDouble(), maxAccelConstraints_x.getAsDouble()));
    translationalController_y.setConstraints(
        new Constraints(maxVelConstraints_y.getAsDouble(), maxAccelConstraints_y.getAsDouble()));
  }

  public static ChassisSpeeds calcSpeeds(Pose2d targetPose) {
    if (tuneTranslational.getAsBoolean()) {
      tuneTranslational.set(false);
      updateTranslationalPid();
    }

    if (tuneRotational.getAsBoolean()) {
      tuneRotational.set(false);
      updateRotationalPid();
    }

    if (tuneConstants.getAsBoolean()) {
      tuneConstants.set(false);
      updateConstraints();
    }

    Pose2d robotPose = Drive.getInstance().getPose();
    targetPose = targetPose.equals(Pose2d.kZero) ? robotPose : targetPose;

    double currentTargetAngle = targetPose.getRotation().getRadians();
    double omegaFFPower = (MathUtil.angleModulus((currentTargetAngle - prevTargetAngle)) / 0.02)
        * omegaKFF.getAsDouble();


    limitTranslationalAclByAngleError();

    double xPower = translationalController_x.calculate(robotPose.getX(), targetPose.getX());
    double yPower = translationalController_y.calculate(robotPose.getY(), targetPose.getY());
    double omegaPower = rotationalController.calculate(robotPose.getRotation().getRadians(),
        targetPose.getRotation().getRadians()) + omegaFFPower;


    xPower = Math.abs(xPower) < xPowerTolorance.getAsDouble() ? 0 : xPower;
    yPower = Math.abs(yPower) < yPowerTolorance.getAsDouble() ? 0 : yPower;
    omegaPower = Math.abs(omegaPower) < omegaPowerTolorance.getAsDouble() ? 0 : omegaPower;


    // omegaPower = 0;
    // xPower = 0;
    // yPower = 0;

    Logger.recordOutput("DriveProfiledPidCalculator/error/x",
        translationalController_x.getPositionError());
    Logger.recordOutput("DriveProfiledPidCalculator/error/y",
        translationalController_y.getPositionError());
    Logger.recordOutput("DriveProfiledPidCalculator/error/omega rad",
        rotationalController.getError());
    Logger.recordOutput("DriveProfiledPidCalculator/error/omega deg",
        Units.radiansToDegrees(rotationalController.getError()));
    Logger.recordOutput("DriveProfiledPidCalculator/power/x", xPower);

    Logger.recordOutput("DriveProfiledPidCalculator/power/y", yPower);
    Logger.recordOutput("DriveProfiledPidCalculator/power/omega rad", omegaPower);
    Logger.recordOutput("DriveProfiledPidCalculator/power/omega deg",
        Units.radiansToDegrees(omegaPower));


    prevTargetAngle = targetPose.getRotation().getRadians();
    return new ChassisSpeeds(xPower, yPower, omegaPower);
  }

  public static ChassisSpeeds calcSpeeds() {
    return calcSpeeds(Drive.getLockPose());
  }

  public static ChassisSpeeds calcSpeeds(Rotation2d rotation2d) {
    return calcSpeeds(GeomUtil.withRotation(Drive.getInstance().getPose(), rotation2d));
  }

  public static double getRotationalErrorDegrees() {
    return Units.radiansToDegrees(rotationalController.getError());
  }

  private static void limitTranslationalAclByAngleError() {
    // TODO: implement
  }
}
