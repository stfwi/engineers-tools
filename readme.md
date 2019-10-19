
## Engineer's Tools

A [Minecraft](https://minecraft.net) (Java Edition) mod based on
[`Forge`](http://www.minecraftforge.net/), adding manual tool items
for the Engineer's use.

[Screenshots in the documentation readme here](documentation/readme.md).

### Distribution file download

Main distribution channel for this mod is CurseForge:

  - Release/beta versions: https://www.curseforge.com/minecraft/mc-mods/engineers-tools/files
  - All versions: https://minecraft.curseforge.com/projects/engineers-tools/files

----
### Details

The mod has its focus on manual tools:

- *REDIA Tool* (REal DIAmond covered tool): Simple manual multi tool combining
  pickaxe, axe, shovel, and shears. Reference efficiency: Diamond. Very efficient
  when in good condition (about Efficiency III), slow when the durability is low
  (Efficiency curve configurable). Repairable with Diamonds.

  - Right-click action: Place torch.
  - Sneak-right-click ground: Cycle Dirt - Farmland - Coarse Dirt - Grass Path.
  - Sneak-right-click: Shaering of Grass, Vines, Sheep, etc.
  - Sneak while breaking a wood log: Tree felling (also higher durability loss).

  *Please read the community references/credits below*. Crafted with iron axe,
  pickaxe, and shovel, and a diamond.

- *Auto Stim Pack*: May save your life by pushing back a few hearts when your
  health falls below three hearts. Also gives some small buffs to get out of the
  present misery. Can also be used as Bauble. Warning: The stim pack pulse
  injector needs to charge first, so there is a small delay.

- *Sleeping Bag*: Weatherproof sleeping bag made of tough fabric. Does not need
  to be placed, and does not change the spawn point of the last bed.

- *Ariadne Coal*: A coal pen that allows you to draw arrows on walls, floors
  and ceilings to find the way out of caves. Build like a "coal sword".

- *Ore Crushing Hammer*: Early game ore-to-grit duplication, as known from
  the IE ore crusher. The hammer is crafted like the Engineers Hammer, except
  that one of the two iron ingots is replaced with an iron block. The tool
  has a comparatively low durability and allowed (by default) to crush 64
  stack of ore before breaking. It's not repairable, so a new one has to be
  crafted - or get an IE Ore Crusher machine ASAP.

----
### Mod pack integration, forking, back ports, bug reports, testing

  - Packs: If your mod pack ***is open source as well and has no installer***,
    you don't need to ask and simply integrate this mod.

  - Bug reports: Yes, please let me know. Drop a mail or better open an issue
    for the repository.

  - Pull requests: Happily accepted. Please make sure that use the ***develop
    branch*** for pull requests. The master branch is for release versions only.
    I might merge the pull request locally if I'm ahead of the github repository,
    we will communicate this in the pull request thread then.

  - The mod config has an "include testing features" option. Enabling this causes
    blocks under development to be registered as well.

----
## Version history

Mod versions are tracked in the readme files for individual Minecraft versions, and
of course in the commits of this repository. Beta and release versions that are
distributed as compiled `jar`s are tagged accordingly. Release versions are merged
into the `master` branch, while beta/alpha version tags link to `develop` branch
commits.

  - [1.12](1.12/readme.md)

  - [1.14](1.14/readme.md)

### Community references

- [Immersive Engineering](https://github.com/BluSunrize/ImmersiveEngineering/):
  IE can be seen as a kind of base mod for my small mod, so the items are designed
  fit into the Immersive Context.

- [Botania](https://botaniamod.net/): The REDIA tool has two features derived from
  Vazzki's tools, see credits file.

- [Actually Additions](https://github.com/Ellpeck/ActuallyAdditions): The REDIA
  tool combo-functionality can also be found in the all-in-one tools Ellpeck's
  Actually Additions.

- [Simple Grinder](https://www.curseforge.com/minecraft/mc-mods/simple-grinder) provides
  a vanilla ore-to-dust device.

- The [Furnus](https://www.curseforge.com/minecraft/mc-mods/furnus) also adds the "Pulvus",
  an ore grinder with upgrade capabilities.</li>
