package frc.robot.commands.drive;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import frc.robot.RobotContainer;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.commands.DriveCommandUtil;
import java.util.function.DoubleSupplier;
import miscar.util.AllianceUtil;
import org.littletonrobotics.junction.Logger;

public class DriveCommand extends DriveCommandUtil {
  private final boolean fieldRelativeDrive = true;
  private final double maxVel = 5;
  private final double maxAnglerVel = Units.rotationsToRadians(3);
  private String driveSpeedsLogPath = getName() + "/drive speeds";

  protected DoubleSupplier x, y, omega;
  double directionMultiplier = AllianceUtil.isRedAlliance() ? -1 : 1;

  {
    x = () -> MathUtil.applyDeadband(-RobotContainer.driver.getLeftY(), 0.1);
    y = () -> MathUtil.applyDeadband(-RobotContainer.driver.getLeftX(), 0.1);
    omega = () -> MathUtil.applyDeadband(-RobotContainer.driver.getRightX(), 0.1);
  }

  public DriveCommand(Drive drive) {
    super(drive, "DriveCommand");
  }

  public DriveCommand(Drive drive, String name) {
    super(drive, name);
  }

  @Override
  public void execute() {

    // getWanted movement vectors
    double xVel = x.getAsDouble();
    double yVel = y.getAsDouble();
    double omegaVel = omega.getAsDouble();

    // scale movement vectors by the maxVel
    xVel = xVel * MathUtil.clamp(maxVel, -drive.getSpeedCap(), drive.getSpeedCap());
    yVel = yVel * MathUtil.clamp(maxVel, -drive.getSpeedCap(), drive.getSpeedCap());
    omegaVel *= maxAnglerVel;

    if (fieldRelativeDrive) {
      // rotate movement vectors to match alliance side
      xVel *= directionMultiplier;
      yVel *= directionMultiplier;
    }
    ChassisSpeeds chassisSpeeds = new ChassisSpeeds(xVel, yVel, omegaVel);
    driveBy(chassisSpeeds);
  }

  public void driveBy(ChassisSpeeds speeds) {
    if (fieldRelativeDrive)
      drive.fieldRelativeDrive(speeds);
    else
      drive.robotRelativeDrive(speeds);

    Logger.recordOutput(driveSpeedsLogPath, speeds);
  }
}
