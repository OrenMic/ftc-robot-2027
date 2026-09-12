package miscar.swerve;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.function.Function;
import miscar.configs.encoder.EncoderConfig;
import miscar.configs.mecs.Ratios;
import miscar.configs.motors.MotorIOConfig;
import miscar.mecsIOs.Mechanism;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;

public class SwerveBase extends SubsystemBase {

  public final SwerveModule[] modules = new SwerveModule[4];

  public LoggedNetworkBoolean resetEncoders = new LoggedNetworkBoolean("Swerve/Reset/Encoders");
  public LoggedNetworkBoolean setToCoast = new LoggedNetworkBoolean("Swerve/setToCoast", false);

  LoggedNetworkBoolean disintegrate = new LoggedNetworkBoolean("Swerve/disintegrate", false);


  {
    resetEncoders.setDefault(false);
  }

  protected SwerveDriveKinematics kinematics;

  public SwerveBase(MotorIOConfig driveConfig, Ratios driveRatios, MotorIOConfig rotationConfig,
      EncoderConfig<?> encoderConfig, Ratios rotationRatios, double robotSize) {
    Function<MotorIOConfig, Mechanism> drive = Mechanism::create;
    Function<MotorIOConfig, Mechanism> rotation = Mechanism::create;
    for (int i = 0; i < 4; i++) {
      Mechanism driveMec = drive.apply(driveConfig);
      Mechanism rotationMec = rotation.apply(rotationConfig);
      driveMec.setMotorToMecRatio(driveRatios.getMotorToMecRatio());
      rotationMec.setMotorToMecRatio(rotationRatios.getMotorToMecRatio());
      modules[i] = new SwerveModule(driveMec, rotationMec, encoderConfig, i);

      // update module configs for the next module
      driveConfig.motorPort += 2;
      rotationConfig.motorPort += 2;
      encoderConfig.encoderPort += 1;
    }

    initKinematics(robotSize);
  }

  public SwerveBase(Ratios driveRatios, Ratios rotationRatios, double robotSize,
      EncoderConfig<?> encoderConfig) {
    for (int i = 0; i < 4; i++) {
      Mechanism drive = Mechanism.empty();
      Mechanism rotation = Mechanism.empty();

      drive.setMotorToMecRatio(driveRatios.getMotorToMecRatio());
      rotation.setMotorToMecRatio(rotationRatios.getMotorToMecRatio());

      modules[i] = new SwerveModule(drive, rotation, encoderConfig, i);

      encoderConfig.encoderPort += 1;
    }
    initKinematics(robotSize);
  }

  private void initKinematics(double robotSize) {
    kinematics = new SwerveDriveKinematics(createModulePosesUsing(robotSize));
  }

  protected static Translation2d[] createModulePosesUsing(double robotSize) {
    return new Translation2d[] {new Translation2d(robotSize / 2, robotSize / 2),
        new Translation2d(robotSize / 2, -robotSize / 2),
        new Translation2d(-robotSize / 2, robotSize / 2),
        new Translation2d(-robotSize / 2, -robotSize / 2)};
  }

  public void robotRelativeDrive(ChassisSpeeds speeds) {
    ChassisSpeeds discreteSpeeds = ChassisSpeeds.discretize(speeds, 0.02);
    SwerveModuleState[] setpointStates = kinematics.toSwerveModuleStates(discreteSpeeds);

    Logger.recordOutput("SwerveDrive/SwerveStates/Setpoints", setpointStates);
    for (SwerveModule module : modules) {
      module.setState(setpointStates[module.index]);
    }
    Logger.recordOutput("SwerveDrive/SwerveStates/Optimized Setpoints", setpointStates);
  }

  public void stop() {
    for (SwerveModule module : modules) {
      module.stop();
    }
  }

  public SwerveModulePosition[] getModulePositions() {
    SwerveModulePosition[] modulePositions = new SwerveModulePosition[4];

    for (SwerveModule module : modules) {
      modulePositions[module.index] = module.getPosition();
    }

    return modulePositions;
  }

  public SwerveModuleState[] getModuleStatessByEncoder() {
    SwerveModuleState[] modulePositions = new SwerveModuleState[4];

    for (SwerveModule module : modules) {
      modulePositions[module.index] = module.getStateByEncoder();
    }

    return modulePositions;
  }

  public SwerveModuleState[] getModuleStates() {
    SwerveModuleState[] moduleStates = new SwerveModuleState[4];

    for (SwerveModule module : modules) {
      moduleStates[module.index] = module.getState();
    }

    return moduleStates;
  }

  public void updateOffset() {
    for (SwerveModule module : modules) {
      module.updateOffset();
    }
  }

  public boolean isConnected() {
    return modules[0].isConnected() && modules[1].isConnected() && modules[2].isConnected()
        && modules[3].isConnected();
  }

  public void disintegrate() {
    for (SwerveModule module : modules) {
      module.disintegrate();
    }
  }

  @Override
  public void periodic() {
    for (SwerveModule module : modules) {
      module.periodic();
    }
    Logger.recordOutput("SwerveDrive/ChassisStates/Setpoints", getModuleStates());
    Logger.recordOutput("SwerveDrive/ChassisStates/Setpoints by encoder",
        getModuleStatessByEncoder());
    Logger.recordOutput("SwerveDrive/allMotorsConnected", isConnected());

    if (resetEncoders.get()) {
      updateOffset();
      resetEncoders.set(false);
    }
    if (setToCoast.get()) {
      for (SwerveModule module : modules) {
        module.setToCoast();
      }
      setToCoast.set(false);
    }
    if (disintegrate.getAsBoolean()) {
      disintegrate();
    }
  }
}
