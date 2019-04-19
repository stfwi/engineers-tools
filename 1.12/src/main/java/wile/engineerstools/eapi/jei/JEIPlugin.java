/*
 * @file JEIPlugin.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * JEI plugin (see https://github.com/mezz/JustEnoughItems/wiki/Creating-Plugins)
 */
package wile.engineerstools.eapi.jei;

import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.items.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements mezz.jei.api.IModPlugin
{
  @SuppressWarnings("deprecation")
  private void blacklist(mezz.jei.api.IModRegistry registry, Item item)
  {
    if(!registry.getJeiHelpers().getIngredientBlacklist().isIngredientBlacklisted(new ItemStack(item))) {
      registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(item));
    }
    if(!registry.getJeiHelpers().getItemBlacklist().isItemBlacklisted(new ItemStack(item))) {
      registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(item));
    }
  }

  @Override
  public void register(mezz.jei.api.IModRegistry registry)
  {
    try {
      // Don't show the internal grits in JEI, other mod grits shall be preferred.
      blacklist(registry, ModItems.IRON_GRIT);
      blacklist(registry, ModItems.GOLD_GRIT);
    } catch(Throwable ex) {
      ModEngineersTools.logger.error("Failed to blacklist an item:" + ex.getMessage());
    }
  }
}
