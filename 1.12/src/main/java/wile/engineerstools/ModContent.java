/*
 * @file ModContent.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Definition and initialisation of items of this module.
 */
package wile.engineerstools;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import wile.engineerstools.items.*;
import wile.engineerstools.blocks.*;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ModContent
{
  //--------------------------------------------------------------------------------------------------------------------
  //-- Blocks
  //--------------------------------------------------------------------------------------------------------------------

  public static final BlockAriadneCoal ARIADNE_COAL_X = new BlockAriadneCoal("ariadne_coal_x", EnumFacing.Axis.X);
  public static final BlockAriadneCoal ARIADNE_COAL_Y = new BlockAriadneCoal("ariadne_coal_y", EnumFacing.Axis.Y);
  public static final BlockAriadneCoal ARIADNE_COAL_Z = new BlockAriadneCoal("ariadne_coal_z", EnumFacing.Axis.Z);

  private static final Object content_blocks[] = {
    ARIADNE_COAL_X,
    ARIADNE_COAL_Y,
    ARIADNE_COAL_Z
  };

  private static ArrayList<Block> registeredBlocks = new ArrayList<>();

  @Nonnull
  public static List<Block> getRegisteredBlocks()
  { return Collections.unmodifiableList(registeredBlocks); }

  //--------------------------------------------------------------------------------------------------------------------
  //-- Items
  //--------------------------------------------------------------------------------------------------------------------

  public static final ItemCrushingHammer CRUSHING_HAMMER = new ItemCrushingHammer("crushing_hammer");
  public static final ItemRediaTool REDIA_TOOL = new ItemRediaTool("redia_tool");
  public static final ItemGrit IRON_GRIT = new ItemGrit("iron_grit");
  public static final ItemGrit GOLD_GRIT = new ItemGrit("gold_grit");
  public static final ItemAriadneCoal ARIADNE_COAL = new ItemAriadneCoal("ariadne_coal");
  public static final ItemStimPack STIMPACK = new ItemStimPack("stimpack");


  private static final Item modItems[] = {
    CRUSHING_HAMMER,
    REDIA_TOOL,
    IRON_GRIT,
    GOLD_GRIT,
    ARIADNE_COAL,
    STIMPACK
  };

  private static final ArrayList<Item> registeredItems = new ArrayList<>();

  @Nonnull
  public static List<Item> getRegisteredItems()
  { return Collections.unmodifiableList(registeredItems); }

  //--------------------------------------------------------------------------------------------------------------------
  //-- Init
  //--------------------------------------------------------------------------------------------------------------------

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
      if(item instanceof ItemRediaTool) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
      } else {
        ModelResourceLocation rc = new ModelResourceLocation(item.getRegistryName(),"inventory");
        ModelBakery.registerItemVariants(item, rc);
        ModelLoader.setCustomMeshDefinition(item, stack->rc);
      }
    } else {
      ModEngineersTools.logger.error("Invalid registerItemModel() args");
    }
  }

  public static final void registerBlocks(RegistryEvent.Register<Block> event)
  {
    // Config based registry selection
    int num_block_registrations_skipped = 0;
    int num_block_registrations_skipped_noie = 0;
    for(Object e:content_blocks) {
      if(e instanceof Block) {
        registeredBlocks.add((Block) e);
      }
    }
    for(Block e:registeredBlocks) event.getRegistry().register(e);
    ModEngineersTools.logger.info("Registered " + Integer.toString(registeredBlocks.size()) + " blocks.");
  }

  public static final void registerItemBlocks(RegistryEvent.Register<Item> event)
  {
    int n = 0;
    for(Block e:registeredBlocks) {
      ResourceLocation rl = e.getRegistryName();
      if(rl == null) continue;
      event.getRegistry().register(new ItemBlock(e).setRegistryName(rl));
      ++n;
    }
  }

  @SideOnly(Side.CLIENT)
  public static final void initModels()
  {
    for(Item e:registeredItems) {
      registerItemModel(e);
    }
    for(Block e:registeredBlocks) {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(e), 0, new ModelResourceLocation(e.getRegistryName(), "inventory"));
    }
  }

}
