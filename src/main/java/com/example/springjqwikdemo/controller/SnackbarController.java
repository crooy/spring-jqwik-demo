package com.example.springjqwikdemo.controller;

import com.example.springjqwikdemo.domain.Frituurbaar;
import com.example.springjqwikdemo.service.SnackbarService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SnackbarController {

  private final SnackbarService snackbarService;

  public SnackbarController(SnackbarService snackbarService) {
    this.snackbarService = snackbarService;
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("OK");
  }

  @PostMapping("/bakken")
  public ResponseEntity<List<String>> processWords(@RequestBody List<String> input) {
    List<String> result = snackbarService.processWords(input);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/frituren")
  public ResponseEntity<List<String>> processSnacks(@RequestBody List<Frituurbaar> snacks) {
    List<String> result = snackbarService.frituren(snacks);
    return ResponseEntity.ok(result);
  }
}
