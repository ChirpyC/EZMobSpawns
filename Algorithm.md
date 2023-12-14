# The Spawning Algorithm:

See [Configuration](https://github.com/ChirpyC/EZMobSpawns/blob/main/Configuration.md) for a description of the config file's properties. 

1. The algorithm begins by taking `chunkRange`x`chunkRange` chunks from around each player.
   > Ex. `chunkRange=3`
   > 
   > <img src="https://github.com/ChirpyC/EZMobSpawns/blob/main/wikiPics/ex_range.png" width="600">
3. A random subset of the chunks from step 1 is selected by taking a total of `chunkCoverage`% chunks from the entire chunk set.
   > Ex. `3 players, chunkRange = 3, chunkCoverage = 0.3`
   > 
   > <img src="https://github.com/ChirpyC/EZMobSpawns/blob/main/wikiPics/ex_chunkCoverage.png" width="600">
5. Each of the spawn groups is then updated for each of the chunks selected in step 3. Each spawn group waits at least `ticksBetweenAttempts` server ticks between spawn attempts. 
6. If the spawn group is activated, a coarse-check is run to quickly determine if the group applies to that chunk. The coarse-check takes the chunk's center block and runs the following tests:
   - Checks to see if the random `chanceToAttemptSpawns` activates.
   - Checks that the chunk is in a valid dimension as defined in `dimensions`. 
   - Checks that the chunk is in a valid biome as defined in `biomes` (supports both biome ids and biome tags).
   - Checks that the chunk falls within at least one of the regions defined in `areaRestrictions`.
   - Checks that the spawn attempt is occuring during a valid timeframe as defined in `timeframes`.
   - Checks that the spawn attempt is occuring during a valid moon phase as defined in `moonphases`.
   - Checks that the spawn attempt is occuring diromg a valid weather pattern as defined in `weather`.
   - Checks that the chunk's climate is within a valid temperature range as defined in `temperatures`.
                     
8. If the coarse check passes for a given spawn group, the algorithm then checks that the mob `limit` for that mob has not been reached.
9. If the mob limit check passes for a given spawn group, the algorithm then picks `totalMobsToPick` from the entities defined in `mobs`.
10. For each mob selected, the algorithm then selects up to `maxPtsPerChunk` locations within the chunk.
   > Ex. `1 player, chunkRange = 3, chunkCoverage = 0.44, maxPtsPerChunk = 3`
   > 
   > <img src="https://github.com/ChirpyC/EZMobSpawns/blob/main/wikiPics/ex_maxPtsPerChunk.png" width="600">
11. For each of the points found, the algorithm runs the following fine-grained checks:
   - Checks that the potential spawn location has a valid light level defined in `lightLevels`.
   - Checks that the potential spawn location is not within a region defined in `blacklistedAreas`.
   - Checks that the potential spawn location is on top of an accepted ground block defined in `groundBlocks`.
   - Checks that the potential spawn location is not on top of an invalid ground block defined in `disallowedBlocks`.
   - Checks that the potential spawn location meets at least one of the `placement` requirements.
   - Checks that the potential spawn location is near any required blocks defined in `requiredBlocks`.
  
11. If all of the fine-grained checks pass, the mob is added to the world and the group enters a cooldown period defined by `spawnCoolDown` after which it will resume regular tick updates.


## Performance Considerations

- For best performance, try to eliminate chunks from consideration early in the spawning process. In general, it is better to rule out a spawn attempt during the coarse-checks rather than the fine-grained checks as they can be slower.
- For groups that rely heavily on the fine-grained checks, consider using a longer `spawnCoolDown` to help prevent lag when doing the entity/placement calculations. 
- Excercise caution when designing a new spawn config and always try out any new settings in a test world to ensure that spawn rates are reasonable.

