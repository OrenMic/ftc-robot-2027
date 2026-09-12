package miscar.mecsIOs;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Preferences;
import frc.robot.Constants;
import frc.robot.Constants.Mode;
import java.util.ArrayList;
import java.util.Optional;
import miscar.configs.mecs.Ratios;
import miscar.mecsIOs.features.Feature;
import miscar.motorIOs.EmptyMotor;
import miscar.motorIOs.MotorIOInputsAutoLogged;
import miscar.motorIOs.MotorIOSim;
import miscar.util.MountedMec;
import org.littletonrobotics.junction.Logger;

public abstract class AbstractMechanism implements MechanismIO {

  /** the mec's robot relative target pose in the mec's units */
  public double targetMecPose = 0;

  public double targetVelocity = 0;

  /** The pose calculator, in charge of all the motor to mec math */
  public MountedMec poseCalc;

  ArrayList<Feature> features = new ArrayList<>();

  /** The maximum movement we allow to the mec in the mec's units */
  public double maxAllowedMovement = Double.POSITIVE_INFINITY;
  /** The minimum movement we allow to the mec in the mec's units */
  public double minAllowedMovement = Double.NEGATIVE_INFINITY;
  /** The maximum velocity we allow to the mec in the mec's units */
  protected double maxAllowedVelocity = Double.POSITIVE_INFINITY;

  protected double positionTolerance = -1;
  protected double velocityTolerance = -1;

  /**
   * The path onto which this mec's offset should be save to the RoboRio
   */
  protected Optional<String> saveOffsetPath = Optional.empty();

  // protected double mecPosition = 0;
  // protected double mecVelocity = 0;

  /**
   * The motor IO class delegation, used to preform function calls for
   * the T motorIO class
   */
  public EmptyMotor motorIODelegation;

  private String mecPositionPath;
  private String mecVelocityPath;
  private String targetMecPosePath;
  private String targetVelocityPath;
  public String mecInVelocityPath;
  public String mecInPositionPath;

  /** This {@link MechanismIO}'s' inputs */
  public MotorIOInputsAutoLogged inputs = new MotorIOInputsAutoLogged();

  /**
   * @param motorIODelegation - - The motor IO class delegation, used to
   *        preform function calls for the motorIO class
   * @param poseCalc - See {@link MountedMec} for more
   */
  public AbstractMechanism(EmptyMotor motorIODelegation) {
    this.poseCalc = new MountedMec(motorIODelegation::getPoseRotations, 1);
    this.motorIODelegation = motorIODelegation;
  }

  public AbstractMechanism() {
    this(new EmptyMotor());
  }

  @Override
  public void addFeature(Feature feature) {
    feature.connectMec(this);
    this.features.add(feature);
  }

  // @Override
  // public Feature addFeature(Builder builder) {
  // Feature feature = builder.build(this);
  // this.features.add(feature);
  // return feature;
  // }

  @Override
  public void setMotorToMecRatio(double motorToMecRatio) {
    if (motorToMecRatio <= 0) {
      throw new IllegalArgumentException("motorToMecRatio must be positive and non-zero");
    }
    poseCalc.updateMotorToMecRatio(motorToMecRatio);
  }

  @Override
  public void setRatio(Ratios ratioConfig) {
    poseCalc.updateRatio(ratioConfig);
  }

  @Override
  public void setStartPose(double startMecPose) {
    poseCalc.setStartPose(startMecPose);
  }

  @Override
  public void setMaxMovement(double maxAllowedMovement) {
    if (maxAllowedMovement <= 0) {
      throw new IllegalArgumentException(
          "maxAllowedMovement must be positive and non-zero (instead of having a negative max movement, consider inverting the motor?)");
    }
    this.maxAllowedMovement = maxAllowedMovement;
  }

  @Override
  public void setMinMovement(double minAllowedMovement) {
    this.minAllowedMovement = minAllowedMovement;
  }

  @Override
  public void setMaxVelocity(double maxAllowedVelocity) {
    if (maxAllowedVelocity <= 0) {
      throw new IllegalArgumentException("maxAllowedVelocity must be positive and non-zero");
    }
    this.maxAllowedVelocity = maxAllowedVelocity;
  }

  public void setPositionTolerance(double positionTolerance) {
    this.positionTolerance = positionTolerance;

    motorIODelegation.setPositionTolerance(poseCalc.MecToMotorRaw(positionTolerance));
  }

  public void setVelocityTolerance(double velocityTolerance) {
    this.velocityTolerance = velocityTolerance;

    motorIODelegation.setVelocityTolerance(poseCalc.MecToMotorRaw(velocityTolerance));
  }

  @Override
  public void saveMecOffset(String saveOffsetPath) {
    if (Constants.currentMode != Mode.REAL)
      return;
    Preferences.setDouble(saveOffsetPath, poseCalc.getOffset());
  }

  @Override
  public void saveMecOffset(String saveOffsetPath, boolean continueSaving) {
    if (Constants.currentMode != Mode.REAL)
      return;
    if (continueSaving) {
      Preferences.setDouble(saveOffsetPath, poseCalc.getOffset());
      this.saveOffsetPath = Optional.of(saveOffsetPath);
    }
  }

  public void updateInputs() {
    updateInputs(inputs);

    logData();
  }

  public void setName(String name) {
    motorIODelegation.setName(name);
    updateLoggingPaths(name);
  }

  private void updateLoggingPaths(String name) {

    mecPositionPath = name + "/Data/" + "mecPosition";
    mecVelocityPath = name + "/Data/" + "mecVelocity";
    targetMecPosePath = name + "/Data/" + "mecTargetPose";
    targetVelocityPath = name + "/Data/" + "mecTargetVelocity";
    mecInPositionPath = name + "/Data/" + "mecInPose";
    mecInVelocityPath = name + "/Data/" + "mecInVelocity";
  }

  private void updateInputs(MotorIOInputsAutoLogged inputs) {
    for (Feature feature : features) {
      feature.executeBeforePeriodic();
    }

    motorIODelegation.updateInputs(inputs);

    for (Feature feature : features) {
      feature.executeAfterPeriodic();
    }
  }

  public double getMecPosition() {
    return poseCalc.GetRobotRelativeMecPose();
  }

  public double getMecVelocity() {
    return poseCalc.MotorToMec(inputs.velocityRotationsPerMinute);
  }

  public void logData() {
    motorIODelegation.logData();

    Logger.recordOutput(mecPositionPath, getMecPosition());
    Logger.recordOutput(mecVelocityPath, getMecVelocity());

    Logger.recordOutput(targetMecPosePath, targetMecPose);
    Logger.recordOutput(targetVelocityPath, targetVelocity);
    Logger.recordOutput(mecInPositionPath, mecInPosition());
    Logger.recordOutput(mecInVelocityPath, mecInVelocity());
  }

  public void setTargetMotorAngularVelocity(double angularVelocityRPM) {
    // update the target velocity, this is used for logging
    targetVelocity = poseCalc.MotorToMec(angularVelocityRPM);

    // clamp the target velocity so that we don't break the mec
    angularVelocityRPM = MathUtil.clamp(angularVelocityRPM,
        -poseCalc.MecToMotorRaw(maxAllowedVelocity),
        poseCalc.MecToMotorRaw(maxAllowedVelocity));
    // set the mec motor's target pose
    motorIODelegation.setTargetMotorAngularVelocity(angularVelocityRPM);
  }

  @Override
  public void setTargetMecVelocity(double velocity) {

    targetVelocity = velocity;

    velocity = MathUtil.clamp(velocity, -maxAllowedVelocity, maxAllowedVelocity);
    // set the mec motor's target pose
    motorIODelegation.setTargetMotorAngularVelocity(poseCalc.MecToMotorRaw(velocity));
  }

  public void setClosedLoopSlot(int slot) {
    motorIODelegation.setClosedLoopSlot(slot);
  }

  public void setPower(double power) {
    Logger.recordOutput(motorIODelegation.name + "/Data/setPower", power);
    // check if we are currently in sim

    if (Constants.currentMode == Mode.SIM && motorIODelegation instanceof MotorIOSim motorSim) {
      // make sure the lift sim does not go out of border.
      if (poseCalc.GetRobotRelativeMecPose() >= maxAllowedMovement && power >= 0) {
        // if we are out of bounds, stop the motor (set power to 0)
        power = 0;
        // set the currant pose of the motor to be the max allowed movement
        motorSim.motorSim.setAngle(
            Units.rotationsToRadians(poseCalc.RobotRelativeMecToMotor(maxAllowedMovement)));
      }
      // check if we are below the min allowed movement (in mec relative
      // pos this is 0).
      if (poseCalc.GetRobotRelativeMecPose() <= minAllowedMovement && power <= 0) {
        // if we are out of bounds, stop the motor (set power to 0)
        power = 0;
        // set the currant pose of the motor to be 0
        motorSim.motorSim.setAngle(
            Units.rotationsToRadians(poseCalc.RobotRelativeMecToMotor(minAllowedMovement)));
      }
    }
    motorIODelegation.setPower(power);
  }

  public void setTargetPosition(double pose) {
    // update the target pose, this is used for logging
    targetMecPose = pose;

    // clamp the target pose so that we don't break the mec
    pose = MathUtil.clamp(pose, minAllowedMovement, maxAllowedMovement);

    //
    double poseError = poseCalc.MotorToMec(inputs.positionRotations) - getMecPosition();

    double targetMotorPose = poseCalc.MecToMotorRaw(pose + poseError);

    // double targetMotorPose = poseCalc.RobotRelativeMecToMotor(pose);

    // set the mec motor's target pose
    motorIODelegation.setTargetPosition(targetMotorPose);
  }

  @Override
  public void reloadOffset() {
    if (saveOffsetPath.isPresent()) {
      if (Constants.currentMode != Mode.REAL)
        return;
      // reload the save offset from memory
      double savedOffset = Preferences.getDouble(saveOffsetPath.get(), 0);
      // load the saved offset into the pose calc
      poseCalc.overrideOffset(savedOffset);
    }
  }

  @Override
  public void reloadOffset(String saveOffsetPath) {
    if (Constants.currentMode != Mode.REAL)
      return;
    // reload the save offset from memory
    double savedOffset = Preferences.getDouble(saveOffsetPath, 0);
    // load the saved offset into the pose calc
    poseCalc.overrideOffset(savedOffset);
  }

  @Override
  public double updateOffset() {
    // update the offset
    double newOffset = poseCalc.updateOffset();
    // save the mec's offsets
    if (saveOffsetPath.isPresent()) {
      Preferences.setDouble(saveOffsetPath.get(), newOffset);
    }
    // return the new offset
    return newOffset;
  }

  @Override
  public void setCurrentPose(double newPose) {
    // update the offset
    poseCalc.setCurrentPose(newPose);
    // save the mec's offsets
    if (saveOffsetPath.isPresent()) {
      Preferences.setDouble(saveOffsetPath.get(), poseCalc.getOffset());
    }
  }

  @Override
  public void overrideOffset(double newOffset) {
    // Override the old offset
    poseCalc.overrideOffset(newOffset);
    // Save the mec's offsets
    if (saveOffsetPath.isPresent()) {
      Preferences.setDouble(saveOffsetPath.get(), newOffset);
    }
  }

  @Override
  public boolean mecInPosition() {
    return positionTolerance > 0
        ? MathUtil.isNear(targetMecPose, getMecPosition(), positionTolerance)
        : false;
  }

  @Override
  public boolean mecInVelocity() {
    return velocityTolerance > 0
        ? MathUtil.isNear(targetVelocity, getMecVelocity(), velocityTolerance)
        : false;
  }

  public boolean mecInPosition(double targetMecPose) {
    return positionTolerance > 0
        ? MathUtil.isNear(targetMecPose, getMecPosition(), positionTolerance)
        : false;
  }

  public boolean mecInVelocity(double targetVelocity) {
    return velocityTolerance > 0
        ? MathUtil.isNear(targetVelocity, getMecVelocity(), velocityTolerance)
        : false;
  }

  public void disintegrate() {
    setPower(0);
    String name = motorIODelegation.name;
    this.motorIODelegation = new EmptyMotor();
    this.motorIODelegation.setName(name);
  }
}
