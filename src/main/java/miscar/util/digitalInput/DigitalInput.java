package miscar.util.digitalInput;

import frc.robot.Constants;
import frc.robot.Constants.Mode;
import org.littletonrobotics.junction.Logger;

public class DigitalInput {
  private final DigitalInputIO digitalInputDelegation;
  private final DigitalInputsAutoLogged inputs = new DigitalInputsAutoLogged();
  private final int channel;
  private boolean lastValue;
  private boolean hasChanged = false;

  public DigitalInput(int channel) {
    digitalInputDelegation = Constants.currentMode == Mode.REPLAY ? new DigitalInputIO() {}
        : new DigitalInputUtil(channel);

    this.channel = channel;
    lastValue = digitalInputDelegation.get();
  }

  public void updateInputs() {
    digitalInputDelegation.updateInputs(inputs);

    Logger.processInputs("DigitalIO/Inputs/" + channel, inputs);
    // Logger.processInputs("endEffector/Beam break/Inputs", inputs);

    hasChanged = lastValue != get();
    lastValue = inputs.value;
  }

  public boolean get() {
    return inputs.value;
  }

  public boolean hasChanged() {
    return hasChanged;
  }

  public boolean hasChangedToTrue() {
    return hasChanged && get();
  }

  public boolean hasChangedToFalse() {
    return hasChanged && !get();
  }
}
