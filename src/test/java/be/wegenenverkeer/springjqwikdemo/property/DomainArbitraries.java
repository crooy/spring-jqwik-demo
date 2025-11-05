package be.wegenenverkeer.springjqwikdemo.property;

import be.wegenenverkeer.springjqwikdemo.domain.*;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

public class DomainArbitraries {

  public static Arbitrary<Krokettype> krokettypes() {
    return Arbitraries.of(Krokettype.values());
  }

  public static Arbitrary<Pataten> pataten() {
    return Arbitraries.integers()
        .between(10, 200)
        .map(Pataten::new);
  }

  public static Arbitrary<Pataten> patatenIncludingEdgeCases() {
    // Include negative, zero, and very small values for edge case testing
    return Arbitraries.integers()
        .between(-50, 200)
        .map(Pataten::new);
  }

  public static Arbitrary<Frikandellen> frikandellen() {
    return Arbitraries.integers()
        .between(1, 20)
        .map(Frikandellen::new);
  }

  public static Arbitrary<Frikandellen> frikandellenLarge() {
    return Arbitraries.integers()
        .between(120, 200)
        .map(Frikandellen::new);
  }

  public static Arbitrary<Frikandellen> frikandellenIncludingEdgeCases() {
    return Arbitraries.integers()
        .between(-10, 20)
        .map(Frikandellen::new);
  }

  public static Arbitrary<Kroketten> kroketten() {
    return Combinators.combine(
            krokettypes(),
            Arbitraries.integers().between(1, 15))
        .as(Kroketten::new);
  }

  public static Arbitrary<Kroketten> krokettenLarge() {
    return Combinators.combine(
            krokettypes(),
            Arbitraries.integers().between(120, 200))
        .as(Kroketten::new);
  }

  public static Arbitrary<Cervela> cervela() {
    return Arbitraries.integers()
        .between(1, 10)
        .map(Cervela::new);
  }

  public static Arbitrary<Bereklauw> bereklauw() {
    return Arbitraries.integers()
        .between(1, 8)
        .map(Bereklauw::new);
  }

  public static Arbitrary<Bereklauw> bereklauwLarge() {
    // Large counts for byte wraparound testing
    return Arbitraries.integers()
        .between(120, 200)
        .map(Bereklauw::new);
  }

  public static Arbitrary<Frituurbaar> frituurbaar() {
    return Arbitraries.oneOf(
        pataten(),
        frikandellen(),
        kroketten(),
        cervela(),
        bereklauw());
  }

  public static Arbitrary<java.util.List<Frituurbaar>> frituurbaarLists() {
    return frituurbaar()
        .list()
        .ofMinSize(0)
        .ofMaxSize(20);
  }
}
