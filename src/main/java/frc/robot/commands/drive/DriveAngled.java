package frc.robot.commands.drive;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import frc.robot.commands.drive.DrivePidCalculator.PidState;
import frc.robot.subsystems.drive.Drive;

public class DriveAngled extends DriveCommand {

  public DriveAngled(Drive drive) {
    super(drive, "DriveAngled");
  }

  @Override
  public void initialize() {
    DrivePidCalculator.setPidState(PidState.FASTER_TURNING);
    DrivePidCalculator.initialize();
  }

  @Override
  public void driveBy(ChassisSpeeds speeds) {
    // calcs speed updates the controler error so this needs to be called
    // after it
    double rotationalVelocity = DrivePidCalculator.calcSpeeds().omegaRadiansPerSecond;

    speeds.omegaRadiansPerSecond = rotationalVelocity;
    super.driveBy(speeds);
  }

  @Override
  public void end(boolean interrupted) {
    DrivePidCalculator.setPidState(PidState.NORMAL);
  }
}
