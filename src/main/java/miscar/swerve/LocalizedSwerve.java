package miscar.swerve;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.wpilibj.Timer;
import miscar.configs.encoder.EncoderConfig;
import miscar.configs.mecs.Ratios;
import miscar.configs.motors.MotorIOConfig;
import miscar.gyro.GyroIO;
import miscar.gyro.GyroIOInputsAutoLogged;
import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;

public class LocalizedSwerve extends SwerveBase {

  protected Rotation2d rawGyroRotation = new Rotation2d();

  protected final GyroIO gyro;

  protected GyroIOInputsAutoLogged gyroInputs = new GyroIOInputsAutoLogged();

  double pitchOffset;
  double rollOffset;

  public LoggedNetworkBoolean resetGyro = new LoggedNetworkBoolean("Swerve/Reset/Gyro", false);


  SwerveModulePosition[] lastModulePositions = {new SwerveModulePosition(),
      new SwerveModulePosition(), new SwerveModulePosition(), new SwerveModulePosition()};

  protected final SwerveDrivePoseEstimator poseEstimator = new SwerveDrivePoseEstimator(kinematics,
      rawGyroRotation, lastModulePositions, new Pose2d(0, 0, Rotation2d.kZero));

  public LocalizedSwerve(MotorIOConfig driveConfig, Ratios driveRatios,
      MotorIOConfig rotationConfig, EncoderConfig<?> encoderConfig, Ratios rotationRatios,
      GyroIO gyro, double robotSize) {
    super(driveConfig, driveRatios, rotationConfig, encoderConfig, rotationRatios, robotSize);
    this.gyro = gyro;
    updateGyroInputs();
    resetGyro();
  }

  public LocalizedSwerve(Ratios driveRatios, Ratios rotationRatios, double robotSize,
      EncoderConfig<?> encoderConfig) {
    super(driveRatios, rotationRatios, robotSize, encoderConfig);
    this.gyro = new GyroIO() {};
    updateGyroInputs();
    resetGyro();
  }

  @AutoLogOutput(key = "SwerveDrive/Robot Pose")
  public Pose2d getPose() {
    return poseEstimator.getEstimatedPosition();
  }

  public void fieldRelativeDrive(ChassisSpeeds speeds) {
    Logger.recordOutput(getName() + "/field relative speeds", speeds);
    speeds = ChassisSpeeds.fromFieldRelativeSpeeds(speeds, getRotation());
    robotRelativeDrive(speeds);
  }

  public Rotation2d getRotation() {
    return getPose().getRotation();
  }

  @AutoLogOutput(key = "SwerveDrive/Robot Speed")
  public ChassisSpeeds getChassisSpeed() {
    return kinematics.toChassisSpeeds(getModuleStates());
  }

  @AutoLogOutput(key = "SwerveDrive/Robot Speed hype")
  public double getChassisSpeedHype() {
    return Math.hypot(getChassisSpeed().vxMetersPerSecond, getChassisSpeed().vyMetersPerSecond);
  }

  protected void updateGyroInputs() {
    gyro.updateInputs(gyroInputs);

    Logger.processInputs("Swerve/Gyro", gyroInputs);
  }

  public double getRawPitch() {
    return gyroInputs.pitchPosition.getDegrees();
  }

  public double getRawRoll() {
    return gyroInputs.rollPosition.getDegrees();
  }

  public double getPitch() {
    return MathUtil.inputModulus(getRawPitch() - pitchOffset, -180, 180);
  }

  public double getRoll() {
    return MathUtil.inputModulus(getRawRoll() - rollOffset, -180, 180);
  }

  protected void resetGyro() {
    pitchOffset = getRawPitch();
    rollOffset = getRawRoll();
  }

  @Override
  public void periodic() {
    super.periodic();

    updateGyroInputs();


    if (resetGyro.getAsBoolean()) {
      resetGyro();
      resetGyro.set(false);
    }

    Logger.recordOutput("Swerve/Gyro/roll", getRoll());
    Logger.recordOutput("Swerve/Gyro/pitch", getPitch());


    SwerveModulePosition[] moduleDeltas = new SwerveModulePosition[4];
    for (SwerveModule module : modules) {

      moduleDeltas[module.index] = new SwerveModulePosition(
          module.getPosition().distanceMeters - lastModulePositions[module.index].distanceMeters,
          module.getPosition().angle);
    }

    lastModulePositions = getModulePositions();
    if (gyroInputs.connected) {
      rawGyroRotation = gyroInputs.yawPosition;
    } else {
      rawGyroRotation =
          rawGyroRotation.plus(new Rotation2d(kinematics.toTwist2d(moduleDeltas).dtheta));
    }

    poseEstimator.updateWithTime(Timer.getTimestamp(), rawGyroRotation, lastModulePositions);
  }
}
