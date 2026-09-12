package frc.robot.commands.drive;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.commands.DriveCommandUtil;
import org.littletonrobotics.junction.Logger;

public class AutoDriveCommand extends DriveCommandUtil {
  DrivePidFollow pidFollow;
  Command trajectoryFollow = Commands.none();

  private String willColideLogPath = getName() + "/willCollide";
  private String isPidActiveLogPath = getName() + "/isPidActive";
  private String isTrajectoryFollowActiveLogPath = getName() + "/isTrajectoryFollowActive";
  private String autoDriveMethodLogPath = getName() + "/autoDriveMethod";
  private String targetPoseDidntChangeLogPath = getName() + "/targetPoseDidntChanged";

  public AutoDriveCommand(Drive drive) {
    super(drive, "AutoDriveCommand");
    pidFollow = new DrivePidFollow(drive);
  }

  @Override
  public void execute() {
    boolean targetPoseDidntChanged = drive.getLastLockPose().equals(drive.getLockPose());
    Logger.recordOutput(isPidActiveLogPath, pidFollow.isScheduled());
    Logger.recordOutput(isTrajectoryFollowActiveLogPath, trajectoryFollow.isScheduled());
    Logger.recordOutput(autoDriveMethodLogPath,
        pidFollow.isScheduled() ? "pidFollow" : "trajectoryFollow");
    Logger.recordOutput(targetPoseDidntChangeLogPath, targetPoseDidntChanged);
    if ((trajectoryFollow.isScheduled() || (pidFollow.isScheduled())) && targetPoseDidntChanged) {
      return;
    }
    pidFollow.schedule();
  }

  @Override
  public void end(boolean interrupted) {
    CommandScheduler.getInstance().cancel(pidFollow, trajectoryFollow);
  }
}
