package miscar.mecsIOs.features;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import miscar.mecsIOs.AbstractMechanism;

public class AutoUpdateOffset implements Feature {
  private final double resetPose;
  private final BooleanSupplier resetCondition;
  private DoubleConsumer offsetConsumer;

  public AutoUpdateOffset(double resetPose, BooleanSupplier resetCondition) {
    this.resetPose = resetPose;
    this.resetCondition = resetCondition;
  }

  @Override
  public void connectMec(AbstractMechanism mechanism) {
    offsetConsumer = mechanism::setCurrentPose;
  }

  @Override
  public void executeBeforePeriodic() {}

  @Override
  public void executeAfterPeriodic() {
    if (resetCondition.getAsBoolean()) {
      offsetConsumer.accept(resetPose);
    }
  }
}
