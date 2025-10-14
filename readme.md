# 🏰 Siege — Attackers vs Defenders

![Java](https://img.shields.io/badge/Java-1.8%2B-blue?logo=java&logoColor=white)
![Spigot](https://img.shields.io/badge/Spigot-1.8.8%2B-brightgreen?logo=spigot)
![Status](https://img.shields.io/badge/Status-Experimental-orange)
![License](https://img.shields.io/badge/License-MIT-yellow?logo=opensourceinitiative)

**A fast-paced PvP minigame for Spigot servers**  
Two sides, one king. Defenders must build and protect. Attackers must break through and slay the king.
> ⚠️ This plugin is experimental and intended for testing, prototyping, or forking — not production use.

![Siege Banner](https://placehold.co/800x200/8B0000/ffffff?text=SIEGE+GAME+PLUGIN&font=montserrat)

---

## 🕹️ Gameplay Overview

- 👑 **Defenders**
    - Start inside the keep.
    - Loot chests for weapons, armor, and resources.
    - Build defensive walls within a restricted **cuboid region**.
    - Protect the **King** at all costs.
    - Can respawn after death.

- ⚔️ **Attackers**
    - Begin outside the castle.
    - Loot chests for weapons, armor, and tools.
    - Break through walls and **invade** after the timer ends.
    - Cannot respawn — every life counts.
    - Victory by **killing the King**.

- ⏳ **Match Flow**
    1. **Prep Phase** (5 min): Attackers loot, defenders build.
    2. **Siege Phase**: Attackers advance and attempt to breach.
    3. **End**: Game ends when the King dies, every attackers is slayed or time runs out.

---

## 🧱 Key Features

- 🧭 **Cuboid-Restricted Building** — Defenders can only build inside a clearly visualized wall zone.
- 🧍 **Blacksmith NPC** — A custom NMS entity acts as the blacksmith. He can upgrade weapons and armors.
- 🧠 **Dynamic Loot System** — Randomized, weighted loot tables for unique game experiences.
- 💥 **Attackers vs Defenders** — Simple but tense team gameplay.
- 🌍 **Protected World** — No mob spawns, no weather, no random griefing. Pure controlled gameplay.
- 🧊 **Particle Wall Preview** — Real-time visualization of the building boundary.

---

## 🧠 Tech Behind the Game

- 🛰️ **NMS & Packet Interception**
    - Used to create NPCs and handle interactions efficiently.
    - Allows more precise control than Bukkit events alone.

- 📦 **Cuboid Region Logic**
    - Clean boundary calculations.
    - Visual feedback with particles.
    - Enforced building restrictions based on team & phase.

- 🧱 **Custom Effects & UI**
    - Action bar messages, titles, and particle effects for immersion.
    - World control (no weather, explosions, or mobs) ensures consistent gameplay.

---

## 🖼️ Gameplay Showcase

<p align="center">
  <img src="https://placehold.co/600x150/8B0000/ffffff?text=Border+Delimitations" alt="Border Delimitations" width="600"/>
</p>

<p align="center">
  <img src="src/main/resources/images/border_particle_2.gif" alt="Border Particle Preview" width="600"/>
</p>

<p align="center">
  <img src="https://placehold.co/600x150/8B0000/ffffff?text=Custom+Upgrade+NPC" alt="Custom Upgrade NPC Banner" width="600"/>
</p>

<p align="center">
  <img src="src/main/resources/images/upgrade_loot_npc.png" alt="Upgrade NPC Screenshot" width="600"/>
</p>

<p align="center">
  <img src="https://placehold.co/600x150/8B0000/ffffff?text=Custom+Loot+Tables" alt="Custom Loot Tables Banner" width="600"/>
</p>

<p align="center">
  <img src="src/main/resources/images/custom_loot_table.png" alt="Custom Loot Table Screenshot" width="600"/>
</p>

---

## 🧭 Commands (Example)

| Command                | Description                            |
|-------------------------|------------------------------------------|
| `/siege start`          | Starts a new siege game.               |
| `/siege stop`           | Forces the game to stop.               |
| `/siege team join <t>`  | Join a team (`attacker` / `defender`). |
| `/siege king`           | Spawn or manage the King NPC.         |

---

## 🧪 Development Notes

- This project is a **proof of concept**.
- Not a fully featured minigame — perfect for:
    - Experimenting with **NMS**, packets & custom entities.
    - Extending the gameplay loop.
    - Forking and customizing.

> Contributions and forks are welcome 🤝
or extend at your own risk. Ideal for testing, not production servers.