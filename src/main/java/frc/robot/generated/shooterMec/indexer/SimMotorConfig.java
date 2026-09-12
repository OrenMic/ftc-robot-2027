package frc.robot.generated.shooterMec.indexer;

import miscar.annotation.DoNotOverride;
import miscar.configs.motors.SimConfig;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

@DoNotOverride
public class SimMotorConfig {
  /** The ShooterMec's indexer gear box */
  private final DCMotorUtil indexerGearBox = DCMotorUtil.from(DCMotorUtil.getKrakenX60(1));

  /** The ShooterMec's indexer PID controller */
  private final PIDControllerUtil indexerPIDController = new PIDControllerUtil(8, 0, 0);

  /** The ShooterMec's indexer MOI */
  private final double indexerMOI = 1;

  /** The indexer's sim config constructor */
  public final SimConfig config = new SimConfig(indexerGearBox, indexerPIDController, indexerMOI);

  public SimMotorConfig() {}
}
