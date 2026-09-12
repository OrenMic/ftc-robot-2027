package miscar.util;

import edu.wpi.first.math.controller.PIDController;
import java.lang.reflect.Field;

public class PIDControllerUtil extends PIDController implements Cloneable {

  public PIDControllerUtil(double kp, double ki, double kd) {
    super(kp, ki, kd);
  }

  public PIDControllerUtil(double kp, double ki, double kd, double period) {
    super(kp, ki, kd, period);
  }

  public static PIDControllerUtil from(PIDController otherPIDController) {
    return new PIDControllerUtil(otherPIDController.getP(), otherPIDController.getI(),
        otherPIDController.getD());
  }

  public PIDControllerUtil clone() {
    PIDControllerUtil pidController = new PIDControllerUtil(0, 0, 0);

    // Init PID
    pidController.setP(getP());
    pidController.setI(getI());
    pidController.setD(getD());
    pidController.setIZone(getIZone());
    pidController.setTolerance(getErrorTolerance());
    pidController.setTolerance(getErrorTolerance(), getErrorDerivativeTolerance());

    if (pidController.isContinuousInputEnabled()) {
      try {
        Field fieldMinimumInput = PIDController.class.getDeclaredField("m_minimumInput");
        Field fieldMaximumInput = PIDController.class.getDeclaredField("m_maximumInput");

        fieldMinimumInput.setAccessible(true);
        fieldMaximumInput.setAccessible(true);

        double minimumInput = (double) fieldMinimumInput.get(pidController);
        double maximumInput = (double) fieldMaximumInput.get(pidController);

        pidController.enableContinuousInput(minimumInput, maximumInput);
      } catch (Exception e) {
        throw new RuntimeException("enableContinuousInput fields' names have been changed");
      }
    }

    return pidController;
  }
}
