
package frc.robot.subsystems.drive;

import static frc.robot.Tuners.Drive.rotarionalKp;
import static frc.robot.Tuners.Drive.rotationalKd;
import static frc.robot.Tuners.Drive.rotationalKi;
import static frc.robot.Tuners.Drive.translationalKd;
import static frc.robot.Tuners.Drive.translationalKi;
import static frc.robot.Tuners.Drive.translationalKp;
import static frc.robot.generated.swerveModule.SwerveModuleConstants.driveConstants;
import static frc.robot.generated.swerveModule.SwerveModuleConstants.rotationConstants;
import java.util.List;
import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.util.PathPlannerLogging;
import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.Constants.Mode;
import frc.robot.Tuners;
import frc.robot.generated.SeasonConfigs.VisionConstants;
import frc.robot.subsystems.drive.commands.DriveCommands;
import frc.robot.subsystems.drive.util.DriveUtil;
import frc.robot.subsystems.shooter.util.ShootingUtil;
import frc.robot.subsystems.vision.LimelightVision;
import frc.robot.util.Elastic;
import frc.robot.util.GeomUtil;
import frc.robot.util.LoggedTracer;
import lombok.Getter;
import lombok.Setter;
import miscar.annotation.ConstantsUser;
import miscar.annotation.Singleton;
import miscar.configs.motors.MotorIOConfig;
import miscar.gyro.GyroIO;
import miscar.util.AllianceUtil;

@ConstantsUser
@Singleton
public class Drive extends miscar.swerve.VisionedSwerve {
  {
    setName(Tuners.Drive.getName());
  }

  @Getter
  @Setter
  private double speedCap = 5;
  private static Drive instance;

  @Getter
  private DriveState state = DriveState.IDLE;
  public static Field2d field = new Field2d();
  private static final double robotSize = 0.5598; // 0.6106;

  Debouncer isRobotInPose = new Debouncer(Constants.currentMode != Mode.SIM ? 0.1 : 0.0);


  @Getter
  private static Pose2d lockPose = new Pose2d(1, 1, Rotation2d.kZero);

  @Getter
  private Pose2d lastLockPose = lockPose;
  public DriveCommandManager commandManager;

  public LoggedNetworkBoolean resetPathPlannerPid =
      new LoggedNetworkBoolean(getName() + "/resetPathPlannerPid", false);

  private String LastLockposeLogPath = getName() + "/LastLockpose";
  private String lockPoseLogPath = getName() + "/lockpose";
  private String runningCommandLogPath = getName() + "/runningCommand";

  private String stateLogPath = getName() + "/state";
  private String isRobotInPoseLogPath = getName() + "/is robot in pose";

  private static ModuleConfig moduleConfig =
      new ModuleConfig(0.05207, 3.5, 1, DCMotor.getKrakenX60(1), 10.909, 9, 1);

  private static RobotConfig config =
      new RobotConfig(15.000, 1.500, moduleConfig, createModulePosesUsing(robotSize));

  {
    configureAutoBuilder();

    PathPlannerLogging.setLogActivePathCallback((trajectory) -> {
      Logger.recordOutput(getName() + "/pathPlanner/activePath",
          trajectory.stream().toArray(Pose2d[]::new));
      field.getObject("Trajectory").setPoses(trajectory);
    });
    PathPlannerLogging.setLogTargetPoseCallback((pose) -> {
      Logger.recordOutput(getName() + "/pathPlanner/targetPose", pose);
    });
  }

  public void configureAutoBuilder() {
    AutoBuilder.configure(this::getPose, // Robot pose supplier
        this::setPose, // Method to reset odometry (will be called if your auto has a
        // starting pose)
        this::getChassisSpeed, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
        (speeds, feedforwards) -> robotRelativeDrive(speeds), // Method that
        // will
        // drive the robot
        // given ROBOT RELATIVE ChassisSpeeds.
        // Also optionally outputs individual
        // module feedforwards
        // controller for holonomic drive trains
        new PPHolonomicDriveController( // PPHolonomicController is the built in path following
            // controller for holonomic drive trains
            new PIDConstants(translationalKp.getAsDouble(), translationalKi.getAsDouble(),
                translationalKd.getAsDouble()), // Translation PID constants
            new PIDConstants(rotarionalKp.getAsDouble(), rotationalKi.getAsDouble(),
                rotationalKd.getAsDouble()) // Rotation PID constants
        ),
        config, // The robot configuration
        () -> {
          // Boolean supplier that controls when the path will be mirrored for
          // the red alliance
          // This will flip the path being followed to the red side of the
          // field.
          // THE ORIGIN WILL REMAIN ON THE BLUE SIDE

          return AllianceUtil.isRedAlliance();
        },
        this
    // this // Reference to this subsystem to set requirements
    );
  }

  public static void init(MotorIOConfig driveConfig, MotorIOConfig rotationConfig, GyroIO gyro) {
    instance = new Drive(driveConfig, rotationConfig, gyro);
    SmartDashboard.putData(field);
  }

  public static void init() {
    instance = new Drive();
    SmartDashboard.putData(field);
  }

  public static Drive getInstance() {
    return instance;
  }

  private Drive(MotorIOConfig driveConfig, MotorIOConfig rotationConfig, GyroIO gyro) {
    super(driveConfig, driveConstants.mecRatiosConstants.config, rotationConfig,
        rotationConstants.throughBoreEncoderConfig.config,
        rotationConstants.mecRatiosConstants.config, gyro, robotSize);
    commandManager = new DriveCommandManager(this);
    commandManager.scheduleByState(state);
  }

  private Drive() {
    super(driveConstants.mecRatiosConstants.config, rotationConstants.mecRatiosConstants.config,
        robotSize, rotationConstants.throughBoreEncoderConfig.config);
    commandManager = new DriveCommandManager(this);
    commandManager.scheduleByState(state);
  }

  @Override
  public void addVisionMeasurement(Pose2d visionRobotPoseMeters, double timestampSeconds,
      Matrix<N3, N1> visionMeasurementStdDevs) {
    super.addVisionMeasurement(visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);
  }

  public void restPoseByVision() {
    Pose2d correctPose = getPose();

    Pose2d newPose = LimelightVision.getInstance().getBestPose();
    Logger.recordOutput("bestPose", newPose);
    if (!newPose.equals(Pose2d.kZero)) {
      correctPose = newPose;
    }

    setPose(correctPose);
  }

  public double getDistanceToTag(int tagID) {
    Pose2d robotPose = getPose();
    Pose2d tagPose = VisionConstants.aprilTagLayout.getTagPose(tagID).get().toPose2d();

    return robotPose.getTranslation().getDistance(tagPose.getTranslation());
  }

  @AutoLogOutput(key = "getClosestTagDistance")
  public double getClosestTagDistance() {
    List<AprilTag> tags = VisionConstants.aprilTagLayout.getTags();
    Pose2d robotPose = getPose();
    return tags.stream()
        .mapToDouble(
            (tag) -> tag.pose.toPose2d().getTranslation().getDistance(robotPose.getTranslation()))
        .min().getAsDouble();
  }

  public void setPose(Pose2d pose) {
    poseEstimator.resetPosition(rawGyroRotation, getModulePositions(), pose);
  }

  @Override
  public void robotRelativeDrive(ChassisSpeeds speeds) {

    super.robotRelativeDrive(new ChassisSpeeds(
        Math.abs(speeds.vxMetersPerSecond) > 3 ? Math.copySign(3, speeds.vxMetersPerSecond)
            : speeds.vxMetersPerSecond,
        Math.abs(speeds.vyMetersPerSecond) > 3 ? Math.copySign(3, speeds.vyMetersPerSecond)
            : speeds.vyMetersPerSecond,
        speeds.omegaRadiansPerSecond));
  }

  public void setState(DriveState wantedState) {
    state = wantedState;
    commandManager.scheduleByState(wantedState);
  }


  public void setLockToPosition(Pose2d lockPose) {
    Drive.lockPose = lockPose;
  }

  public void setLockToPosition(Translation2d lockPose) {
    setLockToPosition(GeomUtil.toPose2d(lockPose));
  }

  public void setLockToAngle(Rotation2d angle) {
    lockPose = GeomUtil.withRotation(getPose(), angle);
  }

  private void logToElastic() {
    Elastic.recordWidgetType("Drive/elastic", "EncoderdSwerveDrive");
    for (int i = 0; i < modules.length; i++) {
      Logger.recordOutput("Drive/elastic/Module " + i + "/Rotation",
          modules[i].getAngle().getRadians());
      Logger.recordOutput("Drive/elastic/Module " + i + "/Encoder",
          modules[i].getAngleByEncoder().getRadians());
    }
    Logger.recordOutput("Drive/elastic/Robot Angle", getRotation().getRadians());
  }


  @Override
  public void periodic() {
    super.periodic();
    field.setRobotPose(getPose());
    if (resetPathPlannerPid.getAsBoolean()) {
      configureAutoBuilder();
      resetPathPlannerPid.set(false);
    }



    DriveCommands.periodic();
    Logger.recordOutput(LastLockposeLogPath, lastLockPose);
    Logger.recordOutput(lockPoseLogPath, lockPose);
    lastLockPose = lockPose;

    Logger.recordOutput(runningCommandLogPath, DriveCommands.getActiveCommandName());
    Logger.recordOutput(stateLogPath, state);
    Logger.recordOutput(isRobotInPoseLogPath, DriveUtil.isRobotInPose());
    Logger.recordOutput(getName() + "/isRobotInTransition", DriveUtil.isRobotInTransition());
    Logger.recordOutput(getName() + "/isRobotInAngle", DriveUtil.isRobotInAngle());
    Logger.recordOutput(getName() + "/isRobotAlinedToShoot", ShootingUtil.isRobotAlinedToShoot());
    Logger.recordOutput(getName() + "/errorToVirtualHub", ShootingUtil.getErrorToVirtualHub());
    Logger.recordOutput(getName() + "/canDeliver", ShootingUtil.canDeliver());
    Logger.recordOutput(getName() + "/speedCap", speedCap);
    logToElastic();

    LoggedTracer.record(getName());
  }
}
