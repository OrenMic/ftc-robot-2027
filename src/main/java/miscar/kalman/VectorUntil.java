package miscar.kalman;

import edu.wpi.first.math.Nat;
import edu.wpi.first.math.Num;
import edu.wpi.first.math.Vector;

public class VectorUntil {
    public static <N extends Num> Vector<N> getFirstN(Nat<N> nat, Vector<?> vector) {
        var firstN = new Vector<N>(nat);

        int n = 0;
        for (double i : vector.getStorage().toArray2()[0]) {
            firstN.set(n, 0, i);
            n++;
        }

        return firstN;
    }
}
