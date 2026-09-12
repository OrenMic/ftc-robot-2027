package miscar.kalman;

import edu.wpi.first.math.geometry.Rotation2d;

public class Kalman {
    public static void main(String[] args) {
        // Matrix<N1, N1> B = Matrix.eye(Nat.N1());
        // System.out.println(B);
        Rotation2d current = new Rotation2d(0.633246);
        Rotation2d vision = new Rotation2d(1.978286);
        double wrapped = current.getRadians() + vision.minus(current).getRadians();
        System.out.println(wrapped);
        // Matrix<N6, N3> B = new Matrix<>(Nat.N6(), Nat.N3());
        // B.set(3, 0, 1);
        // B.set(4, 1, 1);
        // B.set(5, 2, 1);
        // System.out.println(B);
        // LinearSystem<N3, N3, N3> system;
        // KalmanFilter<N3, N3, N3> filter;
        // Vector<N3> stateStdDevs = VecBuilder.fill(0.05, 0.05, 0.02);
        // Vector<N3> visionStdDevs = VecBuilder.fill(0.1, 0.1, 0.05);

        // Matrix<N3, N3> A = new Matrix<>(Nat.N3(), Nat.N3());
        // Matrix<N3, N3> B = Matrix.eye(Nat.N3());
        // Matrix<N3, N3> C = Matrix.eye(Nat.N3());
        // Matrix<N3, N3> D = new Matrix<>(Nat.N3(), Nat.N3());

        // system = new LinearSystem<>(A, B, C, D);
        // filter = new KalmanFilter<>(Nat.N3(), Nat.N3(), system,
        // stateStdDevs, visionStdDevs, 0.02);

        // filter.setXhat(VecBuilder.fill(0, 0, 0));
        // filter.
    }
}
