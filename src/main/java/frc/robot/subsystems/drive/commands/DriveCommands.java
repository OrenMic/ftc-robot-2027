package frc.robot.subsystems.drive.commands;

import java.util.Optional;

public class DriveCommands {

  private static Optional<DriveCommandUtil> command = Optional.empty();

  public static void activeCommand(DriveCommandUtil command) {
    DriveCommands.command.ifPresent((activeCommand) -> activeCommand.end(true));
    DriveCommands.command = Optional.of(command);
  }

  public static boolean isScheduled(DriveCommandUtil command) {
    return DriveCommands.command.isPresent()
        ? DriveCommands.command.get().getName().equals(command.getName())
        : false;
  }

  public static void periodic() {
    command.ifPresent((command) -> command.execute());
  }

  public static String getActiveCommandName() {
    return command.isPresent() ? command.get().getName() : "NAN";
  }
}
