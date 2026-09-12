package miscar.motorIOs;

import org.littletonrobotics.junction.AutoLog;

@AutoLog
public class MotorIOInputs {
  /** the motor's connection status */
  public boolean connected = false;
  /** the motor's connection status */
  public double temperature = 0;
  /** the motor's position in rotations */
  public double positionRotations = 0.0;
  /** the motor's velocity in rotations per second */
  public double velocityRotationsPerMinute = 0.0;
  /** the motor's applied Volts */
  public double appliedVolts = 0.0;
  /** the motor's currant Amp consumption */
  public double currentAmps = 0.0;
}
