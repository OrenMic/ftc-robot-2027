package miscar.kalman;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Ellipse2d;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.numbers.N2;
import frc.robot.util.GeomUtil;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(value = GeomUtil.class)
public class Ellipse2dUtil {
    public static Pair<Translation2d, Translation2d> getYExtrema(Ellipse2d ellipse) {
        double a = ellipse.getXSemiAxis();
        double b = ellipse.getYSemiAxis();

        double theta = ellipse.getCenter().getRotation().getRadians();

        double tMax = Math.atan2(b * Math.cos(theta), a * Math.sin(theta));

        double tMin = tMax + Math.PI;

        Translation2d maxPoint = new Translation2d(
                ellipse.getCenter().getX() + a * Math.cos(tMax) * Math.cos(theta)
                        - b * Math.sin(tMax) * Math.sin(theta),

                ellipse.getCenter().getY() + a * Math.cos(tMax) * Math.sin(theta)
                        + b * Math.sin(tMax) * Math.cos(theta));

        Translation2d minPoint = new Translation2d(
                ellipse.getCenter().getX() + a * Math.cos(tMin) * Math.cos(theta)
                        - b * Math.sin(tMin) * Math.sin(theta),

                ellipse.getCenter().getY() + a * Math.cos(tMin) * Math.sin(theta)
                        + b * Math.sin(tMin) * Math.cos(theta));

        return new Pair<>(maxPoint, minPoint);
    }

    public static Translation2d getMaxYPoint(Ellipse2d ellipse) {
        double a = ellipse.getXSemiAxis();
        double b = ellipse.getYSemiAxis();

        double theta = ellipse.getCenter().getRotation().getRadians();

        double t = Math.atan2(b * Math.cos(theta), a * Math.sin(theta));

        double x = ellipse.getCenter().getX() + a * Math.cos(t) * Math.cos(theta)
                - b * Math.sin(t) * Math.sin(theta);

        double y = ellipse.getCenter().getY() + a * Math.cos(t) * Math.sin(theta)
                + b * Math.sin(t) * Math.cos(theta);

        return new Translation2d(x, y);
    }

    public static Ellipse2d toCovarianceEllipse(double stddev, Pose2d center) {
        return toCovarianceEllipse(stddev, center.getTranslation());
    }

    public static Ellipse2d toCovarianceEllipse(double stddev, Translation2d center) {
        return toCovarianceEllipse(
                new Matrix<N2, N2>(Nat.N2(), Nat.N2(), new double[] {stddev, 0, 0, stddev}),
                center);
    }

    public static Ellipse2d toCovarianceEllipse(Matrix<N2, N2> covariance, Translation2d center) {
        double a = covariance.get(0, 0);
        double b = covariance.get(0, 1);
        double d = covariance.get(1, 1);

        // Eigenvalues
        double term = Math.sqrt(Math.pow((a - d) / 2.0, 2) + b * b);

        double lambda1 = (a + d) / 2.0 + term;
        double lambda2 = (a + d) / 2.0 - term;

        double lambdaMax = Math.max(lambda1, lambda2);
        double lambdaMin = Math.min(lambda1, lambda2);

        // 1-sigma semi-axis lengths
        double majorAxis = Math.sqrt(lambdaMax);
        double minorAxis = Math.sqrt(lambdaMin);

        // Rotation of major axis
        double angle = 0.5 * Math.atan2(2.0 * b, a - d);

        return new Ellipse2d(center.toPose2d().withRotation(Rotation2d.fromRadians(angle)),
                majorAxis, minorAxis);


    }
}
