package frc.robot.commands.autoCommands;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.commands.drive.DrivePidCalculator;
import frc.robot.commands.drive.DriveProfiledPidFollow;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.DriveState;
import frc.robot.subsystems.drive.util.DriveUtil;
import frc.robot.subsystems.intake.IntakeStates.ExtensionState;
import frc.robot.subsystems.intake.IntakeStates.IntakeState;
import frc.robot.subsystems.shooter.util.ShootingUtil;
import frc.robot.subsystems.superStructure.SuperStructure;
import frc.robot.subsystems.superStructure.SuperStructureState;
import frc.robot.util.AllianceFlipUtil;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.littletonrobotics.junction.Logger;

public class AutoActions {

  public static Command autoSequence(SuperStructure superStructure, Command... commands) {
    return Commands.sequence(commands)
        .andThen(superStructure.runOnce(() -> superStructure.setState(SuperStructureState.IDLE)));
  }


  public static Command setRedPose(SuperStructure superStructure, Pose2d pose) {
    return superStructure.runOnce(() -> {
      Pose2d newPose = AllianceFlipUtil.apply(pose);
      if (superStructure.drive.getPose().getTranslation()
          .getDistance(newPose.getTranslation()) > 0.7)
        superStructure.drive.setPose(newPose);
    });

  }

  public static Command initAuto(SuperStructure superStructure, Pose2d pose) {
    return Commands.parallel(setRedPose(superStructure, pose),
        Commands.runOnce(() -> Drive.getInstance().setState(DriveState.IDLE)),
        setAutoControlled(superStructure));
  }

  public static Command shootFirst8(SuperStructure superStructure) {
    return shoot(superStructure, 0, 10000);
  }

  public static Command shoot(SuperStructure superStructure, double pulseDelay, double timeOut) {
    return Commands
        .sequence(Commands.parallel(log("Shot"),
            superStructure
                .runOnce(() -> superStructure.setState(SuperStructureState.SHOOTING_ASSISTED))))
        .withTimeout(timeOut).andThen(resetAutoCommand(superStructure));
  }

  private static Command resetAutoCommand(SuperStructure superStructure) {
    return superStructure.runOnce(() -> {
      resetAuto(superStructure);
    });
  }

  private static void resetAuto(SuperStructure superStructure) {
    superStructure.IDLE();

    superStructure.drive.setState(DriveState.IDLE);
    superStructure.setState(SuperStructureState.AUTO_CONTROLLED);
  }



  public static Command intake(SuperStructure superStructure, boolean right, double timeOut) {
    return Commands.parallel(log("intake"),
        setAutoControlled(superStructure),
        driveFieldRelativeAt(superStructure,
            new ChassisSpeeds(0, right ? -2 : 2, 0),
            right ? Rotation2d.kCW_90deg : Rotation2d.kCCW_90deg),
        superStructure.intake.runOnce(
            () -> superStructure.intake.setState(IntakeState.INTAKING, ExtensionState.EXTENDED)))
        .andThen(Commands.waitSeconds(timeOut));
  }

  public static Command driveFieldRelativeAt(SuperStructure superStructure, ChassisSpeeds speeds,
      Rotation2d angle) {
    return Commands.sequence(Commands.parallel(setAutoControlled(superStructure)),
        superStructure.run(() -> {
          superStructure.drive.setState(DriveState.IDLE);
          superStructure.drive.setLockToAngle(angle);
          superStructure.drive.fieldRelativeDrive(new ChassisSpeeds(speeds.vxMetersPerSecond,
              speeds.vyMetersPerSecond, DrivePidCalculator.calcSpeeds().omegaRadiansPerSecond));
        }));
  }


  public static Command shootWhileDrivingTo(SuperStructure superStructure, Translation2d point,
      double pulseDelay) {
    return Commands.sequence(Commands.parallel(log("shotWhileDrivingTo"),
        setAutoControlled(superStructure),
        superStructure.run(() -> {
          superStructure.shootingLogic(true);
        }),
        new DriveProfiledPidFollow(superStructure.drive),
        Commands.run(() -> {
          superStructure.drive.setState(DriveState.IDLE);
          superStructure.drive.setLockToPosition(
              new Pose2d(AllianceFlipUtil.apply(point), ShootingUtil.getAngleToHub()));
        })).until(DriveUtil::isRobotInTransition),
        Commands.waitSeconds(1),
        resetAutoCommand(superStructure));
  }

  public static Command driveToRedPose(SuperStructure superStructure, Pose2d point) {
    return Commands.sequence(Commands
        .parallel(log("driveTo"), setAutoControlled(superStructure), superStructure.runOnce(() -> {
          superStructure.drive.setLockToPosition(AllianceFlipUtil.apply(point));
          superStructure.drive.setState(DriveState.AUTO_DRIVE);
        })), Commands.waitUntil(() -> DriveUtil.isRobotInPose()));
  }

  private static Command log(String name) {
    return Commands.runOnce(() -> Logger.recordOutput("Auto/ActiveCommand", name));
  }

  public static Command followPath(String name, boolean mirror) {
    try {
      PathPlannerPath path = PathPlannerPath.fromPathFile(name).flipPath();

      if (mirror) {
        return Commands.parallel(AutoBuilder.followPath(path.mirrorPath()), log("followPath"));
      } else {
        return Commands.parallel(AutoBuilder.followPath(path), log("followPath"));
      }
    } catch (RuntimeException | IOException | ParseException e) {
      throw new RuntimeException("no path by the name: " + name);
    }
  }

  public static Command setAutoControlled(SuperStructure superStructure) {
    return Commands.runOnce(() -> {
      superStructure.setState(SuperStructureState.AUTO_CONTROLLED);
    });
  }
}
