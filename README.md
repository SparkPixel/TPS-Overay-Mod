# TPS Overlay (Forge 1.8.9)

A **client‑side** quality‑of‑life mod that prints the current **server TPS** (ticks per second) in the top‑left corner of your HUD.
Runs on vanilla servers, Spigot/Paper forks, and large proxy‑based networks (e.g. Hypixel) without any server‑side installation.

> **Latest release:** `v1.1`
> **Minecraft:** `1.8.9`
> **Forge:** `11.15.1.2318` (any 1.8.9 Forge build ≥ 11.15.1.1722 should work)

---

## 🎮  Installation (for players)

| Requirement     | Recommended version  | Notes                                                                                     |
| --------------- | -------------------- | ----------------------------------------------------------------------------------------- |
| **Minecraft**   | 1.8.9                | The mod is hard‑pinned to 1.8.9; it will not load on newer versions.                      |
| **Forge**       | 11.15.1.2318         | Latest 1.8.9 installer from [files.minecraftforge.net](https://files.minecraftforge.net). |
| **TPS Overlay** | `tpsoverlay‑1.1.jar` | Grab it from the [Releases](../../releases) page.                                         |

1. Download **`tpsoverlay‑1.1.jar`**.
2. Install **Forge 1.8.9** if you have not already.
3. Drop the jar into your `<minecraft>/mods/` directory.
4. Start Minecraft → *Mods* → verify **“TPS Overlay”** is listed.

That’s it! Join any 1.8.9‑compatible server and watch the number appear in the upper‑left corner.

---

## ⚙️  Understanding the reading

| On‑screen value | What it usually means                                               |
| --------------- | ------------------------------------------------------------------- |
| **20.00 TPS**   | Perfect: the server is running at the vanilla maximum.              |
| **15 – 19 TPS** | Minor lag spike or heavy load. Usually self‑corrects.               |
| **< 10 TPS**    | Severe lag. Expect block delay, rubber‑banding, and queue back‑ups. |

The mod averages the **last 100 samples**, so brief spikes do not cause the number to flicker.

---

## 🔍  How it works (technical overview)

| Data path    | Packet(s) used                         | When it is active                                             |
| ------------ | -------------------------------------- | ------------------------------------------------------------- |
| **Primary**  | `S03PacketTimeUpdate` (every 20 ticks) | Most dedicated servers & SSP.                                 |
| **Fallback** | `S00PacketKeepAlive` interval          | When the server throttles or omits `S03` (common on Hypixel). |

For each sample:

```java
TPS = ticksAdvanced / realTimeElapsed;
if (TPS > 20) TPS = 20;      // hard cap
add to 100‑sample rolling window;

// display = average(window);
```

All logic lives in [`TPSOverlayMod.java`](src/main/java/com/example/tpsoverlay/TPSOverlayMod.java) and executes **client‑side only**. The mod never sends packets or writes to disk.

---

## 🛠  Building from source

> Java 8 and an ancient Gradle wrapper are required because ForgeGradle 2.x predates Gradle 3.

```bash
git clone https://github.com/yourname/tpsoverlay.git
cd tpsoverlay
./gradlew setupDecompWorkspace
./gradlew build
```

| Output                                | Purpose                                           |
| ------------------------------------- | ------------------------------------------------- |
| `build/libs/tpsoverlay‑1.1.jar`       | Obfuscated jar – drop in `mods/` (players)        |
| `build/libs/tpsoverlay‑1.1‑deobf.jar` | De‑obfuscated jar – attach in an IDE (developers) |

---

## 🙋  FAQ

**Is this bannable on competitive servers?**

> The mod is purely cosmetic and leaves every outbound packet untouched. It belongs to the same category as minimaps and HUD info mods that are widely accepted on Hypixel and similar networks. *No bans have been reported*, but ultimately each server’s rules apply—use at your own risk.

**Why does the overlay freeze at 20 TPS on some servers?**

> A few anti‑cheat forks suppress both `TimeUpdate` **and** `KeepAlive` when the t‑server hitches. With no fresh packets the overlay keeps the last value until traffic resumes.

**Will you port it to modern Minecraft versions?**

> There are no plans. Newer versions already expose server TPS through `/forge tps` or server‑side mods; client‑side overlays can read that data instead of inference.

---
