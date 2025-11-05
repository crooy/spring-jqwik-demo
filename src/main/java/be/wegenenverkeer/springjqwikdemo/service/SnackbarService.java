package be.wegenenverkeer.springjqwikdemo.service;

import be.wegenenverkeer.springjqwikdemo.domain.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SnackbarService {

  private static final int MINIMUM_FRYING_TEMPERATURE = 160;
  private static final int OPTIMAL_FRYING_TEMPERATURE = 180;
  private static final int MAXIMUM_FRYING_TEMPERATURE = 190;

  public List<String> processWords(List<String> input) {
    List<String> result = new ArrayList<>();
    for (String word : input) {
      // Bug: Case-sensitive comparison - "Aardappel" or "AARDAPPEL" won't match
      // Bug: Empty strings pass the null check but have length 0
      // Bug: What about whitespace? " aardappel " won't match
      // Bug: What about unicode variations? "aardappel" vs "??rd????l" (cyrillic a)
      if (word != null && (word.equals("aardappel") || word.equals("pieper"))) {
        int length = word.length();
        for (int i = 0; i < length; i++) {
          result.add("friet");
        }
      }
    }
    return result;
  }

  public List<String> frituren(List<Frituurbaar> items) {
    List<String> result = new ArrayList<>();
    // Bug: No null check - if items contains null, this will throw NullPointerException
    // Property-based testing with null values will catch this
    int totalVolume = calculateTotalVolume(items);
    int fryingTemperature = determineOptimalTemperature(totalVolume);

    for (Frituurbaar item : items) {
      // Bug: item could be null, causing NPE when calling processFrituurbaarItem
      List<String> friedItems = processFrituurbaarItem(item, fryingTemperature);
      result.addAll(friedItems);
    }

    return result;
  }

  private int calculateTotalVolume(List<Frituurbaar> items) {
    // Bug: Integer overflow possible when multiplying large counts
    // Example: Frikandellen with count = Integer.MAX_VALUE / 5 causes overflow
    // Also: No null check - null items will cause NPE in switch expression
    return items.stream()
        .mapToInt(
            item -> {
              return switch (item) {
                case Pataten p -> p.size();
                case Frikandellen f -> f.count() * 5; // Bug: Can overflow if count is large
                case Kroketten k -> k.count() * calculateKroketVolume(k.type()); // Bug: Can overflow
                case Cervela c -> c.count() * 8; // Bug: Can overflow
                case Bereklauw b -> b.count() * 12; // Bug: Can overflow with large counts
              };
            })
        .sum(); // Bug: Sum can also overflow if many items
  }

  private int calculateKroketVolume(Krokettype type) {
    return switch (type) {
      case KAAS -> 4;
      case KALF -> 5;
      case GARNALEN -> 6;
      case KIP -> 4;
      case GROENTE -> 3;
      case GEZOND -> 2; // Naturally smaller, allegedly
    };
  }

  private int determineOptimalTemperature(int totalVolume) {
    // Bug: Boundary conditions - exactly 50 and exactly 100 are ambiguous
    // totalVolume = 50 falls into MINIMUM (should probably be OPTIMAL?)
    // totalVolume = 100 falls into OPTIMAL (should probably be MAXIMUM?)
    // Also: Negative volumes from overflow could cause wrong temperature
    if (totalVolume > 100) {
      return MAXIMUM_FRYING_TEMPERATURE; // High volume requires maximum heat
    } else if (totalVolume > 50) {
      return OPTIMAL_FRYING_TEMPERATURE; // Standard optimal temperature
    } else {
      return MINIMUM_FRYING_TEMPERATURE; // Small batches can use lower temperature
    }
  }

  private List<String> processFrituurbaarItem(Frituurbaar item, int temperature) {
    return switch (item) {
      case Pataten p -> processPataten(p, temperature);
      case Frikandellen f -> processFrikandellen(f, temperature);
      case Kroketten k -> processKroketten(k, temperature);
      case Cervela c -> processCervela(c, temperature);
      case Bereklauw b -> processBereklauw(b, temperature);
    };
  }

  private List<String> processPataten(Pataten pataten, int temperature) {
    List<String> result = new ArrayList<>();
    int portions = calculatePortions(pataten.size(), temperature);
    for (int i = 0; i < portions; i++) {
      result.add("gefrituurde aardappelportie");
    }
    return result;
  }

  private List<String> processFrikandellen(Frikandellen frikandellen, int temperature) {
    List<String> result = new ArrayList<>();
    int adjustedCount = adjustCountForTemperature(frikandellen.count(), temperature);
    // Bug: Using byte type causes integer wraparound!
    // If adjustedCount > 127, casting to byte wraps around to negative values
    // Then decrementing from negative values wraps around again when reaching -128
    // This can cause infinite loops or completely wrong behavior
    // Example: adjustedCount = 200 -> byte i = (byte)200 = -56
    //          Loop: i = -56, decrement... -57, -58... -128, then wraps to 127!
    //          Condition i >= 0 is now true again, causing infinite loop
    for (byte i = (byte) adjustedCount; i >= 0; i--) {
      result.add("gefrituurde frikandel");
    }
    return result;
  }

  private List<String> processKroketten(Kroketten kroketten, int temperature) {
    List<String> result = new ArrayList<>();
    String kroketDescription = getKroketDescription(kroketten.type());
    int adjustedCount = adjustCountForTemperature(kroketten.count(), temperature);
    // Bug: Using byte causes wraparound - same issue as frikandellen
    // If adjustedCount > 127, wraps to negative, then decrements to -128 and wraps to 127
    // This creates an infinite loop when adjustedCount > 127
    for (byte i = (byte) adjustedCount; i >= 0; i--) {
      result.add("gefrituurde " + kroketDescription + " kroket");
    }
    return result;
  }

  private List<String> processCervela(Cervela cervela, int temperature) {
    List<String> result = new ArrayList<>();
    int adjustedCount = adjustCountForTemperature(cervela.count(), temperature);
    // Bug: Byte wraparound - infinite loop risk
    // Example: adjustedCount = 150 -> byte i = (byte)150 = -106
    //          Decrementing: -106, -107... -128, wraps to 127, 126... 0
    //          This creates a huge number of iterations!
    for (byte i = (byte) adjustedCount; i >= 0; i--) {
      result.add("gefrituurde cervela");
    }
    return result;
  }

  private List<String> processBereklauw(Bereklauw bereklauw, int temperature) {
    List<String> result = new ArrayList<>();
    int adjustedCount = adjustCountForTemperature(bereklauw.count(), temperature);
    // Bug: Byte wraparound bug - same pattern
    // Large adjustedCount values (> 127) wrap to negative bytes
    // Decrementing eventually wraps from -128 to 127, causing massive iteration
    for (byte i = (byte) adjustedCount; i >= 0; i--) {
      result.add("gefrituurde bereklauw");
    }
    return result;
  }

  private int calculatePortions(int size, int temperature) {
    // Bug: Doesn't handle negative sizes or zero properly
    // When size is negative, size / 10 will be negative, Math.max(1, negative) = 1
    // But we should return 0 or handle it differently
    int basePortions = Math.max(1, size / 10);
    if (temperature >= OPTIMAL_FRYING_TEMPERATURE) {
      return basePortions + 1; // Higher temperature yields more portions
    }
    return basePortions;
  }

  private int adjustCountForTemperature(int count, int temperature) {
    // Bug: Doesn't validate that count is non-negative
    // Negative counts could result in weird behavior
    if (temperature >= MAXIMUM_FRYING_TEMPERATURE) {
      return count + 1; // Maximum temperature yields bonus item
    }
    return count;
  }

  private String getKroketDescription(Krokettype type) {
    return switch (type) {
      case KAAS -> "kaas";
      case KALF -> "kalfs";
      case GARNALEN -> "garnalen";
      case KIP -> "kip";
      case GROENTE -> "groente";
      case GEZOND -> "gezonde"; // Note: The healthy option still gets fried
    };
  }
}
