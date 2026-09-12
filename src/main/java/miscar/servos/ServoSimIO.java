package miscar.servos;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class ServoSimIO implements ServoIO {

  private boolean isOn = false;

  private int pulseWidthPose = -1;

  // the standard pulse widths
  int minPulseWidth = 500, maxPulseWidth = 2500;

  OptionalDouble maxAngle = OptionalDouble.empty();

  public ServoSimIO() {
    turnOn(); // auto turn on for easy use
  }

  @Override
  public void updateInputs(ServoIOInputs inputs) {
    inputs.current = -1;
    inputs.pulseWidthPose = pulseWidthPose;
    inputs.rangedPose = (pulseWidthPose - minPulseWidth) / (double) (maxPulseWidth - minPulseWidth);
    inputs.anglePose = scalePulseWidthToAngle(pulseWidthPose).orElse(-1);
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
    pulseWidthPose = scaleAngleToPulseWidth(angle).orElse(pulseWidthPose);
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
    pulseWidthPose = pulseWidth;
  }

  @Override
  public void setPulseWidthPose(int pulseWidthPose) {
    if (!isOn)
      return;
    // Clamp pulseWidthPose to valid range to prevent overflow
    int clampedPulseWidth = Math.max(minPulseWidth, Math.min(maxPulseWidth, pulseWidthPose));
    this.pulseWidthPose = clampedPulseWidth;
  }

  @Override
  public void setPulseWidthRange(int min, int max, int deadband) {
    minPulseWidth = min;
    maxPulseWidth = max;
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
