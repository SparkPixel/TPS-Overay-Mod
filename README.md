# TPS Overlay (Forge 1.8.9)

[![GitHub release](https://img.shields.io/github/v/release/SparkPixel/TPS-Overay-Mod?include_prereleases\&label=latest)](https://github.com/SparkPixel/TPS-Overay-Mod/releases)
[![GitHub downloads](https://img.shields.io/github/downloads/SparkPixel/TPS-Overay-Mod/latest/total)](https://github.com/SparkPixel/TPS-Overay-Mod/releases)
[![License: MIT](https://img.shields.io/github/license/SparkPixel/TPS-Overay-Mod)](LICENSE)

A **client‑side** quality‑of‑life mod that prints the current **server TPS** (ticks per second) in the top‑left corner of your HUD.
Runs on vanilla servers, Spigot/Paper forks, and large proxy‑based networks (e.g. Hypixel) with **no server‑side installation required**.

> **Latest version:** `v1.1` · **Minecraft:** `1.8.9` · **Forge:** `11.15.1.2318` (any 1.8.9 Forge ≥ 11.15.1.1722 should work)

---

## 🎮 Installation

| Requirement     | Recommended version                                                                  | Notes                                                                |
| --------------- | ------------------------------------------------------------------------------------ | -------------------------------------------------------------------- |
| **Minecraft**   | 1.8.9                                                                                | The mod is hard‑pinned to 1.8.9; it will not load on newer versions. |
| **Forge**       | 11.15.1.2318                                                                         | Grab the installer from the official Forge site.                     |
| **TPS Overlay** | [`tpsoverlay‑1.1.jar`](https://github.com/SparkPixel/TPS-Overay-Mod/releases/latest) | Download from the *Releases* page.                                   |

1. Drop the JAR into your `<minecraft>/mods/` folder.
2. Launch Minecraft with the **Forge 1.8.9** profile.
3. Open the *Mods* menu – **TPS Overlay** should appear in the list.

Join any 1.8.9‑compatible server and watch the number appear in the upper‑left corner of your screen.

---

## ⚙️ Reading the overlay

| On‑screen value | Meaning                                                    |
| --------------- | ---------------------------------------------------------- |
| **20.00 TPS**   | Perfect performance (vanilla maximum).                     |
| **15 – 19 TPS** | Temporary lag spike or heavy load – usually self‑corrects. |
| **< 10 TPS**    | Severe lag. Expect block delay, rubber‑banding, etc.       |

The display shows a rolling average of **100 samples**, so momentary jitters do not flicker the value.

---

## 🔍 Under the hood

| Path         | Packet(s)                              | When it is used                                               |
| ------------ | -------------------------------------- | ------------------------------------------------------------- |
| **Primary**  | `S03PacketTimeUpdate` (every 20 ticks) | Most servers + single‑player.                                 |
| **Fallback** | `S00PacketKeepAlive` interval          | When the server throttles or omits `S03` (common on Hypixel). |

```java
// simplified core
TPS = ticksAdvanced / realTimeElapsed;
if (TPS > 20) TPS = 20; // hard cap
addSample(TPS);         // sliding‑window average (100)
```

All logic lives in [`TPSOverlayMod.java`](src/main/java/com/example/tpsoverlay/TPSOverlayMod.java) and runs **client‑side only** – the mod never sends packets.

---

## 🛠 Building from source

> Requires **Java 8** and the legacy **ForgeGradle 2.x** tool‑chain that ships with the repo.

```bash
git clone https://github.com/SparkPixel/TPS-Overay-Mod.git
cd TPS-Overay-Mod
./gradlew setupDecompWorkspace
./gradlew build
```

| Output                                | Use‑case                                                   |
| ------------------------------------- | ---------------------------------------------------------- |
| `build/libs/tpsoverlay‑1.1.jar`       | Obfuscated jar – drop into `mods/` (players)               |
| `build/libs/tpsoverlay‑1.1‑deobf.jar` | De‑obfuscated jar – add to your IDE workspace (developers) |

---

## 🙋 FAQ

> **Is this bannable on competitive servers?**
> The mod is purely cosmetic and leaves every outbound packet untouched. It belongs to the same category as minimaps and HUD info mods that are widely accepted on Hypixel and similar networks. *No bans have been reported*, but each server’s rules apply—use at your own risk.

> **Why does the overlay freeze at 20 TPS on some servers?**
> A few anti‑cheat forks suppress both `TimeUpdate` **and** `KeepAlive` during extreme lag. With no fresh packets, the overlay holds the last value until traffic resumes.

> **Will you port it to modern Minecraft versions?**
> There are no plans. Newer versions expose TPS through commands or server‑side mods—client overlays can simply read that data instead of inferring it.

---
