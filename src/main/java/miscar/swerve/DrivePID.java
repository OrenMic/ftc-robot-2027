// package miscar.swerve;

// import edu.wpi.first.math.controller.PIDController;
// import edu.wpi.first.math.controller.SimpleMotorFeedforward;
// import edu.wpi.first.math.geometry.Pose2d;
// import edu.wpi.first.math.kinematics.ChassisSpeeds;
// import edu.wpi.first.math.util.Units;
// import edu.wpi.first.wpilibj2.command.Command;
// import edu.wpi.first.wpilibj2.command.CommandScheduler;
// import lombok.Getter;
// import miscar.util.PIDControllerUtil;
// import org.littletonrobotics.junction.Logger;
// import
// org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
// import
// org.littletonrobotics.junction.networktables.LoggedNetworkNumber;

// public class DrivePID extends Command {
// {
// setName("DrivePID");
// }

// private final LocalizedSwerve drive;

// private final LoggedNetworkNumber kp, ki, kd, ks, kv, ka;
// private final LoggedNetworkBoolean updatePID, updateFF;

// private String targetPoseLogPath = getName() + "/targetPose";
// private String errorXLogPath = getName() + "/error/x";
// private String errorYLogPath = getName() + "/error/y";
// private String errorOmegaRadLogPath = getName() + "/error/omega
// rad";
// private String errorOmegaDegLogPath = getName() + "/error/omega
// deg";
// private String powerxLogpath = getName() + "/power/x";
// private String poweryLogPath = getName() + "/power/y";
// private String powerOmegaRadLogPath = getName() + "/power/omega
// rad";
// private String powerOmegaDegLogPath = getName() + "/power/omega
// deg";
// private String isInPoseLogPath = getName() + "/is in pose";

// {
// kp = new LoggedNetworkNumber(getName() + "PID/kp");
// ki = new LoggedNetworkNumber(getName() + "PID/ki");
// kd = new LoggedNetworkNumber(getName() + "PID/kd");

// ks = new LoggedNetworkNumber(getName() + "PID/ks");
// ka = new LoggedNetworkNumber(getName() + "PID/ka");
// kv = new LoggedNetworkNumber(getName() + "PID/kv");

// kp.setDefault(2.8); // 5
// ki.setDefault(0.06);
// kd.setDefault(0);
// ks.setDefault(0);
// ka.setDefault(0);
// kv.setDefault(0);

// updatePID = new LoggedNetworkBoolean(getName() + "PID/update/pid");

// updateFF = new LoggedNetworkBoolean(getName() + "PID/update/ff");

// updatePID.setDefault(false);
// updateFF.setDefault(false);
// }

// private Pose2d targetPose = new Pose2d();

// @Getter
// private boolean lockedToPose = false;
// private PIDControllerUtil translationalController_x;
// private PIDController translationalController_y;
// private SimpleMotorFeedforward rotationalFF = new
// SimpleMotorFeedforward(0, 0);
// private PIDController rotationalController = new PIDController(2.8,
// 0.06, 0.0);

// {
// rotationalController.enableContinuousInput(-Math.PI, Math.PI);
// }

// public void setPid(PIDControllerUtil pid) {
// translationalController_x = pid.clone();
// translationalController_y = pid.clone();
// }

// @Override
// public void schedule() {
// CommandScheduler.getInstance().schedule(this);
// }

// public DrivePID(LocalizedSwerve drive, PIDControllerUtil
// translationalController) {
// this.drive = drive;

// translationalController_x = translationalController;
// translationalController_y = translationalController_x.clone();

// addRequirements(drive);
// }

// public DrivePID lockToPose(Pose2d targetPose) {
// this.targetPose = targetPose;
// return this;
// }

// public DrivePID lockToCurrantPose() {
// this.targetPose = new Pose2d();
// return this;
// }

// public DrivePID lock(boolean lock) {
// this.lockedToPose = lock;
// return this;
// }

// @Override
// public void initialize() {
// translationalController_x.reset();
// translationalController_y.reset();
// rotationalController.reset();
// }

// @Override
// public void execute() {
// if (!lockedToPose) {
// drive.stop();
// return;
// }

// if (updatePID.get()) {
// rotationalController.setPID(kp.get(), ki.get(), kd.get());
// }
// if (updateFF.get()) {
// rotationalFF.setKs(ks.get());
// rotationalFF.setKa(ka.get());
// rotationalFF.setKv(kv.get());
// }

// Pose2d robotPose = drive.getPose();
// Pose2d targetPose = this.targetPose.equals(Pose2d.kZero) ?
// robotPose : this.targetPose;
// // Pose2d targetPose = new Pose2d(12.61, 2.83, new
// // Rotation2d(Units.degreesToRadians(64.04)));
// double xPower =
// translationalController_x.calculate(robotPose.getX(),
// targetPose.getX());
// double yPower =
// translationalController_y.calculate(robotPose.getY(),
// targetPose.getY());
// // double rotationError =
// //
// robotPose.getRotation().minus(targetPose.getRotation()).getRadians();
// double omegaPower =
// rotationalController.calculate(robotPose.getRotation().getRadians(),
// targetPose.getRotation().getRadians());

// // xPower = translationalController_x.atSetpoint() ? 0 : xPower;
// // yPower = translationalController_y.atSetpoint() ? 0 : yPower;
// // omegaPower = rotationalController.atSetpoint() ? 0 : omegaPower;

// drive.fieldRelativeDrive(new ChassisSpeeds(xPower, yPower,
// omegaPower));

// Logger.recordOutput(targetPoseLogPath, targetPose);
// Logger.recordOutput(errorXLogPath,
// translationalController_x.getError());
// Logger.recordOutput(errorYLogPath,
// translationalController_y.getError());
// Logger.recordOutput(errorOmegaRadLogPath,
// rotationalController.getError());
// Logger.recordOutput(errorOmegaDegLogPath,
// Units.radiansToDegrees(rotationalController.getError()));
// Logger.recordOutput(powerxLogpath, xPower);
// Logger.recordOutput(poweryLogPath, yPower);
// Logger.recordOutput(powerOmegaRadLogPath, omegaPower);
// Logger.recordOutput(powerOmegaDegLogPath,
// Units.radiansToDegrees(omegaPower));

// Logger.recordOutput(isInPoseLogPath,
// (Math.abs(Units.radiansToDegrees(rotationalController.getError()))
// < 1
// && Math.abs(translationalController_x.getError()) < 0.03
// && Math.abs(translationalController_y.getError()) < 0.03));
// }

// @Override
// public boolean isFinished() {
// return false;
// }

// @Override
// public void end(boolean interrupted) {
// drive.stop();
// }
// }
