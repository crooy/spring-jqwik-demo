package com.example.springjqwikdemo.service;

import java.util.ArrayList;
import java.util.List;

public class WordProcessorService {

  public List<String> processWords(List<String> input) {
    List<String> result = new ArrayList<>();
    for (String word : input) {
      if (word != null && (word.equals("aardappel") || word.equals("pieper"))) {
        int length = word.length();
        for (int i = 0; i < length; i++) {
          result.add("friet");
        }
      }
    }
    return result;
  }
}
