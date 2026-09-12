package frc.robot.commands.drive;

import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.commands.DriveCommandUtil;

public class DriveIdle extends DriveCommandUtil {
  public DriveIdle(Drive drive) {
    super(drive, "DriveIdle");
  }

  @Override
  public void initialize() {
    drive.stop();
  }

  @Override
  public void execute() {}
}
