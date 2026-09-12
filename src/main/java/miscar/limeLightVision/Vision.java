package miscar.limeLightVision;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.Mode;
import frc.robot.subsystems.drive.Drive;
import frc.robot.util.LoggedTracer;
import java.util.function.Function;
import miscar.configs.vision.LimelightConfig;
import miscar.limeLightVision.LimeLightInputs.PoseObservation;
import miscar.limeLightVision.LimeLightInputs.VisionConsumer;
// import miscar.vision.Vision.VisionConsumer;
import org.littletonrobotics.junction.Logger;

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
    for (int i = 0; i < ios.length; i++) {
      ios[i].updateInputs();
      Logger.processInputs("Limelights/" + Integer.toString(i), ios[i].inputs);
      disconnectedAlerts[i].set(!ios[i].inputs.connected && Constants.currentMode != Mode.SIM);
      tempAlerts[i].set(ios[i].getTemp() > 70.0);
      PoseObservation[] poseObservations = visionCalc.apply(ios[i]);

      for (PoseObservation observation : poseObservations) {
        consumer.accept(observation.pose(),
            observation.timestamp(),
            VecBuilder.fill(observation.linearStdDev(),
                observation.linearStdDev(),
                observation.anglerStdDev()));
      }
    }

    LoggedTracer.record(getName());
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
