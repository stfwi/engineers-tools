/*
 * @file ModItems.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Definition and initialisation of items of this module.
 */
package wile.engineerstools.items;

import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.detail.ModConfig;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ModItems
{

  @GameRegistry.ObjectHolder("engineerstools:crushing_hammer")
  public static final ItemCrushingHammer CRUSHING_HAMMER = new ItemCrushingHammer("crushing_hammer");

  @GameRegistry.ObjectHolder("engineerstools:iron_grit")
  public static final ItemGrit IRON_GRIT = new ItemGrit("iron_grit");

  @GameRegistry.ObjectHolder("engineerstools:gold_grit")
  public static final ItemGrit GOLD_GRIT = new ItemGrit("gold_grit");

  private static final Item modItems[] = {
    CRUSHING_HAMMER,
    IRON_GRIT,
    GOLD_GRIT
  };

  private static final ArrayList<Item> registeredItems = new ArrayList<>();

  @Nonnull
  public static List<Item> getRegisteredItems()
  { return Collections.unmodifiableList(registeredItems); }

  public static final void registerItems(RegistryEvent.Register<Item> event)
  {
    // Config based registry selection
    int num_registrations_skipped = 0;
    ArrayList<Item> allItems = new ArrayList<>();
    Collections.addAll(allItems, modItems);
    final boolean woor = ModConfig.isWithoutOptOutRegistration();
    for(Item e:allItems) {
      if((!woor) || (!ModConfig.isOptedOut(e))) {
        registeredItems.add(e);
      } else {
        ++num_registrations_skipped;
      }
    }
    for(Item e:registeredItems) event.getRegistry().register(e);
    ModEngineersTools.logger.info("Registered " + Integer.toString(registeredItems.size()) + " items.");
    if(num_registrations_skipped > 0) {
      ModEngineersTools.logger.info("Skipped registration of " + num_registrations_skipped + " items.");
    }
  }

  @SideOnly(Side.CLIENT)
  public static final void initModels()
  {
    for(Item e:registeredItems) {
      if(e instanceof ItemTools) ((ItemTools)e).initModel();
    }
  }
}
