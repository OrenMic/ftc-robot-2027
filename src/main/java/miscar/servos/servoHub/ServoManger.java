package miscar.servos.servoHub;

import frc.robot.Constants;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import miscar.servos.ServoIO;
import miscar.servos.ServoSimIO;
import miscar.servos.WpiServoIO;
import org.littletonrobotics.junction.Logger;

public class ServoManger extends Thread implements ServoHubIO {

  /** The type of servos to manage */
  public enum ServoMangerType {
    /** Will manage the REV servo hub and its connected servos */
    RevServoHub,
    /** Will manage standard WPI servos and log their inputs */
    WpiServos,

    /**
     * Will create empty servos which do not move or do any thing, used
     * for when temporary disabling the robot's servos is needed
     */
    EmptyServos
  }

  private final ServoMangerType servoMangerType;

  static ServoManger instance;

  private volatile boolean running = false;

  private Function<Integer, ServoIO> getServoFunction = (channelId) -> new ServoIO() {};

  private final AtomicReference<ServoHubIOInputs> inputs =
      new AtomicReference<ServoHubIO.ServoHubIOInputs>(new ServoHubIOInputs());

  private Consumer<ServoHubIOInputs> updateInputsFunction = (inputs) -> {
  };

  private ServoManger(int port, ServoMangerType type) {
    servoMangerType = type;
    switch (Constants.currentMode) {
      case REAL:
        switch (type) {
          case RevServoHub:
            RevServoHub.build(port);

            getServoFunction = RevServoHub.getInstance()::getServo;
            updateInputsFunction = RevServoHub.getInstance()::updateInputs;
            break;
          case WpiServos:
            getServoFunction = WpiServoIO::new;
            break;

          case EmptyServos:
            break;
          default:
            break;
        }
        break;
      case SIM:
        getServoFunction = (channelId) -> new ServoSimIO();

        break;

      case REPLAY:
        getServoFunction = (channelId) -> new ServoIO() {};
        break;
      default:
        break;
    }

    setName("ServoHubThread");
    setDaemon(true);
  }

  /**
   * Builds a new ServoManger instance if one does not exist.
   *
   * @param port The port number of the ServoManger.
   * @return The ServoManger instance.
   */
  public static ServoManger build(int port, ServoMangerType type) {
    if (instance == null) {
      instance = new ServoManger(port, type);
    }
    return instance;
  }

  /**
   * @return The ServoManger instance.
   */
  public static ServoManger getInstance() {
    if (instance == null) {
      throw new RuntimeException(
          "ServoManger.getInstance() was called before ServoManger.build(int port)");
    }
    return instance;
  }

  @Override
  public synchronized void start() {
    if (!running && servoMangerType == ServoMangerType.RevServoHub) {
      running = true;
      super.start();
    }
  }

  /**
   * Will start a thread which will auto log the inputs of the
   * configured {@link ServoHubIO}
   */
  @Override
  public void run() {
    if (servoMangerType != ServoMangerType.RevServoHub) {
      return;
    }
    while (running) {
      updateInputs(new ServoHubIOInputs());
      try {
        Thread.sleep(20); // 50Hz update rate
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  public ServoHubIOInputs getInputs() {
    return new ServoHubIOInputs(inputs.get());
  }

  @Override
  public ServoIO getServo(int channelId) {
    return getServoFunction.apply(channelId);
  }

  @Override
  public void updateInputs(ServoHubIOInputs inputs) {
    updateInputsFunction.accept(inputs);

    // Logging
    Logger.processInputs("ServoHub/DeviceInputs", inputs.deviceInputs);
    Logger.processInputs("ServoHub/FaultsInputs", inputs.faultsInputs);
    Logger.processInputs("ServoHub/StickyFaultsInputs", inputs.stickyFaultsInputs);
    Logger.processInputs("ServoHub/WarningsInputs", inputs.warningsInputs);
    Logger.processInputs("ServoHub/StickyWarningsInputs", inputs.stickyWarningsInputs);
  }
}
