/*
 * @file ModContent.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.engineerstools;

import wile.engineerstools.blocks.AriadneCoalBlock;
import wile.engineerstools.items.*;
import wile.engineerstools.items.MaterialBoxItem.MaterialBoxContainer;
import wile.engineerstools.items.MaterialBoxItem.MaterialBoxGui;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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

  public static final AriadneCoalBlock ARIADNE_COAL_X = (AriadneCoalBlock)(new AriadneCoalBlock(
    coal_properties,Direction.Axis.X
  )).setRegistryName(new ResourceLocation(MODID, "ariadne_coal_x"));

  public static final AriadneCoalBlock ARIADNE_COAL_Y = (AriadneCoalBlock)(new AriadneCoalBlock(
    coal_properties,Direction.Axis.Y
  )).setRegistryName(new ResourceLocation(MODID, "ariadne_coal_y"));

  public static final AriadneCoalBlock ARIADNE_COAL_Z = (AriadneCoalBlock)(new AriadneCoalBlock(
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

  public static final RediaToolItem REDIA_TOOL = (RediaToolItem)((new RediaToolItem(
    default_item_properties().maxStackSize(1).rarity(Rarity.RARE)
  ).setRegistryName(MODID, "redia_tool")));

  public static final CrushingHammerItem CRUSHING_HAMMER = (CrushingHammerItem)((new CrushingHammerItem(
    default_item_properties().maxStackSize(1).rarity(Rarity.UNCOMMON)
  ).setRegistryName(MODID, "crushing_hammer")));

  public static final AriadneCoalItem ARIADNE_COAL = (AriadneCoalItem)((new AriadneCoalItem(
    default_item_properties().maxStackSize(1).rarity(Rarity.UNCOMMON)
  ).setRegistryName(MODID, "ariadne_coal")));

  public static final AutoStimPackItem STIM_PACK = (AutoStimPackItem)((new AutoStimPackItem(
    default_item_properties().maxStackSize(1).rarity(Rarity.UNCOMMON)
  ).setRegistryName(MODID, "stimpack")));

  public static final DivingCapsuleItem DIVING_CAPSULE = (DivingCapsuleItem)((new DivingCapsuleItem(
    default_item_properties().maxStackSize(1).rarity(Rarity.UNCOMMON)
  ).setRegistryName(MODID, "diving_capsule")));

  public static final SleepingBagItem SLEEPING_BAG = (SleepingBagItem)((new SleepingBagItem(
    default_item_properties().maxStackSize(1).rarity(Rarity.UNCOMMON)
  ).setRegistryName(MODID, "sleeping_bag")));

  public static final GritItem IRON_GRIT = (GritItem)((new GritItem(
    default_item_properties()
  ).setRegistryName(MODID, "iron_grit")));

  public static final GritItem GOLD_GRIT = (GritItem)((new GritItem(
    default_item_properties()
  ).setRegistryName(MODID, "gold_grit")));

  public static final MusliBarItem MUSLI_BAR = (MusliBarItem)((new MusliBarItem(
    default_item_properties().rarity(Rarity.COMMON)
  ).setRegistryName(MODID, "musli_bar")));

  public static final MusliBarPressItem MUSLI_BAR_PRESS = (MusliBarPressItem)((new MusliBarPressItem(
    default_item_properties().maxStackSize(1).rarity(Rarity.UNCOMMON)
  ).setRegistryName(MODID, "musli_bar_press")));

  public static final MaterialBoxItem MATERIAL_BAG = (MaterialBoxItem)((new MaterialBoxItem(
    default_item_properties().rarity(Rarity.UNCOMMON)
  ).setRegistryName(MODID, "material_box")));


  private static final Item modItems[] = {
    REDIA_TOOL,
    CRUSHING_HAMMER,
    ARIADNE_COAL,
    STIM_PACK,
    DIVING_CAPSULE,
    SLEEPING_BAG,
    MATERIAL_BAG,
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

  public static final ContainerType<MusliBarPressItem.MusliBarPressContainer> CT_MUSLI_BAR_PRESS = register(MusliBarPressItem.MusliBarPressContainer::new, "ct_musli_bar_press");
  public static final ContainerType<MaterialBoxContainer> CT_MATERIAL_BAG = register(MaterialBoxContainer::new, "ct_material_bag");

  private static final ContainerType<?> CONTAINER_TYPES[] = {
    CT_MUSLI_BAR_PRESS,
    CT_MATERIAL_BAG,
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
    ScreenManager.registerFactory(CT_MUSLI_BAR_PRESS, MusliBarPressItem.MusliBarPressGui::new);
    ScreenManager.registerFactory(CT_MATERIAL_BAG, MaterialBoxGui::new);
  }

  public static final void processRegisteredContent()
  {}
}
