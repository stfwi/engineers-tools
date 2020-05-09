/*
 * @file ModEngineersTools.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.engineerstools;

import wile.engineerstools.libmc.detail.*;
import wile.engineerstools.detail.*;
import wile.engineerstools.items.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod("engineerstools")
public class ModEngineersTools
{
  public static final String MODID = "engineerstools";
  public static final String MODNAME = "Engineer's Tools";
  public static final int VERSION_DATAFIXER = 0;
  public static final Logger LOGGER = LogManager.getLogger();

  // -------------------------------------------------------------------------------------------------------------------

  public ModEngineersTools()
  {
    Auxiliaries.init(MODID, LOGGER, ModConfig::getServerConfig);
    Auxiliaries.logGitVersion(MODNAME);
    OptionalRecipeCondition.init(MODID, LOGGER);
    MinecraftForge.EVENT_BUS.register(this);
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.COMMON_CONFIG_SPEC);
    MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onSleepingLocationCheckEvent);
    MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onPlayerWakeUpEvent);
  }

  // -------------------------------------------------------------------------------------------------------------------

  public static final Logger logger() { return LOGGER; }

  // -------------------------------------------------------------------------------------------------------------------
  // Events
  // -------------------------------------------------------------------------------------------------------------------

  @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
  public static final class ForgeEvents
  {
    @SubscribeEvent
    public static final void onItemRegistry(final RegistryEvent.Register<Item> event)
    { ModContent.registerItems(event); }

    @SubscribeEvent
    public static final void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event)
    { event.getRegistry().register(wile.engineerstools.libmc.detail.ExtendedShapelessRecipe.SERIALIZER); }

    @SubscribeEvent
    public static final void onBlockRegistry(final RegistryEvent.Register<Block> event)
    { ModContent.registerBlocks(event); }

    @SubscribeEvent
    public static final void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event)
    { ModContent.registerTileEntities(event); }

    @SubscribeEvent
    public static final void onRegisterContainerTypes(final RegistryEvent.Register<ContainerType<?>> event)
    { ModContent.registerContainers(event); }

    @SubscribeEvent
    public static final void onRegisterSounds(final RegistryEvent.Register<SoundEvent> event)
    { ModResources.registerSoundEvents(event); }

    @SubscribeEvent
    public static final void onCommonSetup(final FMLCommonSetupEvent event)
    {
      ModConfig.apply();
      LOGGER.info("Registering recipe condition processor ...");
      CraftingHelper.register(OptionalRecipeCondition.Serializer.INSTANCE);
      LOGGER.info("Init networking, content processors ...");
      Networking.init(MODID);
      ModContent.processRegisteredContent();
    }

    @SubscribeEvent
    public static final void onClientSetup(final FMLClientSetupEvent event)
    { ModContent.registerGuis(event); }

    @SubscribeEvent
    public static final void onConfigLoad(net.minecraftforge.fml.config.ModConfig.Loading event)
    { ModConfig.onLoad(event.getConfig()); }

    @SubscribeEvent
    public static final void onConfigChanged(net.minecraftforge.fml.config.ModConfig.ConfigReloading event)
    { ModConfig.onFileChange(event.getConfig()); }

    @SubscribeEvent
    public static void onDataGeneration(GatherDataEvent event)
    { event.getGenerator().addProvider(new wile.engineerstools.libmc.datagen.LootTableGen(event.getGenerator(), ModContent::allBlocks)); }

    public final void onPlayerUpdateEvent(final LivingEvent.LivingUpdateEvent event)
    {
     if(!(event.getEntity() instanceof PlayerEntity)) return;
     final PlayerEntity player = (PlayerEntity)event.getEntity();
     if(player.world == null) return;
    }

    public static final void onSleepingLocationCheckEvent(net.minecraftforge.event.entity.player.SleepingLocationCheckEvent event)
    { ItemSleepingBag.onSleepingLocationCheckEvent(event); }

    public static final void onPlayerWakeUpEvent(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event)
    { ItemSleepingBag.onPlayerWakeUpEvent(event); }

  }

  // -------------------------------------------------------------------------------------------------------------------
  // Item group / creative tab
  // -------------------------------------------------------------------------------------------------------------------

  public static final ItemGroup ITEMGROUP = (new ItemGroup("tab" + MODID) {
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon()
    { return new ItemStack(ModContent.CRUSHING_HAMMER); }
  });
}
