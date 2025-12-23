# SpeedRunParkour

Minecraft plugin for Paper 1.21+ that allows creating and managing parkour courses with timer system and leaderboards.

## Requirements

- Java 21
- Paper 1.21.4+
- PlaceholderAPI

## Building

```bash
mvn clean package
```

The JAR file will be generated at `target/SpeedRunParkour-1.0.0.jar`

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/parkour <subcommand>` | Arena administration | `parkourtimer.admin` |
| `/parkourreload` | Reload configuration | `parkourtimer.reload` |
| `/parkourqueue [difficulty]` | Join queue or open menu | `parkourtimer.queue` |
| `/parkourtop <arena_id> [limit]` | View arena leaderboard | `parkourtimer.top` |

## Aliases

- `/pq` - Alias for `/parkourqueue`
- `/ptop` - Alias for `/parkourtop`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `parkourtimer.admin` | Full administration | OP |
| `parkourtimer.reload` | Reload configuration | OP |
| `parkourtimer.queue` | Use queues and menus | true |
| `parkourtimer.top` | View leaderboards | true |

## Author

AlexVila0204
