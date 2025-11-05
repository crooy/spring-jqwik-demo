package com.example.springjqwikdemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

/**
 * Demonstreert jqwik property-based testing met string inputs.
 *
 * <p>Belangrijke jqwik features die worden gedemonstreerd:
 * <ul>
 *   <li>@Property: Definieert een property-based test die meerdere keren draait met willekeurige data</li>
 *   <li>@ForAll: Genereert automatisch willekeurige testdata</li>
 *   <li>@Size: Beperkt collectiegroottes (lijsten, sets, etc.)</li>
 *   <li>@From: Gebruikt aangepaste arbitraries om specifieke data te genereren</li>
 *   <li>@Provide: Creëert aangepaste data generators</li>
 * </ul>
 */
@PropertyDefaults(tries = 100, generation = GenerationMode.RANDOMIZED)
class SnackbarServiceTest {

  private final SnackbarService service = new SnackbarService();

  /**
   * Demonstreert: @ForAll met @Size constraint voor lijsten
   *
   * jqwik genereert automatisch 100 willekeurige lijsten van strings (grootte 0-10)
   * en verifieert dat de property geldt voor allemaal.
   */
  @Property
  void propertyBasedTest_GeneratesRandomListsWithSizeConstraint(
      @ForAll @Size(min = 0, max = 10) List<String> otherWords) {
    // Given - jqwik genereert willekeurige lijsten van strings
    List<String> input = new java.util.ArrayList<>(otherWords);
    input.add("aardappel");

    // When
    List<String> result = service.processWords(input);

    // Then - Property moet gelden voor ALLE gegenereerde inputs
    assertThat(result).hasSize(9);
    assertThat(result).containsOnly("friet");
  }

  /**
   * Demonstreert: Meerdere @ForAll parameters met verschillende constraints
   *
   * jqwik genereert combinaties van twee onafhankelijke willekeurige lijsten
   * en test de property voor alle combinaties.
   */
  @Property
  void propertyBasedTest_MultipleParametersGenerateCombinations(
      @ForAll @Size(min = 0, max = 5) List<String> otherWords,
      @ForAll @Size(min = 1, max = 5) List<String> aardappels) {
    // Given - jqwik genereert combinaties van beide lijsten
    List<String> input = new java.util.ArrayList<>(otherWords);
    aardappels.forEach(word -> input.add("aardappel"));

    // When
    List<String> result = service.processWords(input);

    // Then - Property geldt voor alle combinaties
    int expectedSize = aardappels.size() * 9;
    assertThat(result).hasSize(expectedSize);
    assertThat(result).containsOnly("friet");
  }

  /**
   * Demonstreert: Aangepaste arbitrary met @From en @Provide
   *
   * Gebruikt een aangepaste data generator (@Provide) om gefilterde testdata te creëren.
   * Dit is krachtiger dan simpele constraints - we kunnen precies
   * de data genereren die we nodig hebben voor ons testscenario.
   */
  @Property
  void propertyBasedTest_CustomArbitraryWithFilter(
      @ForAll @Size(min = 0, max = 20) List<@From("nonTargetWords") String> input) {
    // When - jqwik gebruikt onze aangepaste arbitrary om gefilterde woorden te genereren
    List<String> result = service.processWords(input);

    // Then - Property geldt voor alle gefilterde inputs
    assertThat(result).isEmpty();
  }

  /**
   * Aangepaste data generator met jqwik's Arbitraries API.
   *
   * Dit demonstreert hoe je domein-specifieke testdata generators creëert
   * die waarden produceren die voldoen aan specifieke criteria (in dit geval, woorden
   * die NIET de doelwoorden zijn).
   */
  @Provide
  Arbitrary<String> nonTargetWords() {
    return Arbitraries.strings()
        .alpha()
        .ofMinLength(1)
        .ofMaxLength(20)
        .filter(word -> !word.equals("aardappel") && !word.equals("pieper"));
  }
}
