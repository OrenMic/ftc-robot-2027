package miscar.swerve;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.numbers.N3;
import miscar.configs.encoder.EncoderConfig;
import miscar.configs.mecs.Ratios;
import miscar.configs.motors.MotorIOConfig;
import miscar.gyro.GyroIO;

public class VisionedSwerve extends LocalizedSwerve {

  public VisionedSwerve(MotorIOConfig driveConfig, Ratios driveRatios, MotorIOConfig rotationConfig,
      EncoderConfig encoderConfig, Ratios rotationRatios, GyroIO gyro, double robotSize) {
    super(driveConfig, driveRatios, rotationConfig, encoderConfig, rotationRatios, gyro, robotSize);
  }

  public VisionedSwerve(Ratios driveRatios, Ratios rotationRatios, double robotSize,
      EncoderConfig encoderConfig) {
    super(driveRatios, rotationRatios, robotSize, encoderConfig);
  }

  public void addVisionMeasurement(Pose2d visionRobotPoseMeters, double timestampSeconds,
      Vector<N3> visionMeasurementStdDevs) {
    poseEstimator
        .addVisionMeasurement(visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);


    // double wrapped = poseKalman.getRotationRad()
    // +
    // visionRobotPoseMeters.getRotation().minus(poseKalman.getRotation()).getRadians();
    // var y = VecBuilder.fill(visionRobotPoseMeters.getX(),
    // visionRobotPoseMeters.getY(), wrapped);

    // poseKalman.setPoseObservation(new
    // Pose2d(visionRobotPoseMeters.getTranslation(),
    // new Rotation2d(wrapped)), timestampSeconds,
    // visionMeasurementStdDevs);

    // var y = VecBuilder.fill(visionRobotPoseMeters.getX(),
    // visionRobotPoseMeters.getY(), wrapped);

    // poseKalman2.setPoseObservation(new
    // Pose2d(visionRobotPoseMeters.getTranslation(),
    // new Rotation2d(wrapped)), timestampSeconds,
    // visionMeasurementStdDevs);

    double wrapped = poseKalman2.getRotationRad()
        + visionRobotPoseMeters.getRotation().minus(poseKalman2.getRotation()).getRadians();

    poseKalman2.correct(
        VecBuilder.fill(visionRobotPoseMeters.getX(), visionRobotPoseMeters.getY(), wrapped),
        VecBuilder.fill(visionMeasurementStdDevs.get(0), visionMeasurementStdDevs.get(1),
            visionMeasurementStdDevs.get(2)));

    // poseKalman.setPoseObservation(new
    // PoseObservation(visionRobotPoseMeters, timestampSeconds,
    // visionMeasurementStdDevs.get(0), visionMeasurementStdDevs.get(2)));
    // poseKalman.correct(y, visionMeasurementStdDevs, timestampSeconds);
  }

  @Override
  public void periodic() {
    super.periodic();
  }
}
