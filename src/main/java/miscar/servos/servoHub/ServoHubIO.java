package miscar.servos.servoHub;

import miscar.servos.ServoIO;
import org.littletonrobotics.junction.AutoLog;

public interface ServoHubIO {

  @AutoLog
  public class FaultsInputs {
    public boolean firmware = false;
    public boolean hardware = false;
    public int rawBits = -1;
    public boolean regulatorPowerGood = true;
  }

  @AutoLog
  public class WarningsInputs {
    public boolean brownout = false;
    public boolean canWarning = false;
    public boolean canBusOff = false;
    public boolean hasReset = false;
    public boolean channel0Overcurrent = false;
    public boolean channel1Overcurrent = false;
    public boolean channel2Overcurrent = false;
    public boolean channel3Overcurrent = false;
    public boolean channel4Overcurrent = false;
    public boolean channel5Overcurrent = false;
    public int rawBits = -1;
  }

  @AutoLog
  public class DeviceInputs {
    public int controlFramePeriodMs = -1;
    public int ID = -1;
    public double current = -1.0;
    public double voltage = -1.0;
    public String firmwareVersion = "Undefined";
    public double servoVoltage = -1;

    public boolean hasActiveFaults = false;
    public boolean hasActiveWarnings = false;
    public boolean hasStickyFaults = false;
    public boolean hasStickyWarnings = false;
  }

  public class ServoHubIOInputs {
    FaultsInputsAutoLogged faultsInputs = new FaultsInputsAutoLogged();
    FaultsInputsAutoLogged stickyFaultsInputs = new FaultsInputsAutoLogged();

    WarningsInputsAutoLogged warningsInputs = new WarningsInputsAutoLogged();
    WarningsInputsAutoLogged stickyWarningsInputs = new WarningsInputsAutoLogged();

    DeviceInputsAutoLogged deviceInputs = new DeviceInputsAutoLogged();

    public ServoHubIOInputs() {}

    public ServoHubIOInputs(ServoHubIOInputs inputs) {
      faultsInputs = inputs.faultsInputs.clone();
      stickyFaultsInputs = inputs.stickyFaultsInputs.clone();

      warningsInputs = inputs.warningsInputs.clone();
      stickyWarningsInputs = inputs.stickyWarningsInputs.clone();

      deviceInputs = inputs.deviceInputs.clone();
    }
  }

  public default void updateInputs(ServoHubIOInputs inputs) {}

  public default ServoIO getServo(int channelId) {
    return new ServoIO() {};
  }
}
