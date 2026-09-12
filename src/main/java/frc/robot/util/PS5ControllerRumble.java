package frc.robot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.PS5Controller;
import edu.wpi.first.wpilibj.Timer;

public class PS5ControllerRumble extends PS5Controller {

    public record Pulse(RumbleType type, double value, double time) {
    }

    Timer rumbleTimer = new Timer();
    double lastRumbleValue;
    private int pulseIndex = 0;
    GenericHID hid;

    private List<Pulse> pulses = new ArrayList<>();

    public PS5ControllerRumble(int controllerPort, int rumblePort) {
        super(controllerPort);
        hid = new GenericHID(rumblePort);
    }

    @Override
    public void setRumble(RumbleType type, double value) {
        hid.setRumble(type, value);
    }

    public void stopRumble() {
        setRumble(RumbleType.kBothRumble, 0);
    }


    public void rumblePulse(Pulse... pulses) {
        if (differentiatePulse(pulses)) {
            pulseIndex = 0;
            this.pulses = Arrays.asList(pulses);
        }

        rumbleFor(pulses[pulseIndex].type, pulses[pulseIndex].value, pulses[pulseIndex].time);
        if (changeRumble(pulses[pulseIndex].time)) {
            pulseIndex++;
            rumbleTimer.restart();
            if (pulseIndex > pulses.length - 1) {
                pulseIndex = 0;
                stopRumble();
            }
        }
    }


    public void rumbleFor(RumbleType type, double value, double time) {
        if (lastRumbleValue != value) {
            lastRumbleValue = value;
            rumbleTimer.restart();
        }

        setRumble(type, changeRumble(time) ? 0 : value);
    }

    private boolean changeRumble(double time) {
        return rumbleTimer.get() > time;

    }



    private boolean differentiatePulse(Pulse... pulses) {
        if (pulses.length != this.pulses.size()) {
            return true;
        }
        return !this.pulses.containsAll(Arrays.asList(pulses));
    }
}
