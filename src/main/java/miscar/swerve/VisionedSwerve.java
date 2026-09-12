package miscar.swerve;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.numbers.N1;
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
      Matrix<N3, N1> visionMeasurementStdDevs) {
    poseEstimator
        .addVisionMeasurement(visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);
  }

  @Override
  public void periodic() {
    super.periodic();
  }
}
