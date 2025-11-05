<div align="center">

<!-- Replace this with a custom banner for your project -->
![Siege Banner](https://placehold.co/800x200/d35400/ffffff?text=Siege&font=montserrat)

![Java](https://img.shields.io/badge/Java-8-blue?logo=openjdk&logoColor=white)
![Spigot API](https://img.shields.io/badge/Spigot-1.8.8-orange?logo=spigotmc)
![License](https://img.shields.io/badge/License-MIT-yellow?logo=opensourceinitiative)

</div>

**Siege** is my take on the classic attack/defend gamemode, built from the ground up as a complete Minecraft minigame.

---

## Showcase: The Gameplay Experience

Siege is a round-based game pitting two teams against each other: the **Defenders**, who must protect their King, and the **Attackers**, who must eliminate him. The game is split into distinct phases, each with its own unique mechanics and objectives.

| Gameplay Moment                                                   | Description                                                                                                                                                                                                                                 |
|:------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![Defender Building](.github/assets/defender_building.gif)        | **Phase 1: The Build-Up.** Defenders frantically build their masterpiece inside a designated zone, marked out by a real-time particle wall. It's a race against the clock to create the most impenetrable fortress possible.                |
| ![Breaking Though Wall](.github/assets/breaking_through_wall.gif) | **Phase 2: The Assault!** The fight is on. Attackers must breach the walls and hunt the King. While Defenders can respawn, the Attackers and the King only have one life each, making every encounter a high-stakes moment.                 
| ![NPC Interaction](.github/assets/npc_interaction.gif)            | **Custom NPC Interaction:** The world is populated with custom, server-side NPCs for gameplay functions like shops or upgrades. These NPCs are fully interactive, feature custom skins, and intelligently track and look at nearby players. |

---

## Features

This project was built with a focus on creating a stable, maintainable, and extensible foundation. Here are the core architectural and gameplay systems:

*   **Core Architecture**
    *   **State-Driven Game Logic:** The game's lifecycle is managed by a Finite State Machine (`GameStateManager`). Each phase (`Lobby`, `Preparation`, `Fighting`, `End`) is an isolated state object, which simplifies logic, prevents state conflicts, and makes adding new phases straightforward.
    *   **Decoupled Service Design:** Core functionalities are separated into distinct service classes (e.g., `PlayerSetupService`, `MapValidationService`). This keeps the game states clean and focused on coordination, adhering to the Single Responsibility Principle.
    *   **Dependency Injection:** Dependencies between managers and services are handled through constructor injection, promoting modularity and avoiding the pitfalls of static access patterns.

*   **Version-Agnostic NMS & Packet Handling**
    *   **NMS Abstraction Layer:** All version-specific code is isolated behind a custom `NMSHandler` interface. This allows the core plugin to remain version-agnostic, with support for new Minecraft versions added simply by creating a new handler implementation.
    *   **Server-Side NPCs:** A lightweight, from-scratch NPC system handles spawning, custom skins, and player interaction. It uses `PacketPlayOutPlayerInfo` to manage skins and removes NPCs from the tablist to keep the UI clean.
    *   **Packet-Based Block Invisibility:** The "Ghost Wall" is achieved by intercepting `PacketPlayOutBlockChange` packets to hide defender-placed blocks from attackers. A simple `PacketPlayOutBlockChange` packet is then used for a reliable and efficient reveal.

*   **Gameplay Systems**
    *   **Procedural Build Zones:** The defender build area is not hard-coded. It is generated dynamically based on a single in-game marker, with particle boundaries providing clear visual feedback to players.
    *   **Marker-Driven Map Configuration:** All key locations (spawns, chests, etc.) are defined by placing named Armor Stands. A `MarkerManager` scans these on startup, and a validation service ensures the map is correctly configured, preventing runtime errors.
    *   **Configurable Weighted Loot:** A `LootGenerator` service populates chests based on YAML-defined loot tables, supporting weighted chances and item categories for easy balancing.