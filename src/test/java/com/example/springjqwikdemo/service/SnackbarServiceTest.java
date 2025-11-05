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

  /**
   * Demonstreert: SHRINKING in actie met een echte bug
   *
   * Deze test onthult een bug in de processWords() methode: de methode hanteert
   * null values niet correct in lijsten met veel elementen.
   *
   * SHRINKING DEMO:
   * Als je deze test runt (verwijder @Disabled), zal jqwik:
   * 1. Een complexe lijst vinden die faalt, bijvoorbeeld:
   *    ["foo", "bar", null, "baz", "test", "aardappel", "xyz"]
   * 2. Automatisch SHRINKEN naar het minimale falende voorbeeld:
   *    [null, "aardappel"]
   * 3. Dit laat PRECIES zien waar de bug zit: combinatie van null + target word
   *
   * Zonder shrinking zou je een grote willekeurige lijst zien en zou debugging
   * veel moeilijker zijn. Met shrinking zie je meteen het probleem!
   */
  @Property
  @org.junit.jupiter.api.Disabled("Demonstreert shrinking - verwijder @Disabled om falende test te zien")
  @Label("SHRINKING DEMO: Bug met null values wordt automatisch geminimaliseerd")
  void demonstratesShrinking_FindsMinimalFailingCase(
      @ForAll @Size(min = 1, max = 20) List<String> words) {
    // Given - Een lijst die null values KAN bevatten (jqwik genereert deze automatisch)

    // When - Process de words (deze methode heeft een bug met null handling!)
    List<String> result = service.processWords(words);

    // Then - Assert dat result size consistent is
    // Deze test faalt als de lijst null EN een target word bevat
    // jqwik zal automatisch shrinken naar: [null, "aardappel"] of ["aardappel", null]
    long targetWordCount = words.stream()
        .filter(w -> w != null && (w.equals("aardappel") || w.equals("pieper")))
        .count();

    int expectedSize = (int) (targetWordCount * 9);
    assertThat(result)
        .as("jqwik shrinkt automatisch naar kleinste lijst die deze property schendt")
        .hasSize(expectedSize);

    // Zonder shrinking: faalt met complexe lijst van 15+ elementen
    // Met shrinking: faalt met minimale lijst zoals [null, "aardappel"]
    // Dit maakt de bug METEEN duidelijk!
  }

  /**
   * Demonstreert: SHRINKING met meerdere parameters
   *
   * Deze test laat zien hoe jqwik ALLE parameters tegelijk shrinkt naar
   * de minimale combinatie die faalt.
   *
   * VERWACHT RESULTAAT na shrinking:
   * - otherWords: [] (lege lijst)
   * - targetWords: ["aardappel"] (minimale lijst van 1 element)
   *
   * Dit laat zien dat de bug triggered wordt met 1 target word in combinatie
   * met een lijst van 11+ totale elementen.
   */
  @Property
  @org.junit.jupiter.api.Disabled("Demonstreert shrinking - verwijder @Disabled om falende test te zien")
  @Label("SHRINKING DEMO: Meerdere parameters shrinken tegelijk")
  void demonstratesShrinking_MultipleParameters(
      @ForAll @Size(min = 0, max = 15) List<@From("nonTargetWords") String> otherWords,
      @ForAll @Size(min = 1, max = 10) List<String> targetWords) {
    // Given - Twee onafhankelijke lijsten
    List<String> allWords = new java.util.ArrayList<>(otherWords);
    targetWords.forEach(w -> allWords.add("aardappel"));

    // When
    List<String> result = service.processWords(allWords);

    // Then - Deze property faalt voor grote combinaties
    // jqwik shrinkt BEIDE parameters naar minimale waarden
    int totalSize = otherWords.size() + targetWords.size();
    assertThat(totalSize)
        .as("jqwik shrinkt beide lijsten naar kleinste falende combinatie")
        .isLessThanOrEqualTo(10);

    // Zonder shrinking: faalt met otherWords size=12, targetWords size=7
    // Met shrinking: shrinkt naar otherWords=[], targetWords=["aardappel"] × 11
    // Of andere minimale combinatie die som > 10 geeft
  }
}
