# zxing-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

ZXing Extensions — QR Code and Bar Code utilities —— 基于 ZXing Core 3.5.4 的纯 Java 扩展库，提供 QR Code、Aztec 和一维条形码的生成与解析，配套类型化请求模型与统一的输出 / 结果对象。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 能力与状态](#2-能力与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本线与分支](#10-版本线与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

`zxing-extension` 是**独立的 ZXing 扩展** —— 不依赖 Spring、Spring Boot、Javalin、Quarkus 或 DDD4J。提供三个使用模式一致的门面：

- `QrCodes` —— QR Code 生成（PNG、SVG、Base64、Data URI、彩色渐变、码眼颜色、Logo、外套壳）与解析（单码与多码）。
- `AztecCodes` —— Aztec 码生成（尺寸、纠错百分比、quiet zone）与解析。
- `BarCodes` —— 一维条形码（EAN-8/13、UPC-A/E、Code 39/93/128、ITF、Codabar）与解析。

**它不是什么**

- 不是对 ZXing 弱类型 `hints` 映射的包装 —— 公共 API 全部类型化（`QrCodeRequest`、`QrCodeStyle`、`CodeOutput` 等）。
- 不是图片后处理工具 —— 它返回 PNG 字节 / SVG 文本 / `BufferedImage`，如何处理由调用方决定。
- 不是网络抓取器 —— 核心模块从不下载远程 URL（无 SSRF 攻击面）。

**典型场景**

| 场景 | 组件的作用 |
|:---|:---|
| 为 URL / 支付载荷生成二维码 | `QrCodes.encode(...)` / `QrCodes.encoder()` + `QrCodeRequest` |
| 品牌化二维码（颜色、Logo、外套壳） | `QrCodeStyle`、`QrCodeLogo`、`QrCodeFrame` |
| 印刷级矢量输出 | 通过 `QrCodeImageFormat.SVG` 输出 SVG |
| 从图片 / 流 / 文件解析码 | `decode(byte[] | BufferedImage | File | Path | InputStream)` |
| 一张图解析多个码 | `QrCodeDecodeRequest.multiple(true)` |
| 仓储 / 零售条形码 | `BarCodes.ean13(...)` 或 `BarCodeRequest` + `BarcodeFormat` |

## 2. 能力与状态

| 能力 | 状态 | 说明 |
|:---|:---|:---|
| QR Code 生成 | 稳定 | PNG、SVG、Base64、Data URI；彩色渐变、码眼颜色、Logo、外套壳、`selfCheck`、尺寸 / 边距 / 字符集 / 纠错级别 |
| QR Code 解析 | 稳定 | 单码与多码；输入形态：`byte[]`、`BufferedImage`、`File`、`Path`、`InputStream` |
| Aztec 码 | 稳定 | 尺寸、纠错百分比、quiet zone；PNG / Base64 输出；常见输入解析 |
| 一维条形码 | 稳定 | EAN-8/13、UPC-A/E、Code 39/93/128、ITF、Codabar |
| 统一输出 / 结果 | 稳定 | 所有码制共用 `CodeOutput`（字节、图片、base64、dataUri、writeTo）与 `CodeResult`（文本、格式、原始字节、定位点、元数据） |
| 类型化请求模型 | 稳定 | `QrCodeRequest`、`QrCodeDecodeRequest`、`AztecCodeRequest`、`BarCodeRequest`、`QrCodeStyle`、`QrCodeLogo`、`QrCodeImageFormat` |
| 外套壳组合 | 稳定 | `QrCodeFrame` 组合二维码 + 文字 + 图片元素，支持层级排序 |
| JDK 8 兼容 | 稳定 | 不使用高于 Java 8 的 API；不下载远程 URL |
| 中文 Javadoc | 稳定 | 全部 29 个源文件带中文 Javadoc |
| 测试与覆盖率门禁 | 稳定 | 25 个测试类 / 188 个 `@Test` 方法；POM 内 JaCoCo 覆盖率规则 + GitHub Actions CI |

## 3. 环境要求与兼容性

| 要求 | 版本 |
|:---|:---|
| JDK | 8+（`feature/1.0.x` 分支基线） |
| Maven | 3.0+ |
| ZXing | `com.google.zxing:core` 3.5.4 |
| 其他 | `commons-lang3` 3.20.0、`slf4j-api` 2.0.18、`checker-qual`（provided） |

**版本线矩阵**

| 分支 | JDK | 版本模式 |
|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

本文档描述 `feature/1.0.x` 版本线（当前版本：`1.0.x.20260630-SNAPSHOT`）。

## 4. 架构与模块

```text
      调用方
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
   PNG 字节 | SVG 文本 | BufferedImage
```

**模块清单**

| 模块 | 类型 | 职责 |
|:---|:---|:---|
| `zxing-extension` | 单 jar（库） | 门面、编码器 / 解码器、模型、外套壳组合、图像支持 |

**包结构**（`com.google.zxing`）

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

## 5. 安装

> **假设**：制品目前通过项目私有 Maven 仓库（阿里云）与 GitHub Releases 分发；该库**尚未发布到 Maven Central**。若下列坐标无法解析，请在构建中配置私有仓库，或使用 `./mvnw install` 本地安装。

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

## 6. 快速开始

**QR Code**

```java
// 普通二维码
QrCodeOutput normal = QrCodes.encode("https://github.com/hiwepy");

// 彩色二维码
QrCodeOutput colorful = QrCodes.colorful("https://github.com/hiwepy");

// 带 Logo
BufferedImage logo = ImageIO.read(new File("logo.png"));
QrCodeOutput withLogo = QrCodes.withLogo("https://github.com/hiwepy", logo);

// 字节、Base64 和 Data URI
byte[] png = colorful.getBytes();
String base64 = colorful.base64();
String dataUri = colorful.dataUri();

// 解析
QrCodeDecodeResult result = QrCodes.decode(png);
String content = result.getText();
```

高级生成使用 `QrCodeRequest`：

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

输出 SVG：

```java
QrCodeOutput svg = QrCodes.encoder().encode(
    QrCodeRequest.builder("hello")
        .format(QrCodeImageFormat.SVG)
        .build());
```

外套壳使用 `QrCodeFrame` 组合二维码、文字和图片元素：

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

多码解析：

```java
List<QrCodeDecodeResult> results = QrCodes.decoder().decode(
    QrCodeDecodeRequest.from(image)
        .multiple(true)
        .build());
```

**Aztec**

```java
// 快捷生成与解析
CodeOutput output = AztecCodes.encode("hello-aztec");
CodeResult result = AztecCodes.decode(output.getBytes());

// 自定义尺寸、纠错百分比和 quiet zone
CodeOutput custom = AztecCodes.encode(
    AztecCodeRequest.builder("hello-aztec")
        .size(320, 280)
        .errorCorrectionPercent(40)
        .margin(4)
        .build());
```

**一维条形码**

```java
// EAN-13 快捷入口
CodeOutput ean13 = BarCodes.ean13("6901234567892");
CodeResult result = BarCodes.decode(ean13.getBytes());

// 其他一维码制
CodeOutput code128 = BarCodes.encode(
    BarCodeRequest.builder("ORDER-20260715", BarcodeFormat.CODE_128)
        .size(360, 120)
        .margin(8)
        .build());
```

**预期结果**：`encode(...)` 返回 `CodeOutput`，其 `getBytes()` / `image()` / `base64()` / `dataUri()` 持有渲染后的码；`decode(...)` 返回 `CodeResult`，含 `getText()` / `getFormat()` / `getRawBytes()` / `getPoints()` / `getMetadata()`。非法输入或超大数据载荷抛出 `CodeException` 子类（如携带 `QrCodeErrorCode` 的 `QrCodeException`）。

## 7. 配置

这是**纯库 —— 没有配置文件、没有属性前缀**。所有行为通过类型化请求 Builder 配置：

| 请求 | 关键选项 |
|:---|:---|
| `QrCodeRequest.builder(content)` | `size`、`margin`、`charset`、`errorCorrectionLevel`、`format`、`style`、`logo`、`frame`、`selfCheck` |
| `QrCodeDecodeRequest.from(...)` | `multiple`、`tryHarder`、`pureBarcode`、`alsoInverted` |
| `AztecCodeRequest.builder(content)` | `size`、`margin`、`errorCorrectionPercent`、`maxInputBytes` |
| `BarCodeRequest.builder(content, format)` | `size`、`margin` |
| `QrCodeStyle.builder()` | `foregroundColor`、`gradientEndColor`、`eyeColor`、`backgroundColor`、`cornerRadius` |
| `QrCodeLogo.builder(image)` | `size`、`padding`、`backgroundColor`、`cornerRadius` |

注意：调用方传入的 `InputStream` 和 `OutputStream` 均**不会被库关闭**。

## 8. 核心用法 / API

三个门面保持一致的使用模式：

| 门面 | 快捷生成 | 高级生成 | 解析输入 |
|:---|:---|:---|:---|
| `QrCodes` | `encode`、`colorful`、`withLogo` | 经 `encoder()` 使用 `QrCodeRequest` | `byte[]`、`BufferedImage`、`File`、`Path`、`InputStream` |
| `AztecCodes` | `encode` | `AztecCodeRequest` | 同上 |
| `BarCodes` | `ean13` | `BarCodeRequest` | 同上 |

`CodeOutput` API：

- `getBytes()` —— 渲染后的字节（PNG，或 SVG 文本）
- `image()` —— `Optional<BufferedImage>`
- `base64()` / `dataUri()` —— Base64 与 Data URI 字符串
- `writeTo(OutputStream)` —— 将字节写入流
- `getWidth()` / `getHeight()` / `getMimeType()` —— 元数据

**设计约束**

- QR 的 L/M/Q/H 与 Aztec 的纠错百分比语义不同，因此分别建模。
- 一维码制不暴露 Logo / 渐变 / 外套壳选项 —— 不暴露无效参数。
- 三种码制共享真正相同的输出与解析结果类型；弱类型 hints 不属于公共 API。
- 核心模块不下载远程 URL，避免 SSRF 风险。

## 9. 测试与构建

```bash
./mvnw clean verify     # 单元测试 + JaCoCo 覆盖率报告（全部 25 个测试类，188 个 @Test 方法）
./mvnw clean test       # 仅跑测试，跳过覆盖率门禁
```

**覆盖率策略**（规则集中在 POM 的 `jacoco-maven-plugin` 中）：

- `verify` 阶段生效的检查：整体（BUNDLE）行覆盖率 ≥ 90%（`haltOnFailure=false`）。
- 按文件规则（位于 `pluginManagement`）：常规源文件目标为 LINE = BRANCH = 100%；含防御性 catch 的文件（`CodeImageSupport`、`DefaultQrCodeEncoder`、`DefaultQrCodeDecoder`、`BarCodes`、`QrCodeFrame`、`QrCodeDecodeRequest`、`source/BufferedImageLuminanceSource`、`source/MatrixToImageWriter`）放宽至 LINE ≥ 90% / BRANCH ≥ 75%；`AztecCodes` 的 BRANCH ≥ 45%；`CodeImageSupport` 的 LINE ≥ 70%。这些 catch 分支依赖 ZXing 上游 / JDK `ImageIO` 行为，在标准测试环境下无法自然触达。

**测试覆盖分布**

| 区域 | 测试类 | 覆盖内容 |
|:---|:---|:---|
| 门面 | `QrCodesTests`、`AztecCodesTests`、`BarCodesTests`、`CodeImageSupportTests` | 5 种输入形态、所有支持格式往返、错误码触发、接口默认方法 |
| 编码器 / 解码器 | `QrCodeEncoderTests`、`DefaultQrCodeEncoderTests`、`QrCodeDecoderTests`、`DefaultQrCodeDecoderTests` | PNG / SVG、外套壳、Logo、selfCheck、CapacityExceeded、超大尺寸 |
| 模型 | `model.*Tests`（9 个类） | Builder 全部 setter、构造器校验、防御性拷贝、`base64`/`dataUri` 一致性、`CodeResult.from(Result)` |
| 外套壳 | `frame.QrCodeFrameTests`、`frame.QrCodeTextElementTests`、`frame.QrCodeImageElementTests` | 排序、zIndex、bounds 校验、BlockElement 必需 |
| 异常 | `exception.*Tests`（3 个类） | 错误码 / 构造器 / 序列化 |
| ZXing 支撑 | `source.BufferedImageLuminanceSourceTests`、`source.MatrixToImageWriterTests` | ABGR / USHORT_GRAY 转换、旋转、IO 异常 |

**CI**（`.github/workflows/maven.yml`）：push / pull_request 到 `main` 与 `release/*`（另支持 `workflow_dispatch`）时执行 `./mvnw verify`；JDK 8 + Maven 3.9；缓存 `~/.m2/repository`；上传 `target/site/jacoco` 与 `target/surefire-reports` 作为工作产物（保留 14 天）。

## 10. 版本线与分支

| 分支 | JDK | 版本模式 | 说明 |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前版本线；ZXing core 3.5.4 |
| `feature/2.0.x` | 17 | `2.0.x.*` | 下一代版本线 |
| `feature/3.0.x` | 21 | `3.0.x.*` | 最新版本线 |

- 快照版本遵循 `1.0.x.yyyyMMdd-SNAPSHOT` 命名；发布版本以 `v{version}` 打标签，并通过项目私有仓库与 GitHub Releases 分发。
- `1.0.x` 是持续维护的 JDK 8 版本线；需要更新的 JDK 基线请升级到 `feature/2.0.x`（JDK 17）或 `feature/3.0.x`（JDK 21）。

## 11. 贡献与许可

欢迎贡献 —— 请在 GitHub 上提交 Issue 或 Pull Request。

本项目基于 **Apache License, Version 2.0** 许可发布。详见 [LICENSE](./LICENSE) 文件。
