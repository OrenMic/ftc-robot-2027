package miscar.limeLightVision;

import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.numbers.N3;
import org.littletonrobotics.junction.AutoLog;

@AutoLog
public class LimeLightInputs {
  public boolean connected = false;
  // double[] camerapose_robotspace = new double[] {};
  // double[] camerapose_targetpace = new double[] {};
  public double[] botpose_orb_wpiblue = new double[] {};
  public double[] botpose_wpiblue = new double[] {};
  // public double[] crosshairs = new double[] {};
  public double[] hw = new double[] {};
  // public double[] imu = new double[] {};
  double[] llpython = new double[] {};
  // public double[] rawdetections = new double[] {};
  // public double[] rawfiducials = new double[] {};
  // public double[] rawtargets = new double[] {};
  public double[] robot_orientation_set = new double[] {};
  public double[] rewind = new double[] {};
  public double[] t2d = new double[] {};
  public double[] targetpose_cameraspace = new double[] {};
  // public double[] targetpose_robotspace = new double[] {};
  // public double[] tc = new double[] {};

  public double cl = 0;
  public int getpipe = 0;
  public double hb = 0;
  // public double pipeline = 0;
  public double ta = 0;
  public double tid = 0;
  public double tl = 0;
  public double tv = 0;
  public double tx = 0;
  // public double txnc = 0;
  public double ty = 0;
  // public double tync = 0;

  // public String getpipetype = "none";

  // public Pose2d botPose1 = Pose2d.kZero;
  // public Pose2d botPose2 = Pose2d.kZero;

  public record PoseObservation(Pose2d pose, double timestamp, double linearStdDev,
      double anglerStdDev) {
    // public static PoseObservation kzero = new
    // PoseObservation(Pose2d.kZero, 0, 0, 0);
  }

  public record PoseData(Pose2d pose, double timestamp) {
    public static PoseData kzero = new PoseData(Pose2d.kZero, -1);
  }

  @FunctionalInterface
  public static interface VisionConsumer {
    public void accept(Pose2d visionRobotPoseMeters, double timestampSeconds,
        Vector<N3> visionMeasurementStdDevs);
  }
}
