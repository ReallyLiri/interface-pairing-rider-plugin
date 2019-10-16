# Interface Pairing JetBrains Rider Plugin

<img src="src/main/resources/META-INF/pluginIcon.svg" alt="drawing" width="40"/>

[![badge](https://img.shields.io/jetbrains/plugin/v/13185-interface-pairing.svg?label=Rider%20plugin)](https://plugins.jetbrains.com/plugin/13185-interface-pairing)

https://plugins.jetbrains.com/plugin/13185-interface-pairing

Pairing C# interface files with implementations files in solution explorer for a better display experience.

![preview](https://i.imgur.com/4sKj5GF.png)

## Build

```bash
gradle build
```

Plugin artifact will be written to

`build/libs/interface-pairing-rider-plugin-<version>.jar`

## Publish

```bash
export ORG_GRADLE_PROJECT_intellijPublishToken="..."
gradle publishPlugin
```

## Debug

Open in IntelliJ to simply run and debug in a real Rider instance.
