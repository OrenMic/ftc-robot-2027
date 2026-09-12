package miscar.kalman;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.StateSpaceUtil;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.estimator.KalmanFilter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.wpilibj.Timer;

public class RotationalKalman {
    static Matrix<N1, N1> A = new Matrix<>(Nat.N1(), Nat.N1());
    static Matrix<N1, N1> B = Matrix.eye(Nat.N1());
    static Matrix<N1, N1> C = Matrix.eye(Nat.N1());
    static Matrix<N1, N1> D = new Matrix<>(Nat.N1(), Nat.N1());

    static LinearSystem<N1, N1, N1> system = new LinearSystem<>(A, B, C, D);
    KalmanFilter<N1, N1, N1> filter;

    Vector<N1> lastU = VecBuilder.fill(0.0);

    Rotation2d previousAngle;
    Rotation2d gyroOffset;

    double dt = 0.02;
    double lastPredict;

    public RotationalKalman(Rotation2d initialRotation, Matrix<N1, N1> stateStdDevs,
            Matrix<N1, N1> visionStdDevs) {
        filter = new KalmanFilter<N1, N1, N1>(Nat.N1(), Nat.N1(), system, stateStdDevs,
                visionStdDevs, dt);

        lastPredict = Timer.getTimestamp();
        Rotation2d gyroAngle = new Rotation2d();
        setPose(gyroAngle, initialRotation);
    }

    public void predict(Vector<N1> u) {
        // dt = Timer.getTimestamp() - lastPredict;
        // lastPredict = Timer.getTimestamp();
        // if (dt > 1)
        // dt = 0.02;
        filter.predict(u, dt);
        lastU = u;
    }

    public void correct(Vector<N1> y, Vector<N1> stddevs) {
        filter.correct(lastU, y, StateSpaceUtil.makeCovarianceMatrix(Nat.N1(), stddevs));
    }

    public Rotation2d getRotation() {
        return new Rotation2d(getRotationRad());
    }

    public double getRotationRad() {
        return filter.getXhat(0);
    }

    public double getP(int row, int col) {
        return filter.getP(row, col);
    }

    public void setPose(Rotation2d rawGyro, Rotation2d rotation) {
        filter.reset();
        filter.setXhat(VecBuilder.fill(rotation.getRadians()));
        // filter.setP(Matrix.eye(Nat.N1()).times(999));

        previousAngle = rotation;
        gyroOffset = rawGyro.unaryMinus().rotateBy(rotation);
    }

    public double updateRotation(Rotation2d newRotation2d) {
        Rotation2d adjustedAngle = newRotation2d.rotateBy(gyroOffset);
        double omega = adjustedAngle.minus(previousAngle).getRadians() / 0.02;
        previousAngle = adjustedAngle;
        return omega;
    }

}
