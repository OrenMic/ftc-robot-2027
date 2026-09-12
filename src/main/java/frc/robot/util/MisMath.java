package frc.robot.util;

public class MisMath {
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static double doubleInverseLerp(double start, double end, double q) {
        return (q - start) / (end - start);
    }
}
