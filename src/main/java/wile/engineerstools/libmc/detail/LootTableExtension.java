/*
 * @file LootTableAppendix.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Additional loot based on vanilla loot tables.
 */
package wile.engineerstools.libmc.detail;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;

public class LootTableExtension
{
  public static boolean enabled = false;

  public static void onLootTableLoad(LootTableLoadEvent event)
  {
    if(!enabled) return;
    ResourceLocation table_path = new ResourceLocation(Auxiliaries.modid(), "additional/" + event.getName().getNamespace() + "/" + event.getName().getPath());
    if(LootTableExtension.class.getResource("/data/" + table_path.getNamespace() + "/loot_tables/" + table_path.getPath() + ".json") == null) return;
    event.getTable().addPool(LootPool.lootPool().add(TableLootEntry.lootTableReference(table_path).setWeight(1)).name(Auxiliaries.modid()+"_additional_loot").bonusRolls(0, 1).build());
  }
}
