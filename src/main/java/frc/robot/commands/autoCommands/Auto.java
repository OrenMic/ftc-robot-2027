package frc.robot.commands.autoCommands;

import static frc.robot.commands.autoCommands.AutoActions.*;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.superStructure.SuperStructure;
import frc.robot.util.AllianceFlipUtil;

public class Auto {

    // public static Command leftCycleCCAuto(SuperStructure
    // superStructure) {
    // return Commands.sequence(
    // initAuto(superStructure,
    // new Pose2d(12.160, 0.593, Rotation2d.fromDegrees(-178.788))),
    // followPath("Cycle_1", false),
    // shootWhileDrivingTo(superStructure, new Translation2d(13.483,
    // 0.910), 1),
    // followPath("Cycle_2_OLD", false),
    // shootWhileDrivingTo(superStructure, new Translation2d(13.712,
    // 0.871), 1));
    // }

    // public static Command poc(SuperStructure superStructure) {
    // return Commands.sequence(
    // initAuto(superStructure, new Pose2d(13.764, 2.385,
    // Rotation2d.fromDegrees(133.73))),
    // shootWhileDrivingTo(superStructure, new Translation2d(13.483,
    // 0.910), 1));
    // }

    public static Command shootFirst8Right(SuperStructure superStructure) {
        return Commands.sequence(
                initAuto(superStructure, new Pose2d(12.160, 0.593, Rotation2d.fromDegrees(90))),
                shootFirst8(superStructure));
    }

    public static Command shootFirst8Middle(SuperStructure superStructure) {
        return Commands.sequence(
                initAuto(superStructure, new Pose2d(13.001, 3.997, Rotation2d.fromDegrees(180))),
                followPath("Middle", false),
                shootFirst8(superStructure));
    }

    public static Command shootFirst8Left(SuperStructure superStructure) {
        return Commands
                .sequence(
                        initAuto(superStructure,
                                new Pose2d(12.160, AllianceFlipUtil.applyY(0.593, true),
                                        Rotation2d.fromDegrees(-90))),
                        shootFirst8(superStructure));
    }

    public static Command leftCycleAuto(SuperStructure superStructure) {
        return Commands.sequence(
                initAuto(superStructure, new Pose2d(12.160, 0.593, Rotation2d.fromDegrees(0))),
                followPath("Cycle_1", false),
                shootWhileDrivingTo(superStructure, new Translation2d(13.483, 0.910), 1),
                followPath("Cycle_2", false),
                shoot(superStructure, 1, 10));
    }

    public static Command rightCycleAuto(SuperStructure superStructure) {
        return Commands.sequence(
                initAuto(superStructure,
                        new Pose2d(12.160, AllianceFlipUtil.applyY(0.593, true),
                                Rotation2d.fromDegrees(0))),
                followPath("Cycle_1", true),
                shootWhileDrivingTo(superStructure,
                        new Translation2d(13.483, AllianceFlipUtil.applyY(0.910, true)),
                        1),
                followPath("Cycle_2", true),
                shoot(superStructure, 1, 10));
    }

    // public static Command rightCycleCCAuto(SuperStructure
    // superStructure) {
    // return Commands.sequence(
    // initAuto(superStructure,
    // new Pose2d(12.160, AllianceFlipUtil.applyY(0.593),
    // Rotation2d.fromDegrees(-178.788))),
    // followPath("Cycle_1", true),
    // shootWhileDrivingTo(superStructure,
    // new Translation2d(13.483, AllianceFlipUtil.applyY(0.910, true)),
    // 1),
    // followPath("Cycle_2_OLD", true),
    // shoot(superStructure, 1, 10));
    // }


}
