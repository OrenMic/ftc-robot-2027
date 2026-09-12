package frc.robot.generated.transfer.belt;

import miscar.annotation.DoNotOverride;
import miscar.configs.motors.SimConfig;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

@DoNotOverride
public class SimMotorConfig {
  /** The Transfer's belt gear box */
  private final DCMotorUtil beltGearBox = DCMotorUtil.from(DCMotorUtil.getKrakenX60(1));

  /** The Transfer's belt PID controller */
  private final PIDControllerUtil beltPIDController = new PIDControllerUtil(8, 0, 0);

  /** The Transfer's belt MOI */
  private final double beltMOI = 1;

  /** The belt's sim config constructor */
  public final SimConfig config = new SimConfig(beltGearBox, beltPIDController, beltMOI);

  public SimMotorConfig() {}
}
