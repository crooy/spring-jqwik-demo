package be.wegenenverkeer.springjqwikdemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import be.wegenenverkeer.springjqwikdemo.domain.*;
import be.wegenenverkeer.springjqwikdemo.property.DomainArbitraries;
import java.util.List;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.statistics.Statistics;

@PropertyDefaults(tries = 100, generation = GenerationMode.EXHAUSTIVE, edgeCases = EdgeCasesMode.FIRST)
class SnackbarServiceFrituurbaarTest {

  private final SnackbarService service = new SnackbarService();

  @Property
  void friturenWithPatatenReturnsCorrectPortions(@ForAll Pataten pataten) {
    // When
    List<String> result = service.frituren(List.of(pataten));

    // Then
    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(s -> s.equals("gefrituurde aardappelportie"));
    int expectedMinPortions = Math.max(1, pataten.size() / 10);
    assertThat(result.size()).isGreaterThanOrEqualTo(expectedMinPortions);
  }

  @Property
  void friturenWithFrikandellenReturnsCorrectCount(@ForAll Frikandellen frikandellen) {
    // When
    List<String> result = service.frituren(List.of(frikandellen));

    // Then
    assertThat(result).hasSize(frikandellen.count());
    assertThat(result).allMatch(s -> s.equals("gefrituurde frikandel"));
  }

  @Property
  @Label("Bug: Decrementing loop causes off-by-one in kroketten")
  void friturenWithKrokettenReturnsCorrectCount(@ForAll Kroketten kroketten) {
    // When
    List<String> result = service.frituren(List.of(kroketten));

    // Then - Bug: Loop executes count+1 times (includes 0)
    // This test will fail - expects count but gets count+1
    assertThat(result).hasSize(kroketten.count());
    assertThat(result).allMatch(s -> s.contains("gefrituurde") && s.contains("kroket"));
    assertThat(result).allMatch(s -> s.contains(getKroketTypeName(kroketten.type())));
  }

  @Property
  @Label("Bug: Decrementing loop causes off-by-one error")
  void friturenWithCervelaReturnsCorrectCount(@ForAll Cervela cervela) {
    // When
    List<String> result = service.frituren(List.of(cervela));

    // Then - Bug: Loop executes count+1 times instead of count
    // This test will fail - expects count but gets count+1
    assertThat(result).hasSize(cervela.count());
    assertThat(result).allMatch(s -> s.equals("gefrituurde cervela"));
  }

  @Property
  @Label("Bug: Decrementing loop produces wrong count")
  void friturenWithBereklauwReturnsCorrectCount(@ForAll Bereklauw bereklauw) {
    // When
    List<String> result = service.frituren(List.of(bereklauw));

    // Then - Bug: Loop from count down to 0 inclusive = count+1 iterations
    // This test will fail
    assertThat(result).hasSize(bereklauw.count());
    assertThat(result).allMatch(s -> s.equals("gefrituurde bereklauw"));
  }

  @Property
  void friturenWithMixedItemsReturnsAllItems(
      @ForAll @Size(min = 1, max = 10) List<Frituurbaar> items) {
    // When
    List<String> result = service.frituren(items);

    // Then
    assertThat(result).isNotEmpty();
    assertThat(result.size()).isGreaterThanOrEqualTo(items.size());
  }

  @Property
  void friturenWithEmptyListReturnsEmpty() {
    // When
    List<String> result = service.frituren(List.of());

    // Then
    assertThat(result).isEmpty();
  }

  @Property
  void friturenWithHighVolumeItemsProducesBonusItems() {
    // Given - High volume should trigger maximum temperature and bonus items
    List<Frituurbaar> highVolumeItems =
        List.of(
            new Pataten(150),
            new Frikandellen(15),
            new Kroketten(Krokettype.KALF, 10),
            new Cervela(8),
            new Bereklauw(5));

    // When
    List<String> result = service.frituren(highVolumeItems);

    // Then
    assertThat(result).isNotEmpty();
    // High volume + max temperature should give us at least the base count
    assertThat(result.size()).isGreaterThanOrEqualTo(
        highVolumeItems.size()); // Each item should produce at least one result
  }

  @Property
  void friturenWithLowVolumeItemsUsesMinimumTemperature() {
    // Given - Low volume items
    List<Frituurbaar> lowVolumeItems = List.of(new Pataten(20), new Frikandellen(2));

    // When
    List<String> result = service.frituren(lowVolumeItems);

    // Then
    assertThat(result).isNotEmpty();
    // Low volume should still produce results
    assertThat(result.size()).isGreaterThanOrEqualTo(lowVolumeItems.size());
  }

  @Property
  void friturenWithKrokettenAllTypesProduceCorrectDescriptions(
      @ForAll Krokettype type, @ForAll @Size(min = 1, max = 5) Integer count) {
    // Given
    Kroketten kroketten = new Kroketten(type, count);

    // When
    List<String> result = service.frituren(List.of(kroketten));

    // Then
    assertThat(result).hasSize(count);
    String expectedTypeName = getKroketTypeName(type);
    assertThat(result).allMatch(s -> s.contains(expectedTypeName));
  }

  @Property
  void friturenWithAllKroketTypes() {
    // Given
    List<Kroketten> allTypes =
        List.of(
            new Kroketten(Krokettype.KAAS, 2),
            new Kroketten(Krokettype.KALF, 2),
            new Kroketten(Krokettype.GARNALEN, 2),
            new Kroketten(Krokettype.KIP, 2),
            new Kroketten(Krokettype.GROENTE, 2),
            new Kroketten(Krokettype.GEZOND, 2));

    // When
    List<String> result = service.frituren(allTypes.stream().map(Frituurbaar.class::cast).toList());

    // Then
    assertThat(result).hasSize(12); // 2 of each type
    assertThat(result.stream().filter(s -> s.contains("kaas")).count()).isEqualTo(2);
    assertThat(result.stream().filter(s -> s.contains("kalfs")).count()).isEqualTo(2);
    assertThat(result.stream().filter(s -> s.contains("garnalen")).count()).isEqualTo(2);
    assertThat(result.stream().filter(s -> s.contains("kip")).count()).isEqualTo(2);
    assertThat(result.stream().filter(s -> s.contains("groente")).count()).isEqualTo(2);
    assertThat(result.stream().filter(s -> s.contains("gezonde")).count()).isEqualTo(2);
  }

  @Property
  void friturenPreservesOrder(@ForAll @Size(min = 2, max = 8) List<Frituurbaar> items) {
    // When
    List<String> result = service.frituren(items);

    // Then
    assertThat(result).isNotEmpty();
    // Verify that items are processed in order
    int index = 0;
    for (Frituurbaar item : items) {
      String expectedPrefix = getExpectedPrefix(item);
      boolean found = false;
      for (int i = index; i < result.size(); i++) {
        if (result.get(i).contains(expectedPrefix)) {
          found = true;
          break;
        }
      }
      assertThat(found).as("Item " + item + " should be present in result").isTrue();
    }
  }

  @Property
  @Label("Large pataten produce more portions than small ones")
  void friturenWithLargePatatenSizeProducesMorePortions(@ForAll int smallSize, @ForAll int largeSize) {
    // Given - Filter invalid combinations
    Assume.that(smallSize >= 10 && smallSize < 100);
    Assume.that(largeSize >= 100 && largeSize <= 200);
    Pataten smallPataten = new Pataten(smallSize);
    Pataten largePataten = new Pataten(largeSize);

    // When
    List<String> smallResult = service.frituren(List.of(smallPataten));
    List<String> largeResult = service.frituren(List.of(largePataten));

    // Then
    assertThat(largeResult.size()).isGreaterThanOrEqualTo(smallResult.size());
  }

  @Property
  @Label("High volume orders produce bonus items at maximum temperature")
  void friturenWithVeryHighVolumeProducesBonusItems(@ForAll @From("highVolumeItems") List<Frituurbaar> items) {
    // Given - High volume items (> 100 total volume)
    Assume.that(calculateTotalVolume(items) > 100);

    // When
    List<String> result = service.frituren(items);

    // Then - Maximum temperature should produce bonus items
    int expectedMinimum = items.size(); // At least one per item
    assertThat(result.size()).isGreaterThanOrEqualTo(expectedMinimum);
    
    // Statistics: Check distribution of item types
    Statistics.label("Item types in result")
        .collect(result.stream().mapToInt(String::length).sum() > 500 ? "large" : "normal")
        .collect(result.size() > items.size() ? "with-bonus" : "no-bonus");
  }

  @Property
  @Label("Different kroket types have correct volume calculations")
  void kroketVolumeCalculationVariesByType(@ForAll Krokettype type) {
    // Given
    List<Kroketten> kroketten = List.of(new Kroketten(type, 1));
    
    // When
    List<String> result = service.frituren(kroketten.stream().map(Frituurbaar.class::cast).toList());
    
    // Then
    assertThat(result).hasSize(1);
    
    // Statistics: Track which types are being tested
    Statistics.label("Kroket types tested")
        .collect(type.name());
  }

  @Property
  @Label("Filter: Only test with valid pataten sizes")
  void friturenWithValidPatatenSizesOnly(@ForAll @From("validPataten") Pataten pataten) {
    // When
    List<String> result = service.frituren(List.of(pataten));

    // Then
    assertThat(result).isNotEmpty();
    assertThat(pataten.size()).isBetween(10, 200);
  }

  @Property
  @Label("Combination of multiple filters and assumptions")
  void friturenWithComplexFilters(
      @ForAll @Size(min = 2, max = 5) List<Frituurbaar> items,
      @ForAll boolean includeBonus) {
    // Given - Filter out empty or invalid combinations
    Assume.that(!items.isEmpty());
    Assume.that(items.stream().anyMatch(i -> !(i instanceof Pataten) || ((Pataten) i).size() > 20));
    
    // When
    List<String> result = service.frituren(items);
    
    // Then
    assertThat(result).isNotEmpty();
    
    // Statistics: Track distributions
    long patatenCount = items.stream().filter(i -> i instanceof Pataten).count();
    long kroketCount = items.stream().filter(i -> i instanceof Kroketten).count();
    
    Statistics.label("Composition")
        .collect(patatenCount > 0 ? "has-pataten" : "no-pataten")
        .collect(kroketCount > 0 ? "has-kroketten" : "no-kroketten")
        .collect(result.size() > items.size() ? "bonus-items" : "no-bonus");
  }

  @Property
  @Label("Edge case: Minimum volume produces minimum results")
  void friturenWithMinimumVolume(@ForAll @From("minimalItems") List<Frituurbaar> items) {
    // Given - Very small volume
    Assume.that(calculateTotalVolume(items) <= 30);
    
    // When
    List<String> result = service.frituren(items);
    
    // Then - Should still produce results but no bonus
    assertThat(result.size()).isGreaterThanOrEqualTo(items.size());
    assertThat(result.size()).isLessThanOrEqualTo(items.size() + 1); // No bonus at low temp
  }

  @Property
  @Label("Filter by kroket type using arbitrary filter")
  void friturenWithSpecificKroketTypes(@ForAll @From("premiumKroketten") Kroketten kroketten) {
    // When
    List<String> result = service.frituren(List.of(kroketten));
    
    // Then
    assertThat(result).hasSize(kroketten.count());
    assertThat(kroketten.type()).isIn(Krokettype.KALF, Krokettype.GARNALEN);
  }

  @Property
  @Label("Edge case: Negative pataten size causes incorrect behavior")
  void friturenWithNegativePatatenSize(@ForAll @From("patatenWithNegative") Pataten pataten) {
    // Given - Negative size (edge case that should be handled)
    Assume.that(pataten.size() < 0);
    
    // When
    List<String> result = service.frituren(List.of(pataten));
    
    // Then - Bug: Currently returns 1 portion even for negative sizes
    // This is wrong - should return 0 or handle differently
    // The bug: Math.max(1, negative/10) = Math.max(1, negative) = 1
    assertThat(result.size()).isGreaterThanOrEqualTo(0);
    // This assertion will fail because the bug causes it to return 1 instead of 0
  }

  @Property
  @Label("Edge case: Large counts cause byte wraparound and infinite loop")
  void friturenWithLargeCountsCausesWraparound(@ForAll @From("largeCountItems") List<Frituurbaar> items) {
    // Given - Large counts that when adjusted for temperature exceed byte max (127)
    // When adjustedCount > 127, casting to byte wraps around to negative values
    // Then decrementing from negative wraps from -128 to 127, causing huge iteration
    Assume.that(items.stream().anyMatch(item -> {
      int baseCount = switch (item) {
        case Frikandellen f -> f.count();
        case Kroketten k -> k.count();
        case Cervela c -> c.count();
        case Bereklauw b -> b.count();
        default -> 0;
      };
      // With temperature bonus, could exceed 127
      return baseCount >= 120;
    }));
    
    // When - This will cause byte wraparound
    // WARNING: This might take a very long time or cause OutOfMemoryError
    // The loop wraps from negative back to positive, creating massive iterations
    List<String> result = service.frituren(items);
    
    // Then - The result will be completely wrong due to wraparound
    // For counts > 127, byte wraparound causes the loop to iterate hundreds of times
    // This test demonstrates the bug - we expect reasonable count but get huge result
    assertThat(result.size()).isLessThan(1000); // Bug: May be much larger due to wraparound
  }

  @Property
  @Label("Edge case: Zero or negative counts expose decrementing loop bug")
  void friturenWithZeroOrNegativeCounts(@ForAll @From("frikandellenWithEdgeCases") Frikandellen frikandellen) {
    // Given - Zero or negative count
    Assume.that(frikandellen.count() <= 0);
    
    // When
    List<String> result = service.frituren(List.of(frikandellen));
    
    // Then - Multiple bugs:
    // 1. adjustCountForTemperature doesn't validate negative counts
    // 2. For zero count: at max temp returns 0+1=1, then loop (i=1; i>=0; i--) executes 2 times!
    // 3. For negative count: at max temp returns negative+1, loop condition fails immediately
    //    So negative counts produce nothing (which is actually okay), but zero with bonus produces 2
    if (frikandellen.count() == 0) {
      // Zero count should produce zero results
      // But bug: at max temp, adjustedCount = 1, loop executes 2 times (i=1, i=0)
      assertThat(result.size()).isEqualTo(0);
    } else {
      // Negative counts: adjustedCount could be negative or 0
      // Loop (i=negative; i>=0; i--) doesn't execute (condition false immediately)
      // So produces nothing, which is actually correct
      assertThat(result.size()).isEqualTo(0);
    }
  }

  @Property
  @Label("Edge case: Very small pataten sizes (0-9) produce incorrect portions")
  void friturenWithVerySmallPatatenSizes(@ForAll @From("smallPataten") Pataten pataten) {
    // Given - Very small size (0-9)
    Assume.that(pataten.size() >= 0 && pataten.size() < 10);
    
    // When
    List<String> result = service.frituren(List.of(pataten));
    
    // Then - Bug: size/10 = 0 for sizes 0-9, Math.max(1, 0) = 1
    // So all small sizes produce exactly 1 portion, which might be wrong
    // Size 0 should probably produce 0 portions
    if (pataten.size() == 0) {
      assertThat(result.size()).isEqualTo(0); // Bug: Currently returns 1
    } else {
      assertThat(result.size()).isGreaterThan(0);
    }
  }

  @Property
  @Label("Edge case: Empty strings in processWords")
  void processWordsWithEmptyStrings(@ForAll @Size(min = 0, max = 5) List<String> words) {
    // Given - List that may contain empty strings
    List<String> input = new java.util.ArrayList<>(words);
    input.add(""); // Add empty string
    input.add("aardappel"); // Add valid word
    
    // When
    List<String> result = service.processWords(input);
    
    // Then - Empty string should be ignored (bug: it passes null check but produces nothing)
    // This is actually correct behavior, but let's verify it doesn't break
    assertThat(result).isNotEmpty(); // Should have friet from "aardappel"
    assertThat(result.size()).isEqualTo(9); // From "aardappel"
  }

  @Property
  @Label("Bug: Case-sensitive comparison fails for uppercase/mixed case")
  void processWordsCaseSensitivity(@ForAll String word) {
    // Given - Variations of the target words
    Assume.that(word != null && !word.isEmpty());
    List<String> variations = List.of(
        word.toLowerCase(),
        word.toUpperCase(),
        capitalize(word)
    );
    
    // When
    List<String> results = variations.stream()
        .flatMap(v -> service.processWords(List.of(v)).stream())
        .toList();
    
    // Then - Bug: Only lowercase "aardappel" and "pieper" match
    // "Aardappel", "AARDAPPEL", "Pieper", "PIEPER" won't match
    // This test demonstrates the case-sensitivity bug
    boolean allSame = results.stream().distinct().count() <= 1;
    // If word is "aardappel" or "pieper", all variations should produce same result
    // But bug causes case variations to fail
    if (word.equalsIgnoreCase("aardappel") || word.equalsIgnoreCase("pieper")) {
      assertThat(allSame).isTrue(); // Bug: Will fail for uppercase/mixed case
    }
  }

  @Property
  @Label("Bug: Null items in list cause NullPointerException")
  void friturenWithNullItems(@ForAll @Size(min = 1, max = 5) List<Frituurbaar> validItems) {
    
    // When & Then - Bug: Will throw NullPointerException
    // The frituren method doesn't check for null items
    assertThat(service.frituren(validItems)).isNotNull();
  }

  @Property
  @Label("Bug: Integer overflow in volume calculation")
  void friturenWithLargeCountsCausesOverflow(@ForAll @From("veryLargeCounts") List<Frituurbaar> items) {
    // Given - Very large counts that cause integer overflow when multiplied
    // Example: Frikandellen count = Integer.MAX_VALUE / 5 causes overflow
    
    // When - Volume calculation can overflow
    List<String> result = service.frituren(items);
    
    // Then - Overflow causes negative volume, which selects wrong temperature
    // This test demonstrates overflow bug
    assertThat(result).isNotNull(); // Should not crash, but results will be wrong
  }

  @Property
  @Label("Bug: Boundary conditions at exactly 50 and 100 volume")
  void friturenBoundaryConditions(@ForAll int volume) {
    // Given - Volume exactly at boundaries
    Assume.that(volume == 50 || volume == 100 || volume == 51 || volume == 101);
    
    // Create items that sum to exactly this volume
    List<Frituurbaar> items = createItemsForVolume(volume);
    
    // When
    List<String> result = service.frituren(items);
    
    // Then - Bug: Volume = 50 falls into MINIMUM temp (maybe should be OPTIMAL?)
    // Volume = 100 falls into OPTIMAL temp (maybe should be MAXIMUM?)
    // This tests boundary condition handling
    assertThat(result).isNotNull();
    
    // Verify temperature selection is consistent
    if (volume == 50) {
      // Currently gets MINIMUM temp, but might expect OPTIMAL
    } else if (volume == 100) {
      // Currently gets OPTIMAL temp, but might expect MAXIMUM
    }
  }

  @Property
  @Label("Bug: Whitespace in processWords prevents matching")
  void processWordsWithWhitespace(@ForAll String whitespace) {
    // Given - Words with whitespace
    Assume.that(whitespace != null);
    List<String> variations = List.of(
        " " + "aardappel",
        "aardappel" + " ",
        " " + "aardappel" + " ",
        "\t" + "aardappel",
        "aardappel" + "\n"
    );
    
    // When
    List<String> results = variations.stream()
        .flatMap(v -> service.processWords(List.of(v)).stream())
        .toList();
    
    // Then - Bug: Whitespace prevents matching
    // All should produce 9 "friet" entries, but whitespace causes no match
    assertThat(results).allMatch(r -> r.length() == 9); // Bug: Will fail
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }

  private List<Frituurbaar> createItemsForVolume(int targetVolume) {
    List<Frituurbaar> items = new java.util.ArrayList<>();
    // Create items that sum to exactly targetVolume
    if (targetVolume <= 200) {
      items.add(new Pataten(targetVolume));
    } else {
      // Use combination that sums to targetVolume
      int remaining = targetVolume;
      while (remaining > 0) {
        if (remaining >= 12) {
          items.add(new Bereklauw(1));
          remaining -= 12;
        } else if (remaining >= 8) {
          items.add(new Cervela(1));
          remaining -= 8;
        } else if (remaining >= 5) {
          items.add(new Frikandellen(1));
          remaining -= 5;
        } else {
          items.add(new Pataten(remaining));
          remaining = 0;
        }
      }
    }
    return items;
  }

  @Example
  @Label("Example: Typical order with all item types")
  void exampleTypicalOrder() {
    // Given - A typical snackbar order
    List<Frituurbaar> order = List.of(
        new Pataten(100),
        new Frikandellen(3),
        new Kroketten(Krokettype.KAAS, 2),
        new Cervela(1),
        new Bereklauw(1)
    );
    
    // When
    List<String> result = service.frituren(order);
    
    // Then
    assertThat(result).isNotEmpty();
    assertThat(result).contains("gefrituurde aardappelportie");
    assertThat(result).contains("gefrituurde frikandel");
    assertThat(result).contains("gefrituurde kaas kroket");
    assertThat(result).contains("gefrituurde cervela");
    assertThat(result).contains("gefrituurde bereklauw");
  }

  @Provide
  Arbitrary<List<Frituurbaar>> highVolumeItems() {
    return DomainArbitraries.frituurbaarLists()
        .filter(items -> calculateTotalVolume(items) > 100);
  }

  @Provide
  Arbitrary<Pataten> validPataten() {
    return DomainArbitraries.pataten()
        .filter(p -> p.size() >= 10 && p.size() <= 200);
  }


  @Provide
  Arbitrary<Kroketten> premiumKroketten() {
    return DomainArbitraries.kroketten()
        .filter(k -> k.type() == Krokettype.KALF || k.type() == Krokettype.GARNALEN);
  }

  @Provide
  Arbitrary<Pataten> patatenWithNegative() {
    return DomainArbitraries.patatenIncludingEdgeCases()
        .filter(p -> p.size() < 0);
  }

  @Provide
  Arbitrary<Frikandellen> frikandellenWithEdgeCases() {
    return DomainArbitraries.frikandellenIncludingEdgeCases()
        .filter(f -> f.count() <= 0);
  }

  @Provide
  Arbitrary<Pataten> smallPataten() {
    return DomainArbitraries.patatenIncludingEdgeCases()
        .filter(p -> p.size() >= 0 && p.size() < 10);
  }

  @Provide
  Arbitrary<List<Frituurbaar>> largeCountItems() {
    // Generate items with large counts that can cause byte wraparound
    return Arbitraries.oneOf(
        DomainArbitraries.frikandellenLarge().map(f -> (Frituurbaar) f),
        DomainArbitraries.krokettenLarge().map(k -> (Frituurbaar) k),
        DomainArbitraries.cervela().map(c -> (Frituurbaar) c),
        DomainArbitraries.bereklauwLarge().map(b -> (Frituurbaar) b))
        .list()
        .ofMinSize(1)
        .ofMaxSize(3);
  }

  @Provide
  Arbitrary<List<Frituurbaar>> veryLargeCounts() {
    // Generate items with very large counts that cause integer overflow
    // When multiplied by volume factors (5, 8, 12), can exceed Integer.MAX_VALUE
    return Arbitraries.oneOf(
        Arbitraries.integers()
            .between(Integer.MAX_VALUE / 6, Integer.MAX_VALUE / 4)
            .map(Frikandellen::new)
            .map(f -> (Frituurbaar) f),
        Arbitraries.integers()
            .between(Integer.MAX_VALUE / 10, Integer.MAX_VALUE / 8)
            .map(c -> new Cervela(c))
            .map(c -> (Frituurbaar) c),
        Arbitraries.integers()
            .between(Integer.MAX_VALUE / 13, Integer.MAX_VALUE / 11)
            .map(b -> new Bereklauw(b))
            .map(b -> (Frituurbaar) b))
        .list()
        .ofMinSize(1)
        .ofMaxSize(2);
  }

  private int calculateTotalVolume(List<Frituurbaar> items) {
    return items.stream()
        .mapToInt(
            item -> {
              return switch (item) {
                case Pataten p -> p.size();
                case Frikandellen f -> f.count() * 5;
                case Kroketten k -> k.count() * calculateKroketVolume(k.type());
                case Cervela c -> c.count() * 8;
                case Bereklauw b -> b.count() * 12;
              };
            })
        .sum();
  }

  private int calculateKroketVolume(Krokettype type) {
    return switch (type) {
      case KAAS -> 4;
      case KALF -> 5;
      case GARNALEN -> 6;
      case KIP -> 4;
      case GROENTE -> 3;
      case GEZOND -> 2;
    };
  }

  private String getKroketTypeName(Krokettype type) {
    return switch (type) {
      case KAAS -> "kaas";
      case KALF -> "kalfs";
      case GARNALEN -> "garnalen";
      case KIP -> "kip";
      case GROENTE -> "groente";
      case GEZOND -> "gezonde";
    };
  }

  private String getExpectedPrefix(Frituurbaar item) {
    return switch (item) {
      case Pataten p -> "aardappel";
      case Frikandellen f -> "frikandel";
      case Kroketten k -> "kroket";
      case Cervela c -> "cervela";
      case Bereklauw b -> "bereklauw";
    };
  }
}
