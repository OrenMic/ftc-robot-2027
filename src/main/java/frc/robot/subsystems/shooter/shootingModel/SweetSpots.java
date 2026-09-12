package frc.robot.subsystems.shooter.shootingModel;

import frc.robot.subsystems.shooter.shootingModel.DataTypes.ShootingVelocities;
import frc.robot.subsystems.shooter.shootingModel.DataTypes.SweetSpot;
import frc.robot.subsystems.shooter.util.ShootingUtil;

public class SweetSpots {
        public static final ShootingVelocities hubSweetSpots =
                        new ShootingVelocities(ShootingUtil::getDistanceToVirtualHub);
        // public static final ShootingVelocities deliverySweetSpotsLior =
        // new
        // ShootingVelocities(ShootingUtil::getDistanceToVirtualDeliveryLine);

        public static final SweetSpot tower = new SweetSpot(1597.775197, 1650);
        public static final SweetSpot defaultDelivery = new SweetSpot(1500, 2000);

        static {

                // hub sweetSpots

                hubSweetSpots.put(1.8, new SweetSpot(1370, 1650));
                hubSweetSpots.put(2.3, new SweetSpot(1550, 1850));
                hubSweetSpots.put(2.53, new SweetSpot(1650, 1800));
                hubSweetSpots.put(2.92, new SweetSpot(1900, 1900));
                hubSweetSpots.put(3.19, new SweetSpot(1900, 2100));
                hubSweetSpots.put(3.6, new SweetSpot(2100, 2100));
                // hubSweetSpots.put(4.1, new SweetSpot(2250, 2300));

                // for competition work linearly with shooting in motion
                // hubSweetSpots.put(4.6, new SweetSpot(2350, 2100));
                // hubSweetSpots.put(4.95, new SweetSpot(2400, 2250));


                // first game
                // hubSweetSpots.put(3, new SweetSpot(3000, 3000));
                // hubSweetSpots.put(4, new SweetSpot(2400, 2300));

                // secnod game
                // hubSweetSpots.put(3.8, new SweetSpot(2200, 2200));
                hubSweetSpots.put(4.2, new SweetSpot(2400, 2300));
                hubSweetSpots.put(5.2, new SweetSpot(2600, 2400));

                // DCMP p4
                hubSweetSpots.put(3.909216, new SweetSpot(2050, 2200));



                // for workshop only
                // hubSweetSpots.put(4.6, new SweetSpot(3050, 1400));
                // hubSweetSpots.put(4.94, new SweetSpot(3700, 900));



                // delivery sweetSpots

                // deliverySweetSpotsLior.put(2.8, new SweetSpot(1500, 2000));
                // deliverySweetSpotsLior.put(4.0, new SweetSpot(2000, 2000));
                // deliverySweetSpotsLior.put(5.4, new SweetSpot(2700, 2000));
        }
}
