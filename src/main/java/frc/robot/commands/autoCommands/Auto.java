package frc.robot.commands.autoCommands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.superStructure.SuperStructure;

public class Auto {
	public static Command poc(SuperStructure superStructure) {
		return Commands.sequence(
				AutoActions.initAuto(superStructure,
						new Pose2d(0.315, 0.346, Rotation2d.fromDegrees(90))),
				AutoActions.followPath("Shoot1", false),
				AutoActions.followPath("Intake1", false),
				AutoActions.followPath("Travel1", false));
	}
}
