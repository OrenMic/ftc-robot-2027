package miscar.limeLightVision;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.littletonrobotics.junction.Logger;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Ellipse2d;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.Mode;
import frc.robot.subsystems.drive.Drive;
import frc.robot.util.LoggedTracer;
import miscar.configs.vision.LimelightConfig;
import miscar.kalman.Ellipse2dUtil;
import miscar.limeLightVision.LimeLightInputs.PoseObservation;
import miscar.limeLightVision.LimeLightInputs.VisionConsumer;

public class Vision extends SubsystemBase {
  {
    setName("limeLightVision");
  }

  public final Limelight[] ios;
  private final VisionConsumer consumer;
  private final Alert[] disconnectedAlerts;
  private final Alert[] tempAlerts;
  protected final Function<Limelight, PoseObservation[]> visionCalc;

  public Vision(VisionConsumer consumer, Function<Limelight, PoseObservation[]> visionCalc,
      Limelight... ios) {
    this.ios = ios;
    this.consumer = consumer;
    this.visionCalc = visionCalc;

    // Initialize disconnected alerts
    this.disconnectedAlerts = new Alert[ios.length];
    this.tempAlerts = new Alert[ios.length];
    for (int i = 0; i < ios.length; i++) {
      disconnectedAlerts[i] =
          new Alert("Limelight " + Integer.toString(i) + " is disconnected.", AlertType.kError);
      tempAlerts[i] =
          new Alert("Limelight " + Integer.toString(i) + " is to hot.", AlertType.kWarning);
    }
  }

  public Vision(VisionConsumer consumer, Function<Limelight, PoseObservation[]> visionCalc,
      LimelightConfig... configs) {
    this(consumer, visionCalc, createLimelights(configs));

  }

  private static Limelight[] createLimelights(LimelightConfig... configs) {
    Limelight[] limelights = new Limelight[configs.length];
    for (int i = 0; i < configs.length; i++) {
      limelights[i] = new Limelight(configs[i], Drive.getInstance()::getRotation);
    }
    return limelights;


  }

  @Override
  public void periodic() {
    // LoggedTracer.reset();
    List<PoseObservation> observations = new ArrayList<>();
    for (int i = 0; i < ios.length; i++) {
      ios[i].updateInputs();
      Logger.processInputs("Limelights/" + Integer.toString(i), ios[i].inputs);
      disconnectedAlerts[i].set(!ios[i].inputs.connected && Constants.currentMode != Mode.SIM);
      tempAlerts[i].set(ios[i].getTemp() > 70.0);
      PoseObservation[] poseObservations = visionCalc.apply(ios[i]);

      observations.addAll(List.of(poseObservations));
    }
    Logger.recordOutput(getName() + "/accepted",
        observations.stream().map(PoseObservation::pose).toArray(Pose2d[]::new));

    Logger.recordOutput(getName() + "/accepted/covariance",
        observations.stream()
            .map(pose -> Ellipse2dUtil.toCovarianceEllipse(pose.linearStdDev(), pose.pose()))
            .toArray(Ellipse2d[]::new));

    if (observations.size() > 0) {

      Pose2d[] points = observations.stream().map(PoseObservation::pose).toArray(Pose2d[]::new);
      double[] stdDevs = observations.stream().mapToDouble(PoseObservation::linearStdDev).toArray();
      double[] angularStdDevs =
          observations.stream().mapToDouble(PoseObservation::anglerStdDev).toArray();
      double[] timestamps = observations.stream().mapToDouble(PoseObservation::timestamp).toArray();

      var merged = mergePoses(points, stdDevs, angularStdDevs, timestamps);

      Logger.recordOutput(getName() + "/accepted/mergedCovariance",
          new Ellipse2d[] {
              Ellipse2dUtil.toCovarianceEllipse(merged.linearStdDev(), merged.pose())});

      consumer.accept(merged.pose(),
          merged.timestamp(),
          VecBuilder.fill(merged.linearStdDev(), merged.linearStdDev(), merged.anglerStdDev()));
      // consumer.accept(Pose2d.kZero, -1, VecBuilder.fill(0, 0, 0));
    } else {
      Logger.recordOutput(getName() + "/accepted/mergedCovariance", new Ellipse2d[] {});
      // consumer.accept(Pose2d.kZero, -1, VecBuilder.fill(0, 0, 0));
    }

    LoggedTracer.record(getName());
  }

  public static PoseObservation mergePoses(Pose2d[] poses, double[] stdDevs,
      double[] angularStdDevs, double[] timestamps) {

    if (poses.length != stdDevs.length || poses.length != angularStdDevs.length
        || timestamps.length != poses.length) {
      throw new IllegalArgumentException("All arrays must have the same length");
    }

    if (poses.length == 0) {
      throw new IllegalArgumentException("Must provide at least one pose");
    }

    // -------------------------
    // Position
    // -------------------------

    double weightedX = 0.0;
    double weightedY = 0.0;
    double totalPositionWeight = 0.0;

    for (int i = 0; i < poses.length; i++) {
      double sigma = stdDevs[i];

      if (sigma <= 0.0) {
        throw new IllegalArgumentException("Position standard deviations must be > 0");
      }

      double weight = 1.0 / (sigma * sigma);

      weightedX += poses[i].getX() * weight;
      weightedY += poses[i].getY() * weight;

      totalPositionWeight += weight;
    }

    double mergedX = weightedX / totalPositionWeight;
    double mergedY = weightedY / totalPositionWeight;

    double mergedStdDev = 1.0 / Math.sqrt(totalPositionWeight);

    // -------------------------
    // Rotation
    // -------------------------

    double weightedSin = 0.0;
    double weightedCos = 0.0;
    double totalAngularWeight = 0.0;

    for (int i = 0; i < poses.length; i++) {
      double sigma = angularStdDevs[i];

      if (sigma <= 0.0) {
        throw new IllegalArgumentException("Angular standard deviations must be > 0");
      }

      double weight = 1.0 / (sigma * sigma);
      double angle = poses[i].getRotation().getRadians();

      weightedSin += Math.sin(angle) * weight;
      weightedCos += Math.cos(angle) * weight;

      totalAngularWeight += weight;
    }

    double mergedAngle = Math.atan2(weightedSin, weightedCos);

    double mergedAngularStdDev = 1.0 / Math.sqrt(totalAngularWeight);

    Pose2d mergedPose = new Pose2d(mergedX, mergedY, new Rotation2d(mergedAngle));

    double mergedTimestamp = 0.0;
    for (double timestamp : timestamps) {
      mergedTimestamp += timestamp;
    }
    mergedTimestamp /= timestamps.length;
    return new PoseObservation(mergedPose, mergedTimestamp, mergedStdDev, mergedAngularStdDev);
  }

  public Pose2d getBestPose() {
    Pose2d bestPose = Pose2d.kZero;

    double bestStdDev = Double.POSITIVE_INFINITY;

    for (Limelight io : ios) {
      PoseObservation[] poseObservations = visionCalc.apply(io);

      for (PoseObservation poseObservation : poseObservations) {
        if (poseObservation.linearStdDev() < bestStdDev) {
          bestStdDev = poseObservation.linearStdDev();
          bestPose = poseObservation.pose();
        }
      }
    }

    return bestPose;
  }
}
