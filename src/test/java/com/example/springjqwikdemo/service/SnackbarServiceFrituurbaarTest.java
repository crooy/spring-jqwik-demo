package com.example.springjqwikdemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.springjqwikdemo.domain.*;
import java.util.List;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

/**
 * Demonstreert jqwik property-based testing met domeinobjecten.
 *
 * <p>Belangrijke jqwik features die worden gedemonstreerd:
 * <ul>
 *   <li>Automatische domeinobject generatie via DomainArbitraryProvider</li>
 *   <li>@ForAll met aangepaste domeintypes (Pataten, Frikandellen, etc.)</li>
 *   <li>Sealed interfaces en records werken naadloos met jqwik</li>
 *   <li>@Example: Deterministische voorbeelden naast property tests</li>
 * </ul>
 *
 * <p>De DomainArbitraryProvider genereert automatisch instanties van Frituurbaar
 * en zijn subtypes, waardoor het makkelijk is om te testen met domeinobjecten.
 */
@PropertyDefaults(tries = 50, generation = GenerationMode.RANDOMIZED)
class SnackbarServiceFrituurbaarTest {

  private final SnackbarService service = new SnackbarService();

  /**
   * Demonstreert: Automatische domeinobject generatie
   *
   * jqwik genereert automatisch Pataten instanties met behulp van de DomainArbitraryProvider.
   * Geen handmatige testdata creatie nodig - jqwik regelt het!
   */
  @Property
  void propertyBasedTest_AutomaticDomainObjectGeneration(@ForAll Pataten pataten) {
    // When - jqwik genereert automatisch willekeurige Pataten instanties
    List<String> result = service.frituren(List.of(pataten));

    // Then - Property geldt voor alle gegenereerde Pataten instanties
    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(s -> s.equals("gefrituurde aardappelportie"));
    int expectedMinPortions = Math.max(1, pataten.size() / 10);
    assertThat(result.size()).isGreaterThanOrEqualTo(expectedMinPortions);
  }

  /**
   * Demonstreert: Sealed interface subtypes werken met jqwik
   *
   * jqwik kan elk subtype van Frituurbaar genereren (Frikandellen, Kroketten, etc.)
   * dankzij de DomainArbitraryProvider configuratie.
   */
  @Property
  void propertyBasedTest_SealedInterfaceSubtypes(@ForAll Frikandellen frikandellen) {
    // When - jqwik genereert Frikandellen instanties
    List<String> result = service.frituren(List.of(frikandellen));

    // Then - Property geldt voor alle gegenereerde instanties
    assertThat(result).hasSize(frikandellen.count());
    assertThat(result).allMatch(s -> s.equals("gefrituurde frikandel"));
  }

  /**
   * Demonstreert: Complexe domeinobjecten met meerdere velden
   *
   * jqwik genereert Kroketten met willekeurige type en count waarden,
   * en test automatisch alle combinaties.
   */
  @Property
  void propertyBasedTest_ComplexDomainObjects(@ForAll Kroketten kroketten) {
    // When - jqwik genereert Kroketten met willekeurige type en count
    List<String> result = service.frituren(List.of(kroketten));

    // Then - Property geldt voor alle combinaties
    assertThat(result).allMatch(s -> s.contains("gefrituurde") && s.contains("kroket"));
    assertThat(result).allMatch(s -> s.contains(getKroketTypeName(kroketten.type())));
  }

  /**
   * Demonstreert: Collecties van domeinobjecten
   *
   * jqwik genereert lijsten van Frituurbaar objecten met size constraints,
   * en creëert automatisch gevarieerde testscenario's.
   */
  @Property
  void propertyBasedTest_CollectionsOfDomainObjects(
      @ForAll @Size(min = 1, max = 10) List<Frituurbaar> items) {
    // When - jqwik genereert lijsten van gemengde snack types
    List<String> result = service.frituren(items);

    // Then - Property geldt voor alle gegenereerde lijsten
    // Elke snack moet minstens één resultaat opleveren
    assertThat(result).isNotEmpty();
    assertThat(result.size()).isGreaterThanOrEqualTo(items.size());
  }

  /**
   * Demonstreert: @Example voor deterministische testgevallen
   *
   * @Example draait eenmaal met vaste data, handig voor:
   * - Documentatiedoeleinden
   * - Zorgen dat specifieke scenario's werken
   * - Leesbare voorbeelden van verwacht gedrag bieden
   */
  @Example
  @Label("Deterministisch voorbeeld: Typische snackbar bestelling")
  void deterministicExample_TypicalOrder() {
    // Given - Vaste, deterministische testdata
    List<Frituurbaar> order =
        List.of(
            new Pataten(100),
            new Frikandellen(3),
            new Kroketten(Krokettype.KAAS, 2),
            new Cervela(1),
            new Bereklauw(1));

    // When
    List<String> result = service.frituren(order);

    // Then - Verifieer verwacht gedrag
    assertThat(result).isNotEmpty();
    assertThat(result).contains("gefrituurde aardappelportie");
    assertThat(result).contains("gefrituurde frikandel");
    assertThat(result).contains("gefrituurde kaas kroket");
    assertThat(result).contains("gefrituurde cervela");
    assertThat(result).contains("gefrituurde bereklauw");
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
}
