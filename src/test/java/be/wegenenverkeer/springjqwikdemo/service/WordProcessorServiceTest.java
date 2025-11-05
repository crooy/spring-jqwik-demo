package be.wegenenverkeer.springjqwikdemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.Test;

@PropertyDefaults(tries = 100, generation = GenerationMode.RANDOMIZED)
class WordProcessorServiceTest {

  private final WordProcessorService service = new WordProcessorService();

  @Property
  void processWordsWithAardappelReturnsCorrectNumberOfFriet(
      @ForAll @Size(min = 0, max = 10) List<String> otherWords) {
    // Given
    List<String> input = new java.util.ArrayList<>(otherWords);
    input.add("aardappel");

    // When
    List<String> result = service.processWords(input);

    // Then
    assertThat(result).hasSize(9);
    assertThat(result).containsOnly("friet");
  }

  @Property
  void processWordsWithPieperReturnsCorrectNumberOfFriet(
      @ForAll @Size(min = 0, max = 10) List<String> otherWords) {
    // Given
    List<String> input = new java.util.ArrayList<>(otherWords);
    input.add("pieper");

    // When
    List<String> result = service.processWords(input);

    // Then
    assertThat(result).hasSize(6);
    assertThat(result).containsOnly("friet");
  }

  @Property
  void processWordsWithMultipleAardappelReturnsCorrectTotal(
      @ForAll @Size(min = 0, max = 5) List<String> otherWords,
      @ForAll @Size(min = 1, max = 5) List<String> aardappels) {
    // Given
    List<String> input = new java.util.ArrayList<>(otherWords);
    aardappels.forEach(word -> input.add("aardappel"));

    // When
    List<String> result = service.processWords(input);

    // Then
    int expectedSize = aardappels.size() * 9;
    assertThat(result).hasSize(expectedSize);
    assertThat(result).containsOnly("friet");
  }

  @Property
  void processWordsWithBothAardappelAndPieperReturnsCorrectTotal(
      @ForAll @Size(min = 0, max = 5) List<String> otherWords,
      @ForAll @Size(min = 0, max = 3) List<String> aardappels,
      @ForAll @Size(min = 0, max = 3) List<String> piepers) {
    // Given
    List<String> input = new java.util.ArrayList<>(otherWords);
    aardappels.forEach(word -> input.add("aardappel"));
    piepers.forEach(word -> input.add("pieper"));

    // When
    List<String> result = service.processWords(input);

    // Then
    int expectedSize = aardappels.size() * 9 + piepers.size() * 6;
    assertThat(result).hasSize(expectedSize);
    assertThat(result).containsOnly("friet");
  }

  @Property
  void processWordsWithoutAardappelOrPieperReturnsEmpty(
      @ForAll @Size(min = 0, max = 20) List<@From("nonTargetWords") String> input) {
    // When
    List<String> result = service.processWords(input);

    // Then
    assertThat(result).isEmpty();
  }

  @Provide
  Arbitrary<String> nonTargetWords() {
    return Arbitraries.strings()
        .alpha()
        .ofMinLength(1)
        .ofMaxLength(20)
        .filter(word -> !word.equals("aardappel") && !word.equals("pieper"));
  }

}
