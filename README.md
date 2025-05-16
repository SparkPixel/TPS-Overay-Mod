# TPS Overlay – Forge 1.8.9

A lightweight client‑side mod that prints the **server TPS** in the upper‑left corner of your HUD.
Version **1.1** adds a keep‑alive fallback so it works on large networks such as **Hypixel SkyBlock**.

---

## 🎮 For Players

| Requirement   | Recommended version               |
| ------------- | --------------------------------- |
| **Minecraft** | **1.8.9**                         |
| **Forge**     | 11.15.1.2318 (latest 1.8.9 build) |

1. **Download** `tpsoverlay‑1.1.jar` from the Releases page.
2. **Install Forge 1.8.9** if you haven’t already.
3. Drop the JAR into your `<minecraft>/mods/` folder.
4. Launch Minecraft → *Mods* tab → *TPS Overlay* should appear.

That’s it! Join any 1.8.9‑compatible server and watch the top‑left corner.

---

## ⚙️  What the Numbers Mean

| Overlay reads | Interpretation                                             |
| ------------- | ---------------------------------------------------------- |
| **20.00**     | Perfect—server is ticking at the vanilla maximum.          |
| **15 ‑ 19**   | Minor lag spike or heavy load. Usually self‑corrects.      |
| **< 10**      | Serious server lag. Expect block delay and rubber‑banding. |

The mod averages the last 100 samples, so transient jitters won’t flicker the display.

---

## 🔍 How It Works

* **Primary sampling** – uses `S03PacketTimeUpdate` (sent every 20 ticks).
* **Fallback sampling** – uses `S00PacketKeepAlive` intervals when the server throttles `S03` (e.g., Hypixel).
* **Sliding‑window average** of 100 samples smooths both data streams.

The entire algorithm lives in **`TPSOverlayMod.java`** (≈ 180 LOC) and runs only on the client thread—no packets are sent to the server.

---

## 🙋 FAQ

> **Q: Is this bannable on Hypixel / competitive servers?**
> **A:** The mod is purely cosmetic and never modifies gameplay packets. It’s functionally similar to status HUD mods commonly whitelisted by Hypixel. As always, use at your own risk and keep the jar unmodified.

> **Q: Why does the overlay sometimes freeze at 20 TPS?**
> **A:** If the server suppresses both time‑update and keep‑alive packets during extreme lag, there’s no new data to sample. The display holds the last value until packets resume.

---

## 📜 License

This project is licensed under the **MIT License** – see `LICENSE` for details.
