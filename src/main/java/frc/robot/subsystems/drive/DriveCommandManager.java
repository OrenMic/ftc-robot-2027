package frc.robot.subsystems.drive;

import frc.robot.commands.drive.AutoDriveCommand;
import frc.robot.commands.drive.DriveAngled;
import frc.robot.commands.drive.DriveCommand;
import frc.robot.commands.drive.DriveIdle;
import frc.robot.commands.drive.DriveLockedOnTarget;
import frc.robot.subsystems.drive.commands.DriveCommandUtil;

public class DriveCommandManager {
  public DriveIdle IDLE; //
  public DriveCommand FREE_DRIVE; //
  public AutoDriveCommand AUTO_DRIVE; //
  public DriveLockedOnTarget LOCKED_ON_TARGET; //
  public DriveAngled ANGLED; //

  public DriveCommandManager(Drive drive) {
    IDLE = new DriveIdle(drive);
    FREE_DRIVE = new DriveCommand(drive);
    AUTO_DRIVE = new AutoDriveCommand(drive);
    LOCKED_ON_TARGET = new DriveLockedOnTarget(drive);
    ANGLED = new DriveAngled(drive);
  }

  public void scheduleByState(DriveState state) {
    DriveCommandUtil wantedCommand;
    switch (state) {
      case IDLE:
        wantedCommand = IDLE;
        break;
      case FREE_DRIVE:
        wantedCommand = FREE_DRIVE;
        break;
      case AUTO_DRIVE:
        wantedCommand = AUTO_DRIVE;
        break;
      case LOCKED_ON_TARGET:
        wantedCommand = LOCKED_ON_TARGET;
        break;
      case ANGLED:
        wantedCommand = ANGLED;
        break;

      default:
        wantedCommand = IDLE;
        break;
    }

    wantedCommand.schedule();
  }
}
