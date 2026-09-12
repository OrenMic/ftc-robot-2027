package frc.robot.commands.drive;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.drive.Drive;

public class DriveCommandBase extends Command {
  protected final Drive drive;

  public DriveCommandBase(Drive drive) {
    this.drive = drive;
    setSubsystem("Drive");
    addRequirements(drive);
  }

  @Override
  public boolean runsWhenDisabled() {
    return false;
  }
}
