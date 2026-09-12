package frc.robot.subsystems.leds;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.generated.ports.RoboRIOPorts;

public class Leds extends SubsystemBase {
  AddressableLED led;
  AddressableLEDBuffer ledBuffer;
  int length = 13;

  private static Leds instance = new Leds();

  public static Leds getInstance() {
    return instance;
  }

  private Leds() {
    led = new AddressableLED(RoboRIOPorts.PWM.leds);

    ledBuffer = new AddressableLEDBuffer(length);
    led.setLength(ledBuffer.getLength());

    // Set the buffer to the LEDs
    led.setData(ledBuffer);
    led.start();
  }

  public void setColor(Color color) {
    for (int i = 0; i < length; i++) {
      ledBuffer.setLED(i, color);
    }
    led.setData(ledBuffer);
  }
}
