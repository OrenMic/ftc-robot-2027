package frc.robot.subsystems.drive.commands;

import frc.robot.subsystems.drive.Drive;

public class DriveCommandUtil {
  private final String name;
  protected final Drive drive;

  public DriveCommandUtil(Drive drive, String commandName) {
    this.drive = drive;
    this.name = commandName;
  }

  public void schedule() {
    if (!DriveCommands.isScheduled(this)) {
      initialize();
      DriveCommands.activeCommand(this);
    }
  }

  /**
   * The initial subroutine of a command. Called once when the command
   * is initially scheduled.
   */
  public void initialize() {}

  /**
   * The main body of a command. Called repeatedly while the command is
   * scheduled.
   */
  public void execute() {}

  public void end(boolean interrupted) {}

  public String getName() {
    return name;
  }
}
