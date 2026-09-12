package frc.robot.commands.drive;

import org.littletonrobotics.junction.Logger;
import frc.robot.subsystems.drive.Drive;

public class DriveProfiledPidFollow extends DriveCommandBase {
  public DriveProfiledPidFollow(Drive drive) {
    super(drive);
  }

  @Override
  public void initialize() {
    DriveProfiledPidCalculator.initialize();
  }

  @Override
  public void execute() {
    drive.fieldRelativeDrive(DriveProfiledPidCalculator.calcSpeeds());
    Logger.recordOutput("profiled pid", true);
  }

  @Override
  public void end(boolean interrupted) {
    Logger.recordOutput("profiled pid", false);

  }

}
