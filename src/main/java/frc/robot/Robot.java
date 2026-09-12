package frc.robot;

import com.ctre.phoenix6.SignalLogger;
import com.pathplanner.lib.commands.FollowPathCommand;
import com.pathplanner.lib.commands.PathfindingCommand;
import edu.wpi.first.math.MathShared;
import edu.wpi.first.math.MathSharedStore;
import edu.wpi.first.math.MathUsageId;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.generated.limelightVision.LimelightVisionConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.leds.Leds;
import frc.robot.subsystems.superStructure.SuperStructureState;
import frc.robot.subsystems.vision.LimelightVision;
import frc.robot.subsystems.intake.IntakeStates.IntakeState;
// import frc.robot.subsystems.leds.Leds;
import frc.robot.util.Elastic;
import frc.robot.util.HubShiftUtil;
import frc.robot.util.LoggedTracer;
import frc.robot.util.LoggedTunableNumber;
import frc.robot.util.LoggerUtil;
import frc.robot.util.PS5ControllerRumble.Pulse;
import frc.robot.util.PulsingUtil;
import frc.robot.util.logs.WPILOGWriter;
import static frc.robot.RobotContainer.driver;
import java.util.Random;
import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;
import miscar.util.AllianceUtil;
import miscar.util.BackgroundProcess;
import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;

/**
 * 7 The VM is configured to automatically run this class, and to call
 * the functions corresponding to each mode, as described in the
 * TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the project.
 */
public class Robot extends LoggedRobot {
  {
    // AllianceUtil.configureTo(Alliance.Blue);
    AllianceUtil.reloadConfiguration();
  }
  // private Command autonomousCommand;

  private RobotContainer robotContainer;

  // private Alert batteryLowAlert = new Alert("Battery is low",
  // AlertType.kWarning);
  private Alert batteryVeryLowAlert = new Alert("Battery is very low", AlertType.kError);

  private Command autoCommand;

  private String[] driverStars = new String[] {"Lior 👨🏼‍💻", "Liran 👨‍💻", "Scheimann ♟️🔌",
      "Pelag 🖥️", "Yoav🚗", "Idan Sahar 🚢💣", "Amit Bar🐻", "Working Autonomous💪"};

  private String[] pitStars = new String[] {"Silis😎", "Liran 👨‍💻", "Lior 👨🏼‍💻",
      "Scheimann ♟️🔌", "Mami🚗🔥", "Idan Sahar 👳‍♂️✈🏢🏢", "Working Autonomous💪"};

  private PulsingUtil shiftPulsing = new PulsingUtil(0.1);
  private Pulse[] controllerPulsing = new Pulse[1];
  {
    controllerPulsing[0] = new Pulse(RumbleType.kBothRumble, 1, 0.2);
    // controllerPulsing[1] = new Pulse(RumbleType.kBothRumble, 0, 0.2);
  }

  private final Alert jitAlert =
      new Alert("Please wait to enable, JITing in progress.", AlertType.kWarning);

  int brownoutCount = 0;
  boolean lastBrowntOut = false;

  LoggedTunableNumber shiftPulsingFeq = new LoggedTunableNumber("shiftPulsing", 0.2, false);

  boolean hasRobotExperiencedAuto = false;
  boolean hasRobotExperiencedTeleop = false;

  public Robot() {

    // Record metadata
    Logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME);
    Logger.recordMetadata("BuildDate", BuildConstants.BUILD_DATE);
    Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
    Logger.recordMetadata("GitDate", BuildConstants.GIT_DATE);
    Logger.recordMetadata("GitBranch", BuildConstants.GIT_BRANCH);
    switch (BuildConstants.DIRTY) {
      case 0:
        Logger.recordMetadata("GitDirty", "All changes committed");
        break;
      case 1:
        Logger.recordMetadata("GitDirty", "Uncomitted changes");
        break;
      default:
        Logger.recordMetadata("GitDirty", "Unknown");
        break;
    }

    // Set up data receivers & replay source
    switch (Constants.currentMode) {
      case REAL:
        // Running on a real robot, log to a USB stick ("/U/logs")
        Logger.addDataReceiver(new WPILOGWriter("/home/lvuser/logs"));
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case SIM:
        // Running a physics simulator, log to NT
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case REPLAY:
        // Replaying a log, set up replay source
        setUseTiming(false); // Run as fast as possible
        String logPath = LogFileUtil.findReplayLog();
        Logger.setReplaySource(new WPILOGReader(logPath));
        Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
        break;
    }

    // Start AdvantageKit logger
    Logger.start();
    Random random = new Random();
    int firstStar = 0;
    int secondStar = 0;
    firstStar = random.nextInt(driverStars.length);
    secondStar = random.nextInt(driverStars.length);

    while (firstStar == secondStar) {
      secondStar = random.nextInt(driverStars.length);
    }

    Logger.recordOutput("Stars/drivers/1", driverStars[firstStar]);
    Logger.recordOutput("Stars/drivers/2", driverStars[secondStar]);
    Logger.recordOutput("Stars/pit", pitStars[random.nextInt(pitStars.length)]);

    SignalLogger.enableAutoLogging(false);

    DriverStation.silenceJoystickConnectionWarning(true);

    // Instantiate our RobotContainer. This will perform all our button
    // bindings,
    // and put our autonomous chooser on the dashboard.
    robotContainer = new RobotContainer();

    // Silence Rotation2d warnings
    var mathShared = MathSharedStore.getMathShared();
    MathSharedStore.setMathShared(new MathShared() {
      @Override
      public void reportError(String error, StackTraceElement[] stackTrace) {
        if (error.startsWith("x and y components of Rotation2d are zero")) {
          return;
        }
        mathShared.reportError(error, stackTrace);
      }

      @Override
      public void reportUsage(MathUsageId id, int count) {
        mathShared.reportUsage(id, count);
      }

      @Override
      public double getTimestamp() {
        return mathShared.getTimestamp();
      }
    });
    Elastic.recordWidgetType("PowerDistribution",
        LoggerUtil.getRoot().getSubtable("PowerDistribution"));

    Leds.getInstance()
        .setColor(AllianceUtil.isRedAlliance() ? new Color(255, 0, 0) : new Color(0, 0, 255));

    PathfindingCommand.warmupCommand().schedule();
    FollowPathCommand.warmupCommand().schedule();
    // LimelightHelpers.setupPortForwardingUSB(0);
    // LimelightHelpers.setupPortForwardingUSB(1);
    // LimelightHelpers.setupPortForwardingUSB(2);

    Pose3d leftPose =
        LimelightVisionConstants.sideConstants.limelightConstants.backLeftRobotToCamera;

    LimelightVision.getInstance().setSidePose(leftPose);
    Logger.recordOutput("Robot/isRedAllince", AllianceUtil.isRedAlliance());
  }

  /**
   * Returns whether we should wait to enable because JIT optimizations
   * are in progress.
   */
  public static boolean isJITing() {
    return Timer.getTimestamp() < 45.0;
  }

  public static double timeUntilJITingDone() {
    double time = 45.0 - Timer.getTimestamp();
    return time > 0 ? time : 0;
  }

  @Override
  public void robotInit() {}

  /** This function is called periodically during all modes. */
  @Override
  public void robotPeriodic() {

    LoggedTracer.reset();

    BackgroundProcess.periodicAll();

    CommandScheduler.getInstance().run();

    double batteryVoltage = RobotController.getBatteryVoltage();
    Logger.recordOutput("batteryVoltage", batteryVoltage);
    Logger.recordOutput("Clock", (double) ((int) DriverStation.getMatchTime() * 100) / 100);

    // batteryLowAlert.set(batteryVoltage < 12);
    batteryVeryLowAlert.set(batteryVoltage < 10);

    // JIT alert
    jitAlert.set(isJITing());
    Logger.recordOutput("Robot/timeUntilJITingDone", timeUntilJITingDone());

    boolean currntBrownout = RobotController.isBrownedOut();
    if (currntBrownout != lastBrowntOut) {
      lastBrowntOut = currntBrownout;
      brownoutCount++;
    }
    Logger.recordOutput("Robot/brownoutCount", brownoutCount);

    LoggedTracer.record("Robot");
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    Elastic.selectTab(0);
    driver.stopRumble();
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
    robotContainer.superStructure.setState(SuperStructureState.DISABLED);

    boolean sideAtLeftPose = robotContainer.isSideAtLeftPose();

    Pose3d leftPose =
        LimelightVisionConstants.sideConstants.limelightConstants.backLeftRobotToCamera;
    Pose3d rightPose =
        LimelightVisionConstants.sideConstants.limelightConstants.backRightRobotToCamera;

    Pose3d newPose = sideAtLeftPose ? leftPose : rightPose;

    Pose3d currntPose = LimelightVision.getInstance().getSidePose();
    if (!currntPose.equals(newPose)) {
      LimelightVision.getInstance().setSidePose(newPose);
      LimelightVision.getInstance().resetVisionResets();
    }
  }

  /**
   * This autonomous runs the autonomous command selected by your
   * {@link RobotContainer} class.
   */
  @Override
  public void autonomousInit() {
    hasRobotExperiencedAuto = true;
    autoCommand = robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (autoCommand != null) {
      CommandScheduler.getInstance().schedule(autoCommand);
    }

    Elastic.selectTab(1);
    robotContainer.intake.setExtensionNaturalMode(NeutralMode.COAST);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    if (!hasRobotExperiencedAuto) {
      robotContainer.intake.setExtensionNaturalMode(NeutralMode.COAST);
    } // This makes sure that the autonomous stops running when
      // teleop starts running. If you want the autonomous to
      // continue until interrupted by another command, remove
      // this line or comment it out.
    if (autoCommand != null) {
      autoCommand.cancel();
    }

    robotContainer.superStructure.setState(SuperStructureState.INTAKING);

    HubShiftUtil.initialize();
    Elastic.selectTab(2);
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    shiftPulsing.setEndTime(shiftPulsingFeq.getAsDouble());
    boolean shiftTrueActive = HubShiftUtil.getActive();
    boolean shiftActive = HubShiftUtil.getActive();
    double remianingTime = HubShiftUtil.remianingTime();
    boolean pulsing = false;
    if (HubShiftUtil.getActive()) {
      if (remianingTime < 7) {
        pulsing = true;
        if (shiftPulsing.timesHasPassed() == 0) {
          shiftActive = !shiftActive;
        }
        if (shiftPulsing.timesHasPassed() > 2) {
          shiftPulsing.restart();
        }
      }
    } else {
      if (remianingTime < 10) {
        pulsing = true;
        if (shiftPulsing.timesHasPassed() == 0) {
          shiftActive = !shiftActive;
        }
        if (shiftPulsing.timesHasPassed() > 2) {
          shiftPulsing.restart();
        }
      }
    }
    String activeColor;

    if (pulsing && !shiftTrueActive) {
      activeColor = (shiftActive ? "#00FF00" : "#000000");
    } else if (pulsing) {
      activeColor = (shiftActive ? "#ff0000" : "#000000");
    } else {
      activeColor = (shiftActive ? "#00FF00" : "#ff0000");
    }

    if (robotContainer.superStructure.intake.getIntakeState() == IntakeState.INTAKING) {
      driver.rumblePulse(controllerPulsing);
    } else {
      driver.stopRumble();
    }


    Logger.recordOutput("HubShiftUtil/active", activeColor);
    Logger.recordOutput("HubShiftUtil/firstActiveAlliance",
        HubShiftUtil.getFirstActiveAllianceRaw() == Alliance.Red ? "B" : "R");
    Logger.recordOutput("HubShiftUtil/remianingTime", HubShiftUtil.remianingTime());
    Logger.recordOutput("HubShiftUtil/currntShift",
        HubShiftUtil.getShiftedShiftInfo().currentShift());
  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();

    robotContainer.intake.disintegrate();
    robotContainer.shooter.disintegrate();
    robotContainer.transfer.disintegrate();
    Drive.getInstance().disintegrate();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /**
   * This function is called once when the robot is first started up.
   */

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {
    robotContainer.updateSimulation();
  }
}
