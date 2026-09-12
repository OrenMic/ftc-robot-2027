// package miscar.motorIOs;

// import miscar.configs.motors.MotorIOConfig;
// import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;
// import org.littletonrobotics.junction.AutoLog;

// /** an inputs/outputs interface, used for the generic motors */
// public interface MotorIO {

// /** An IO for generic motors */
// @AutoLog
// public class GenericMotorIOInputs {
// /** the motor's connection status */
// public boolean connected = false;
// /** the motor's connection status */
// public double temperature = 0;
// /** the motor's position in rotations */
// public double positionRotations = 0.0;
// /** the motor's velocity in rotations per second */
// public double velocityRotationsPerMinute = 0.0;
// /** the motor's applied Volts */
// public double appliedVolts = 0.0;
// /** the motor's currant Amp consumption */
// public double currentAmps = 0.0;
// }

// // /**
// // * Updates the set of loggable inputs.
// // *
// // * @param inputs - The var inputs to be modified
// // */
// // public default void updateInputs(GenericMotorIOInputs inputs) {}

// public default void logData() {}

// public default void setName(String name) {}

// /**
// * Moves the motor to te wanted pose using closed loop control
// *
// * @param pose - The pose we want to set to the motor
// */
// public default void setTargetPosition(double pose) {}
// ;

// /** Tells the mec to stay in pose and don't move */
// public default void stayInPose() {}

// /**
// * Moves the motor to te in the wanted {@code angularVelocity} using
// closed loop control
// *
// * @param angularVelocityRPM - The angular velocity we want to set
// to the motor, in RPM
// */
// public default void setTargetMotorAngularVelocity(double
// angularVelocityRPM) {}
// ;

// /**
// * Sets the power output for the motor
// *
// * @param power - The Power to set to the motor, in the range of -1
// to 1
// */
// public default void setPower(double power) {}

// /**
// * Sets the power output for the motor
// *
// * @param output - The voltage output to set to the motor, in the
// range of -12 to 12
// */
// public default void setVoltage(double output) {}

// /**
// * @return the currant pose of the motorIO
// */
// public default double getPoseRotations() {
// return 0;
// }

// /** * @param PositionTolerance - The new {@code PositionTolerance}
// */
// public default void setPositionTolerance(double positionTolerance)
// {}

// /** * @param VelocityTolerance - The new {@code VelocityTolerance}
// */
// public default void setVelocityTolerance(double velocityTolerance)
// {}

// public default boolean motorInPosition() {
// return false;
// }

// public default boolean motorInVelocity() {
// return false;
// }

// public default void setClosedLoopSlot(int slot) {}

// /**
// * @return the this motorIO currant config
// */
// public default MotorIOConfig getMotorConfig() {
// return null;
// }

// public default void setNaturalMode(NeutralMode neutralMode) {}
// }
