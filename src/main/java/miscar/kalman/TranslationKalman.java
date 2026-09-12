package miscar.kalman;

import java.util.function.DoubleSupplier;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.StateSpaceUtil;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.estimator.ExtendedKalmanFilter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;

public class TranslationKalman {
  private final DoubleSupplier thetaSupplier;

  private final ExtendedKalmanFilter<N2, N2, N2> filter;

  private Vector<N2> lastU = VecBuilder.fill(0, 0);

  private final double dt = 0.02;

  public TranslationKalman(Vector<N2> stateStdDevs, Vector<N2> measurementStdDevs,
      DoubleSupplier thetaSupplier) {
    this.thetaSupplier = thetaSupplier;
    filter = new ExtendedKalmanFilter<N2, N2, N2>(Nat.N2(), Nat.N2(), Nat.N2(),
        this::f, this::h, stateStdDevs, measurementStdDevs, dt);
  }

  /**
   * @param x the previous state [x, y] (field-relative position)
   * @param u the current input [vx, vy] (robot-relative velocity)
   *
   * @return the derivative of the state, i.e. the field-relative velocity
   */
  private Matrix<N2, N1> f(Matrix<N2, N1> x, Matrix<N2, N1> u) {
    double vx = u.get(0, 0);
    double vy = u.get(1, 0);
    double theta = thetaSupplier.getAsDouble();
    double cos = Math.cos(theta);
    double sin = Math.sin(theta);
    return VecBuilder.fill(vx * cos - vy * sin, vx * sin + vy * cos);
  }

  /**
   * @param x the current state [x, y]
   * @param u the current input [vx, vy]
   *
   * @return the measurement [x, y]
   */
  private Matrix<N2, N1> h(Matrix<N2, N1> x, Matrix<N2, N1> u) {
    return x.copy();
  }

  public void predict(Vector<N2> u) {
    filter.predict(u, dt);
    lastU = u;
  }

  public void correct(Vector<N2> y, Vector<N2> stddevs) {
    filter.correct(lastU, y, StateSpaceUtil.makeCovarianceMatrix(Nat.N2(), stddevs));
  }

  public double getX() {
    return filter.getXhat(0);
  }

  public double getY() {
    return filter.getXhat(1);
  }

  public double getP(int row, int col) {
    return filter.getP(row, col);
  }

  public void setPose(Pose2d pose) {
    filter.reset();
    filter.setXhat(VecBuilder.fill(pose.getX(), pose.getY()));
  }
}
