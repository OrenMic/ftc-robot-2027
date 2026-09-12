package frc.robot;

import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
import frc.robot.util.LoggedTunableNumber;
import lombok.Getter;

public abstract class Tuners {
        public static class Drive {
                @Getter
                private static String name = "Drive";
                private static final boolean tune = true;

                public static final LoggedTunableNumber rotationalTolerance =
                                new LoggedTunableNumber(getName() + "/RotationalTolerance", 5,
                                                tune);

                public static final LoggedTunableNumber shootTolerance =
                                new LoggedTunableNumber(getName() + "/ShootTolerance", 0.3, tune);

                public static LoggedTunableNumber translationalKp = new LoggedTunableNumber(
                                getName() + "/translational" + "/kp", 5, tune);
                public static LoggedTunableNumber translationalKi = new LoggedTunableNumber(
                                getName() + "/translational" + "/ki", 0, tune);
                public static LoggedTunableNumber translationalKd = new LoggedTunableNumber(
                                getName() + "/translational" + "/kd", 0, tune);

                public static LoggedTunableNumber rotarionalKp =
                                new LoggedTunableNumber(getName() + "/rotational/kp", 3, tune);
                public static LoggedTunableNumber rotationalKi =
                                new LoggedTunableNumber(getName() + "/rotational" + "/ki", 0, tune);
                public static LoggedTunableNumber rotationalKd =
                                new LoggedTunableNumber(getName() + "/rotational" + "/kd", 0, tune);
        }

        public static class Transfer {
                @Getter
                private static String name = "Transfer";
                private static final boolean tune = false;

                public static LoggedTunableNumber beltVelocityOffset =
                                new LoggedTunableNumber(getName() + "/beltSpeedOffset", 0, tune);
        }


        public static class Intake {
                @Getter
                private static String name = "Intake";
                private static final boolean tune = true;

                public static final LoggedNetworkBoolean tuneSensors =
                                new LoggedNetworkBoolean(getName() + "/tuneSensors", tune);

                public static final LoggedTunableNumber extensionOffset =
                                new LoggedTunableNumber(getName() + "/extensionOffset", 0, tune);

                public static final LoggedTunableNumber intakeVelocityOffset =
                                new LoggedTunableNumber(getName() + "/intakeVelocityOffset", 0,
                                                tune);
                public static final LoggedTunableNumber resetAmps =
                                new LoggedTunableNumber(getName() + "/resetAmps", 13, tune);
                public static final LoggedTunableNumber resetSpeed =
                                new LoggedTunableNumber(getName() + "/resetSpeed", 4, tune);
                public static final LoggedTunableNumber pulseDelay =
                                new LoggedTunableNumber(getName() + "/pulseDelay", 0.36, tune);
                // public static final LoggedTunableNumber pulseTolerance =
                // new LoggedTunableNumber(getName() + "/pulseTolerance", 0.7, tune);

                public static final LoggedTunableNumber pulseExtensionOffset =
                                new LoggedTunableNumber(getName() + "/pulseExtensionOffset", 0.1,
                                                tune);

                public static final LoggedTunableNumber extensionPulsePower =
                                new LoggedTunableNumber(getName() + "/extensionPulsePower", 0.1,
                                                tune);
                public static final LoggedTunableNumber extensionsStopOffset =
                                new LoggedTunableNumber(getName() + "/extensionsStopOffset", 0.1,
                                                tune);



        }

        public static class Shooter {
                @Getter
                private static String name = "Shooter";
                private static boolean tune = false;

        }
}
