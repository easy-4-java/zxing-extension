# zxing-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

ZXing Extensions — QR Code and Bar Code utilities — a pure-Java extension library built on ZXing Core 3.5.4 for generating and decoding QR Codes, Aztec codes and 1D barcodes, with typed request models and unified output/result objects.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`zxing-extension` is a **standalone ZXing extension** — it does not depend on Spring, Spring Boot, Javalin, Quarkus or DDD4J. It offers three facades with a consistent usage pattern:

- `QrCodes` — QR Code generation (PNG, SVG, Base64, Data URI, gradients, eye colors, Logo, outer frames) and decoding (single and multiple codes).
- `AztecCodes` — Aztec code generation (size, error-correction percent, quiet zone) and decoding.
- `BarCodes` — 1D barcodes (EAN-8/13, UPC-A/E, Code 39/93/128, ITF, Codabar) and decoding.

**What it is not**

- Not a wrapper around weak-typed ZXing `hints` maps — the public API is typed (`QrCodeRequest`, `QrCodeStyle`, `CodeOutput`, ...).
- Not an image post-processor — it returns PNG bytes / SVG text / `BufferedImage` and lets the caller decide what to do with them.
- Not a network fetcher — the core never downloads remote URLs (no SSRF surface).

**Typical scenarios**

| Scenario | How this component helps |
|:---|:---|
| Generate QR codes for URLs / payment payloads | `QrCodes.encode(...)` / `QrCodes.encoder()` with `QrCodeRequest` |
| Branded QR (colors, logo, outer frame) | `QrCodeStyle`, `QrCodeLogo`, `QrCodeFrame` |
| Print-ready vector output | SVG output via `QrCodeImageFormat.SVG` |
| Decode codes from images / streams / files | `decode(byte[] | BufferedImage | File | Path | InputStream)` |
| Scan multiple codes from one image | `QrCodeDecodeRequest.multiple(true)` |
| Warehouse / retail barcodes | `BarCodes.ean13(...)` or `BarCodeRequest` with `BarcodeFormat` |

## 2. Features & Status

| Capability | Status | Description |
|:---|:---|:---|
| QR Code generation | Stable | PNG, SVG, Base64, Data URI; colorful gradients, eye color, Logo, outer frames, `selfCheck`, size / margin / charset / error-correction level |
| QR Code decoding | Stable | Single and multiple codes; inputs: `byte[]`, `BufferedImage`, `File`, `Path`, `InputStream` |
| Aztec codes | Stable | Size, error-correction percent, quiet zone; PNG / Base64 output; common-input decoding |
| 1D barcodes | Stable | EAN-8/13, UPC-A/E, Code 39/93/128, ITF, Codabar |
| Unified output / result | Stable | All code types share `CodeOutput` (bytes, image, base64, dataUri, writeTo) and `CodeResult` (text, format, raw bytes, points, metadata) |
| Typed request models | Stable | `QrCodeRequest`, `QrCodeDecodeRequest`, `AztecCodeRequest`, `BarCodeRequest`, `QrCodeStyle`, `QrCodeLogo`, `QrCodeImageFormat` |
| Outer frame composition | Stable | `QrCodeFrame` combines QR + text + image elements with z-ordering |
| JDK 8 compatible | Stable | No APIs above Java 8; no remote URL downloads |
| Chinese Javadoc | Stable | All 29 source files carry Chinese Javadoc |
| Test & coverage gate | Stable | 25 test classes / 188 `@Test` methods; JaCoCo coverage rules in the POM + GitHub Actions CI |

## 3. Requirements & Compatibility

| Requirement | Version |
|:---|:---|
| JDK | 8+ (baseline of the `feature/1.0.x` branch) |
| Maven | 3.0+ |
| ZXing | `com.google.zxing:core` 3.5.4 |
| Others | `commons-lang3` 3.20.0, `slf4j-api` 2.0.18, `checker-qual` (provided) |

**Version line matrix**

| Branch | JDK | Version pattern |
|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

This document describes the `feature/1.0.x` line (current version: `1.0.x.20260630-SNAPSHOT`).

## 4. Architecture & Modules

```text
      Caller
        |
   +----+----------------+----------------+
   |    |                |                |
 QrCodes             AztecCodes       BarCodes
   |                    |                |
   +--------+-----------+--------+-------+
            |                    |
  DefaultQrCodeEncoder     ZXing MultiFormatWriter
  DefaultQrCodeDecoder     ZXing MultiFormatReader
            |
   CodeOutput / CodeResult
            |
   PNG bytes | SVG text | BufferedImage
```

**Module list**

| Module | Type | Responsibility |
|:---|:---|:---|
| `zxing-extension` | Single jar (library) | Facades, encoders/decoders, models, frame composition, image support |

**Package layout** (`com.google.zxing`)

```text
com.google.zxing
├── QrCodes.java
├── AztecCodes.java
├── BarCodes.java
├── DefaultQrCodeEncoder.java
├── DefaultQrCodeDecoder.java
├── QrCodeEncoder.java
├── QrCodeDecoder.java
├── CodeImageSupport.java
├── model/
│   ├── CodeOutput.java
│   ├── CodeResult.java
│   ├── QrCodeOutput.java
│   ├── QrCodeDecodeResult.java
│   ├── QrCodeRequest.java
│   ├── QrCodeDecodeRequest.java
│   ├── AztecCodeRequest.java
│   ├── BarCodeRequest.java
│   ├── QrCodeStyle.java
│   ├── QrCodeLogo.java
│   └── QrCodeImageFormat.java
├── frame/
│   ├── QrCodeFrame.java
│   ├── QrCodeFrameElement.java
│   ├── QrCodeBlockElement.java
│   ├── QrCodeTextElement.java
│   └── QrCodeImageElement.java
├── source/
│   ├── BufferedImageLuminanceSource.java
│   └── MatrixToImageWriter.java
└── exception/
    ├── CodeException.java
    ├── QrCodeException.java
    └── QrCodeErrorCode.java
```

## 5. Installation

> **Assumption**: artifacts are currently distributed through the project's private Maven repository (Aliyun) and GitHub Releases; the library is **not yet published to Maven Central**. If the coordinates below cannot be resolved, either add the private repository to your build or install locally with `./mvnw install`.

**Maven**

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>zxing-extension</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

**Gradle**

```gradle
implementation 'io.github.easy4j:zxing-extension:1.0.x.20260630-SNAPSHOT'
```

## 6. Quick Start

**QR Code**

```java
// Plain QR
QrCodeOutput normal = QrCodes.encode("https://github.com/hiwepy");

// Colorful QR
QrCodeOutput colorful = QrCodes.colorful("https://github.com/hiwepy");

// With Logo
BufferedImage logo = ImageIO.read(new File("logo.png"));
QrCodeOutput withLogo = QrCodes.withLogo("https://github.com/hiwepy", logo);

// Bytes, Base64 and Data URI
byte[] png = colorful.getBytes();
String base64 = colorful.base64();
String dataUri = colorful.dataUri();

// Decode
QrCodeDecodeResult result = QrCodes.decode(png);
String content = result.getText();
```

Advanced generation with `QrCodeRequest`:

```java
QrCodeOutput output = QrCodes.encoder().encode(
    QrCodeRequest.builder("hello")
        .size(430, 430)
        .margin(2)
        .errorCorrectionLevel(ErrorCorrectionLevel.H)
        .style(QrCodeStyle.builder()
            .foregroundColor(new Color(0, 122, 98))
            .gradientEndColor(new Color(69, 54, 143))
            .eyeColor(new Color(24, 45, 110))
            .build())
        .logo(QrCodeLogo.builder(logo).size(56, 28).build())
        .selfCheck(true)
        .build());
```

SVG output:

```java
QrCodeOutput svg = QrCodes.encoder().encode(
    QrCodeRequest.builder("hello")
        .format(QrCodeImageFormat.SVG)
        .build());
```

Outer frame with `QrCodeFrame` — combine QR, text and image elements:

```java
QrCodeFrame frame = QrCodeFrame.builder(420, 520)
    .addElement(QrCodeTextElement.builder("扫码查看详情")
        .bounds(100, 30, 260, 40)
        .font("SansSerif", 28, true)
        .build())
    .addElement(QrCodeBlockElement.builder()
        .x(50).y(100).width(320).height(320).zIndex(1)
        .build())
    .build();
```

Multiple-code decoding:

```java
List<QrCodeDecodeResult> results = QrCodes.decoder().decode(
    QrCodeDecodeRequest.from(image)
        .multiple(true)
        .build());
```

**Aztec**

```java
// Convenience encode & decode
CodeOutput output = AztecCodes.encode("hello-aztec");
CodeResult result = AztecCodes.decode(output.getBytes());

// Custom size, error-correction percent and quiet zone
CodeOutput custom = AztecCodes.encode(
    AztecCodeRequest.builder("hello-aztec")
        .size(320, 280)
        .errorCorrectionPercent(40)
        .margin(4)
        .build());
```

**1D barcodes**

```java
// EAN-13 convenience
CodeOutput ean13 = BarCodes.ean13("6901234567892");
CodeResult result = BarCodes.decode(ean13.getBytes());

// Other 1D formats
CodeOutput code128 = BarCodes.encode(
    BarCodeRequest.builder("ORDER-20260715", BarcodeFormat.CODE_128)
        .size(360, 120)
        .margin(8)
        .build());
```

**Expected results**: `encode(...)` returns a `CodeOutput` whose `getBytes()` / `image()` / `base64()` / `dataUri()` hold the rendered code; `decode(...)` returns a `CodeResult` with `getText()` / `getFormat()` / `getRawBytes()` / `getPoints()` / `getMetadata()`. Invalid input or oversized payloads raise `CodeException` subtypes (e.g. `QrCodeException` with `QrCodeErrorCode`).

## 7. Configuration

This is a **pure library with no configuration file and no property prefix**. All behavior is configured through the typed request builders:

| Request | Key options |
|:---|:---|
| `QrCodeRequest.builder(content)` | `size`, `margin`, `charset`, `errorCorrectionLevel`, `format`, `style`, `logo`, `frame`, `selfCheck` |
| `QrCodeDecodeRequest.from(...)` | `multiple`, `tryHarder`, `pureBarcode`, `alsoInverted` |
| `AztecCodeRequest.builder(content)` | `size`, `margin`, `errorCorrectionPercent`, `maxInputBytes` |
| `BarCodeRequest.builder(content, format)` | `size`, `margin` |
| `QrCodeStyle.builder()` | `foregroundColor`, `gradientEndColor`, `eyeColor`, `backgroundColor`, `cornerRadius` |
| `QrCodeLogo.builder(image)` | `size`, `padding`, `backgroundColor`, `cornerRadius` |

Note: `InputStream` and `OutputStream` passed by the caller are **never closed by the library**.

## 8. Core Usage / API

All three facades share the same usage pattern:

| Facade | Convenience generation | Advanced generation | Decode inputs |
|:---|:---|:---|:---|
| `QrCodes` | `encode`, `colorful`, `withLogo` | `QrCodeRequest` via `encoder()` | `byte[]`, `BufferedImage`, `File`, `Path`, `InputStream` |
| `AztecCodes` | `encode` | `AztecCodeRequest` | same as above |
| `BarCodes` | `ean13` | `BarCodeRequest` | same as above |

`CodeOutput` API:

- `getBytes()` — rendered bytes (PNG, or SVG text)
- `image()` — `Optional<BufferedImage>`
- `base64()` / `dataUri()` — Base64 and Data URI strings
- `writeTo(OutputStream)` — stream out the bytes
- `getWidth()` / `getHeight()` / `getMimeType()` — metadata

**Design constraints**

- QR's L/M/Q/H and Aztec's error-correction percent are different semantics and are modeled separately.
- 1D formats expose no Logo / gradient / frame options — invalid parameters are not exposed.
- All three code types share genuinely identical output and decode result types; weak-typed hints are not part of the public API.
- The core never downloads remote URLs, avoiding SSRF risks.

## 9. Testing & Build

```bash
./mvnw clean verify     # unit tests + JaCoCo coverage report (all 25 test classes, 188 @Test methods)
./mvnw clean test       # tests only, skip the coverage gate
```

**Coverage strategy** (rules defined in the POM's `jacoco-maven-plugin`):

- Active check at `verify`: bundle-level LINE coverage ≥ 90% (`haltOnFailure=false`).
- Per-file rules (in `pluginManagement`): all regular source files target LINE = BRANCH = 100%; the files with defensive catch blocks (`CodeImageSupport`, `DefaultQrCodeEncoder`, `DefaultQrCodeDecoder`, `BarCodes`, `QrCodeFrame`, `QrCodeDecodeRequest`, `source/BufferedImageLuminanceSource`, `source/MatrixToImageWriter`) are relaxed to LINE ≥ 90% / BRANCH ≥ 75%; `AztecCodes` BRANCH ≥ 45%; `CodeImageSupport` LINE ≥ 70%. Those catch branches depend on ZXing upstream / JDK `ImageIO` behavior that cannot be reached naturally in the standard test environment.

**Test coverage by area**

| Area | Test classes | Coverage |
|:---|:---|:---|
| Facades | `QrCodesTests`, `AztecCodesTests`, `BarCodesTests`, `CodeImageSupportTests` | 5 input forms, round-trips for all supported formats, error codes, interface default methods |
| Encoders / decoders | `QrCodeEncoderTests`, `DefaultQrCodeEncoderTests`, `QrCodeDecoderTests`, `DefaultQrCodeDecoderTests` | PNG / SVG, outer frames, Logo, selfCheck, CapacityExceeded, oversized sizes |
| Models | `model.*Tests` (9 classes) | Builder setters, constructor validation, defensive copies, `base64`/`dataUri` consistency, `CodeResult.from(Result)` |
| Frames | `frame.QrCodeFrameTests`, `frame.QrCodeTextElementTests`, `frame.QrCodeImageElementTests` | Ordering, zIndex, bounds validation, required block element |
| Exceptions | `exception.*Tests` (3 classes) | Error codes / constructors / serialization |
| ZXing support | `source.BufferedImageLuminanceSourceTests`, `source.MatrixToImageWriterTests` | ABGR / USHORT_GRAY conversions, rotation, IO exceptions |

**CI** (`.github/workflows/maven.yml`): runs `./mvnw verify` on push / pull_request to `main` and `release/*` (plus `workflow_dispatch`); JDK 8 + Maven 3.9; caches `~/.m2/repository`; uploads `target/site/jacoco` and `target/surefire-reports` as artifacts (14-day retention).

## 10. Versioning & Branches

| Branch | JDK | Version pattern | Notes |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` | Current line; ZXing core 3.5.4 |
| `feature/2.0.x` | 17 | `2.0.x.*` | Next generation line |
| `feature/3.0.x` | 21 | `3.0.x.*` | Latest line |

- Snapshot versions follow the `1.0.x.yyyyMMdd-SNAPSHOT` scheme; releases are tagged `v{version}` and published through the project's private repository and GitHub Releases.
- The `1.0.x` line is the actively maintained JDK 8 line; upgrade to `feature/2.0.x` (JDK 17) or `feature/3.0.x` (JDK 21) for newer JDK baselines.

## 11. Contributing & License

Contributions are welcome — please open an issue or a pull request on GitHub.

This project is licensed under the **Apache License, Version 2.0**. See the [LICENSE](./LICENSE) file for details.
