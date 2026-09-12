package miscar.servos.servoHub;

import com.revrobotics.servohub.ServoChannel.ChannelId;
import com.revrobotics.servohub.ServoHub;
import miscar.servos.RevServoIO;
import miscar.servos.ServoIO;

public class RevServoHub implements ServoHubIO {

  private static volatile RevServoHub instance;
  public ServoHub servoHub;

  private RevServoHub(int port) {
    servoHub = new ServoHub(port);
  }

  /**
   * Builds a new RevServoHub instance if one does not exist.
   *
   * @param port The port number of the RevServoHub.
   * @return The RevServoHub instance.
   */
  public static RevServoHub build(int port) {
    if (instance == null) {
      synchronized (RevServoHub.class) {
        if (instance == null) {
          instance = new RevServoHub(port);
        }
      }
    }
    return instance;
  }

  /**
   * Builds a new RevServoHub instance if one does not exist.
   *
   * @return The RevServoHub instance.
   */
  public static RevServoHub getInstance() {
    if (instance == null) {
      throw new RuntimeException(
          "RevServoHub.getInstance() was called before RevServoHub.build(int port)");
    }
    return instance;
  }

  @Override
  public void updateInputs(ServoHubIOInputs inputs) {
    // Device Inputs
    inputs.deviceInputs.current = servoHub.getDeviceCurrent();
    inputs.deviceInputs.ID = servoHub.getDeviceId();
    inputs.deviceInputs.controlFramePeriodMs = servoHub.getControlFramePeriodMs();
    inputs.deviceInputs.firmwareVersion = servoHub.getFirmwareVersionString();
    inputs.deviceInputs.servoVoltage = servoHub.getServoVoltage();
    inputs.deviceInputs.voltage = servoHub.getDeviceVoltage();
    inputs.deviceInputs.hasActiveFaults = servoHub.hasActiveFault();
    inputs.deviceInputs.hasActiveWarnings = servoHub.hasActiveWarning();
    inputs.deviceInputs.hasStickyFaults = servoHub.hasStickyFault();
    inputs.deviceInputs.hasStickyWarnings = servoHub.hasStickyWarning();

    // Faults
    var faults = servoHub.getFaults();
    inputs.faultsInputs.firmware = faults.firmware;
    inputs.faultsInputs.hardware = faults.hardware;
    inputs.faultsInputs.rawBits = faults.rawBits;
    inputs.faultsInputs.regulatorPowerGood = faults.regulatorPowerGood;

    // Sticky Faults
    var stickyFaults = servoHub.getStickyFaults();
    inputs.stickyFaultsInputs.firmware = stickyFaults.firmware;
    inputs.stickyFaultsInputs.hardware = stickyFaults.hardware;
    inputs.stickyFaultsInputs.rawBits = stickyFaults.rawBits;
    inputs.stickyFaultsInputs.regulatorPowerGood = stickyFaults.regulatorPowerGood;

    // Warnings
    var warnings = servoHub.getWarnings();
    inputs.warningsInputs.brownout = warnings.brownout;
    inputs.warningsInputs.canWarning = warnings.canWarning;
    inputs.warningsInputs.canBusOff = warnings.canBusOff;
    inputs.warningsInputs.hasReset = warnings.hasReset;
    inputs.warningsInputs.channel0Overcurrent = warnings.channel0Overcurrent;
    inputs.warningsInputs.channel1Overcurrent = warnings.channel1Overcurrent;
    inputs.warningsInputs.channel2Overcurrent = warnings.channel2Overcurrent;
    inputs.warningsInputs.channel3Overcurrent = warnings.channel3Overcurrent;
    inputs.warningsInputs.channel4Overcurrent = warnings.channel4Overcurrent;
    inputs.warningsInputs.channel5Overcurrent = warnings.channel5Overcurrent;
    inputs.warningsInputs.rawBits = warnings.rawBits;

    // Sticky Warnings
    var stickyWarnings = servoHub.getStickyWarnings();
    inputs.stickyWarningsInputs.brownout = stickyWarnings.brownout;
    inputs.stickyWarningsInputs.canWarning = stickyWarnings.canWarning;
    inputs.stickyWarningsInputs.canBusOff = stickyWarnings.canBusOff;
    inputs.stickyWarningsInputs.hasReset = stickyWarnings.hasReset;
    inputs.stickyWarningsInputs.channel0Overcurrent = stickyWarnings.channel0Overcurrent;
    inputs.stickyWarningsInputs.channel1Overcurrent = stickyWarnings.channel1Overcurrent;
    inputs.stickyWarningsInputs.channel2Overcurrent = stickyWarnings.channel2Overcurrent;
    inputs.stickyWarningsInputs.channel3Overcurrent = stickyWarnings.channel3Overcurrent;
    inputs.stickyWarningsInputs.channel4Overcurrent = stickyWarnings.channel4Overcurrent;
    inputs.stickyWarningsInputs.channel5Overcurrent = stickyWarnings.channel5Overcurrent;
    inputs.stickyWarningsInputs.rawBits = stickyWarnings.rawBits;
  }

  @Override
  public ServoIO getServo(int channelId) {
    return new RevServoIO(servoHub.getServoChannel(ChannelId.fromInt(channelId)));
  }
}
