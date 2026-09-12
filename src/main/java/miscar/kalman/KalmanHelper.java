// package miscar.kalman;

// import java.util.function.Supplier;
// import org.littletonrobotics.junction.Logger;
// import edu.wpi.first.math.Matrix;
// import edu.wpi.first.math.Nat;
// import edu.wpi.first.math.estimator.PoseEstimator;
// import edu.wpi.first.math.kinematics.SwerveModulePosition;
// import edu.wpi.first.math.numbers.N3;

// public class KalmanHelper {
// Supplier<Matrix<N3, N3>> visionKSupplier;

// @SuppressWarnings("unchecked")
// public KalmanHelper(PoseEstimator<SwerveModulePosition[]>
// poseEstimator) {
// try {
// var visionK = PoseEstimator.class.getDeclaredField("m_visionK");
// visionK.setAccessible(true);

// visionKSupplier = () -> {
// try {
// return ((Matrix<N3, N3>) visionK.get(poseEstimator));
// } catch (Exception e) {
// return new Matrix<>(Nat.N3(), Nat.N3());
// }
// };
// } catch (Exception e) {
// e.printStackTrace();
// }
// }

// public void log() {
// double[][] p = new double[3][3];
// for (int i = 0; i < 3; i++) {
// for (int j = 0; j < 3; j++) {
// p[i][j] = visionKSupplier.get().get(i, j);
// }
// }
// Logger.recordOutput("KalmanHelper/p", p);
// }
// }
