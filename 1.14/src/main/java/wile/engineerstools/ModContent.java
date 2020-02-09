/*
 * @file ModContent.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.engineerstools;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import wile.engineerstools.blocks.BlockAriadneCoal;
import wile.engineerstools.items.*;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ModContent
{
  private static final Logger LOGGER = ModEngineersTools.LOGGER;
  private static final String MODID = ModEngineersTools.MODID;

  // -----------------------------------------------------------------------------------------------------------------
  // -- All blocks
  // -----------------------------------------------------------------------------------------------------------------

  private static final Block.Properties coal_properties = Block.Properties.create(Material.ROCK, MaterialColor.STONE)
                            .hardnessAndResistance(3f, 50f).sound(SoundType.STONE)
                            .doesNotBlockMovement().noDrops();

  public static final BlockAriadneCoal ARIADNE_COAL_X = (BlockAriadneCoal)(new BlockAriadneCoal(
    coal_properties,Direction.Axis.X
  )).setRegistryName(new ResourceLocation(MODID, "ariadne_coal_x"));

  public static final BlockAriadneCoal ARIADNE_COAL_Y = (BlockAriadneCoal)(new BlockAriadneCoal(
    coal_properties,Direction.Axis.Y
  )).setRegistryName(new ResourceLocation(MODID, "ariadne_coal_y"));

  public static final BlockAriadneCoal ARIADNE_COAL_Z = (BlockAriadneCoal)(new BlockAriadneCoal(
    coal_properties,Direction.Axis.Z
  )).setRegistryName(new ResourceLocation(MODID, "ariadne_coal_z"));

  private static final ArrayList<Block> modBlocks;

  static {
    modBlocks = new ArrayList<Block>();
    modBlocks.add(ARIADNE_COAL_X);
    modBlocks.add(ARIADNE_COAL_Y);
    modBlocks.add(ARIADNE_COAL_Z);
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Tile entities bound exclusively to the blocks above
  //--------------------------------------------------------------------------------------------------------------------

  private static final TileEntityType<?> tile_entity_types[] = {
  };

  //--------------------------------------------------------------------------------------------------------------------
  // Items
  //--------------------------------------------------------------------------------------------------------------------

  private static Item.Properties default_item_properties()
  { return (new Item.Properties()).group(ModEngineersTools.ITEMGROUP); }

  public static final ItemRediaTool REDIA_TOOL = (ItemRediaTool)((new ItemRediaTool(
    default_item_properties()
  ).setRegistryName(MODID, "redia_tool")));

  public static final ItemCrushingHammer CRUSHING_HAMMER = (ItemCrushingHammer)((new ItemCrushingHammer(
    default_item_properties()
  ).setRegistryName(MODID, "crushing_hammer")));

  public static final ItemAriadneCoal ARIADNE_COAL = (ItemAriadneCoal)((new ItemAriadneCoal(
    default_item_properties()
  ).setRegistryName(MODID, "ariadne_coal")));

  public static final ItemStimPack STIM_PACK = (ItemStimPack)((new ItemStimPack(
    default_item_properties()
  ).setRegistryName(MODID, "stimpack")));

  public static final ItemSleepingBag SLEEPING_BAG = (ItemSleepingBag)((new ItemSleepingBag(
    default_item_properties()
  ).setRegistryName(MODID, "sleeping_bag")));

  public static final ItemTools IRON_GRIT = (ItemTools)((new ItemTools(
    default_item_properties()
  ).setRegistryName(MODID, "iron_grit")));

  public static final ItemTools GOLD_GRIT = (ItemTools)((new ItemTools(
    default_item_properties()
  ).setRegistryName(MODID, "gold_grit")));

  public static final ItemMusliBar MUSLI_BAR = (ItemMusliBar)((new ItemMusliBar(
    default_item_properties()
  ).setRegistryName(MODID, "musli_bar")));

  public static final ItemMusliBarPress MUSLI_BAR_PRESS = (ItemMusliBarPress)((new ItemMusliBarPress(
    default_item_properties()
  ).setRegistryName(MODID, "musli_bar_press")));

  private static final Item modItems[] = {
    REDIA_TOOL,
    CRUSHING_HAMMER,
    ARIADNE_COAL,
    STIM_PACK,
    SLEEPING_BAG,
    MUSLI_BAR_PRESS,
    MUSLI_BAR,
    IRON_GRIT,
    GOLD_GRIT,
  };

  //--------------------------------------------------------------------------------------------------------------------
  // Containers
  //--------------------------------------------------------------------------------------------------------------------

  private static <T extends Container> ContainerType<T> register(ContainerType.IFactory<T> factory, String regname)
  {
    ContainerType<T> container_type = new ContainerType<T>(factory);
    container_type.setRegistryName(new ResourceLocation(MODID, regname));
    return container_type;
  }

  public static final ContainerType<ItemMusliBarPress.MusliBarPressContainer> CT_MUSLI_BAR_PRESS = register(ItemMusliBarPress.MusliBarPressContainer::new, "ct_musli_bar_press");

  private static final ContainerType<?> CONTAINER_TYPES[] = {
    CT_MUSLI_BAR_PRESS,
  };

  //--------------------------------------------------------------------------------------------------------------------
  // Initialisation events
  //--------------------------------------------------------------------------------------------------------------------

  private static final ArrayList<Block> registeredBlocks;
  private static final ArrayList<Item> registeredItems;

  static {
    registeredBlocks = new ArrayList<Block>();
    registeredBlocks.addAll(modBlocks);
    registeredItems = new ArrayList<Item>();
    registeredItems.addAll(Arrays.asList(modItems));
    for(Block e:registeredBlocks) registeredItems.add(new ModBlockItem(e, (new ModBlockItem.Properties())).setRegistryName(e.getRegistryName()));
  }

  public static ArrayList<Block> allBlocks()
  { return registeredBlocks; }

  @Nonnull
  public static List<Block> getRegisteredBlocks()
  { return Collections.unmodifiableList(registeredBlocks); }

  @Nonnull
  public static List<Item> getRegisteredItems()
  { return Collections.unmodifiableList(registeredItems); }

  public static final void registerBlocks(final RegistryEvent.Register<Block> event)
  {
    for(Block e:registeredBlocks) event.getRegistry().register(e);
    LOGGER.info("Registered " + Integer.toString(registeredBlocks.size()) + " blocks.");
  }

  public static final void registerTileEntities(final RegistryEvent.Register<TileEntityType<?>> event)
  {
    for(final TileEntityType<?> e:tile_entity_types) event.getRegistry().register(e);
    LOGGER.info("Registered " + Integer.toString(tile_entity_types.length) + " tile entities.");
  }

  public static final void registerItems(final RegistryEvent.Register<Item> event)
  {
    for(Item e:registeredItems) event.getRegistry().register(e);
    LOGGER.info("Registered " + Integer.toString(registeredItems.size()) + " items.");
  }

  public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
  { for(final ContainerType<?> e:CONTAINER_TYPES) event.getRegistry().register(e); }

  @OnlyIn(Dist.CLIENT)
  public static void registerGuis(final FMLClientSetupEvent event)
  {
    ScreenManager.registerFactory(CT_MUSLI_BAR_PRESS, ItemMusliBarPress.MusliBarPressGui::new);
  }

  public static final void processRegisteredContent()
  {}
}
