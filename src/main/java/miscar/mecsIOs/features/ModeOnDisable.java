package miscar.mecsIOs.features;

import edu.wpi.first.wpilibj.DriverStation;
import java.util.function.Consumer;
import miscar.mecsIOs.AbstractMechanism;

public class ModeOnDisable implements Feature {

  public enum NeutralMode {
    BREAK, COAST
  }

  Consumer<NeutralMode> setMode;
  final NeutralMode disabledMode;
  final NeutralMode enabledMode;

  NeutralMode currentMode;

  public ModeOnDisable(NeutralMode disabledMode, NeutralMode enabledMode) {
    this.disabledMode = disabledMode;
    this.enabledMode = enabledMode;
    currentMode = enabledMode;
  }

  @Override
  public void connectMec(AbstractMechanism mechanism) {
    setMode = mechanism.motorIODelegation::setNaturalMode;
  }

  public void executeBeforePeriodic() {
    NeutralMode wantedMode = DriverStation.isEnabled() ? enabledMode : disabledMode;

    if (wantedMode == currentMode)
      return;
    setMode.accept(wantedMode);
    currentMode = wantedMode;
  }

  public void executeAfterPeriodic() {}
}
