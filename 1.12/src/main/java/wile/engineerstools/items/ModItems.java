/*
 * @file ModItems.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Definition and initialisation of items of this module.
 */
package wile.engineerstools.items;

import net.minecraft.item.ItemTool;
import wile.engineerstools.ModEngineersTools;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
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

  @GameRegistry.ObjectHolder("engineerstools:redia_tool")
  public static final ItemRediaTool REDIA_TOOL = new ItemRediaTool("redia_tool");

  @GameRegistry.ObjectHolder("engineerstools:iron_grit")
  public static final ItemGrit IRON_GRIT = new ItemGrit("iron_grit");

  @GameRegistry.ObjectHolder("engineerstools:gold_grit")
  public static final ItemGrit GOLD_GRIT = new ItemGrit("gold_grit");

  private static final Item modItems[] = {
    CRUSHING_HAMMER,
    REDIA_TOOL,
    IRON_GRIT,
    GOLD_GRIT
  };

  private static final ArrayList<Item> registeredItems = new ArrayList<>();

  @Nonnull
  public static List<Item> getRegisteredItems()
  { return Collections.unmodifiableList(registeredItems); }

  public static void registerItems(RegistryEvent.Register<Item> event)
  {
    Collections.addAll(registeredItems, modItems);
    for(Item e:registeredItems) event.getRegistry().register(e);
    ModEngineersTools.logger.info("Registered " + Integer.toString(registeredItems.size()) + " items.");
  }

  @SideOnly(Side.CLIENT)
  public static void registerItemModel(Item item, Object... args)
  {
    if(args.length == 0) {
      if(item instanceof ItemTool) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName().toString()));
      } else {
        ModelResourceLocation rc = new ModelResourceLocation(item.getRegistryName(),"inventory");
        ModelBakery.registerItemVariants(item, rc);
        ModelLoader.setCustomMeshDefinition(item, stack->rc);
      }
    } else {
      ModEngineersTools.logger.error("Invalid registerItemModel() args");
    }
  }

  @SideOnly(Side.CLIENT)
  public static void initModels()
  { for(Item e:registeredItems) registerItemModel(e); }

}
