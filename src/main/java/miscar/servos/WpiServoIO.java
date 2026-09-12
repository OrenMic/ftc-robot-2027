package miscar.servos;

import edu.wpi.first.wpilibj.Servo;
import java.util.OptionalDouble;

public class WpiServoIO implements ServoIO {

  public final Servo servo;

  private boolean isOn = false;

  private OptionalDouble maxAngle = OptionalDouble.empty();

  public WpiServoIO(int channelId) {
    this(new Servo(channelId));
  }

  public WpiServoIO(Servo servo) {
    this.servo = servo;

    turnOn(); // auto turn on for easy use
  }

  @Override
  public void updateInputs(ServoIOInputs inputs) {
    inputs.anglePose = servo.getAngle();
    inputs.current = -1; // No way to access current draw for servos on RoboRIO
    inputs.pulseWidthPose = servo.getPulseTimeMicroseconds();
    inputs.rangedPose = servo.get();
  }

  @Override
  public void setAngle(double angle) {
    if (!isOn)
      return;

    angle = Math.max(0, Math.min(angle, maxAngle.orElse(angle)));
    servo.setAngle(angle);
  }

  @Override
  public void setRangedPose(double pose) {
    if (!isOn)
      return;

    double clampedPose = Math.max(0.0, Math.min(1.0, pose));
    servo.set(clampedPose);
  }

  @Override
  public void setAngleRange(double maxAngle) {
    this.maxAngle = OptionalDouble.of(maxAngle);
  }

  @Override
  public void setPulseWidthPose(int pulseWidthPose) {
    if (!isOn)
      return;
    servo.setPulseTimeMicroseconds(pulseWidthPose);
  }

  @Override
  public void setPulseWidthRange(int min, int max, int deadband) {
    int center = (min + max) / 2;
    servo.setBoundsMicroseconds(max, center - deadband, center, center + deadband, min);
  }

  @Override
  public void turnOn() {
    isOn = true;
  }

  @Override
  public void turnOff() {
    isOn = false;
  }

  @Override
  public boolean isOn() {
    return isOn;
  }
}
