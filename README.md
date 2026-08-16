# Heracles

MIT questing mod by Terrarium Earth. This GitHub fork (`jtsus/Heracles`, branch `eternia-1.21.1`) is pinned from upstream [`1.21-jayem-dev`](https://github.com/terrarium-earth/Heracles/tree/1.21-jayem-dev) at `4d19eb21c542469afd6d2e8ba5412ef3fef57144` (PR [#275](https://github.com/terrarium-earth/Heracles/pull/275)).

Keep mod id `heracles`. Fetch/rebase onto Terrarium’s 1.21.1 line when that lands; do not flatten Architectury modules in this repo.

## Eternia pin deltas

- Fabric 1.21.1 `fabric.mod.json` depends (`minecraft ~1.21.1`, Resourceful Lib 3.x, Java 21).
- Unused EMI import removed from `ThemedButton` so the Fabric jar compiles without EMI on the classpath.
- Item task icon stacks no longer call a non-existent `DataComponentPredicate.asPatch()` (1.21.1). Left/right click on item task icons still opens REI recipes/usages via `ItemDisplayWidget` + `RecipeViewerHelper`.

Eternia gameplay (gym tiles, Cobblemon tasks, per-player Mongo progress) lives in the Eternia add-on, not this fork.

---

# Heracles
A tree style questing mod allowing creators to set completable quests for their users

Also see [Odysseus](https://github.com/terrarium-earth/odysseus), a Project Odyssey tool for converting FTB and HQM quest-packs to the Heracles format.

## For Mod Developers
<hr>

Be sure to add our maven to your `build.gradle`:
```gradle
repositories {
    maven { url = "https://maven.teamresourceful.com/repository/maven-public/" }
    <--- other repositories here --->
}
```
You can then add our mod as a dependency:

```gradle
dependencies {
    <--- Other dependencies here --->
    modImplementation "earth.terrarium.heracles:heracles-${modloader}-${mc_version}:${heracles_version}"
}
```

