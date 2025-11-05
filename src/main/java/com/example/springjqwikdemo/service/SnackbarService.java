package com.example.springjqwikdemo.service;

import com.example.springjqwikdemo.domain.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SnackbarService {

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
    for (Frituurbaar item : items) {
      switch (item) {
        case Pataten p -> {
          int portions = Math.max(1, p.size() / 10);
          for (int i = 0; i < portions; i++) {
            result.add("gefrituurde aardappelportie");
          }
        }
        case Frikandellen f -> {
          for (int i = 0; i < f.count(); i++) {
            result.add("gefrituurde frikandel");
          }
        }
        case Kroketten k -> {
          String typeName = getKroketTypeName(k.type());
          for (int i = 0; i < k.count(); i++) {
            result.add("gefrituurde " + typeName + " kroket");
          }
        }
        case Cervela c -> {
          result.add("gefrituurde cervela");
        }
        case Bereklauw b -> {
          result.add("gefrituurde bereklauw");
        }
      }
    }
    return result;
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
