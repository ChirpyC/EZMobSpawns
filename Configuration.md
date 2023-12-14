# EZMobSpawns JSON 

Reads any files in the config directory that start with the prefix *"ez_mob_spawn"*.

## Global Settings

<details>
  <summary> <code>chunkRange</code> The number of chucks to select around each player to use for further spawn checks.</summary>
  
  > ##
  >
  > Defaults to 3.
  >
  > ##### Example: chunkRange = 3
  >
  > The areas highlighted in yellow show the chunks around each player that will be used for further spawn checks:
  > # <img src="https://github.com/ChirpyC/EZMobSpawns/blob/main/wikiPics/ex_range.png" width="600">
</details>
<details>
  <summary> <code>chunkCoverage</code> The percentage of loaded chunks to use for further spawn checks.</summary>
  
  > ##
  >
  > Defaults to 1.0.
  > 
  > ##### Example: 3 players, chunkRange = 3, chunkCoverage = 0.3
  >
  > The yellow squares show the chunks that have been randomly selected for additional spawn checks:
  > # <img src="https://github.com/ChirpyC/EZMobSpawns/blob/main/wikiPics/ex_chunkCoverage.png" width="600">
</details>
<details>
  <summary> <code>maxPtsPerChunk</code> The max number of potential spawn locationos to pick per selected chunk.</summary>
  
  > ##
  > 
  > Defaults to 100.
  > 
  > ##### Example: 1 player, chunkRange = 3, chunkCoverage = 0.44, maxPtsPerChunk = 3
  > 
  > The red x's show locations that have been randomly selected as potential spawn points:
  > <img src="https://github.com/ChirpyC/EZMobSpawns/blob/main/wikiPics/ex_maxPtsPerChunk.png" width="600">
</details>

## Spawn Group Settings

<details>
  <summary> <code>groupName</code> A descriptor of the spawn group. Will appear in debug logs if specified.</summary>
</details>
<details>
  <summary> <code>mobs</code> A list of mobs which may be selected to spawn from the group.</summary>

  > ##
  > 
  > - *mob*: the entity id of the mob to spawn
  > - *min*: (optional) the minimum number of the mob to spawn. If omitted, defaults to 1
  > - *max*: (optional) the maximum number of the mob to spawn. If omitted, defaults to 1
  > - *weight*: (optional) allows weighting of the mobs. If omitted, defaults to equal chance for each mob
  > - *limit*: (optional) limits the total number of that mob within a 256x256 range around the potential spawn point. If omitted, defaults to 8. Assigning a value of -1 will remove the limit checks (do so at your own risk).
  > 
  > #### Example: 
  > ```
  > "mobs": [
  >       {"mob":"minecraft:salmon", "min": 1, "max": 1, "weight": 1, "limit": 100},
  >       {"mob":"minecraft:cod", "min": 1, "max": 1, "weight": 1, "limit": 100},
  >       {"mob":"minecraft:tropical_fish", "min": 1, "max": 1, "weight": 1, "limit": 100}
  >     ]
  > ```
</details>
<details>
  <summary> <code>totalMobsToPick</code> The number of entries to select from the <i>mobs</i> array.</summary>

  > ##
  > 
  > Defaults to 1.
  > 
  > #### Example: 
  > 
  > If the ```mobs``` array has 10 different mobs, and ```totalMobsToPick``` is 3, the algorithm will randomly select 3 of the 10 entries for each chunk (repeats allowed) and attempt the fine-grained placement checks for each one.
</details>
<details>
  <summary> <code>dimensions</code> A list of allowed dimensions.</summary>

  > ##
  > 
  > Defaults to all dimensions if omitted.
</details>
<details>
  <summary> <code>biomes</code> A list of allowed biomes/biome tags</summary>

  > ##
  > 
  > Defaults to all vanilla overworld biomes if omitted.
</details>
<details>
  <summary> <code>placement</code> A list of allowed placements</summary>

  > ##
  > 
  > Accepted values are: `surface`, `underground`, `in_water`,  `cave_water`, `in_lava`, `air`
  > 
  > Defaults to surface placement if omitted.
</details>
<details>
  <summary> <code>chanceToAttemptSpawns</code> The chance to check spawning rules for the group.</summary>

  > ##
  > 
  > Accepts float values 0.0 to 1.0 (inclusive). Defaults to 1.0 if omitted.
  > 
  > A value of 1.0 will always run further spawn checks, while a value of 0.0 will never run further spawn checks (and will consequently never trigger any spawns).
</details>
<details>
  <summary> <code>ticksBetweenAttempts</code> The minimum number of server ticks between spawn attempts.</summary>

  > ##
  > 
  > Accepts positive integer values. Defaults to 100 if omitted.
</details>
<details>
  <summary> <code>spawnCoolDown</code> The minimum number of server ticks to wait before restarting regular tick behavior.</summary>

  > ##
  > 
  > Accepts positive integer values. Defaults to 6000 if omitted.
</details>
<details>
  <summary><code>runImmediately</code> Sets whether the spawn group should attempt to spawn mobs for newly loaded chunks immediately or after a delay.</summary>

  > ##
  > 
  > Defaults to true if omitted.
</details>
<details>
  <summary> <code>timeframes</code> A list of times during the day during which spawning is allowed.</summary>

  > ##
  > 
  > Defaults to all times if omitted.
  > 
  > #### Example: 
  > ```
  > "timeframes": [{"start":  0, "end": 24000}]
  > ```
</details>
<details>
  <summary> <code>moonphases</code> A list of moonphases during which spawning is allowed.</summary>

  > ##
  > 
  > Defaults to all moonphases if omitted.
  > 
  > Note: Moonphases occur during both day and night. Use this with the timeframes array if only night-time moonphases are desired.
  > 
  > Accepted values are: `full`, `waning_gibbous`, `last_quarter`, `waning_crescent`,  `new`, `waxing_crescent`, `first_quarter`,  `waxing_gibbous`
  > 
  > #### Example: 
  > ```
  > "moonphases": ["full", "waning_gibbous", "last_quarter", "waning_crescent",  "new", "waxing_crescent", "first_quarter",  "waxing_gibbous"]
  > ```
</details>
<details>
  <summary> <code>weather</code> A list of weathers in which spawning is allowed.</summary>

  > ##
  > 
  > Defaults to all weathers if omitted.
  > 
  > Note: Moonphases occur during both day and night. Use this with the timeframes array if only night-time moonphases are desired.
  > 
  > Accepted values are: `none`, `rain`, `snow`, `storm`
  > 
  > #### Example: 
  > ```
  > "weather": ["none", "rain"]
  > ```
</details>
<details>
  <summary> <code>temperatures</code> A list of temperature ranges in which spawning is allowed.</summary>

  > ##
  > 
  > Defaults to all temperatures if omitted.
  > 
  > #### Example: 
  > ```
  > "temperatures": [{"start": -2.0,"end": 2.0}]
  > ```
</details>
<details>
  <summary> <code>yRanges</code> A list of y-level ranges in which spawning is allowed.</summary>

  > ##
  > 
  > Defaults to all y-levels if omitted.
  > 
  > #### Example: 
  > ```
  > "yRanges": [{"start": -64, "end":  265}]
  > ```
</details>
<details>
  <summary> <code>lightLevels</code> A list of light-level ranges in which spawning is allowed.</summary>

  > ##
  > 
  > Defaults to all light-levels if omitted.
  > 
  > #### Example: 
  > ```
  > "lightLevels": [{"start": 0, "end":  15}]
  > ```
</details>
<details>
  <summary> <code>requiredBlocks</code> A list of blocks of which at least one must be near to the spawnning location in order to spawn.</summary>

  > ##
  > 
  > Defaults to all blocks if omitted.
  > 
  > #### Example: Will only allow spawns if the potential spawn location is near a sunflower:
  > ```
  > "requiredBlocks": ["minecraft:sunflower"]
  > ```
</details>
<details>
  <summary> <code>groundBlocks</code> A list of blocks on which spawning is allowed.</summary>

  > ##
  > 
  > Defaults to all blocks  if omitted.
  > 
  > #### Example: 
  > ```
  > "groundBlocks": ["minecraft:stone","minecraft:deepslate"],
  > ```
</details>
<details>
  <summary> <code>disallowedBlocks</code> A list of blocks on which spawning is prohibited</summary>

  > ##
  > 
  > Defaults to no blocks if omitted.
  > 
  > #### Example: 
  > ```
  > "disallowedBlocks": ["minecraft:oak_leaves"]
  > ```
</details>
<details>
  <summary> <code>areaRestriction</code> Restricts spawning to a series of repeating areas.</summary>

  > ##
  > 
  > - *areaSize*: the size of the area in chunks
  > - *repeat*: how frequently to repeat the area
  > - *offsestX*: (optional) the number chunks to offset the area by on the x-axis. If omitted, defaults to 0
  > - *offsestZ*: (optional) the number chunks to offset the area by on the z-axis. If omitted, defaults to 0
  > 
  > #### Example: 
  > ```
  > "areaRestrictions":[
  >      {"areaSize":1, "repeat": 2, "offsestX": 0, "offsestZ": 0},
  >      {"areaSize":1, "repeat": 2, "offsestX": 1, "offsestZ": 1}
  >    ]
  > ```
  > Spawning is permitted in the yellow and orange areas:
  > # <img src="https://github.com/ChirpyC/EZMobSpawns/blob/main/wikiPics/ex_areaRestriction.png" width="600">   
</details>
<details>
  <summary> <code>blacklistedAreas</code> Prevents spawning within specified areas.</summary>

  > ##
  > 
  > - *startX*: the min block x-coordinate of the area
  > - *startZ*: the min block z-coordinate of the area
  > - *endX*: the max block x-coordinate of the area
  > - *endZ*: the max block z-coordinate of the area
  > 
  > #### Example: 
  > ```
  > "blacklistedAreas":  [
  >       {"startX":-80, "startZ": -80, "endX": 80, "endZ": 80}
  >     ]
  > ```
  > Spawning is permitted in the yellow area:
  > # <img src="https://github.com/ChirpyC/EZMobSpawns/blob/main/wikiPics/ex_blacklistedAreas.png" width="600">   
</details>
<details>
  <summary> <code>effects</code> A list of effects to apply to spawned mobs.</summary>

  > ##
  > 
  > For each mob spawned from the group, a single effect from this list is randomly selected and applied to the new mob.
  >  
  > - *effect*: the name of the effect
  > - *durration*: the number of ticks the effect should be active
  > 
  > #### Example: 
  > ```
  > "effects": [{"effect":"minecraft:glowing", "duration": 6000}]
  > ```
</details>
<details>
  <summary> <code>names</code> A list of names to apply to spawned mobs.</summary>

  > ##
  >
  > #### Example: 
  > ```
  > "names": ["Sam", "Tim", "Bob", "Lou", "Wyn", "Ann", "Meg", "Sue"]
  > ```
</details>
<details>
  <summary> <code>announcements</code> A list of announcements to display in chat when a mob spawns in.</summary>

  > ##
  > 
  > Announcement messages take the form "A <mob_name> <random_message>". If a name is given in the `names` property, the announcement will start with the name instead.
  >   
  > #### Example: 
  > ```
  > "announcements": ["has spawned in!", "has awoken!", "wants to fight!", "entered the area!"]
  > ```
</details>
<details>
  <summary> <code>enableDebug</code> Enables or disables debug messages to the log.</summary>
  > ##
  >
  > Defaults to false.
</details>
