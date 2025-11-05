package be.wegenenverkeer.springjqwikdemo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import be.wegenenverkeer.springjqwikdemo.service.SnackbarService;
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


  @Property
  void processWordsWithAardappelReturnsCorrectResponse(
      @ForAll @Size(min = 0, max = 10) List<String> otherWords) throws Exception {
    // Given
    List<String> input = new java.util.ArrayList<>(otherWords);

    Assume.that(!input.contains("aardappel"));
      Assume.that(!input.contains("pieper"));

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

  @Property
  void processWordsWithBothAardappelAndPieperReturnsCorrectTotal(
      @ForAll @Size(min = 0, max = 3) List<@From("nonTargetWords") String> otherWords,
      @ForAll @Size(min = 1, max = 3) List<@From("aardappels") String> aardappels)
      throws Exception {
    // Given
    List<String> input = new java.util.ArrayList<>(otherWords);
    input.addAll(aardappels);

    String requestBody = objectMapper.writeValueAsString(input);
    Integer expectedSize = aardappels.stream()
        .filter(w -> w.equals("aardappel") || w.equals("pieper"))
        .map(String::length)
        .reduce(0, Integer::sum);

    // When & Then
    mockMvc
        .perform(
            post("/api/bakken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(expectedSize));
  }

  @Property
  void processWordsJustOnce()
      throws Exception {
    // Given
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

  @Provide
  Arbitrary<String> nonTargetWords() {
    return Arbitraries.strings()
        .alpha()
        .ofMinLength(1)
        .ofMaxLength(20)
        .filter(word -> !word.equals("aardappel") && !word.equals("pieper"));
  }

  @Provide
    Arbitrary<String> aardappels() {
      return Arbitraries.of(List.of("aardappel", "pieper", "patat"));
  }
}
