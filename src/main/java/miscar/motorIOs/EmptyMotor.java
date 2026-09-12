package miscar.motorIOs;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import miscar.configs.motors.MotorIOConfig;
import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;
import org.littletonrobotics.junction.Logger;

public class EmptyMotor {

  public String name = "";

  /** The motorIO's inputs */
  protected MotorIOInputs inputs = new MotorIOInputs();

  private String targetPoseRotationsPath;
  private String targetAngularVelocityPath;
  private String motorInPosePath;
  private String motorInVelocity;
  private String inputsPath;

  protected final Alert connectedAlert;

  protected final Alert tempAlert;

  public EmptyMotor(int port) {
    connectedAlert = new Alert("Motor " + port + " is not connected", AlertType.kError);
    tempAlert = new Alert("Motor " + port + " temp to high", AlertType.kWarning);
  }

  public EmptyMotor() {
    connectedAlert = new Alert("Some motor is configured to be empty", AlertType.kWarning);
    tempAlert = new Alert("The empty motor temp is unknown", AlertType.kWarning);
  }

  protected double positionTolerance = -1;
  protected double velocityTolerance = -1;

  /** The target pose of the motor */
  public double targetPoseRotations;

  /** The target angular velocity of the motor */
  protected double targetAngularVelocityRPM;

  public void updateInputs(MotorIOInputsAutoLogged inputs) {
    this.inputs = inputs;
    connectedAlert.set(!inputs.connected);
    tempAlert.set(inputs.temperature > 50.0);

    Logger.processInputs(inputsPath, inputs);

    logData();
  }

  public void setName(String name) {
    this.name = name;
    updateLoggersPath(name);
  }

  private void updateLoggersPath(String name) {

    inputsPath = name + "/Inputs";
    targetPoseRotationsPath = name + "/Data" + "/targetPoseRotations";
    targetAngularVelocityPath = name + "/Data" + "/targetVelocityRotationsPerMinute";
    motorInPosePath = name + "/Data" + "/motorInPose";
    motorInVelocity = name + "/Data" + "/motorInVelocity";
  }

  public void logData() {
    if (name.isEmpty()) {
      throw new RuntimeException("mec name is empty");
    }

    Logger.recordOutput(targetPoseRotationsPath, targetPoseRotations);
    Logger.recordOutput(targetAngularVelocityPath, targetAngularVelocityRPM);
    Logger.recordOutput(motorInPosePath, motorInPosition());
    Logger.recordOutput(motorInVelocity, motorInVelocity());
  }

  public void setTargetPosition(double poseRotations) {
    targetPoseRotations = poseRotations;
  }

  public void stayInPose() {
    setTargetPosition(inputs.positionRotations);
  }

  public void setTargetMotorAngularVelocity(double angularVelocityRPM) {
    targetAngularVelocityRPM = angularVelocityRPM;
  }

  public void setPositionTolerance(double positionTolerance) {
    this.positionTolerance = positionTolerance;
  }

  public void setVelocityTolerance(double velocityTolerance) {
    this.velocityTolerance = velocityTolerance;
  }

  public double getPoseRotations() {
    return inputs.positionRotations;
  }

  public boolean motorInPosition() {
    return positionTolerance > 0
        ? MathUtil.isNear(targetPoseRotations, inputs.positionRotations, positionTolerance)
        : false;
  }

  public boolean motorInVelocity() {
    return velocityTolerance > 0
        ? MathUtil
            .isNear(targetAngularVelocityRPM, inputs.velocityRotationsPerMinute, velocityTolerance)
        : false;
  }

  public void setPower(double power) {}

  public void setVoltage(double output) {}

  public MotorIOConfig getMotorConfig() {
    return null;
  }

  public void setNaturalMode(NeutralMode neutralMode) {}

  public void setClosedLoopSlot(int slot) {}
}
