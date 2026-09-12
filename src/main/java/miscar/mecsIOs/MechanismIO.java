package miscar.mecsIOs;

import java.util.function.BooleanSupplier;
import miscar.configs.mecs.Ratios;
import miscar.mecsIOs.features.Feature;
import miscar.motorIOs.MotorIOInputs;
// import miscar.motorIOs.MotorIO;
import org.littletonrobotics.junction.AutoLog;

/**
 * an inputs/outputs interface, used for the mechanisms on the robot
 */
public interface MechanismIO {
  public record AutoUpdateOffsetState(BooleanSupplier conditionSupplier, double updateOffsetPose) {}

  /** An IO for mechanisms */
  @AutoLog
  public class MechanismIOInputs extends MotorIOInputs {
    /** The mec's currant position in the mec's units */
    public double mecPosition = 0.0;

    /** The mec's currant velocity in the mec's units per minute */
    public double mecVelocity = 0.0;
  }

  /**
   * Updates the set of loggable inputs.
   *
   * @param inputs - The var inputs to be modified
   */
  public default void updateInputs(MechanismIOInputs inputs) {}

  /**
   * Sets the start pose of the mec
   *
   * @param startMecPose - The new start pose, in robot relative units
   */
  public default void setStartPose(double startMecPose) {}

  /**
   * Sets the max allowed movement for the mec
   *
   * @param maxAllowedMovement - The new max movement, this must be
   *        robot relative
   */
  public default void setMaxMovement(double maxAllowedMovement) {}

  /**
   * Sets the min allowed movement for the mec
   *
   * @param minAllowedMovement - The new min movement, this must be
   *        robot relative
   */
  public default void setMinMovement(double minAllowedMovement) {}

  /**
   * Sets the max allowed velocity for the mec
   *
   * @param maxAllowedVelocity - The new max velocity, this must be in
   *        the mec's units
   */
  public default void setMaxVelocity(double maxAllowedVelocity) {}

  /**
   * Makes it so that this mec will save its pose offset to the RoboRIO
   *
   * @param saveOffsetPath - The path to where the offset should be
   *        saved
   */
  public default void saveMecOffset(String saveOffsetPath) {}

  /**
   * // * Makes it so that this mec will automatically save its pose
   * offset to the RoboRIO
   *
   * @param saveOffsetPath - The path to where the offset should be
   *        saved
   */
  public default void saveMecOffset(String saveOffsetPath, boolean continueSaving) {}

  /**
   * reloads the mec's offset from the RoboRIO
   *
   * @apiNote this will not do any thing if you haven't already
   *          configured a save path, see {@link #saveMecOffset(String)}
   *          for more.
   */
  public default void reloadOffset() {}

  /**
   * reloads the mec's offset from the RoboRIO
   *
   * @param saveOffsetPath - The path to where the offset is saved
   */
  public default void reloadOffset(String saveOffsetPath) {}

  /**
   * updates the mec offset
   *
   * @return the new mec offset
   */
  public default double updateOffset() {
    return 0;
  }

  /**
   * @param newOffset - the new mec offset, used when calc the mec and
   *        motor pose based on the mecs, this is the mecs's currant
   *        wanted pose
   */
  public default void overrideOffset(double newOffset) {}

  public default void setCurrentPose(double newPose) {}

  /**
   * @param motorToMecRatio - see {@link MountedMecTest} for more
   */
  public default void setMotorToMecRatio(double motorToMecRatio) {}

  /**
   * @param ratioConfig- see {@link MountedMecTest} and {@link Ratios}
   *        for more
   */
  public default void setRatio(Ratios ratioConfig) {}

  /**
   * Moves the mec to the wanted {@code velocity} using closed-loop
   * control
   *
   * @param velocity - the wanted mec velocity, in mec units
   */
  public default void setTargetMecVelocity(double velocity) {}

  public default void addFeature(Feature feature) {}

  public default boolean mecInPosition() {
    return false;
  }

  public default boolean mecInVelocity() {
    return false;
  }
}
