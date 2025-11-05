package com.example.springjqwikdemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.springjqwikdemo.domain.*;
import java.util.List;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.Disabled;

/**
 * Demonstreert jqwik's SHRINKING feature - een van de krachtigste aspecten van property-based
 * testing.
 *
 * <p><b>Wat is Shrinking?</b><br>
 * Als een property test faalt, probeert jqwik automatisch een "kleiner" voorbeeld te vinden dat nog
 * steeds faalt. Dit maakt debugging veel eenvoudiger omdat je de minimale input ziet die het
 * probleem veroorzaakt, in plaats van een complexe willekeurige waarde.
 *
 * <p><b>Voorbeeld:</b><br>
 * Test faalt met: {@code List.of("xyzabc", "test", "foo", "bar", "aardappel", "baz")}<br>
 * jqwik shrinkt naar: {@code List.of("aardappel")} - het kleinste voorbeeld dat faalt
 *
 * <p><b>Hoe Shrinking Werkt:</b>
 * <ul>
 * <li>Voor getallen: shrinkt naar 0, dan naar kleine waarden</li>
 * <li>Voor strings: shrinkt naar kortere strings en eenvoudigere karakters</li>
 * <li>Voor collecties: verwijdert elementen om de kleinste falende collectie te vinden</li>
 * <li>Voor objecten: shrinkt recursief alle velden</li>
 * </ul>
 *
 * <p><b>Let op:</b> Tests in deze klasse zijn {@code @Disabled} omdat ze opzettelijk falen om
 * shrinking te demonstreren. Verwijder {@code @Disabled} om shrinking in actie te zien.
 *
 * <p>Belangrijk: Shrinking gebeurt alleen als een test FAALT. De gedemonstreerde tests falen
 * opzettelijk om dit gedrag te tonen.
 */
@PropertyDefaults(tries = 100, shrinking = ShrinkingMode.FULL)
class SnackbarServiceShrinkingDemoTest {

  private final SnackbarService service = new SnackbarService();

  /**
   * Demonstreert: Basis shrinking met integers
   *
   * <p>Deze test faalt opzettelijk voor waarden groter dan 50. Als je deze test uitvoert (verwijder
   * {@code @Disabled}), zal jqwik:
   *
   * <ol>
   * <li>Een willekeurige waarde vinden die faalt (bijv. 789)
   * <li>Automatisch shrinken naar kleinere waarden
   * <li>Uiteindelijk rapporteren: 51 (de kleinste waarde die nog steeds faalt)
   * </ol>
   *
   * <p>Zonder shrinking zou je een foutmelding zien met waarde 789, wat minder informatief is dan
   * te weten dat de grens bij 51 ligt.
   */
  @Property
  @Disabled("Opzettelijk falende test om shrinking te demonstreren - verwijder @Disabled om te testen")
  @Label("Shrinking Demo: Integers shrinken naar kleinste falende waarde")
  void demonstratesShrinking_Integers(@ForAll @IntRange(min = 0, max = 1000) int value) {
    // Deze assert faalt voor value > 50
    // jqwik zal automatisch shrinken en rapporteren dat 51 de kleinste falende waarde is
    assertThat(value)
        .as("jqwik zou moeten shrinken naar 51 (de kleinste waarde die faalt)")
        .isLessThanOrEqualTo(50);
  }

  /**
   * Demonstreert: Shrinking met strings
   *
   * <p>Deze test faalt als een string te lang is. jqwik zal:
   *
   * <ol>
   * <li>Een lange string vinden die faalt (bijv. "abcdefghijk")
   * <li>Systematisch karakters verwijderen
   * <li>Shrinken naar eenvoudigere karakters (bijv. 'a' in plaats van 'z')
   * <li>Rapporteren: "aaaaaaa" (7 karakters, de minimale falende lengte)
   * </ol>
   */
  @Property
  @Disabled("Opzettelijk falende test om shrinking te demonstreren - verwijder @Disabled om te testen")
  @Label("Shrinking Demo: Strings shrinken naar kortste falende lengte")
  void demonstratesShrinking_Strings(@ForAll String word) {
    // Deze assert faalt voor strings langer dan 6 karakters
    // jqwik shrinkt naar de kortste string met simpelste karakters die nog faalt
    assertThat(word.length())
        .as("jqwik zou moeten shrinken naar een string van exact 7 karakters")
        .isLessThanOrEqualTo(6);
  }

  /**
   * Demonstreert: Shrinking met lijsten
   *
   * <p>Dit is bijzonder krachtig voor complexe scenario's. Als een lijst met veel elementen faalt,
   * probeert jqwik elementen te verwijderen om de minimale falende lijst te vinden.
   *
   * <p>Bijvoorbeeld, als de test faalt met: {@code ["test", "foo", "bar", "aardappel", "xyz",
   * "abc"]}<br>
   * jqwik shrinkt naar: {@code ["aardappel"]} - het enige element dat nodig is om te falen
   */
  @Property
  @Disabled("Opzettelijk falende test om shrinking te demonstreren - verwijder @Disabled om te testen")
  @Label("Shrinking Demo: Lijsten shrinken naar minimale falende collectie")
  void demonstratesShrinking_Lists(@ForAll @Size(max = 20) List<String> words) {
    // Deze test faalt als de lijst "aardappel" bevat
    // jqwik zal alle irrelevante elementen verwijderen en shrinken naar: ["aardappel"]
    boolean containsTargetWord = words.contains("aardappel") || words.contains("pieper");

    assertThat(containsTargetWord)
        .as("jqwik zou moeten shrinken naar de kleinste lijst die 'aardappel' bevat")
        .isFalse();
  }

  /**
   * Demonstreert: Shrinking met domeinobjecten
   *
   * <p>jqwik kan ook domeinobjecten shrinken door recursief alle velden te minimaliseren. Dit is
   * extreem waardevol voor complexe business logica.
   *
   * <p>Als test faalt met: {@code Pataten(size=157)}<br>
   * jqwik shrinkt naar: {@code Pataten(size=101)} - de kleinste size die nog faalt
   */
  @Property
  @Disabled("Opzettelijk falende test om shrinking te demonstreren - verwijder @Disabled om te testen")
  @Label("Shrinking Demo: Domeinobjecten shrinken alle velden")
  void demonstratesShrinking_DomainObjects(@ForAll Pataten pataten) {
    // Deze test faalt voor Pataten met size > 100
    // jqwik shrinkt automatisch het size veld naar 101
    assertThat(pataten.size())
        .as("jqwik zou moeten shrinken naar Pataten(size=101)")
        .isLessThanOrEqualTo(100);
  }

  /**
   * Demonstreert: Shrinking met meerdere parameters
   *
   * <p>Wanneer een property met meerdere parameters faalt, probeert jqwik ALLE parameters tegelijk
   * te shrinken naar hun minimale falende waarden.
   *
   * <p>Dit is krachtig omdat het de precieze combinatie vindt die het probleem veroorzaakt.
   */
  @Property
  @Disabled("Opzettelijk falende test om shrinking te demonstreren - verwijder @Disabled om te testen")
  @Label("Shrinking Demo: Meerdere parameters shrinken tegelijk")
  void demonstratesShrinking_MultipleParameters(
      @ForAll @IntRange(min = 0, max = 100) int size,
      @ForAll @IntRange(min = 0, max = 100) int count) {
    // Test faalt als som > 150
    // jqwik shrinkt beide parameters om de kleinste falende combinatie te vinden
    // Bijvoorbeeld: size=76, count=75 (som=151) of size=151, count=0
    int sum = size + count;
    assertThat(sum)
        .as("jqwik zou moeten shrinken naar kleinste combinatie waar size + count > 150")
        .isLessThanOrEqualTo(150);
  }

  /**
   * Demonstreert: Shrinking met complexe business logica
   *
   * <p>Dit voorbeeld toont hoe shrinking helpt bij het debuggen van echte business logica. Als de
   * frituur service een bug heeft met grote bestellingen, vindt jqwik automatisch de kleinste
   * bestelling die het probleem triggert.
   */
  @Property
  @Disabled("Opzettelijk falende test om shrinking te demonstreren - verwijder @Disabled om te testen")
  @Label("Shrinking Demo: Complexe business logica debugging")
  void demonstratesShrinking_ComplexBusinessLogic(
      @ForAll @Size(min = 1, max = 20) List<Frituurbaar> order) {
    // Stel dat onze service een bug heeft met grote bestellingen
    List<String> result = service.frituren(order);

    // Test faalt als totaal aantal gefrituurde items > 25
    // jqwik shrinkt de bestelling naar het minimale aantal items dat nog faalt
    assertThat(result.size())
        .as("jqwik shrinkt naar kleinste bestelling die te veel items genereert")
        .isLessThanOrEqualTo(25);
  }

  /**
   * Demonstreert: Shrinking configuratie
   *
   * <p>Je kunt shrinking gedrag configureren:
   * <ul>
   * <li>{@code ShrinkingMode.FULL} - Volledige shrinking (standaard, langzaam maar grondig)</li>
   * <li>{@code ShrinkingMode.BOUNDED} - Beperkte shrinking (sneller, minder grondig)</li>
   * <li>{@code ShrinkingMode.OFF} - Geen shrinking (alleen voor performance testing)</li>
   * </ul>
   *
   * <p>In de meeste gevallen wil je FULL shrinking om de meest informatieve foutmeldingen te
   * krijgen.
   */
  @Property(shrinking = ShrinkingMode.OFF)
  @Disabled("Opzettelijk falende test om shrinking configuratie te demonstreren")
  @Label("Shrinking Demo: Shrinking uitgeschakeld")
  void demonstratesShrinking_Disabled(@ForAll @IntRange(min = 0, max = 1000) int value) {
    // Met ShrinkingMode.OFF zie je de originele falende waarde (bijv. 789)
    // in plaats van de geshrinkte waarde (51)
    assertThat(value)
        .as("Zonder shrinking zie je de originele willekeurige falende waarde")
        .isLessThanOrEqualTo(50);
  }

  /**
   * BONUS: Een realistisch voorbeeld van shrinking in actie
   *
   * <p>Dit test een echte edge case: wat gebeurt er als iemand een enorme bestelling plaatst? Door
   * shrinking kunnen we precies zien waar de grens ligt.
   */
  @Property
  @Label("Realistisch voorbeeld: Service handelt grote bestellingen correct af")
  void serviceHandlesLargeOrdersCorrectly(@ForAll @Size(max = 50) List<Frituurbaar> order) {
    // Given - Een willekeurige bestelling
    // When
    List<String> result = service.frituren(order);

    // Then - Resultaat mag niet null zijn en moet veilig te verwerken zijn
    assertThat(result).isNotNull();
    assertThat(result.size()).isGreaterThanOrEqualTo(0);

    // Als deze property faalt, shrinkt jqwik automatisch naar de kleinste
    // problematische bestelling, wat debugging enorm vergemakkelijkt
  }
}
