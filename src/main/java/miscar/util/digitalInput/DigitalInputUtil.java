package miscar.util.digitalInput;

import edu.wpi.first.wpilibj.DigitalInput;

public class DigitalInputUtil extends DigitalInput implements DigitalInputIO {

  boolean lastValue = false;
  boolean hasChanged;

  public DigitalInputUtil(int channel) {
    super(channel);
    lastValue = get();
  }

  @Override
  public void updateInputs(DigitalInputs inputs) {
    inputs.value = get();
    inputs.connected = true;

    hasChanged = lastValue != inputs.value;
    lastValue = get();
  }

  @Override
  public boolean get() {
    return !super.get();
  }
}
