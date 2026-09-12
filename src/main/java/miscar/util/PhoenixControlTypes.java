package miscar.util;

public class PhoenixControlTypes {
  /** commonly used closed loop motor pose control types */
  public enum PoseControl {
    PositionVoltage, MotionMagicVoltage, MotionMagicExpoVoltage,
    /** will only work if you have FOC enabled for the motor */
    PositionTorqueCurrentFOC
  };
  /** commonly used closed loop motor velocity control types */
  public enum VelocityControl {
    MotionMagicVelocityVoltage, MotionMagicVelocityTorqueCurrentFOC, VelocityVoltage,
    /** will only work if you have FOC enabled for the motor */
    VelocityTorqueCurrentFOC
  };

  /** commonly used open loop motor control types */
  public enum OpenLoopControl {
    // Regular as in that it does not use FOC, so DutyCycle or VoltageOut
    Regular,
    /** will only work if you have FOC enabled for the motor */
    TorqueCurrentFOC
  }
}
