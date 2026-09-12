package miscar.mecsIOs.features;

import miscar.mecsIOs.AbstractMechanism;

public interface Feature {
  abstract void executeBeforePeriodic();

  abstract void executeAfterPeriodic();

  abstract void connectMec(AbstractMechanism mechanism);
}
