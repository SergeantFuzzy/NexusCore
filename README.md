# NexusCore

Welcome to the **NexusCore** ecosystem with commands, GUIs, and powerful utilities in one core.  
Designed for modern Paper/Spigot servers (1.20+).

---

## Features

- **Core Command Suite**  
- **Polished GUIs**  
- **Admin Utilities**  
- **Tab Completion**  
- **Clean Command Registrar**  

---

## Getting Started

### Requirements
- Java 21+ (recommended)
- PaperMC/Spigot 1.20+ (tested on 1.21.x)

### Installation
1. Drop the compiled JAR into your server’s `plugins/` folder.
2. Start the server to generate config files.
3. (Optional) Adjust configuration/locale files, then `/nexus reload`.

---

## Commands

| Command            | Aliases                      | Description                                                                 |
|--------------------|------------------------------|-----------------------------------------------------------------------------|
| `/nexus`           | `/nc`, `/ncore`, `/nexuscore` | Opens NexusCore menu (player) or shows usage in console                     |
| `/nexus reload`    | —                            | Reloads configuration/messages                                              |
| `/nexus version`   | —                            | Shows plugin/version/build information                                      |
| `/feed`            | `/food`, `/hunger`           | Feed yourself; `/feed status` or `/feed <player>`                           |
| `/heal`            | —                            | Heal yourself or another player                                             |
| `/health`          | —                            | View health; `/health status [player]`                                      |
| `/fly`             | —                            | Toggle/set flight: `/fly [on|off] [player]`, `/fly status [player]`         |
| `/jump`            | —                            | Jump to targeted block or make another player jump                          |
| `/randomtp`        | `/rtp`                       | Randomly teleport yourself or another player                                |

> When used with no args by a player, `/nexus` opens the main GUI.

### Permissions

| Permission                   | Default | Description                                   |
|-----------------------------|---------|-----------------------------------------------|
| `nexuscore.use`             | true    | Allows `/nexus` in-game                       |
| `nexuscore.reload`          | op      | Allows `/nexus reload`                        |
| `nexuscore.version`         | true    | Allows `/nexus version`                       |
| `nexuscore.update.notify`   | op      | Receive in-game update notices on join        |
| **Self actions**            |         |                                               |
| `nexuscore.feed`            | true    | Use `/feed`                                   |
| `nexuscore.heal`            | true    | Use `/heal`                                   |
| `nexuscore.health`          | true    | Use `/health`                                 |
| `nexuscore.fly`             | true    | Use `/fly`                                    |
| `nexuscore.jump`            | true    | Use `/jump`                                   |
| `nexuscore.randomtp`        | true    | Use `/randomtp`                               |
| **Affect others**           |         |                                               |
| `nexuscore.feed.others`     | op      | Use `/feed <player>`                          |
| `nexuscore.heal.others`     | op      | Use `/heal <player>`                          |
| `nexuscore.health.others`   | op      | View `/health status <player>`                |
| `nexuscore.fly.others`      | op      | Use `/fly ... <player>`                       |
| `nexuscore.jump.others`     | op      | Make another player jump                      |
| `nexuscore.randomtp.others` | op      | `/randomtp <player>`                          |
| **Groups**                  |         |                                               |
| `nexuscore.admin.*`         | op      | All admin commands + “others” sub-perms       |
| `nexuscore.*`               | op      | Everything (admin + update notify)            |



---

## Troubleshooting

- **Command not found?** Ensure the JAR is loaded and no other plugin claims `/nexus`.

---

## License

This project is provided under the terms outlined in the [LICENSE](./LICENSE) file.  
© 2025 **NexusCore** by Sergeant Fuzzy — all rights reserved.
