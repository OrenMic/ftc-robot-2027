package miscar.configs.motors;

import com.ctre.phoenix.motorcontrol.can.SlotConfiguration;
import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;
import miscar.mecsIOs.features.ModeOnDisable;
import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;
import miscar.motorIOs.EmptyMotor;
import miscar.motorIOs.MotorIOTalon;
import miscar.motorIOs.MotorIOTalonSRX;
import miscar.motorIOs.MotorIOVictor;
import miscar.motorIOs.MotorIOVictorSPX;

@ConstantsName(constantsName = "phoenix5MotorConfig")
public class Phoenix5Config extends MotorIOConfig {
  public enum Type {
    TALON_SRX, TALON, VICTOR_SPX, VICTOR
  }

  public SlotConfiguration slotConfig = new SlotConfiguration();
  public ModeOnDisable.NeutralMode neutralMode = NeutralMode.COAST;
  public final Type type;

  @ConstantsConstructor
  public Phoenix5Config(int motorPort, Type type) {
    super();
    this.motorPort = motorPort;
    this.type = type;
  }

  public Phoenix5Config(Phoenix5Config config) {
    super();
    this.motorPort = config.motorPort;
    this.type = config.type;
    this.withInverted(config.inverted).withMotorPort(config.motorPort);
  }

  @Override
  public EmptyMotor buildMotor() {
    return switch (type) {
      case TALON_SRX -> new MotorIOTalonSRX(this);
      case VICTOR_SPX -> new MotorIOVictorSPX(this);
      case TALON -> new MotorIOTalon(this);
      case VICTOR -> new MotorIOVictor(this);
    };
  }

  @Override
  public Phoenix5Config withMotorPort(int motorPort) {
    this.motorPort = motorPort;
    return this;
  }

  public Phoenix5Config withSlotConfig(SlotConfiguration slotConfig) {
    this.slotConfig = slotConfig;

    return this;
  }

  public Phoenix5Config withNeutralMode(NeutralMode neutralMode) {
    this.neutralMode = neutralMode;

    return this;
  }

  @Override
  public Phoenix5Config withInverted(boolean inverted) {
    super.withInverted(inverted);

    return this;
  }
}
