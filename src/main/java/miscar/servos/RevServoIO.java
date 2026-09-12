package miscar.servos;

import com.revrobotics.servohub.ServoChannel;
import com.revrobotics.servohub.ServoHub;
import com.revrobotics.servohub.config.ServoChannelConfig;
import com.revrobotics.servohub.config.ServoHubConfig;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import miscar.servos.servoHub.RevServoHub;

public class RevServoIO implements ServoIO {

  public final ServoChannel servo;

  private boolean isOn = false;

  // the standard pulse widths
  int minPulseWidth = 500, maxPulseWidth = 2500;

  OptionalDouble maxAngle = OptionalDouble.empty();

  public RevServoIO(ServoChannel servo) {
    this.servo = servo;

    turnOn(); // auto turn on for easy use
  }

  @Override
  public void updateInputs(ServoIOInputs inputs) {
    inputs.current = servo.getCurrent();
    inputs.pulseWidthPose = servo.getPulseWidth();
    int pulseWidth = servo.getPulseWidth();
    inputs.rangedPose = (pulseWidth - minPulseWidth) / (double) (maxPulseWidth - minPulseWidth);
    inputs.anglePose = scalePulseWidthToAngle(pulseWidth).orElse(-1);
  }

  // Helper to scale angle to pulse width
  private OptionalInt scaleAngleToPulseWidth(double angle) {
    double clampedAngle = Math.max(0.0, Math.min(maxAngle.orElse(0.0), angle));
    // maxAngle.map

    // Return the corresponding pulse width for the given angle, if
    // maxAngle is present
    return maxAngle.stream()
        .map(currentMaxAngle -> minPulseWidth
            + clampedAngle * (maxPulseWidth - minPulseWidth) / currentMaxAngle)
        .mapToInt(pulseWidth -> (int) pulseWidth).findFirst();
  }

  // Helper to scale pulse width to angle
  private OptionalDouble scalePulseWidthToAngle(int pulseWidth) {
    double clampedPulse = Math.max(minPulseWidth, Math.min(maxPulseWidth, pulseWidth));

    return maxAngle.stream().map((currantMaxAngle -> (clampedPulse - minPulseWidth)
        * currantMaxAngle / (maxPulseWidth - minPulseWidth))).findFirst();
  }

  @Override
  public void setAngle(double angle) {
    if (!isOn)
      return;
    scaleAngleToPulseWidth(angle).ifPresent(servo::setPulseWidth);
  }

  @Override
  public void setAngleRange(double maxAngle) {
    this.maxAngle = OptionalDouble.of(maxAngle);
  }

  @Override
  public void setRangedPose(double pose) {
    if (!isOn)
      return;

    // Clamp pose to [0, 1]
    double clampedPose = Math.max(0.0, Math.min(1.0, pose));
    int pulseWidth = (int) (minPulseWidth + clampedPose * (maxPulseWidth - minPulseWidth));
    servo.setPulseWidth(pulseWidth);
  }

  @Override
  public void setPulseWidthPose(int pulseWidthPose) {
    if (!isOn)
      return;
    // Clamp pulseWidthPose to valid range to prevent overflow
    int clampedPulseWidth = Math.max(minPulseWidth, Math.min(maxPulseWidth, pulseWidthPose));
    servo.setPulseWidth(clampedPulseWidth);
  }

  @Override
  public void setPulseWidthRange(int min, int max, int deadband) {
    minPulseWidth = min;
    maxPulseWidth = max;

    ServoChannelConfig channelConfig = new ServoChannelConfig(servo.getChannelId());

    channelConfig.pulseRange(min, (min + max) / 2, max);

    ServoHubConfig config = new ServoHubConfig();
    config.apply(servo.getChannelId(), channelConfig);

    // Persist parameters and reset any not explicitly set above to
    // their defaults.
    RevServoHub.getInstance().servoHub.configure(config, ServoHub.ResetMode.kResetSafeParameters);
  }

  @Override
  public void turnOn() {
    servo.setEnabled(true);
    servo.setPowered(true);
    isOn = true;
  }

  @Override
  public void turnOff() {
    servo.setEnabled(false);
    servo.setPowered(false);
    isOn = false;
  }

  @Override
  public boolean isOn() {
    return isOn;
  }
}
