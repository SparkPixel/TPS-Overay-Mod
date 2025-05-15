# TPS Overlay Mod

![Minecraft 1.8.9](https://img.shields.io/badge/Minecraft-1.8.9-brightgreen)
![Forge](https://img.shields.io/badge/Forge-11.15.1.2318-yellow)

A lightweight Minecraft Forge mod that displays the server's ticks-per-second (TPS) in the in-game HUD. Ideal for monitoring server performance during gameplay.

## Features

* **Real-time TPS calculation** by intercepting `S03PacketTimeUpdate` packets.
* **Sliding-window smoothing** over the last 20 ticks to provide a stable TPS reading (capped at 20.00).
* **Proxy-server override**: forces a 20.00 TPS display on Hypixel and PikaNetwork lobbies (which throttle time updates).
* **Single-player detection**: automatically hides the overlay when running an integrated (single-player) server.

## Requirements

* **Minecraft** 1.8.9
* **Forge** 1.8.9-11.15.1.2318-1.8.9
* **Java** 8

## Installation

### For Players

1. Download the latest `tpsoverlay-<version>.jar` from the [Releases](https://github.com/yourusername/tpsoverlay/releases).
2. Move the `.jar` file into your Minecraft `mods/` folder.
3. Launch Minecraft with the **Forge 1.8.9** profile.
4. Join a multiplayer server and watch the TPS readout in the top-left corner.

## Usage

* The TPS overlay appears automatically when connected to any **remote** server.
* On **Hypixel** or **PikaNetwork** lobbies, the overlay is forced to show **20.00** TPS due to proxy throttling.
  \$1- On private SMPs (vanilla, Spigot/Paper, Forge), you’ll see real \~20.00 TPS readings.
