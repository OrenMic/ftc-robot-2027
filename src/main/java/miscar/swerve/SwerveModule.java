package miscar.swerve;

import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import frc.robot.Constants;
import frc.robot.Constants.Mode;
import miscar.annotation.ConstantsUser;
import miscar.annotation.CreateConstants;
import miscar.configs.encoder.BoreEncoderConfig;
import miscar.configs.encoder.EncoderConfig;
import miscar.mecsIOs.Mechanism;
import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;
import miscar.motorIOs.MotorIOTalonFX;
import miscar.util.encoder2.Encoder;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
import org.littletonrobotics.junction.networktables.LoggedNetworkNumber;

@ConstantsUser
public class SwerveModule {

  @CreateConstants(configType = {miscar.configs.motors.TalonFXConfig.class,
      miscar.configs.motors.SimConfig.class, miscar.configs.mecs.Ratios.class})
  public Mechanism drive;

  @CreateConstants(configType = {miscar.configs.motors.TalonFXConfig.class,
      miscar.configs.motors.SimConfig.class, miscar.configs.mecs.Ratios.class,
      BoreEncoderConfig.class})
  public Mechanism rotation;

  private final LoggedNetworkNumber kp, ki, kd, ks, kv, ka, kg, kAccleration, kVelocity;
  private final LoggedNetworkBoolean updatePID;

  public final Alert notZeroed;

  public Encoder encoder;

  public final int index;

  private boolean resetbyEncoder = true;

  public SwerveModule(Mechanism drive, Mechanism rotation, EncoderConfig<?> encoderConfig,
      int index) {
    kp = new LoggedNetworkNumber("swervePID/kp");
    ki = new LoggedNetworkNumber("swervePID/ki");
    kd = new LoggedNetworkNumber("swervePID/kd");
    ks = new LoggedNetworkNumber("swervePID/ks");
    kv = new LoggedNetworkNumber("swervePID/kv");
    ka = new LoggedNetworkNumber("swervePID/ka");
    kg = new LoggedNetworkNumber("swervePID/kg");
    kAccleration = new LoggedNetworkNumber("swervePID/kAccleation");
    kVelocity = new LoggedNetworkNumber("swervePID/kVelocity");

    kp.setDefault(5);
    ki.setDefault(0);
    kd.setDefault(0);
    ks.setDefault(0);
    kv.setDefault(0);
    ka.setDefault(0);
    kg.setDefault(0);
    kAccleration.setDefault(0);
    kVelocity.setDefault(0);

    updatePID = new LoggedNetworkBoolean("swervePID/update");
    updatePID.setDefault(false);
    this.drive = drive;
    this.rotation = rotation;

    drive.setName("Swerve/Module " + index + "/Drive");
    rotation.setName("Swerve/Module " + index + "/Rotation");
    encoder = Encoder.create(encoderConfig);
    this.index = index;
    notZeroed = new Alert("Swerve Module " + index + " isn't not zeroed", AlertType.kError);

    setStartPose((index % 2) * 180);
  }

  public void setState(SwerveModuleState state) {
    Rotation2d currentAngle = getAngle();

    state.optimize(currentAngle);
    state.cosineScale(currentAngle);

    drive.setTargetMecVelocity(state.speedMetersPerSecond * 60);
    rotation.setTargetPosition(calcAngle(state));
  }

  public double calcAngle(SwerveModuleState state) {
    Rotation2d currentAngle = getAngle();

    double diff =
        MathUtil.inputModulus(currentAngle.getDegrees() - state.angle.getDegrees(), -180, 180);

    return currentAngle.getDegrees() - diff;
  }

  public Rotation2d getAngle() {
    return new Rotation2d(Units.degreesToRadians(rotation.getMecPosition()));
  }

  public Rotation2d getAngleByEncoder() {
    return new Rotation2d(Units.degreesToRadians(encoder.getMecPose()));
  }

  public SwerveModulePosition getPosition() {
    return new SwerveModulePosition(drive.getMecPosition(), getAngle());
  }

  public SwerveModuleState getState() {
    return new SwerveModuleState(getMPS(), getAngle());
  }

  public double getMPS() {
    return drive.getMecVelocity() / 60.0;
  }

  public void disintegrate() {
    drive.setPower(0);
    rotation.setPower(0);

    drive.disintegrate();
    rotation.disintegrate();
  }

  public void stop() {
    drive.setPower(0);

    rotation.setPower(0);
  }

  public void updateOffset() {
    encoder.updateOffset();
    rotation.setCurrentPose(encoder.getMecPose());
  }

  public SwerveModuleState getStateByEncoder() {
    return new SwerveModuleState(getMPS(),
        new Rotation2d(Units.degreesToRadians(encoder.getMecPose())));
  }

  public boolean isConnected() {
    return drive.inputs.connected && rotation.inputs.connected;
  }

  private void setStartPose(double startAngle) {
    rotation.setStartPose(startAngle);
    encoder.poseCalc.setStartPose(startAngle);
  }

  public void setToCoast() {
    rotation.motorIODelegation.setNaturalMode(NeutralMode.COAST);
    drive.motorIODelegation.setNaturalMode(NeutralMode.COAST);
  }

  public void periodic() {
    encoder.updateInputs();
    drive.updateInputs();
    rotation.updateInputs();
    if (resetbyEncoder) {
      rotation.setCurrentPose(encoder.getMecPose());
      resetbyEncoder = false;
    }

    boolean encodersInTolorance = Math.abs(getAngle().minus(getAngleByEncoder()).getDegrees()) < 7;
    boolean isMoving = Math.abs(rotation.getMecVelocity()) > 0.1;
    boolean encodersZeroed = encodersInTolorance || isMoving;
    Logger.recordOutput("SwerveModule/" + index + "/isMoving", isMoving);
    Logger.recordOutput("SwerveModule/" + index + "/encodersInTolorance", encodersInTolorance);
    Logger.recordOutput("SwerveModule/" + index + "/poseDifference",
        getAngleByEncoder().minus(getAngle()));
    notZeroed.set(!encodersZeroed && Constants.currentMode != Mode.SIM);

    if (updatePID.get()) {
      updatePID.set(false);
      if (drive.motorIODelegation instanceof MotorIOTalonFX talonRotation) {
        talonRotation.setPid(new Slot0Configs().withKP(kp.get()).withKI(ki.get()).withKD(kd.get())
            .withKS(ks.get()).withKV(kv.get()).withKA(ka.get()).withKG(kg.get()));
        talonRotation
            .setMotionMagic(new MotionMagicConfigs().withMotionMagicAcceleration(kAccleration.get())
                .withMotionMagicCruiseVelocity(kVelocity.get()));
      }
    }
  }
}
