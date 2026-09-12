package miscar.util;

import java.util.function.DoubleSupplier;
import miscar.configs.mecs.Ratios;

/**
 * This class is used to calc the position of a mechanism (mec) on the
 * robot based on its gear ratio
 */
public class MountedMec {
  /** the pose supplier for the mec */
  public DoubleSupplier poseSupplier;

  /**
   * The rotations ratio of the motor to mec. e.g if the ratio is 3:7
   * and the motor has done 6 full rotations, then the mec has done 14
   */
  private double motorToMecRatio;

  private double offset = 0;

  /** The robot relative start pose of the mec */
  private double startPose = 0;

  /**
   * Creates a new {@code MountedMec} with the provided arguments
   *
   * @param poseSupplier - The encoder pose's supplier we use to calc
   *        the currant pose of the mec/motor
   * @param motorToMecRatio - The rotations ratio of the motor to mec.
   *        e.g if the ratio is 3:7 and the motor has done 6 full
   *        rotations, then the mec has done 14
   */
  public MountedMec(DoubleSupplier poseSupplier, double motorToMecRatio) {

    // make sure we don't accidentally pass a 0 to be used as a ratio
    if (motorToMecRatio == 0) {
      throw new IllegalArgumentException(
          "you passed a 0 to be used as a ratio... did you accidentally do integer division?");
    }
    // make sure we don't accidentally pass a negative value to be used as
    // a ratio
    if (motorToMecRatio < 0) {
      throw new IllegalArgumentException(
          "you passed negative value to be used as a ratio... did you accidentally add a'-'?");
    }

    this.poseSupplier = poseSupplier;
    this.motorToMecRatio = motorToMecRatio;
  }

  /**
   * @param motorToMecRatio - the new {@code motorToMecRatio}
   */
  public void updateMotorToMecRatio(double motorToMecRatio) {
    this.motorToMecRatio = motorToMecRatio;
  }

  /**
   * @param ratioConfig - the new ratios to use. see {@link Ratios} for
   *        more
   */
  public void updateRatio(Ratios ratioConfig) {
    this.motorToMecRatio = ratioConfig.getMotorToMecRatio();
  }

  /**
   * @param startPose - the new {@code startPose}
   */
  public void setStartPose(double startPose) {
    this.startPose = startPose;
  }

  /**
   * @param newOffset - The new mec offset, used when calc the mec and
   *        motor pose based on the mec, this is the mec's currant
   *        wanted pose
   */
  public void overrideOffset(double newOffset) {
    offset = newOffset;
  }

  public void setCurrentPose(double newPose) {
    offset = GetRobotRelativeMecPoseRaw() - newPose;
  }

  /**
   * Updates the encoder offset to the currant pose encoder's reading
   *
   * @return The new encoder offset
   */
  public double updateOffset() {
    offset = GetRawMecPose();
    return offset;
  }

  /**
   * @return The raw mec pose, based on the encoder's current pose, with
   *         out the offset
   * @apiNote 🚫⚠️ This returns a mec relative pose, not a robot
   *          relative pose
   */
  public double GetRawMecPose() {
    return ((poseSupplier.getAsDouble() * motorToMecRatio));
  }

  /**
   * @return The mec pose, based on the encoder current pose and the
   *         offset
   * @apiNote This returns a mec relative pose, not a robot relative
   *          pose
   */
  public double GetMecPose() {
    return GetRawMecPose() - offset;
  }

  /**
   * @return The mec pose, based on the encoder current pose and the
   *         offset
   * @apiNote This returns a robot relative pose, not a mec relative
   *          pose
   */
  public double GetRobotRelativeMecPose() {
    // convert the mec relative pose to be robot relative
    return GetMecPose() + startPose;
  }

  /**
   * @return The mec pose, based on the encoder current pose and the
   *         offset
   * @apiNote This returns a robot relative pose, not a mec relative
   *          pose
   */
  public double GetRobotRelativeMecPoseRaw() {
    // convert the mec relative pose to be robot relative
    return GetRawMecPose() + startPose;
  }

  /**
   * @return The motor pose, based on the encoder current pose
   */
  public double GetMotorPose() {
    return GetRawMecPose() / motorToMecRatio;
  }

  /**
   * @param mecPose - The mec relative mec pose we want to convert into
   *        motor pose
   * @return The pose the motor would be if the mec was in the inputted
   *         pose
   * @apiNote ✅⚠️This will account for the offset
   */
  public double MecToMotor(double mecPose) {
    return MecToMotorRaw(mecPose - offset);
  }

  /**
   * @param mecPose - The mec relative mec pose we want to convert into
   *        motor pose
   * @return The pose the motor would be if the mec was in the inputted
   *         pose
   * @apiNote 🚫⚠️This will not account for the offset
   */
  public double MecToMotorRaw(double mecPose) {
    return (mecPose) / motorToMecRatio;
  }

  /**
   * @param mecPose - The robot pose we want to convert into motor pose
   * @return The pose the motor would be if the mec was in the inputted
   *         pose
   * @apiNote ✅⚠️This will account for the offset
   */
  public double RobotRelativeMecToMotor(double mecPose) {
    return MecToMotor(mecPose - startPose);
  }

  /**
   * @param mecPose - The robot pose we want to convert into motor pose
   * @return The pose the motor would be if the mec was in the inputted
   *         pose
   * @apiNote ✅⚠️This will account for the offset
   */
  public double RobotRelativeMecToMotorRaw(double mecPose) {
    return MecToMotorRaw(mecPose - startPose);
  }

  /**
   * @param motorPose - The motor pose we want to convert into mec pose
   * @return The pose the mec would be if the motor was in the inputted
   *         pose
   * @apiNote 🚫⚠️ This will not account for the offset
   */
  public double MotorToMec(double motorPose) {
    return (motorPose * motorToMecRatio);
  }

  /**
   * @return The mec's offset
   */
  public double getOffset() {
    return offset;
  }

  /**
   * @return The mec's start pose
   */
  public double getStartPose() {
    return startPose;
  }
}
