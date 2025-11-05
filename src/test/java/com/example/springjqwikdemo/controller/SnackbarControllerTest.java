package com.example.springjqwikdemo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.springjqwikdemo.service.SnackbarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;
import net.jqwik.spring.JqwikSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Demonstreert jqwik integratie met Spring Boot en MockMvc.
 *
 * <p>Belangrijke jqwik features die worden gedemonstreerd:
 * <ul>
 *   <li>@JqwikSpringSupport: Schakelt jqwik in binnen Spring Boot test context</li>
 *   <li>Property-based testing van REST endpoints</li>
 *   <li>@Assume: Filteren van testgevallen op basis van condities</li>
 *   <li>Testen van HTTP endpoints met gegenereerde data</li>
 * </ul>
 *
 * <p>Dit laat zien hoe jqwik gebruikt kan worden voor integratietests, niet alleen unit tests.
 * We genereren willekeurige HTTP request bodies en verifi?ren dat de API correct gedraagt.
 */
@JqwikSpringSupport
@WebMvcTest(SnackbarController.class)
@PropertyDefaults(tries = 50, generation = GenerationMode.RANDOMIZED)
class SnackbarControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class TestConfig {
    @Bean
    public SnackbarService snackbarService() {
      return new SnackbarService();
    }
  }

  /**
   * Demonstreert: @Assume voor het filteren van testgevallen
   * 
   * @Assume laat ons testgevallen overslaan die niet aan onze criteria voldoen.
   * jqwik genereert nieuwe data totdat het gevallen vindt die de assumptie doorstaan.
   * Dit is handig voor het testen van specifieke scenario's zonder handmatige filtering.
   */
  @Property
  void propertyBasedTest_AssumeFiltersTestCases(
      @ForAll @Size(min = 0, max = 10) List<String> otherWords) throws Exception {
    // Given - jqwik genereert willekeurige lijsten
    List<String> input = new java.util.ArrayList<>(otherWords);

    // @Assume filtert gevallen eruit die we niet willen testen
    // jqwik genereert nieuwe data als de assumptie faalt
    Assume.that(!input.contains("aardappel"));
    Assume.that(!input.contains("pieper"));

    String requestBody = objectMapper.writeValueAsString(input);

    // When & Then - Test het REST endpoint met gegenereerde data
    mockMvc
        .perform(
            post("/api/bakken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  /**
   * Demonstreert: Property-based testing van REST API's
   * 
   * jqwik genereert willekeurige request bodies en we verifieren dat de API
   * correct reageert. Dit test veel meer scenario's dan
   * traditionele voorbeeld-gebaseerde tests.
   */
  @Property
  void propertyBasedTest_RestEndpointWithGeneratedData(
      @ForAll @Size(min = 0, max = 3) List<@From("nonTargetWords") String> otherWords,
      @ForAll @Size(min = 1, max = 3) List<@From("aardappels") String> aardappels)
      throws Exception {
    // Given - jqwik genereert combinaties van willekeurige data
    List<String> input = new java.util.ArrayList<>(otherWords);
    input.addAll(aardappels);

    String requestBody = objectMapper.writeValueAsString(input);
    Integer expectedSize =
        aardappels.stream()
            .filter(w -> w.equals("aardappel") || w.equals("pieper"))
            .map(String::length)
            .reduce(0, Integer::sum);

    // When & Then - Property geldt voor alle gegenereerde requests
    mockMvc
        .perform(
            post("/api/bakken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(expectedSize));
  }

  /**
   * Demonstreert: Arbitraries programmatisch gebruiken
   * 
   * Soms moet je arbitraries gebruiken buiten @ForAll parameters.
   * Dit laat zien hoe je handmatig kunt samplen uit een arbitrary.
   */
  @Property
  void propertyBasedTest_ManualArbitrarySampling() throws Exception {
    // Given - Sample programmatisch uit arbitrary
    List<String> input = nonTargetWords().list().ofMinSize(1).ofMaxSize(3).sample();
    String requestBody = objectMapper.writeValueAsString(input);

    // When & Then
    mockMvc
        .perform(
            post("/api/bakken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  /**
   * Aangepaste arbitrary voor het genereren van niet-doelwoorden.
   * Demonstreert jqwik's flexibele data generatie API.
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
   * Aangepaste arbitrary voor het genereren van doelwoorden.
   * Laat zien hoe je domein-specifieke generators creëert.
   */
  @Provide
  Arbitrary<String> aardappels() {
    return Arbitraries.of(List.of("aardappel", "pieper", "patat"));
  }
}
