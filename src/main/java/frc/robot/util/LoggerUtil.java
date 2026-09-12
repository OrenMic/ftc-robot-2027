package frc.robot.util;

import java.lang.reflect.Field;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

public class LoggerUtil {
    public static LogTable getRoot() {
        try {
            Field entry = Logger.class.getDeclaredField("entry");

            entry.setAccessible(true);
            LogTable table = (LogTable) entry.get(null);

            return table;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
