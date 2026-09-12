package frc.robot;

import org.littletonrobotics.junction.Logger;
import com.pathplanner.lib.events.EventTrigger;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.commands.autoCommands.Auto;
import frc.robot.generated.intake.IntakeConstants;
import frc.robot.generated.shooterMec.ShooterMecConstants;
import frc.robot.generated.swerveModule.SwerveModuleConstants;
import frc.robot.generated.transfer.TransferConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeStates.ExtensionState;
import frc.robot.subsystems.intake.IntakeStates.IntakeState;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.superStructure.SuperStructure;
import frc.robot.subsystems.transfer.Transfer;
import frc.robot.subsystems.vision.LimelightVision;
import frc.robot.util.ManageableLoggedDashboardChooser;
import frc.robot.util.PS5ControllerRumble;
import miscar.annotation.ConstantsCreator;
import miscar.annotation.ConstantsUser;
import miscar.configs.motors.MotorIOConfig;
import miscar.gyro.GyroIO;
import miscar.gyro.Pigeon2;
import miscar.mecsIOs.Mechanism;
import miscar.util.AllianceUtil;;

/**
 *
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a "declarative" paradigm, very little robot logic
 * should actually be handled in the {@link Robot} periodic methods
 * (other than the scheduler calls). Instead, the structure of the
 * robot (including subsystems, commands, and button mappings) should
 * be declared here.
 */
@ConstantsUser()
public class RobotContainer {

  // Subsystems
  public final SuperStructure superStructure;
  public final Shooter shooter;
  public final Transfer transfer;
  public final Intake intake;

  {
    // ServoManger.build(3, ServoMangerType.RevServoHub).start();
  }

  // Controller
  public static final PS5ControllerRumble driver = new PS5ControllerRumble(0, 1);

  // Dashboard inputs
  private final ManageableLoggedDashboardChooser<Command> autoChooser;

  private final ManageableLoggedDashboardChooser<Boolean> leftSidePoseChooser;

  private final ManageableLoggedDashboardChooser<Boolean> isRedAllianceChooser;

  /**
   * The container for the robot. Contains subsystems, OI devices, and
   * commands.
   */
  public RobotContainer() {


    switch (Constants.currentMode) {
      case REAL:
        Drive.init(SwerveModuleConstants.driveConstants.talonFXMotorConfig.config,
            SwerveModuleConstants.rotationConstants.talonFXMotorConfig.config,
            new Pigeon2(16, "rio"));

        intake = new Intake(IntakeConstants.intakeConstants.talonFXMotorConfig.config,
            Mechanism.create(IntakeConstants.extensionConstants.talonFXMotorConfig.config));

        transfer = new Transfer(
            Mechanism.create(TransferConstants.beltConstants.talonFXMotorConfig.config));

        shooter = new Shooter(
            Mechanism.create(ShooterMecConstants.frontConstants.talonFXMotorConfig.config),
            ShooterMecConstants.backRightConstants.talonFXMotorConfig.config,
            Mechanism.create(ShooterMecConstants.indexerConstants.talonFXMotorConfig.config));
        break;
      case SIM:
        intake = new Intake(IntakeConstants.intakeConstants.simMotorConfig.config,
            Mechanism.create(IntakeConstants.extensionConstants.simMotorConfig.config));

        transfer =
            new Transfer(Mechanism.create(TransferConstants.beltConstants.simMotorConfig.config));

        shooter =
            new Shooter(Mechanism.create(ShooterMecConstants.frontConstants.simMotorConfig.config),
                ShooterMecConstants.backRightConstants.simMotorConfig.config,
                Mechanism.create(ShooterMecConstants.indexerConstants.simMotorConfig.config));

        Drive.init(SwerveModuleConstants.driveConstants.simMotorConfig.config,
            SwerveModuleConstants.rotationConstants.simMotorConfig.config,
            new GyroIO() {});

        break;

      default:

        intake = new Intake(new MotorIOConfig() {}, Mechanism.empty());

        transfer = new Transfer(Mechanism.empty());

        shooter = new Shooter(Mechanism.empty(), new MotorIOConfig() {}, Mechanism.empty());

        Drive.init();

        break;

    }
    LimelightVision.init();

    superStructure = new SuperStructure(Drive.getInstance(), shooter, transfer, intake);

    // Event triggers for pathPlanner
    new EventTrigger("openIntake").whileTrue(Commands
        .run(() -> superStructure.intake.setState(IntakeState.IDLE, ExtensionState.EXTENDED)));

    new EventTrigger("intake").whileTrue(Commands.run(() -> {
      superStructure.intake.setState(IntakeState.INTAKING, ExtensionState.EXTENDED);
      Logger.recordOutput("auto/intake", true);
    }));



    // Set up auto routines
    autoChooser = new ManageableLoggedDashboardChooser<>("auto");
    autoChooser.setDefaultOption("left Cycle (depot)", Auto.poc(superStructure));

    autoChooser.reloadDefaultOption("Auto");
    autoChooser.onChangeSave("Auto");

    leftSidePoseChooser = new ManageableLoggedDashboardChooser<>("side pose");
    // when the limelight is near the roborio the value should be left
    leftSidePoseChooser.setDefaultOption("left (roboRIO)", true);
    leftSidePoseChooser.addOption("right (switch)", false);

    leftSidePoseChooser.reloadDefaultOption("side pose");
    leftSidePoseChooser.onChangeSave("side pose");

    isRedAllianceChooser = new ManageableLoggedDashboardChooser<>("Robot Allince");

    isRedAllianceChooser.setDefaultOption("Red", true);
    isRedAllianceChooser.addOption("Blue", false);

    isRedAllianceChooser.reloadDefaultOption(AllianceUtil.getPrefrensesPath());
    isRedAllianceChooser.onChangeSave(AllianceUtil.getPrefrensesPath());
    // side limelight pipeline chooser

    // Configure the button bindings
    configureButtonBindings();
    ConstantsCreator.createConstantsFiles(this, LimelightVision.getInstance());

  }

  /**
   *
   * // * Use this method to define your button->command mappings.
   * Buttons can be created by instantiating a {@link GenericHID} or one
   * of its subclasses null null ({@link edu.wpi.first.wpilibj.Joystick}
   * or {@link XboxController}), and then passing it to a {@link
   *
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {

  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot }
   * class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // return Commands.none();
    return autoChooser.get();

  }

  public boolean isSideAtLeftPose() {
    return leftSidePoseChooser.get();
  }

  public void resetSimulation() {}

  public void updateSimulation() {
    double extensionPose = -intake.extension.getMecPosition();
    double dx = extensionPose * Math.cos(Units.degreesToRadians(30));
    double dz = extensionPose * Math.sin(Units.degreesToRadians(30));

    Logger.recordOutput("Simulation/Intake", new Pose3d(dx, 0, dz, Rotation3d.kZero));

    Logger.recordOutput("Simulation/Shooter", Pose3d.kZero);

  }

}
