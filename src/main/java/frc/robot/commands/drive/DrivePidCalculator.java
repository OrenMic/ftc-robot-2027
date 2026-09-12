package frc.robot.commands.drive;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import frc.robot.subsystems.drive.Drive;
import frc.robot.util.GeomUtil;
import frc.robot.util.LoggedTunableNumber;
import lombok.Getter;
import miscar.util.PIDControllerUtil;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;

public class DrivePidCalculator {

  public enum PidState {
    NORMAL(new PIDControllerUtil(2.1, 0, 0), new PIDControllerUtil(2.8, 0.06, 0.0)), INTAKING(
        new PIDControllerUtil(2.1, 0, 0), new PIDControllerUtil(2.8, 0.06, 0.0)), FASTER_TURNING(
            new PIDControllerUtil(2.1, 0, 0), new PIDControllerUtil(5.5, 0.0, 0.0));

    public final PIDControllerUtil translationalPid;
    public final PIDControllerUtil rotationalPid;

    PidState(PIDControllerUtil translationalPid, PIDControllerUtil rotationalPid) {
      this.translationalPid = translationalPid;
      this.rotationalPid = rotationalPid;
      rotationalPid.enableContinuousInput(-Math.PI, Math.PI);
    }
  }

  private static LoggedTunableNumber omegaKFF =
      new LoggedTunableNumber("DrivePidCalculator/omegaFF", 0, true);
  private static LoggedTunableNumber kp, ki, kd;
  private static LoggedTunableNumber xPowerTolorance =
      new LoggedTunableNumber("DrivePidCalculator/xPowerTolorance", 0.02, true),
      yPowerTolorance = new LoggedTunableNumber("DrivePidCalculator/yPowerTolorance", 0.02, true),
      omegaPowerTolorance =
          new LoggedTunableNumber("DrivePidCalculator/omegaPowerTolorance", 0.05, true);
  private static LoggedNetworkBoolean tuneTranslational;
  private static LoggedNetworkBoolean tuneRotational;
  private static double prevTargetAngle;

  static {
    kp = new LoggedTunableNumber("DrivePidCalculator/pid/kp", 0, true);
    ki = new LoggedTunableNumber("DrivePidCalculator/pid/ki", 0, true);
    kd = new LoggedTunableNumber("DrivePidCalculator/pid/kd", 0, true);


    tuneTranslational = new LoggedNetworkBoolean("DrivePidCalculator/pid/tuneTranslational", false);
    tuneRotational = new LoggedNetworkBoolean("DrivePidCalculator/pid/tuneRotational", false);


    prevTargetAngle = 0;
  }

  @Getter
  private static PidState pidState = PidState.NORMAL;

  private static PIDControllerUtil translationalController_x = pidState.translationalPid;
  private static PIDController translationalController_y = translationalController_x.clone();
  private static PIDController rotationalController = pidState.rotationalPid;


  public static void initialize() {
    translationalController_x.reset();
    translationalController_y.reset();
    rotationalController.reset();
  }

  public static void setPidState(PidState pidState) {
    DrivePidCalculator.pidState = pidState;
    translationalController_x = pidState.translationalPid;
    translationalController_y = pidState.translationalPid.clone();
    rotationalController = pidState.rotationalPid;
  }

  private static void updateTranslationalPid() {
    translationalController_x.setP(kp.getAsDouble());
    translationalController_x.setI(ki.getAsDouble());
    translationalController_x.setD(kd.getAsDouble());
    translationalController_y.setP(kp.getAsDouble());
    translationalController_y.setI(ki.getAsDouble());
    translationalController_y.setD(kd.getAsDouble());
    translationalController_x.reset();
    translationalController_y.reset();
  }

  private static void updateRotationalPid() {
    rotationalController.setP(kp.getAsDouble());
    rotationalController.setI(ki.getAsDouble());
    rotationalController.setD(kd.getAsDouble());
    rotationalController.reset();
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

    Pose2d robotPose = Drive.getInstance().getPose();
    targetPose = targetPose.equals(Pose2d.kZero) ? robotPose : targetPose;

    double currentTargetAngle = targetPose.getRotation().getRadians();
    double omegaFFPower = (MathUtil.angleModulus((currentTargetAngle - prevTargetAngle)) / 0.02)
        * omegaKFF.getAsDouble();

    double xPower = translationalController_x.calculate(robotPose.getX(), targetPose.getX());
    double yPower = translationalController_y.calculate(robotPose.getY(), targetPose.getY());
    double omegaPower = rotationalController.calculate(robotPose.getRotation().getRadians(),
        targetPose.getRotation().getRadians()) + omegaFFPower;


    xPower = Math.abs(xPower) < xPowerTolorance.getAsDouble() ? 0 : xPower;
    yPower = Math.abs(yPower) < yPowerTolorance.getAsDouble() ? 0 : yPower;
    omegaPower = Math.abs(omegaPower) < omegaPowerTolorance.getAsDouble() ? 0 : omegaPower;

    Logger.recordOutput("DrivePidCalculator/error/x", translationalController_x.getError());
    Logger.recordOutput("DrivePidCalculator/error/y", translationalController_y.getError());
    Logger.recordOutput("DrivePidCalculator/error/omega rad", rotationalController.getError());
    Logger.recordOutput("DrivePidCalculator/error/omega deg",
        Units.radiansToDegrees(rotationalController.getError()));
    Logger.recordOutput("DrivePidCalculator/power/x", xPower);

    Logger.recordOutput("DrivePidCalculator/power/y", yPower);
    Logger.recordOutput("DrivePidCalculator/power/omega rad", omegaPower);
    Logger.recordOutput("DrivePidCalculator/power/omega deg", Units.radiansToDegrees(omegaPower));


    prevTargetAngle = targetPose.getRotation().getRadians();
    return new ChassisSpeeds(xPower, yPower, omegaPower);
  }

  public static ChassisSpeeds calcSpeeds() {
    return calcSpeeds(Drive.getInstance().getLockPose());
  }

  public static ChassisSpeeds calcSpeeds(Rotation2d rotation2d) {
    return calcSpeeds(GeomUtil.withRotation(Drive.getInstance().getPose(), rotation2d));
  }

  public static double getRotationalErrorDegrees() {

    return Units.radiansToDegrees(rotationalController.getError());
  }
}
