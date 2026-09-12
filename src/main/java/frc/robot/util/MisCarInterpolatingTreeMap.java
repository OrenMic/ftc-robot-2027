package frc.robot.util;

import edu.wpi.first.math.interpolation.Interpolator;
import edu.wpi.first.math.interpolation.InverseInterpolator;
import lombok.Getter;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Interpolating Tree Maps are used to get values at points that are
 * not defined by making a guess from points that are defined. This
 * uses linear interpolation.
 *
 * <p>
 * {@code K} must implement {@link Comparable}, or a
 * {@link Comparator} on {@code K} can be provided.
 *
 * @param <K> The type of keys held in this map.
 * @param <V> The type of values held in this map.
 */
public class MisCarInterpolatingTreeMap<K, V> {

  @Getter
  protected final TreeMap<K, V> m_map;

  private final InverseInterpolator<K> m_inverseInterpolator;
  private final Interpolator<V> m_interpolator;

  /**
   * Constructs an InterpolatingTreeMap.
   *
   * @param inverseInterpolator Function to use for inverse
   *        interpolation of the keys.
   * @param interpolator Function to use for interpolation of the
   *        values.
   */
  public MisCarInterpolatingTreeMap(InverseInterpolator<K> inverseInterpolator,
      Interpolator<V> interpolator) {
    m_map = new TreeMap<>();
    m_inverseInterpolator = inverseInterpolator;
    m_interpolator = interpolator;
  }

  /**
   * Constructs an InterpolatingTreeMap using {@code comparator}.
   *
   * @param inverseInterpolator Function to use for inverse
   *        interpolation of the keys.
   * @param interpolator Function to use for interpolation of the
   *        values.
   * @param comparator Comparator to use on keys.
   */
  public MisCarInterpolatingTreeMap(InverseInterpolator<K> inverseInterpolator,
      Interpolator<V> interpolator, Comparator<K> comparator) {
    m_inverseInterpolator = inverseInterpolator;
    m_interpolator = interpolator;
    m_map = new TreeMap<>(comparator);
  }

  /**
   * Inserts a key-value pair.
   *
   * @param key The key.
   * @param value The value.
   */
  public void put(K key, V value) {
    m_map.put(key, value);
  }

  /**
   * Returns the value associated with a given key.
   *
   * <p>
   * If there's no matching key, the value returned will be an
   * interpolation between the keys before and after the provided one,
   * using the {@link Interpolator} and {@link InverseInterpolator}
   * provided.
   *
   * @param key The key.
   * @return The value associated with the given key.
   */
  public V get(K key) {
    V val = m_map.get(key);
    if (val != null) {
      return val;
    }

    K ceilingKey = m_map.ceilingKey(key);
    K floorKey = m_map.floorKey(key);

    // If below smallest key
    if (floorKey == null) {
      K first = m_map.firstKey();
      K second = m_map.higherKey(first);
      return m_interpolator.interpolate(m_map.get(first),
          m_map.get(second),
          m_inverseInterpolator.inverseInterpolate(first, second, key));
    }

    // If above largest key
    if (ceilingKey == null) {
      K last = m_map.lastKey();
      K secondLast = m_map.lowerKey(last);
      return m_interpolator.interpolate(m_map.get(secondLast),
          m_map.get(last),
          m_inverseInterpolator.inverseInterpolate(secondLast, last, key));
    }

    // Normal interpolation
    return m_interpolator.interpolate(m_map.get(floorKey),
        m_map.get(ceilingKey),
        m_inverseInterpolator.inverseInterpolate(floorKey, ceilingKey, key));
  }

  /** Clears the contents. */
  public void clear() {
    m_map.clear();
  }
}
