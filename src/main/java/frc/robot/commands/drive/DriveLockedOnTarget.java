package frc.robot.commands.drive;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.util.DriveUtil;

public class DriveLockedOnTarget extends DriveCommand {
  public DriveLockedOnTarget(Drive drive) {
    super(drive);
  }

  @Override
  public void initialize() {
    DrivePidCalculator.initialize();
  }

  @Override
  public void driveBy(ChassisSpeeds speeds) {
    speeds = new ChassisSpeeds(speeds.vxMetersPerSecond, speeds.vyMetersPerSecond,
        DrivePidCalculator.calcSpeeds(
            DriveUtil.getAngleToPose(drive.getLockPose().getTranslation())).omegaRadiansPerSecond);
    super.driveBy(speeds);
  }
}
