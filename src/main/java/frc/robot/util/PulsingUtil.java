package frc.robot.util;

import edu.wpi.first.wpilibj.Timer;
import lombok.Setter;

public class PulsingUtil {
    private Timer timer = new Timer();
    @Setter
    private double endTime = 0;

    public PulsingUtil(double endTime) {
        timer.restart();
    }

    public boolean hasPassed() {
        return timer.get() > endTime;
    }

    public int timesHasPassed() {
        return (int) (timer.get() / endTime);
    }

    public void restart() {
        timer.restart();
    }



}
