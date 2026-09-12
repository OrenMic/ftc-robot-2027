package miscar.kalman;

import org.littletonrobotics.junction.Logger;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.StateSpaceUtil;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.estimator.ExtendedKalmanFilter;
import edu.wpi.first.math.estimator.KalmanFilter;
import edu.wpi.first.math.geometry.Ellipse2d;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.util.GeomUtil;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import miscar.limeLightVision.LimeLightInputs.PoseObservation;

@ExtensionMethod(value = GeomUtil.class)
public class PoseKalman extends SubsystemBase {
    /*
    * x=[x,y]
    * u=[vx,vy]
    * y=[x,y]
    */
    @Getter
    ExtendedKalmanFilter<N2, N2, N2> poseFilter;

    /*
     * x=[a]
     * u=[omega]
     * y=[a]
     */
    static KalmanFilter<N1, N1, N1> rotationFilter;

    static Matrix<N1, N1> A = new Matrix<>(Nat.N1(), Nat.N1());
    static Matrix<N1, N1> B = Matrix.eye(Nat.N1());
    static Matrix<N1, N1> C = Matrix.eye(Nat.N1());
    static Matrix<N1, N1> D = new Matrix<>(Nat.N1(), Nat.N1());

    static LinearSystem<N1, N1, N1> system = new LinearSystem<>(A, B, C, D);

    double dt = 0.02;
    double lastTime;

    Vector<N2> lastPoseU = VecBuilder.fill(0, 0);
    Vector<N1> lastRotationU = VecBuilder.fill(0);

    Rotation2d previousAngle;
    Rotation2d gyroOffset;

    public PoseKalman(Pose2d startPose, Vector<N3> stateStdDevs, Vector<N3> visionStdDevs) {
        var poseStateStddevs = VectorUntil.getFirstN(Nat.N2(), stateStdDevs);
        var poseVisionStddevs = VectorUntil.getFirstN(Nat.N2(), visionStdDevs);

        poseFilter = new ExtendedKalmanFilter<N2, N2, N2>(Nat.N2(), Nat.N2(), Nat.N2(),
                PoseKalman::f, PoseKalman::h, poseStateStddevs, poseVisionStddevs, dt);


        rotationFilter = new KalmanFilter<N1, N1, N1>(Nat.N1(), Nat.N1(), system,
                VecBuilder.fill(stateStdDevs.get(4)), VecBuilder.fill(visionStdDevs.get(4)), dt);

        Rotation2d gyroAngle = new Rotation2d();
        setPose(gyroAngle, startPose);

        lastTime = Timer.getTimestamp();
    }

    /**
     * @param x the previous state [x,y]
     * @param u the current measurement [vx,vy]
     *
     * @return the derivate of the new state [vx,vy]
     */
    public static Matrix<N2, N1> f(Matrix<N2, N1> x, Matrix<N2, N1> u) {
        double vx = u.get(0, 0);
        double vy = u.get(1, 0);

        double theta = getRotationRad();

        double cos = Math.cos(theta);
        double sin = Math.sin(theta);

        return VecBuilder.fill(vx * cos - vy * sin, vx * sin + vy * cos);
        // double ax = u.get(0, 0);
        // double ay = u.get(1, 0);
        // // double omega = u.get(2, 0);

        // Vector<N4> xdot = new Vector<>(Nat.N4());

        // double fieldRelativeAx = ax * cos - ay * sin;
        // double fieldRelativeAy = ax * sin + ay * cos;

        // xdot.set(0, 0, x.get(2, 0));
        // xdot.set(1, 0, x.get(3, 0));

        // // xdot.set(2, 0, omega);
        // xdot.set(2, 0, fieldRelativeAx);
        // xdot.set(3, 0, fieldRelativeAy);


        // return xdot;
    }


    public static Matrix<N2, N1> h(Matrix<N2, N1> x, Matrix<N2, N1> u) {
        // return VecBuilder.fill(x.get(0, 0), x.get(1, 0), x.get(2, 0));
        return x.copy();
    }

    public static Rotation2d getRotation() {
        return Rotation2d.fromRadians(getRotationRad());
    }

    public static double getRotationRad() {
        return 0;
        // return rotationFilter.getXhat(0);
    }

    public double getY() {
        return poseFilter.getXhat(1);
    }

    public double getX() {
        return poseFilter.getXhat(0);
    }

    public Pose2d getPose() {
        Logger.recordOutput("PoseKalman/getP", getP());
        Logger.recordOutput("PoseKalman/getP/rotation", rotationFilter.getP(0, 0));
        return new Pose2d(getX(), getY(), getRotation());
    }

    public Matrix<N2, N2> getCovarianceMatrix() {
        double[] covariance = new double[4];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                covariance[i * 2 + j] = poseFilter.getP(i, j);
            }
        }
        return new Matrix<>(Nat.N2(), Nat.N2(), covariance);
    }

    public double[][] getCovariance() {
        double[][] covariance = new double[2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                covariance[i][j] = poseFilter.getP(i, j);
            }
        }
        return covariance;
    }

    public double[][] getP() {
        double[][] p = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                p[i][j] = poseFilter.getP(i, j);
            }
        }
        return p;
    }

    public double getYVel() {
        return poseFilter.getXhat(3);
    }

    public double getXVel() {
        return poseFilter.getXhat(4);
    }

    public ChassisSpeeds getVel() {
        return new ChassisSpeeds(getXVel(), getYVel(), 0);
    }

    /**
     * takes acl and predicts
     *
     * @param u the measurements [ax,ay,omega]
     */
    public void predict(Vector<N3> u) {
        dt = Timer.getTimestamp() - lastTime;
        if (dt > 2)
            dt = 0.02;
        lastTime = Timer.getTimestamp();
        var rotationU = VecBuilder.fill(u.get(2));// omega
        var poseU = VectorUntil.getFirstN(Nat.N2(), u); // ax,ay

        rotationFilter.predict(rotationU, dt);
        poseFilter.predict(poseU, dt);
        lastRotationU = rotationU;
        lastPoseU = poseU;
    }

    PoseObservation poseObservation = null;

    public void setPoseObservation(Pose2d visionRobotPoseMeters, double timestampSeconds,
            Vector<N3> visionMeasurementStdDevs) {
        if (timestampSeconds < 0) {
            poseObservation = null;
        } else {
            poseObservation = new PoseObservation(visionRobotPoseMeters, timestampSeconds,
                    visionMeasurementStdDevs.get(0), visionMeasurementStdDevs.get(2));
        }
    }

    // @Setter
    // ChassisSpeeds chassisSpeeds = new ChassisSpeeds();


    /**
     * corrects the pose filter with the given measurements
     *
     * @param y the measurements [x,y,alpha]
     * @param stddevs
     */
    public void correct(Vector<N3> y, Vector<N3> stddevs) {

        var visionStddevs = VecBuilder.fill(stddevs.get(2));
        var visionY = VecBuilder.fill(y.get(2));

        var poseStddevs = VectorUntil.getFirstN(Nat.N2(), stddevs);
        var poseY = VectorUntil.getFirstN(Nat.N2(), y);

        rotationFilter.correct(lastRotationU,
                visionY,
                StateSpaceUtil.makeCovarianceMatrix(Nat.N1(), visionStddevs));

        poseFilter.correct(lastPoseU,
                poseY,
                StateSpaceUtil.makeCovarianceMatrix(Nat.N2(), poseStddevs));
    }

    public void setPose(Rotation2d rawGyro, Pose2d newPose) {
        poseFilter.reset();
        poseFilter.setXhat(VecBuilder.fill(newPose.getX(), newPose.getY()));

        rotationFilter.reset();
        rotationFilter.setXhat(VecBuilder.fill(newPose.getRotation().getRadians()));

        previousAngle = newPose.getRotation();
        gyroOffset = rawGyro.unaryMinus().rotateBy(newPose.getRotation());
    }

    public double updateRotation(Rotation2d newRotation2d) {
        Rotation2d adjustedAngle = newRotation2d.rotateBy(gyroOffset);
        double omega = adjustedAngle.minus(previousAngle).getRadians() / dt;
        previousAngle = adjustedAngle;
        return omega;
    }

    @Override
    public void periodic() {
        // Vector<N3> stddevs = new Vector<>(Nat.N3());
        // Vector<N3> y = new Vector<>(Nat.N3());
        // if (poseObservation == null) {
        // stddevs.set(0, 0, 9999);// x
        // stddevs.set(1, 0, 9999);// y
        // stddevs.set(2, 0, 9999);// rotation
        // y.set(0, 0, getX());// x
        // y.set(1, 0, getY());// y
        // y.set(2, 0, getRotationRad());// rotation
        // } else {
        // stddevs.set(0, 0, poseObservation.linearStdDev());// x
        // stddevs.set(1, 0, poseObservation.linearStdDev());// y
        // stddevs.set(2, 0, poseObservation.anglerStdDev());// rotation
        // y.set(0, 0, poseObservation.pose().getX());// x
        // y.set(1, 0, poseObservation.pose().getY());// y
        // y.set(2, 0, poseObservation.pose().getRotation().getRadians());//
        // rotation
        // }

        // stddevs.set(2, 0, 0.2);
        // stddevs.set(3, 0, 0.2);

        // double vx = chassisSpeeds.vxMetersPerSecond;
        // double vy = chassisSpeeds.vyMetersPerSecond;
        // double theta = getRotationRad();

        // double cos = Math.cos(theta);
        // double sin = Math.sin(theta);

        // y.set(2, 0, vx * cos - vy * sin);
        // y.set(3, 0, vx * sin + vy * cos);
        // Logger.recordOutput(getName() + "/robotSpeed", getVel());
        // correct(y, stddevs);



        Ellipse2d covarianceEllipse = Ellipse2dUtil.toCovarianceEllipse(getCovarianceMatrix(),
                getPose().getTranslation());
        Logger.recordOutput(getName() + "/getCovarianceEllipse", covarianceEllipse);
        Logger.recordOutput(getName() + "/getCovarianceEllipseMax/1",
                Ellipse2dUtil.getYExtrema(covarianceEllipse).getFirst().toPose2d().toPose3d()
                        .withRotation(new Rotation3d(0, Units.degreesToRadians(-90), 0)));
        Logger.recordOutput(getName() + "/getCovarianceEllipseMax/2",
                Ellipse2dUtil.getYExtrema(covarianceEllipse).getSecond().toPose2d().toPose3d()
                        .withRotation(new Rotation3d(0, Units.degreesToRadians(-90), 0)));
    }
}
