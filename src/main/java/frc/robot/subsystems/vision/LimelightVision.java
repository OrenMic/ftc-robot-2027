package frc.robot.subsystems.vision;

import static frc.robot.generated.limelightVision.LimelightVisionConstants.frontLeftConstants;
import static frc.robot.generated.limelightVision.LimelightVisionConstants.frontRightConstants;
import static frc.robot.generated.limelightVision.LimelightVisionConstants.sideConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.littletonrobotics.junction.Logger;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.util.Units;
import frc.robot.generated.SeasonConfigs.VisionConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.util.GeomUtil;
import lombok.experimental.ExtensionMethod;
import miscar.annotation.ConstantsUser;
import miscar.annotation.CreateConstants;
import miscar.annotation.Singleton;
import miscar.configs.vision.LimelightConfig;
import miscar.limeLightVision.LimeLightInputs.PoseData;
import miscar.limeLightVision.LimeLightInputs.PoseObservation;
import miscar.util.AllianceUtil;
import miscar.limeLightVision.Limelight;
import miscar.limeLightVision.Vision;

@Singleton
@ConstantsUser
@ExtensionMethod({GeomUtil.class, Math.class, Units.class})
public class LimelightVision extends Vision {

  private static LimelightVision instance;

  @CreateConstants(configType = LimelightConfig.class)
  private Limelight frontRight;

  @CreateConstants(configType = LimelightConfig.class)
  private Limelight frontLeft;

  @CreateConstants(configType = LimelightConfig.class)
  private Limelight side;

  private static int visionResets = 0;

  List<Integer> blackListedAprilTags = new ArrayList<>();


  public static LimelightVision getInstance() {
    return instance;
  }

  public void resetVisionResets() {
    visionResets = 0;
  }



  public static void init() {
    instance = new LimelightVision();
  }

  private LimelightVision() {
    super(Drive.getInstance()::addVisionMeasurement, LimelightVision::filterPoses,
        frontRightConstants.limelightConstants.config, frontLeftConstants.limelightConstants.config,
        sideConstants.limelightConstants.config);
    frontRight = ios[0];
    frontLeft = ios[1];
    side = ios[2];

    frontRight.setEnabled(true);
    frontLeft.setEnabled(true);
    side.setEnabled(true);

    // red tranchs
    // left tranch
    blackListedAprilTags.add(6);
    blackListedAprilTags.add(7);
    // right tranch
    blackListedAprilTags.add(1);
    blackListedAprilTags.add(12);

    // blue tranchs
    // left tranch
    blackListedAprilTags.add(22);
    blackListedAprilTags.add(23);
    // right tranch
    blackListedAprilTags.add(17);
    blackListedAprilTags.add(28);


    // blue human
    blackListedAprilTags.add(29);
    blackListedAprilTags.add(30);

    // blue tower
    blackListedAprilTags.add(31);
    blackListedAprilTags.add(32);


    // red human
    blackListedAprilTags.add(13);
    blackListedAprilTags.add(14);

    // red tower
    blackListedAprilTags.add(15);
    blackListedAprilTags.add(16);


    // hubs
    if (AllianceUtil.isRedAlliance()) {
      // blue hub
      // right
      blackListedAprilTags.add(18);
      blackListedAprilTags.add(27);
      // far
      blackListedAprilTags.add(19);
      blackListedAprilTags.add(20);
      // left
      blackListedAprilTags.add(21);
      blackListedAprilTags.add(24);
      // near
      blackListedAprilTags.add(25);
      blackListedAprilTags.add(26);
    } else {
      // red hub
      // right
      blackListedAprilTags.add(11);
      blackListedAprilTags.add(2);
      // far
      blackListedAprilTags.add(4);
      blackListedAprilTags.add(3);
      // left
      blackListedAprilTags.add(9);
      blackListedAprilTags.add(10);
      // near
      blackListedAprilTags.add(8);
      blackListedAprilTags.add(5);

    }
  }

  public static PoseObservation[] filterPoses(Limelight limelight) {
    if (!limelight.isEnabled()
        || getInstance().blackListedAprilTags.contains(limelight.getPrimeryTag()))
      return new PoseObservation[] {};

    OptionalDouble stddevBotPose1 = calcBotPose1StdDev(limelight);
    OptionalDouble stddevBotPose2 = calcBotPose2StdDev(limelight);
    OptionalDouble stddevLimitles = calcBotPose1StdDevLimitless(limelight);

    ArrayList<PoseObservation> poseObservations = new ArrayList<>();
    ArrayList<Pose2d> rejectedPoses = new ArrayList<>();

    if (limelight.useMegaBotPose1) {
      Optional<PoseData> pose = limelight.getBotPose1();

      if (pose.isPresent()) {
        visionResets++;
        if (stddevBotPose1.isPresent()) {
          poseObservations.add(new PoseObservation(pose.get().pose(), pose.get().timestamp(),
              stddevBotPose1.getAsDouble(), Units.degreesToRadians(25)));
        } else if (stddevLimitles.isPresent() && visionResets < 100) {
          poseObservations.add(new PoseObservation(pose.get().pose(), pose.get().timestamp(),
              stddevLimitles.getAsDouble(), Units.degreesToRadians(25)));
        } else {
          rejectedPoses.add(pose.get().pose());
        }
      }
    }

    if (limelight.useMegaBotPose2 && stddevBotPose1.isEmpty()) {
      Optional<PoseData> pose = limelight.getBotPose2();

      if (pose.isPresent()) {
        if (stddevBotPose2.isPresent()) {
          poseObservations.add(new PoseObservation(pose.get().pose(), pose.get().timestamp(),
              stddevBotPose2.getAsDouble(), Double.POSITIVE_INFINITY));
        } else {
          rejectedPoses.add(pose.get().pose());
        }
      }
    }

    Logger.recordOutput("LimelightVision/filterPoses/" + limelight.name + "/stdDev/BotPose1",
        stddevBotPose1.orElse(-1));
    Logger.recordOutput("LimelightVision/filterPoses/" + limelight.name + "/stdDev/BotPose2",
        stddevBotPose2.orElse(-1));

    Logger.recordOutput("LimelightVision/filterPoses/" + limelight.name + "/rejected",
        rejectedPoses.toArray(Pose2d[]::new));
    Logger.recordOutput("LimelightVision/filterPoses/" + limelight.name + "/accepted",
        poseObservations.stream().map((pose) -> pose.pose()).toArray(Pose2d[]::new));
    PoseObservation[] observations = poseObservations.toArray(PoseObservation[]::new);

    return observations;
  }

  private static OptionalDouble calcBotPose1StdDev(Limelight limelight) {
    if (limelight.getTargetCount() == 0 || !limelight.isEnabled()) {
      return OptionalDouble.empty();
    }

    if (Math.abs(Drive.getInstance().getRoll()) > 5.0
        || Math.abs(Drive.getInstance().getPitch()) > 5.0) {
      return OptionalDouble.empty();
    }

    // Logger.recordOutput("limelights/" + limelight.name +
    // "/limelight.getYawToTag()",
    // limelight.getYawToTag().orElse(50));
    // if (limelight.getTargetCount() <= 1 &&
    // Math.abs(limelight.getYawToTag().orElse(50)) > 40) {
    // return OptionalDouble.empty();
    // }

    OptionalDouble disToTagMeter = limelight.getDistanceToTag();
    Logger.recordOutput("limelights/" + limelight.name + "/limelight.getTargetCount()",
        limelight.getTargetCount());
    Logger.recordOutput("limelights/" + limelight.name + "/limelight.distance()",
        limelight.getDistanceToTag().orElse(-1));

    if (disToTagMeter.orElse(99) > 1.7) {
      return OptionalDouble.empty();
    }

    if (disToTagMeter.orElse(99) < 0.15) {
      return OptionalDouble.of(0.1);
    }

    return OptionalDouble.of(
        Math.max(
            (0.2 * Math.pow(disToTagMeter.getAsDouble(), 3) / limelight.getTargetCount())
                * limelight.getCameraStdDevFactors(),
            0.15));
  }

  private static OptionalDouble calcBotPose1StdDevLimitless(Limelight limelight) {
    OptionalDouble disToTagMeter = limelight.getDistanceToTag();
    if (disToTagMeter.isEmpty() || limelight.getTargetCount() == 0 || !limelight.isEnabled()) {
      return OptionalDouble.empty();
    }

    if (disToTagMeter.orElse(99) < 0.15) {
      return OptionalDouble.of(0.1);
    }

    return OptionalDouble.of(
        Math.max(
            (0.2 * Math.pow(disToTagMeter.getAsDouble(), 3) / limelight.getTargetCount())
                * limelight.getCameraStdDevFactors(),
            0.15));
  }

  private static OptionalDouble calcBotPose2StdDev(Limelight limelight) {
    if (limelight.getTargetCount() == 0 || !limelight.isEnabled()) {
      return OptionalDouble.empty();
    }

    if (Math.abs(Drive.getInstance().getRoll()) > 10.0
        || Math.abs(Drive.getInstance().getPitch()) > 10.0) {
      return OptionalDouble.empty();

    }
    // if (limelight.getTargetCount() <= 1 &&
    // Math.abs(limelight.getYawToTag().orElse(50)) > 40) {
    // return OptionalDouble.empty();
    // }

    OptionalDouble disToTagMeter = limelight.getDistanceToTag();

    if (disToTagMeter.orElse(99) > 4.5) {
      return OptionalDouble.empty();
    }

    if (disToTagMeter.orElse(99) < 0.15) {
      return OptionalDouble.of(0.1);
    }

    return OptionalDouble.of(Math.max(
        (0.25 * Math.pow(disToTagMeter.getAsDouble(), 3) / limelight.getTargetCount())
            * limelight.getCameraStdDevFactors() * VisionConstants.linearStdDevMegatag2Factor,
        0.15));
  }

  @Override
  public Pose2d getBestPose() {
    Pose2d bestPose = Pose2d.kZero;

    double bestStdDev = Double.POSITIVE_INFINITY;

    for (Limelight io : ios) {
      OptionalDouble stdDev = calcBotPose1StdDevLimitless(io);
      Optional<PoseData> pose = io.getBotPose1();
      if (!pose.isPresent() || !stdDev.isPresent())
        continue;

      if (stdDev.getAsDouble() < bestStdDev) {
        bestStdDev = stdDev.getAsDouble();
        bestPose = pose.get().pose();
      }
    }

    return bestPose;
  }

  public Optional<Pose2d> getClosestPose(Pose2d pose, double tolerance) {
    double bestDistance = tolerance;
    OptionalDouble bestStdDev = OptionalDouble.empty();
    Optional<Pose2d> bestPose = Optional.empty();

    for (Limelight io : ios) {
      OptionalDouble stdDev = calcBotPose1StdDevLimitless(io);
      Optional<PoseData> botPose1 = io.getBotPose1();
      if (!botPose1.isPresent() || !stdDev.isPresent())
        continue;

      double distance = botPose1.get().pose().getTranslation().getDistance(pose.getTranslation());
      if (distance + stdDev.getAsDouble() < bestDistance
          + bestStdDev.orElse(stdDev.getAsDouble())) {
        bestDistance = distance;
        bestStdDev = stdDev;
        bestPose = Optional.of(botPose1.get().pose());
      }
    }

    return bestPose;
  }

  public void setSidePose(Pose3d pose) {
    side.setCameraPose(pose);
  }

  public Pose3d getSidePose() {
    return side.getCameraPose();
  }

  public void rewindGame() {
    frontRight.recordRewind(165);
    frontLeft.recordRewind(165);
  }

  @Override
  public void periodic() {
    super.periodic();
    for (Limelight limelight : ios) {
      Logger.recordOutput(getName() + "/" + limelight.name + "/targetCount",
          limelight.getTargetCount());
      Logger.recordOutput(getName() + "/" + limelight.name + "/hasTarget",
          limelight.getTargetCount() != 0);
    }

  }
}
