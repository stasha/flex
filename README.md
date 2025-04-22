[![CircleCI](https://dl.circleci.com/status-badge/img/gh/stasha/flex/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/stasha/flex/tree/master)
[![Coverage Status](https://coveralls.io/repos/github/stasha/flex/badge.svg)](https://coveralls.io/github/stasha/flex)
# Flex: Lightweight Configuration and Localization for Java

**Flex** is a dependency-free Java library (~21KB) for **configuration** and **localization**, optimized for indie mobile games and resource-constrained environments (~2GB RAM, 60 FPS). It offers advanced **pluralization**, **nested interpolation**, **formatting**, and **asset localization** (sound, video, texture), alongside dynamic configuration, with fast performance (~2.2µs per lookup) and a minimal footprint.

## Key Features
- **Dynamic Interpolation**: Format strings at runtime (e.g., `Server at {0}`, `Welcome {0}`).
- **Pluralization with Numeric Indexing**: Handle plural forms with ranges (e.g., `{0 [0=zero dollars, 1={0}.fmt.currency dollar, 2-4=few dollars, other={0}.fmt.currency dollars]}`).
- **Ranging**: Support range-based formatting (e.g., `2-4=few dollars`).
- **Formatting**: Custom formats for numbers, dates, or assets (e.g., `{0}.fmt.currency`, `{1}.fmt.date`).
- **Arbitrary Interpolation Variables Nesting**: Nested placeholders (e.g., `{host, {env}}`, `{0}.fmt.currency`).
- **Sound, Video, Texture Localization and Pluralization**: Localize and pluralize asset paths (e.g., `{0, plural, one {sound/sfx_single.mp3} other {sound/sfx_multi.mp3}}`).
- **Store Swapping**: Instantly switch configuration or localization data for testing (e.g., English to French).
- **Dynamic Types**: Handle any type (e.g., `String`, `Integer`) via `FlexStore<String, Object>`.
- **Extensible Parsers**: Support any file format (`.properties`, JSON, etc.) with custom parsers.
- **Lightweight**: ~21KB JAR, no dependencies.
- **Mobile-Optimized**: ~1–3MB runtime memory, ~2.2µs lookups, ideal for 60 FPS.

## Installation
1. **Download**: Clone the repository or download the JAR (~21KB) from [GitHub](https://github.com/stasha/flex).
   `git clone https://github.com/stasha/flex.git`
2. **Build**: Compile using Maven/Gradle or your preferred build tool.
   
   ```bash
   cd flex
   mvn clean package
   ```
4. **Add to Project**: Include the JAR in your classpath or add as a dependency (if published).
   
    ```xml
    <dependency>
        <groupId>info.stasha</groupId>
        <artifactId>flex</artifactId>
        <version>1.0</version>
    </dependency>
    ```
## Usage
Flex provides `FlexConfiguration` for configuration and `FlexLocalization` for localization, supporting advanced interpolation, pluralization, and asset localization.

### 1. Configuration with `FlexConfiguration`
Manage `.properties` files and system properties with dynamic interpolation and type conversion.

**Example: Basic Configuration**

```java
import info.stasha.flex.FlexConfiguration;
// config.properties: message=Server running at {0} on port {1}
FlexConfiguration config = new FlexConfiguration();
config.loadConfiguration("config.properties");
String result = config.getValue("message", "localhost", 8080);
// Output: Server running at localhost on port 8080
```

**Example: Type Conversion**

```java
// config.properties: port=Port {0}
Integer port = config.getValue("port", Integer.class, "8080");
// Output: 8080
```

**Example: Nested Interpolation**

```java
// config.properties: env=prod
// server=Server at {0}, {1}
String result = config.getValue("server", "localhost", "prod");
// Output: Server at localhost, prod
```

**Example: Store Swapping**

```java
FlexStore<String, String> testStore = new FlexStoreImpl<>();
testStore.put("message", "Test server at {0}");
config.getStore().setFlexStore(testStore);
String result = config.getValue("message", "test.local");
// Output: Test server at test.local
```

### 2. Localization with `FlexLocalization`
Manage multi-language strings with pluralization, nested formatting, and asset localization.

**Example: Basic Localization**

```java
import info.stasha.flex.FlexLocalization;
// en-US.properties: welcome=Welcome {0}
FlexLocalization loc = new FlexLocalization();
loc.setLanguage("en-US");
String welcome = loc.getValue("welcome", String.class, "Alice");
// Output: Welcome Alice
```

**Example: Complex Pluralization and Formatting**

```java
// en-US.properties: balance=You have {0 [0=zero dollars, 1={0}.fmt.currency dollar, 2-4=few dollars, other={0}.fmt.currency dollars]} to the date {1}.fmt.date
String result = loc.getValue("balance", String.class, 1, new java.util.Date());
// Output: You have $1.00 dollar to the date Apr 23, 2025
result = loc.getValue("balance", String.class, 3, new java.util.Date());
// Output: You have few dollars to the date Apr 23, 2025
result = loc.getValue("balance", String.class, 10, new java.util.Date());
// Output: You have $10.00 dollars to the date Apr 23, 2025
```

**Example: Asset Localization**

```java
// en-US.properties: sfx={0, plural, one {sound/sfx_single.mp3} other {sound/sfx_multi.mp3}}
String sound = loc.getValue("sfx", String.class, 2);
// Output: sound/sfx_multi.mp3
```

**Example: Language Switching**

```java
// fr-FR.properties: balance=Vous avez {0 [0=zéro euros, 1={0}.fmt.currency euro, 2-4=quelques euros, other={0}.fmt.currency euros]} à la date {1}.fmt.date
loc.setLanguage("fr-FR");
String result = loc.getValue("balance", String.class, 1, new java.util.Date());
// Output: Vous avez €1,00 euro à la date 23 avr. 2025
```

**Example: Store Swapping**

```java
FlexStore<String, String> testStore = new FlexStoreImpl<>();
testStore.put("welcome", "Hola {0}");
loc.getStore().setFlexStore(testStore);
String welcome = loc.getValue("welcome", String.class, "Alice");
// Output: Hola Alice
```

### 3. Custom Parsers
Extend `FlexDataParser` for additional formats (e.g., JSON, YAML).

**Example: JSON Parser**

```java
public class JsonParser implements FlexDataParser<String> {
    private FlexDataLoader<String, String> loader;
    @Override
    public void setLoader(FlexDataLoader<String, String> loader) {
        this.loader = loader;
    }
    @Override
    public FlexStore<String, String> load(String source) throws IOException {
        String json = loader.load(source);
        FlexStore<String, String> store = new FlexStoreImpl<>();
        // Parse JSON (e.g., {"balance": "You have {0 [0=zero dollars, 1={0}.fmt.currency dollar, 2-4=few dollars, other={0}.fmt.currency dollars]} to the date {1}.fmt.date"}) to store
        // Implementation depends on JSON library (optional)
        return store;
    }
}
FlexConfiguration config = new FlexConfiguration();
config.setParser(new JsonParser());
config.loadConfiguration("config.json");
String result = config.getValue("message", "localhost");
// Output: Server at localhost
```

## Performance
- **Lookups**: ~2.2µs per `getValue`, supports 60 FPS (~16.6ms frame budget).
- **Memory**: ~1–3MB runtime for 1,000–10,000 entries, fits ~2GB RAM.
- **Loading**: ~50–500ms for initial load (one-time).

## Size
- **JAR**: ~21KB, ideal for mobile (compared to ICU4J ~20MB, Spring ~5–10MB).
- **No Dependencies**: Pure Java, no external libraries.

## Use Cases
- **Indie Mobile Games**: Configure settings (e.g., `server.url`) and localize strings/assets (e.g., `sfx`) with minimal footprint.
- **Embedded Systems**: Manage IoT device configurations and localized assets.
- **Unit Testing**: Swap stores to test configurations or languages.

## Comparison to Alternatives
| Feature/Library             | Flex | ICU4J | ResourceBundle | Spring Framework |
|-----------------------------|-------------|-------|----------------|------------------|
| Complex Pluralization        | Yes         | Yes   | Limited        | Yes              |
| Nested Interpolation        | Yes         | No    | No             | Partial          |
| Asset Localization          | Yes         | No    | No             | No               |
| Store Swapping              | Seamless    | Limited | Limited        | Complex          |
| Dynamic Types               | Yes         | Limited | Limited        | Limited          |
| Size (KB)                   | ~21.5       | ~20,000 | ~50            | ~5,000–10,000    |
| Mobile Suitability          | Excellent   | Poor    | Excellent      | Poor             |

**Why Choose Flex?**
- Smallest footprint (~21KB) and fastest lookups (~2.2µs).
- Advanced pluralization, nesting, formatting, and asset localization.
- Seamless store swapping for testing.

## Contributing
Contributions are welcome! Fork the repository, submit pull requests, or open issues at [GitHub](https://github.com/stasha/flex).

1. **Report Bugs**: Create an issue with details.
2. **Add Features**: Propose new parsers or interpolation patterns.
3. **Improve Docs**: Enhance examples or Javadoc.

## License
MIT License. See [LICENSE](LICENSE) for details.

## Contact
- **Author**: Stasha
- **Repository**: [github.com/stasha/flex](https://github.com/stasha/flex)
- **Issues**: [github.com/stasha/flex/issues](https://github.com/stasha/flex/issues)
