# SpeedRunParkour

A comprehensive Minecraft plugin for creating and managing parkour courses with speedrun functionality, timers, queues, and leaderboards.

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/parkour <subcommand>` | `parkourtimer.admin` | Main administration command |
| `/parkourreload` | `parkourtimer.reload` | Reload plugin configuration |
| `/parkourqueue [difficulty]` | `parkourtimer.queue` | Join course queue or open menu |
| `/parkourtop <arena_id> [limit]` | `parkourtimer.top` | Show arena leaderboard |

### Command Aliases
- `/pq` → `/parkourqueue`
- `/ptop` → `/parkourtop`

## Admin Commands

### Arena Setup
- `/parkour tool` - Get selection tool (WorldEdit-style wand)
- `/parkour create <id> <difficulty>` - Create new arena (requires selection)
- `/parkour setstart <id>` - Set start location (pressure plate position)
- `/parkour setend <id>` - Set end location (pressure plate position)
- `/parkour setspawn <id>` - Set spawn location
- `/parkour setwaiting <id>` - Set waiting area location

### Queue Management
- `/parkour open <id>` - Open arena for queue
- `/parkour start <id>` - Start processing queue
- `/parkour stop <id>` - Stop processing queue
- `/parkour close <id>` - Close arena
- `/parkour queue <id>` - Show queue information

### General Management
- `/parkour list` - List all arenas with queue information
- `/parkour delete <id>` - Delete arena
- `/parkour clear` - Clear current selection

**Valid Difficulties**: EASY, MEDIUM, HARD

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `parkourtimer.admin` | op | Full administration access |
| `parkourtimer.reload` | op | Reload configuration |
| `parkourtimer.queue` | true | Join queues and use menus |
| `parkourtimer.top` | true | View leaderboards |

## Configuration

### config.yml
```yaml
# Display settings
display:
  # Options: ACTION_BAR, BOSS_BAR, SCOREBOARD
  mode: ACTION_BAR

# Storage settings
storage:
  # Options: YAML, SQLITE
  type: SQLITE
  sqlite:
    filename: "parkourtimer.db"
```

# PlaceholderAPI Integration

All placeholders use the identifier `%parkour_<placeholder>%`

### Current Timer Placeholders
- `%parkour_current_time_<course_id>%` - Shows the current running timer for the specified course
    - **Format**: `M:SS.mmm` (minutes:seconds.milliseconds)
    - **Returns**: `0:00.000` if no active timer or not in the specified course
    - **Example**: `%parkour_current_time_easy_parkour%`

### Best Time Placeholders
- `%parkour_best_time_<course_id>%` - Player's personal best time for the course
    - **Returns**: Formatted time or `No time` if never completed
    - **Example**: `%parkour_best_time_hard_course%`

- `%parkour_best_player_<course_id>%` - Name of the player with the world record for the course
    - **Returns**: Player name or `No record` if no completions
    - **Example**: `%parkour_best_player_speed_run%`

### Queue System Placeholders
- `%parkour_queue_position%` - Player's current position in any queue they're in
    - **Returns**: Position number or `0` if not in queue

- `%parkour_queue_size_<difficulty>%` - Number of players in queue for specific difficulty
    - **Valid difficulties**: `EASY`, `MEDIUM`, `HARD`
    - **Returns**: Number of players in queue
    - **Examples**:
        - `%parkour_queue_size_easy%`
        - `%parkour_queue_size_medium%`
        - `%parkour_queue_size_hard%`

### Player Statistics Placeholders

#### Total Statistics
- `%parkour_stats_total_attempts%` - Total number of attempts across all courses
- `%parkour_stats_total_completed%` - Total number of different courses completed at least once

#### Difficulty-Based Statistics
- `%parkour_stats_difficulty_<difficulty>_completed%` - Number of courses completed for specific difficulty
    - **Examples**:
        - `%parkour_stats_difficulty_easy_completed%`
        - `%parkour_stats_difficulty_medium_completed%`
        - `%parkour_stats_difficulty_hard_completed%`

- `%parkour_stats_difficulty_<difficulty>_best%` - Best time achieved in any course of the specified difficulty
    - **Returns**: Formatted time or `No time`
    - **Examples**:
        - `%parkour_stats_difficulty_easy_best%`
        - `%parkour_stats_difficulty_medium_best%`
        - `%parkour_stats_difficulty_hard_best%`

### Leaderboard Placeholders

#### Player Names in Leaderboard
- `%parkour_leaderboard_name_<course_id>_<position>%` - Player name at specific leaderboard position
    - **Position range**: 1-10
    - **Returns**: Player name or `No player` if position doesn't exist
    - **Examples**:
        - `%parkour_leaderboard_name_my_course_1%` (1st place)
        - `%parkour_leaderboard_name_speed_track_5%` (5th place)

#### Times in Leaderboard
- `%parkour_leaderboard_time_<course_id>_<position>%` - Time at specific leaderboard position
    - **Position range**: 1-10
    - **Returns**: Formatted time or `No time` if position doesn't exist
    - **Examples**:
        - `%parkour_leaderboard_time_my_course_1%` (1st place time)
        - `%parkour_leaderboard_time_speed_track_3%` (3rd place time)
