package miscar.limeLightVision;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.DoubleArraySubscriber;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.IntegerSubscriber;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Timer;
import lombok.Getter;
import lombok.Setter;
import miscar.configs.vision.LimelightConfig;
import miscar.limeLightVision.LimeLightInputs.PoseData;

public class Limelight {

  @Setter
  @Getter
  private boolean enabled = true;

  private Supplier<Rotation2d> rotationSupplier;


  private final DoubleArrayPublisher orientationPublisher;
  private final DoubleArrayPublisher cameraposePublisher;
  private final DoubleArrayPublisher rewindPublisher;
  private final IntegerPublisher pipelinePublisher;
  private final DoubleArraySubscriber rewindSubscriber;

  private final DoubleArraySubscriber botpose_orb_wpiblue;
  private final DoubleArraySubscriber botpose_wpiblue;
  // private final DoubleArraySubscriber camerapose_robotspace;
  // private final DoubleArraySubscriber camerapose_targetpace;
  // private final DoubleArraySubscriber crosshairs;
  private final DoubleArraySubscriber hw;
  // private final DoubleArraySubscriber imu;
  private final DoubleArraySubscriber llpython;
  // private final DoubleArraySubscriber rawdetections;
  // private final DoubleArraySubscriber rawfiducials;
  // private final DoubleArraySubscriber rawtargets;
  private final DoubleArraySubscriber robot_orientation_set;
  private final DoubleArraySubscriber t2d;
  private final DoubleArraySubscriber targetpose_cameraspace;
  // private final DoubleArraySubscriber targetpose_robotspace;
  // private final DoubleArraySubscriber tc;

  private final DoubleSubscriber cl;
  private final IntegerSubscriber getpipe;
  private final DoubleSubscriber hb;
  // private final DoubleSubscriber pipeline;
  private final DoubleSubscriber ta;
  private final DoubleSubscriber tid;
  private final DoubleSubscriber tl;
  private final DoubleSubscriber tv;
  private final DoubleSubscriber tx;
  // private final DoubleSubscriber txnc;
  private final DoubleSubscriber ty;
  // private final DoubleSubscriber tync;

  // private final StringSubscriber getpipetype;

  public final boolean useMegaBotPose1;
  public final boolean useMegaBotPose2;

  private final double cameraStdDevFactors;

  public final String name;

  public final LimeLightInputsAutoLogged inputs = new LimeLightInputsAutoLogged();

  private Pose3d cameraPose;

  public Limelight(LimelightConfig config, Supplier<Rotation2d> rotationSupplier) {
    this.rotationSupplier = rotationSupplier;
    name = config.name;
    cameraPose = config.robotToCamera;
    var table = NetworkTableInstance.getDefault().getTable(config.name);

    // Initialize all DoubleArraySubscribers
    botpose_orb_wpiblue =
        table.getDoubleArrayTopic("botpose_orb_wpiblue").subscribe(new double[] {});
    table.getDoubleArrayTopic("botpose_targetspace").subscribe(new double[] {});
    botpose_wpiblue = table.getDoubleArrayTopic("botpose_wpiblue").subscribe(new double[] {});
    // camerapose_robotspace =
    // table.getDoubleArrayTopic("camerapose_robotspace").subscribe(new
    // double[] {});
    // camerapose_targetpace =
    // table.getDoubleArrayTopic("camerapose_targetspace").subscribe(new
    // double[] {});
    // crosshairs = table.getDoubleArrayTopic("crosshairs").subscribe(new
    // double[] {});
    hw = table.getDoubleArrayTopic("hw").subscribe(new double[] {});
    // imu = table.getDoubleArrayTopic("imu").subscribe(new double[] {});
    llpython = table.getDoubleArrayTopic("llpython").subscribe(new double[] {});
    // rawdetections =
    // table.getDoubleArrayTopic("rawdetections").subscribe(new double[]
    // {});
    // rawfiducials =
    // table.getDoubleArrayTopic("rawfiducials").subscribe(new double[]
    // {});
    // rawtargets = table.getDoubleArrayTopic("rawtargets").subscribe(new
    // double[] {});
    robot_orientation_set =
        table.getDoubleArrayTopic("robot_orientation_set").subscribe(new double[] {});
    t2d = table.getDoubleArrayTopic("t2d").subscribe(new double[] {});
    targetpose_cameraspace =
        table.getDoubleArrayTopic("targetpose_cameraspace").subscribe(new double[] {});
    // targetpose_robotspace =
    // table.getDoubleArrayTopic("targetpose_robotspace").subscribe(new
    // double[] {});
    // tc = table.getDoubleArrayTopic("tc").subscribe(new double[] {});

    rewindSubscriber = table.getDoubleArrayTopic("capture_rewind").subscribe(new double[] {});

    // Initialize all DoubleSubscribers
    cl = table.getDoubleTopic("cl").subscribe(0.0);
    getpipe = table.getIntegerTopic("getpipe").subscribe(0);
    hb = table.getDoubleTopic("hb").subscribe(0.0);
    // pipeline = table.getDoubleTopic("pipeline").subscribe(0.0);
    ta = table.getDoubleTopic("ta").subscribe(0.0);
    tid = table.getDoubleTopic("tid").subscribe(0.0);
    tl = table.getDoubleTopic("tl").subscribe(0.0);
    tv = table.getDoubleTopic("tv").subscribe(0.0);
    tx = table.getDoubleTopic("tx").subscribe(0.0);
    // txnc = table.getDoubleTopic("txnc").subscribe(0.0);
    ty = table.getDoubleTopic("ty").subscribe(0.0);
    // tync = table.getDoubleTopic("tync").subscribe(0.0);

    // Initialize StringSubscriber
    // getpipetype =
    // table.getStringTopic("getpipetype").subscribe("none");

    // Initialize orientation publisher
    orientationPublisher = table.getDoubleArrayTopic("robot_orientation_set").publish();
    cameraposePublisher = table.getDoubleArrayTopic("camerapose_robotspace_set").publish();
    rewindPublisher = table.getDoubleArrayTopic("capture_rewind").publish();
    pipelinePublisher = table.getIntegerTopic("pipeline2").publish();

    this.useMegaBotPose1 = config.useMegaBotPose1;
    this.useMegaBotPose2 = config.useMegaBotPose2;
    this.cameraStdDevFactors = config.stdDevFactors;
    LimelightHelpers.SetIMUMode(name, 0); // turn off the limelights internal IMU
  }

  public void updateInputs() {
    if (!enabled)
      return;
    Pose2d botPose1 = getBotPose1().orElse(PoseData.kzero).pose();
    Pose2d botPose2 = getBotPose2().orElse(PoseData.kzero).pose();
    Logger.recordOutput("Limelight/" + name + "/botPose1", botPose1);
    Logger.recordOutput("Limelight/" + name + "/botPose2", botPose2);
    // Publish orientation data
    // updateOrientations();
    // Update orientation for MegaTag 2
    // orientationPublisher
    // .accept(new double[] {orientationsSupplier.get().getDegrees(), 0.0,
    // 0.0, 0.0, 0.0, 0.0});
    orientationPublisher
        .accept(new double[] {rotationSupplier.get().getDegrees(), 0.0, 0.0, 0.0, 0.0, 0.0});

    NetworkTableInstance.getDefault().flush(); // Increases network
    // traffic but recommended by
    // Limelight

    // Update array inputs
    inputs.botpose_orb_wpiblue = botpose_orb_wpiblue.get();
    inputs.botpose_wpiblue = botpose_wpiblue.get();
    // inputs.camerapose_robotspace = camerapose_robotspace.get();
    // inputs.camerapose_targetpace = camerapose_targetpace.get();
    // inputs.crosshairs = crosshairs.get();
    inputs.hw = hw.get();
    // inputs.imu = imu.get();
    inputs.llpython = llpython.get();
    // inputs.rawdetections = rawdetections.get();
    // inputs.rawfiducials = rawfiducials.get();
    // inputs.rawtargets = rawtargets.get();
    inputs.robot_orientation_set = robot_orientation_set.get();
    inputs.rewind = rewindSubscriber.get();
    inputs.t2d = t2d.get();
    inputs.targetpose_cameraspace = targetpose_cameraspace.get();
    // inputs.targetpose_robotspace = targetpose_robotspace.get();
    // inputs.tc = tc.get();

    // Update double inputs
    inputs.cl = cl.get();
    inputs.getpipe = (int) getpipe.get();
    inputs.hb = hb.get();
    // inputs.pipeline = pipeline.get();
    inputs.ta = ta.get();
    inputs.tid = tid.get();
    inputs.tl = tl.get();
    inputs.tv = tv.get();
    inputs.tx = tx.get();
    // inputs.txnc = txnc.get();
    inputs.ty = ty.get();
    // inputs.tync = tync.get();

    // Update string input
    // inputs.getpipetype = getpipetype.get();

    // Update connection status
    // Update connection status based on whether an update has been seen
    // in the last 250ms
    inputs.connected = ((Timer.getTimestamp() * 1000 - tl.getLastChange() / 1_000)) < 250;

  }

  public Optional<PoseData> getBotPose1() {
    Optional<PoseData> pose = parsePose(inputs.botpose_wpiblue);
    return rejectPose(pose) ? Optional.empty() : pose;
  }

  public Optional<PoseData> getBotPose2() {

    Optional<PoseData> pose = parsePose(inputs.botpose_orb_wpiblue);
    return rejectPose(pose) ? Optional.empty() : pose;
  }

  private boolean rejectPose(Optional<PoseData> poseData) {
    if (poseData.isEmpty())
      return true;
    Pose2d pose = poseData.get().pose();
    return pose == null || pose.getTranslation() == null || !Double.isFinite(pose.getX())
        || !Double.isFinite(pose.getY());
  }

  public OptionalDouble getDistanceToTag() {
    double[] pose = inputs.botpose_wpiblue;

    return pose.length < 9 || pose[9] == 0 ? OptionalDouble.empty() : OptionalDouble.of(pose[9]);
  }

  public OptionalDouble getYawToTag() {
    double[] pose = inputs.targetpose_cameraspace;

    if (pose.length < 4) {
      return OptionalDouble.empty();
    }

    if (pose[0] == 0 && pose[1] == 0) {
      return OptionalDouble.empty();
    }

    return OptionalDouble.of(pose[4]);
  }

  public double getLatency() {
    return inputs.tl;
  }

  public int getPrimeryTag() {
    return (int) inputs.tid;
  }

  public int getTargetCount() {
    double[] t2d = inputs.t2d;
    if (t2d.length == 17) {
      return (int) t2d[1];
    }
    return 0;
  }

  public void recordRewind(double durationSeconds) {
    double[] currentArray = inputs.rewind;
    double counter = (currentArray.length > 0) ? currentArray[0] : 0;
    double[] entries = new double[2];
    entries[0] = counter + 1;
    entries[1] = Math.min(durationSeconds, 165);
    rewindPublisher.accept(entries);
    Logger.recordOutput("Limelights/" + name + "rewind", entries);
  }

  public double getTemp() {
    return inputs.hw.length >= 4 ? inputs.hw[0] : -1;
  }

  public void setPipleine(int pipeline) {
    pipelinePublisher.set(pipeline);
  }

  public int getPipeilne() {
    return inputs.getpipe;
  }

  /**
   * Sets the camera pose relative to the robot.
   *
   * @param forward Forward offset in meters
   * @param side Side offset in meters
   * @param up Up offset in meters
   * @param roll Roll angle in degrees
   * @param pitch Pitch angle in degrees
   * @param yaw Yaw angle in degrees
   */
  public void setCameraPose(double forward, double side, double up, double roll, double pitch,
      double yaw) {
    cameraPose = new Pose3d(forward, side, up, new Rotation3d(Units.degreesToRadians(roll),
        Units.degreesToRadians(pitch), Units.degreesToRadians(yaw)));
    double[] entries = new double[6];
    entries[0] = forward;
    entries[1] = side;
    entries[2] = up;
    entries[3] = roll;
    entries[4] = pitch;
    entries[5] = yaw;
    cameraposePublisher.accept(entries);
  }

  /**
   * Sets the camera pose relative to the robot.
   */
  public void setCameraPose(Pose3d pose) {
    setCameraPose(pose.getX(),
        pose.getY(),
        pose.getZ(),
        pose.getRotation().getMeasureX().in(edu.wpi.first.units.Units.Degrees),
        pose.getRotation().getMeasureY().in(edu.wpi.first.units.Units.Degrees),
        pose.getRotation().getMeasureZ().in(edu.wpi.first.units.Units.Degrees));
  }

  public Pose3d getCameraPose() {
    return cameraPose;
  }

  /** Parses the 3D pose from a Limelight botpose array. */
  private static Optional<PoseData> parsePose(double[] rawLLArray) {
    if (rawLLArray.length < 5) {
      return Optional.empty();
    }
    if (rawLLArray[0] == 0 && rawLLArray[1] == 0) {
      return Optional.empty();
    }
    Pose2d pose = new Pose2d(rawLLArray[0], rawLLArray[1],
        new Rotation2d(Units.degreesToRadians(rawLLArray[5])));
    if (pose.equals(Pose2d.kZero)) {
      return Optional.empty();
    }

    return Optional.of(new PoseData(pose, Timer.getTimestamp() - rawLLArray[6] * 1.0e-3));
  }

  public double getCameraStdDevFactors() {
    return cameraStdDevFactors;
  }
}
