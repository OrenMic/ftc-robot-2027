package frc.robot.commands.drive;

import org.littletonrobotics.junction.Logger;
import frc.robot.subsystems.drive.Drive;

public class DrivePidFollow extends DriveCommandBase {
  public DrivePidFollow(Drive drive) {
    super(drive);
  }

  @Override
  public void initialize() {
    DrivePidCalculator.initialize();
  }

  @Override
  public void execute() {
    drive.fieldRelativeDrive(DrivePidCalculator.calcSpeeds());
    Logger.recordOutput("noraml pid", true);
  }

  @Override
  public void end(boolean interrupted) {
    Logger.recordOutput("noraml pid", false);

  }

}
