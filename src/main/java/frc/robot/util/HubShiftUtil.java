package frc.robot.util;

// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.


import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Timer;
import java.util.Optional;
import java.util.function.Supplier;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
import lombok.Setter;
import miscar.util.AllianceUtil;

public class HubShiftUtil {


    public enum ShiftEnum {
        TRANSITION, SHIFT1, SHIFT2, SHIFT3, SHIFT4, ENDGAME, AUTO, DISABLED;
    }

    public record ShiftInfo(ShiftEnum currentShift, double elapsedTime, double remainingTime,
            boolean active) {
    }


    public static boolean isredshift = false;

    private static Timer shiftTimer = new Timer();
    private static final ShiftEnum[] shiftsEnums = ShiftEnum.values();

    private static final double[] shiftStartTimes = {0.0, 10.0, 35.0, 60.0, 85.0, 110.0};
    private static final double[] shiftEndTimes = {10.0, 35.0, 60.0, 85.0, 110.0, 140.0};

    // private static final double minFuelCountDelay = 1.0;
    // private static final double maxFuelCountDelay = 2.0;
    // private static final double shiftEndFuelCountExtension = 3.0;
    // private static final double minTimeOfFlight =
    // LaunchCalculator.getMinTimeOfFlight();
    // private static final double maxTimeOfFlight =
    // LaunchCalculator.getMaxTimeOfFlight();
    // private static final double approachingActiveFudge = -1 *
    // (minTimeOfFlight + minFuelCountDelay);
    // private static final double endingActiveFudge =
    // shiftEndFuelCountExtension + -1 * (maxTimeOfFlight +
    // maxFuelCountDelay);

    public static final double autoEndTime = 20.0;
    public static final double teleopDuration = 140.0;
    private static final boolean[] activeSchedule = {true, true, false, true, false, true};
    private static final boolean[] inactiveSchedule = {true, false, true, false, true, true};
    private static final double timeResetThreshold = 3.0;
    private static double shiftTimerOffset = 0.0;
    @Setter
    private static Supplier<Optional<Boolean>> allianceWinOverride = () -> Optional.empty();

    private static LoggedNetworkBoolean allianceWonOverride =
            new LoggedNetworkBoolean("HubShiftUtil/allianceWonOverride", false);

    public static Alliance getFirstActiveAllianceRaw() {
        // Return FMS value
        String message = DriverStation.getGameSpecificMessage();
        Alliance firstAlliance = null;
        if (message.length() > 0) {
            char character = message.charAt(0);
            if (character == 'R') {
                firstAlliance = Alliance.Blue;
            } else if (character == 'B') {
                firstAlliance = Alliance.Red;
            }
        }
        if (firstAlliance == null) {
            firstAlliance = AllianceUtil.isRedAlliance() ? Alliance.Blue : Alliance.Red;
        }

        return firstAlliance;
    }

    public static Alliance getFirstActiveAlliance() {
        Alliance firstAlliance = getFirstActiveAllianceRaw();

        return !allianceWonOverride.getAsBoolean() ? firstAlliance
                : (firstAlliance == Alliance.Red ? Alliance.Blue : Alliance.Red);
    }



    /** Starts the timer at the begining of teleop. */
    public static void initialize() {
        shiftTimerOffset = 0;
        shiftTimer.restart();
    }



    private static boolean[] getSchedule() {
        boolean[] currentSchedule;
        Alliance startAlliance = getFirstActiveAlliance();
        currentSchedule = (startAlliance == AllianceUtil.getCurrntAlliance()) ? activeSchedule
                : inactiveSchedule;
        return currentSchedule;
    }

    private static ShiftInfo getShiftInfo(boolean[] currentSchedule, double[] shiftStartTimes,
            double[] shiftEndTimes) {
        double timerValue = shiftTimer.get();
        double currentTime = timerValue - shiftTimerOffset;
        double stateTimeElapsed = currentTime;
        double stateTimeRemaining = 0.0;
        boolean active = false;
        ShiftEnum currentShift = ShiftEnum.DISABLED;
        double fieldTeleopTime = 140.0 - DriverStation.getMatchTime();

        if (DriverStation.isAutonomousEnabled()) {
            stateTimeElapsed = currentTime;
            stateTimeRemaining = autoEndTime - currentTime;
            active = true;
            currentShift = ShiftEnum.AUTO;
        } else if (DriverStation.isEnabled()) {
            // Adjust the current offset if the time difference above the theshold
            if (Math.abs(fieldTeleopTime - currentTime) >= timeResetThreshold
                    && fieldTeleopTime <= 135 && DriverStation.isFMSAttached()) {
                shiftTimerOffset += currentTime - fieldTeleopTime;
                currentTime = timerValue + shiftTimerOffset;
            }
            int currentShiftIndex = -1;
            for (int i = 0; i < shiftStartTimes.length; i++) {
                if (currentTime >= shiftStartTimes[i] && currentTime < shiftEndTimes[i]) {
                    currentShiftIndex = i;
                    break;
                }
            }
            if (currentShiftIndex < 0) {
                // After last shift, so assume endgame
                currentShiftIndex = shiftStartTimes.length - 1;
            }

            // Calculate elapsed and remaining time in the current shift, ignoring
            // combined shifts
            stateTimeElapsed = currentTime - shiftStartTimes[currentShiftIndex];
            stateTimeRemaining = shiftEndTimes[currentShiftIndex] - currentTime;

            // If the state is the same as the last shift, combine the elapsed
            // time
            if (currentShiftIndex > 0) {
                if (currentSchedule[currentShiftIndex] == currentSchedule[currentShiftIndex - 1]) {
                    stateTimeElapsed = currentTime - shiftStartTimes[currentShiftIndex - 1];
                }
            }

            // If the state is the same as the next shift, combine the remaining
            // time
            if (currentShiftIndex < shiftEndTimes.length - 1) {
                if (currentSchedule[currentShiftIndex] == currentSchedule[currentShiftIndex + 1]) {
                    stateTimeRemaining = shiftEndTimes[currentShiftIndex + 1] - currentTime;
                }
            }

            active = currentSchedule[currentShiftIndex];
            currentShift = shiftsEnums[currentShiftIndex];
        }
        ShiftInfo shiftInfo =
                new ShiftInfo(currentShift, stateTimeElapsed, stateTimeRemaining, active);
        return shiftInfo;
    }

    public static ShiftInfo getOfficialShiftInfo() {
        return getShiftInfo(getSchedule(), shiftStartTimes, shiftEndTimes);
    }


    public static boolean getActive() {
        return getShiftedShiftInfo().active();
    }

    public static double remianingTime() {
        return getOfficialShiftInfo().remainingTime;
    }

    public static ShiftInfo getShiftedShiftInfo() {
        boolean[] shiftSchedule = getSchedule();
        // Starting active
        if (shiftSchedule[1] == true) {
            double[] shiftedShiftStartTimes = {0.0, 10.0, 35.0, 60.0, 85.0, 110.0};
            double[] shiftedShiftEndTimes = {10.0, 35.0, 60.0, 85.0, 110.0, 140.0};
            return getShiftInfo(shiftSchedule, shiftedShiftStartTimes, shiftedShiftEndTimes);
        }
        double[] shiftedShiftStartTimes = {0.0, 10.0, 35.0, 60.0, 85.0, 110.0};
        double[] shiftedShiftEndTimes = {10.0, 35.0, 60.0, 85.0, 110.0, 140.0};
        return getShiftInfo(shiftSchedule, shiftedShiftStartTimes, shiftedShiftEndTimes);
    }
}
