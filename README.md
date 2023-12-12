# EZMobSpawns 

## Purpose
EZMobSpawns is a utility mod that provides another way to add mob spawns to the game, while also affording the user control over how, when, and where mobs spawn. It standardizes spawn rates accross dimensions and biomes (both modded and unmodded), and introduces a number of additional criteria which can be used to customize spawn mechanics. It also serves to sidestep several vanilla spawn mechanics which can prove unintuitive to casual modders and difficult to change without use of mixins / development.

EZMobSpawns will read any config files in the standard config directory begining with the prefix `ez_mob_spawns`. 

Note: this mod does not remove, or otherwise change, existing spawn rules (whether in place through vanilla or modded means), it only serves to provide another way to add mobs to the world. 

## Algorithm Overview
1. The algorithm begins by taking `chunkRange`x`chunkRange` chunks from around each player.
2. A random subset of the chunks from step 1 is selected by taking a total of `chunkCoverage`% chunks from the entire chunk set.
3. Each of the spawn groups is then updated for each of the chunks selected in step 3. Each spawn group waits at least `ticksBetweenAttempts` server ticks between spawn attempts. 
4. If the spawn group is activated, a coarse-check is run to quickly determine if the group applies to that chunk. The coarse-check takes the chunk's center block and runs the following tests:
   - Checks to see if the random `chanceToAttemptSpawns` activates.
   - Checks that the chunk is in a valid dimension as defined in `dimensions`. 
   - Checks that the chunk is in a valid biome as defined in `biomes` (supports both biome ids and biome tags).
   - Checks that the chunk falls within at least one of the regions defined in `areaRestrictions`.
   - Checks that the spawn attempt is occuring during a valid timeframe as defined in `timeframes`.
   - Checks that the spawn attempt is occuring during a valid moon phase as defined in `moonphases`.
   - Checks that the spawn attempt is occuring diromg a valid weather pattern as defined in `weather`.
   - Checks that the chunk's climate is within a valid temperature range as defined in `temperatures`.
                     
5. If the coarse check passes for a given spawn group, the algorithm then checks that the mob `limit` for that mob has not been reached.
6. If the mob limit check passes for a given spawn group, the algorithm then picks `totalMobsToPick` from the entities defined in `mobs`.
7. For each mob selected, the algorithm then selects up to `maxPtsPerChunk` locations within the chunk and runs the following fine-grained checks:
   - Checks that the potential spawn location has a valid light level defined in `lightLevels`.
   - Checks that the potential spawn location is not within a region defined in `blacklistedAreas`.
   - Checks that the potential spawn location is on top of an accepted ground block defined in `groundBlocks`.
   - Checks that the potential spawn location is not on top of an invalid ground block defined in `disallowedBlocks`.
   - Checks that the potential spawn location meets at least one of the `placement` requirements.
   - Checks that the potential spawn location is near any required blocks defined in `requiredBlocks`.
  
8. If all of the fine-grained checks pass, the mob is added to the world and the group enters a cooldown period defined by `spawnCoolDown` after which it will resume regular tick updates.

## Performance Considerations

- For best performance, try to eliminate chunks from consideration early in the spawning process. In general, it is better to rule out a spawn attempt during the coarse-checks rather than the fine-grained checks as they can be slower.
- For groups that rely heavily on the fine-grained checks, consider using a longer `spawnCoolDown` to help prevent lag when doing the entity/placement calculations. 
- Excercise caution when designing a new spawn config and always try out any new settings in a test world to ensure that spawn rates are reasonable.

