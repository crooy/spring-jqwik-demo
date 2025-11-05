# Spring Boot jqwik Demo

Een uitgebreide demonstratieproject dat **jqwik** toont - een property-based testing framework voor Java.

Dit project demonstreert hoe je jqwik gebruikt voor property-based testing in een Spring Boot applicatie, met gedetailleerde voorbeelden en uitleg van jqwik features.

## Wat is jqwik?

**jqwik** is een property-based testing framework voor Java geïnspireerd door QuickCheck (Haskell) en vergelijkbare frameworks. In plaats van tests te schrijven met vaste voorbeelddata, beschrijf je **properties** (eigenschappen) die moeten gelden voor alle geldige inputs, en jqwik genereert automatisch honderden testgevallen.

### Waarom Property-Based Testing?

Traditionele voorbeeld-gebaseerde tests:
```java
@Test
void testWithAardappel() {
    List<String> input = List.of("aardappel");
    List<String> result = service.processWords(input);
    assertEquals(9, result.size());
}
```

Property-based tests met jqwik:
```java
@Property
void propertyHoldsForAllInputs(@ForAll List<String> input) {
    // jqwik genereert 100+ willekeurige inputs en verifieert
    // dat de property geldt voor ALLE inputs
    List<String> result = service.processWords(input);
    // Assert property die moet gelden voor alle inputs
}
```

**Voordelen:**
- Test automatisch veel meer scenario's
- Ontdekt edge cases die je misschien niet bedenkt
- Verifieert dat properties gelden voor alle geldige inputs
- Vermindert onderhoudslast van tests
- Vindt bugs die handmatige tests missen

## Aan de slag

### Vereisten

- Java 21
- Maven 3.6+

### Setup met Nix (Aanbevolen)

```bash
nix develop
```

Dit biedt Java 21 en Maven in je shell.

### Handmatige Setup

Zorg dat je Java 21 en Maven 3.6+ geïnstalleerd hebt.

## Applicatie draaien

```bash
# Met Nix
nix develop --command mvn spring-boot:run

# Of handmatig
mvn spring-boot:run
```

De server start op poort 8080.

## Tests draaien

```bash
# Met Nix
nix develop --command mvn test

# Of handmatig
mvn test
```

## jqwik Gebruiksgids

Deze sectie legt jqwik features uit met concrete voorbeelden uit dit project.

### 1. Basis Property-Based Testing

De eenvoudigste jqwik test gebruikt `@Property` en `@ForAll`:

```java
@Property
void propertyBasedTest_GeneratesRandomListsWithSizeConstraint(
    @ForAll @Size(min = 0, max = 10) List<String> otherWords) {
    // Given - jqwik genereert willekeurige lijsten van strings
    List<String> input = new ArrayList<>(otherWords);
    input.add("aardappel");

    // When
    List<String> result = service.processWords(input);

    // Then - Property moet gelden voor ALLE gegenereerde inputs
    assertThat(result).hasSize(9);
    assertThat(result).containsOnly("friet");
}
```

**Wat gebeurt er:**
- `@Property` vertelt jqwik dat dit een property-based test is
- `@ForAll` vertelt jqwik om willekeurige data te genereren voor deze parameter
- `@Size(min = 0, max = 10)` beperkt de lijstgrootte
- jqwik draait deze test **100 keer** (standaard) met verschillende willekeurige inputs
- De property moet gelden voor **alle** gegenereerde inputs

**Zie:** `SnackbarServiceTest.propertyBasedTest_GeneratesRandomListsWithSizeConstraint()`

### 2. Meerdere Parameters en Combinaties

jqwik genereert combinaties van meerdere parameters:

```java
@Property
void propertyBasedTest_MultipleParametersGenerateCombinations(
    @ForAll @Size(min = 0, max = 5) List<String> otherWords,
    @ForAll @Size(min = 1, max = 5) List<String> aardappels) {
    // jqwik genereert ALLE combinaties van beide lijsten
    List<String> input = new ArrayList<>(otherWords);
    aardappels.forEach(word -> input.add("aardappel"));

    List<String> result = service.processWords(input);

    // Property geldt voor alle combinaties
    int expectedSize = aardappels.size() * 9;
    assertThat(result).hasSize(expectedSize);
}
```

**Wat gebeurt er:**
- jqwik genereert combinaties van `otherWords` en `aardappels`
- Elke parameter wordt onafhankelijk gegenereerd
- Tests dekken veel meer scenario's dan handmatige voorbeelden

**Zie:** `SnackbarServiceTest.propertyBasedTest_MultipleParametersGenerateCombinations()`

### 3. Aangepaste Arbitraries met @Provide

Maak aangepaste data generators voor domein-specifieke testdata:

```java
@Property
void propertyBasedTest_CustomArbitraryWithFilter(
    @ForAll @Size(min = 0, max = 20) List<@From("nonTargetWords") String> input) {
    // jqwik gebruikt onze aangepaste arbitrary om gefilterde woorden te genereren
    List<String> result = service.processWords(input);
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
```

**Wat gebeurt er:**
- `@Provide` creëert een aangepaste data generator
- `@From("nonTargetWords")` vertelt jqwik om deze generator te gebruiken
- Je kunt arbitraries filteren, transformeren en combineren
- Perfect voor domein-specifieke testdata

**Zie:** `SnackbarServiceTest.propertyBasedTest_CustomArbitraryWithFilter()`

### 4. Automatische Domein Object Generatie

jqwik kan automatisch domeinobjecten genereren met `ArbitraryProvider`:

```java
@Property
void propertyBasedTest_AutomaticDomainObjectGeneration(@ForAll Pataten pataten) {
    // jqwik genereert automatisch Pataten instanties!
    // Geen handmatige testdata creatie nodig
    List<String> result = service.frituren(List.of(pataten));

    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(s -> s.equals("gefrituurde aardappelportie"));
}
```

**Hoe het werkt:**
1. Maak een `ArbitraryProvider` die `net.jqwik.api.providers.ArbitraryProvider` implementeert
2. Registreer deze in `META-INF/services/net.jqwik.api.providers.ArbitraryProvider`
3. jqwik gebruikt deze automatisch wanneer het `@ForAll Pataten` tegenkomt

**Voorbeeld Provider:**
```java
public class DomainArbitraryProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Pataten.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, ...) {
        if (targetType.isOfType(Pataten.class)) {
            return Collections.singleton(
                Arbitraries.integers()
                    .between(10, 200)
                    .map(Pataten::new)
            );
        }
        return Collections.emptySet();
    }
}
```

**Zie:**
- `SnackbarServiceFrituurbaarTest.propertyBasedTest_AutomaticDomainObjectGeneration()`
- `src/test/java/com/example/springjqwikdemo/property/DomainArbitraryProvider.java`

### 5. Testen van Collecties van Domeinobjecten

jqwik kan lijsten van domeinobjecten genereren:

```java
@Property
void propertyBasedTest_CollectionsOfDomainObjects(
    @ForAll @Size(min = 1, max = 10) List<Frituurbaar> items) {
    // jqwik genereert lijsten van gemengde snack types
    List<String> result = service.frituren(items);

    assertThat(result).isNotEmpty();
    assertThat(result.size()).isGreaterThanOrEqualTo(items.size());
}
```

**Wat gebeurt er:**
- jqwik genereert lijsten van `Frituurbaar` objecten
- Elk object wordt gegenereerd met de `DomainArbitraryProvider`
- Lijstgrootte wordt beperkt door `@Size`
- Tests dekken automatisch veel combinaties

**Zie:** `SnackbarServiceFrituurbaarTest.propertyBasedTest_CollectionsOfDomainObjects()`

### 6. Deterministische Voorbeelden met @Example

Soms wil je deterministische voorbeelden naast property tests:

```java
@Example
@Label("Deterministisch voorbeeld: Typische snackbar bestelling")
void deterministicExample_TypicalOrder() {
    // Given - Vaste, deterministische testdata
    List<Frituurbaar> order = List.of(
        new Pataten(100),
        new Frikandellen(3),
        new Kroketten(Krokettype.KAAS, 2)
    );

    // When
    List<String> result = service.frituren(order);

    // Then - Verifieer verwacht gedrag
    assertThat(result).contains("gefrituurde aardappelportie");
    assertThat(result).contains("gefrituurde frikandel");
}
```

**Gebruiksvoorbeelden:**
- Documentatiedoeleinden
- Zorgen dat specifieke scenario's werken
- Leesbare voorbeelden van verwacht gedrag bieden
- Regressietests voor bekende bugs

**Zie:** `SnackbarServiceFrituurbaarTest.deterministicExample_TypicalOrder()`

### 7. Filteren van Testgevallen met @Assume

Sla testgevallen over die niet aan je criteria voldoen:

```java
@Property
void propertyBasedTest_AssumeFiltersTestCases(
    @ForAll @Size(min = 0, max = 10) List<String> otherWords) {
    List<String> input = new ArrayList<>(otherWords);

    // @Assume filtert gevallen eruit die we niet willen testen
    // jqwik genereert nieuwe data als de assumptie faalt
    Assume.that(!input.contains("aardappel"));
    Assume.that(!input.contains("pieper"));

    // Test alleen gevallen die de assumptie doorstaan
    List<String> result = service.processWords(input);
    assertThat(result).isEmpty();
}
```

**Wat gebeurt er:**
- Als de assumptie faalt, genereert jqwik nieuwe data
- Alleen testgevallen die de assumptie doorstaan worden uitgevoerd
- Handig voor het testen van specifieke scenario's zonder handmatige filtering

**Zie:** `SnackbarControllerTest.propertyBasedTest_AssumeFiltersTestCases()`

### 8. Spring Boot Integratie

jqwik integreert met Spring Boot met `@JqwikSpringSupport`:

```java
@JqwikSpringSupport
@WebMvcTest(SnackbarController.class)
@PropertyDefaults(tries = 50, generation = GenerationMode.RANDOMIZED)
class SnackbarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Property
    void propertyBasedTest_RestEndpointWithGeneratedData(
        @ForAll @Size(min = 0, max = 3) List<String> input) throws Exception {

        String requestBody = objectMapper.writeValueAsString(input);

        // Test REST endpoint met gegenereerde data
        mockMvc.perform(
            post("/api/bakken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }
}
```

**Belangrijke punten:**
- `@JqwikSpringSupport` schakelt jqwik in binnen Spring test context
- Gebruik `@Autowired` zoals normaal
- Genereer HTTP request bodies met jqwik
- Test REST API's met property-based testing

**Zie:** `SnackbarControllerTest`

### 9. Shrinking - Automatisch Minimaliseren van Falende Gevallen

**Shrinking** is een van de krachtigste features van property-based testing. Als een test faalt, probeert jqwik automatisch een "kleiner" voorbeeld te vinden dat nog steeds faalt.

**Waarom is dit belangrijk?**
- In plaats van een complexe willekeurige waarde te zien die faalt, krijg je het **minimale voorbeeld**
- Maakt debugging veel eenvoudiger
- Helpt je precies begrijpen waar de bug ligt

**Voorbeeld:**

```java
@Property
@Disabled("Opzettelijk falende test om shrinking te demonstreren")
void demonstratesShrinking(@ForAll @IntRange(min = 0, max = 1000) int value) {
    // Test faalt voor waarden > 50
    assertThat(value).isLessThanOrEqualTo(50);
}
```

**Wat gebeurt er:**
1. jqwik vindt een willekeurige falende waarde (bijv. 789)
2. Probeert automatisch kleinere waarden: 394, 197, 98, 74, 62, 56, 53, 52, 51
3. Rapporteert: **51** - de kleinste waarde die nog steeds faalt

**Shrinking werkt voor:**
- **Integers**: shrinkt naar 0, dan naar kleine waarden
- **Strings**: shrinkt naar kortere strings met eenvoudige karakters
- **Lijsten**: verwijdert elementen om kleinste falende lijst te vinden
- **Domeinobjecten**: shrinkt recursief alle velden

**Voorbeeld met lijsten:**

```java
@Property
void demonstratesShrinking_Lists(@ForAll List<String> words) {
    boolean containsTarget = words.contains("aardappel");
    assertThat(containsTarget).isFalse();

    // Als test faalt met: ["foo", "bar", "aardappel", "xyz", "test"]
    // jqwik shrinkt naar: ["aardappel"] - minimale falende lijst!
}
```

**Configuratie:**

```java
@Property(shrinking = ShrinkingMode.FULL)  // Volledige shrinking (standaard)
@Property(shrinking = ShrinkingMode.BOUNDED)  // Sneller, minder grondig
@Property(shrinking = ShrinkingMode.OFF)  // Geen shrinking (voor performance)
```

**Zie:** `SnackbarServiceShrinkingDemoTest` voor uitgebreide shrinking demonstraties

### 10. Configuratie Opties

Configureer jqwik gedrag met `@PropertyDefaults`:

```java
@PropertyDefaults(
    tries = 100,                    // Aantal test runs
    generation = GenerationMode.RANDOMIZED,  // RANDOMIZED of EXHAUSTIVE
    edgeCases = EdgeCasesMode.FIRST, // Wanneer edge cases proberen
    shrinking = ShrinkingMode.FULL   // Hoe falende gevallen te minimaliseren
)
class MyTest {
    // ...
}
```

**Configuratie opties:**
- `tries`: Aantal willekeurige testgevallen (standaard: 1000)
- `generation`: Hoe data te genereren (RANDOMIZED of EXHAUSTIVE)
- `edgeCases`: Wanneer edge cases proberen (FIRST, MIXIN, of NONE)
- `shrinking`: Hoe falende gevallen te minimaliseren (FULL, OFF, of BOUNDED)

## Overzicht Testbestanden

Dit project bevat vier testbestanden die verschillende jqwik features demonstreren:

### SnackbarServiceTest
**Locatie:** `src/test/java/com/example/springjqwikdemo/service/SnackbarServiceTest.java`

**Demonstreert:**
- Basis `@Property` en `@ForAll` gebruik
- `@Size` constraints voor collecties
- Aangepaste arbitraries met `@Provide` en `@From`
- Meerdere parameter combinaties
- **Shrinking met strings en lijsten** (disabled tests - verwijder `@Disabled` om te zien)

### SnackbarServiceFrituurbaarTest
**Locatie:** `src/test/java/com/example/springjqwikdemo/service/SnackbarServiceFrituurbaarTest.java`

**Demonstreert:**
- Automatische domeinobject generatie via `DomainArbitraryProvider`
- Testen met sealed interfaces en records
- `@Example` voor deterministische testgevallen
- Collecties van domeinobjecten
- **Shrinking met domeinobjecten** (disabled tests - verwijder `@Disabled` om te zien)

### SnackbarServiceShrinkingDemoTest
**Locatie:** `src/test/java/com/example/springjqwikdemo/service/SnackbarServiceShrinkingDemoTest.java`

**Demonstreert:**
- **Shrinking** - automatisch minimaliseren van falende gevallen
- Shrinking met integers, strings, lijsten en domeinobjecten
- Shrinking configuratie (`FULL`, `BOUNDED`, `OFF`)
- Meerdere parameters tegelijk shrinken
- Praktische debugging met shrinking

**Let op:** Tests zijn `@Disabled` omdat ze opzettelijk falen om shrinking te demonstreren.

### SnackbarControllerTest
**Locatie:** `src/test/java/com/example/springjqwikdemo/controller/SnackbarControllerTest.java`

**Demonstreert:**
- `@JqwikSpringSupport` voor Spring Boot integratie
- Property-based testing van REST endpoints
- `@Assume` voor het filteren van testgevallen
- Testen van HTTP API's met gegenereerde data

## Belangrijke jqwik Annotaties

| Annotatie | Doel | Voorbeeld |
|-----------|------|-----------|
| `@Property` | Markeert een property-based test | `@Property void test(@ForAll String s)` |
| `@ForAll` | Genereert willekeurige data | `@ForAll int number` |
| `@Example` | Deterministisch testgeval | `@Example void example()` |
| `@Provide` | Aangepaste data generator | `@Provide Arbitrary<String> words()` |
| `@From` | Gebruik aangepaste arbitrary | `@From("words") String word` |
| `@Size` | Beperk collectiegrootte | `@Size(min=1, max=10) List<String>` |
| `@Label` | Leesbare testnaam | `@Label("Test beschrijving")` |

## Best Practices

1. **Beschrijf properties, niet voorbeelden**
   - Focus op wat altijd waar moet zijn
   - Laat jqwik de voorbeelden vinden

2. **Gebruik betekenisvolle property namen**
   - Noem tests naar de property die wordt getest
   - Gebruik `@Label` voor leesbare beschrijvingen

3. **Vertrouw op shrinking voor debugging**
   - Laat shrinking standaard aanstaan (`ShrinkingMode.FULL`)
   - Als een test faalt, krijg je automatisch het minimale voorbeeld
   - Dit maakt debugging veel eenvoudiger

4. **Maak aangepaste arbitraries voor domeinobjecten**
   - Gebruik `ArbitraryProvider` voor automatische generatie
   - Maakt tests leesbaarder en onderhoudbaarder

5. **Combineer property tests met voorbeelden**
   - Gebruik `@Property` voor brede dekking
   - Gebruik `@Example` voor specifieke scenario's

6. **Gebruik `@Assume` spaarzaam**
   - Geef voorkeur aan filtering in arbitraries waar mogelijk
   - `@Assume` is voor runtime condities

7. **Configureer tries op basis van testsnelheid**
   - Snelle tests: 1000+ tries
   - Langzame tests: 50-100 tries
   - Balanceer dekking vs. uitvoeringstijd

## API Documentatie

Voor gedetailleerde API documentatie, zie [docs/api.md](docs/api.md).

## Business Logica

De applicatie demonstreert jqwik testing met een eenvoudige snackbar service:

- **POST /api/bakken**: Verwerkt strings, converteert "aardappel" en "pieper" naar "friet" (aantal keer de lengte van het woord)
- **POST /api/frituren**: Verwerkt snack objecten (JSON) en genereert gefrituurde snack beschrijvingen

Het domeinmodel gebruikt sealed interfaces (`Frituurbaar`) met records voor verschillende snack types. De logica is opzettelijk eenvoudig gehouden om de focus te leggen op jqwik features in plaats van complexe business rules.

## Verder Lezen

- [jqwik Documentatie](https://jqwik.net/)
- [Property-Based Testing Gids](https://jqwik.net/docs/current/user-guide.html)
- [jqwik Voorbeelden](https://github.com/jqwik-team/jqwik/tree/main/src/test/java/net/jqwik)
