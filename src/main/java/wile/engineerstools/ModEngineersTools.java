/*
 * @file ModEngineersTools.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.engineerstools;

import net.minecraftforge.client.event.ModelRegistryEvent;
import wile.engineerstools.detail.*;
import wile.engineerstools.libmc.detail.*;
import wile.engineerstools.items.*;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, ModConfig.SERVER_CONFIG_SPEC);
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
    public static final void onRegisterContainerTypes(final ModelRegistryEvent event)
    { ModContent.registerModels(); }

    @SubscribeEvent
    public static final void onRegisterSounds(final RegistryEvent.Register<SoundEvent> event)
    { ModResources.registerSoundEvents(event); }

    @SubscribeEvent
    public static final void onCommonSetup(final FMLCommonSetupEvent event)
    {
      wile.engineerstools.libmc.detail.Networking.init(MODID);
      LOGGER.info("Registering recipe condition processor ...");
      CraftingHelper.register(OptionalRecipeCondition.Serializer.INSTANCE);
      ModConfig.apply();
      ModContent.processRegisteredContent();
    }

    @SubscribeEvent
    public static final void onClientSetup(final FMLClientSetupEvent event)
    {
      ModContent.processContentClientSide(event);
      ModContent.registerGuis(event);
      wile.engineerstools.libmc.detail.Overlay.register();
    }

    @SubscribeEvent
    public static final void onConfigLoad(net.minecraftforge.fml.config.ModConfig.Loading event)
    { ModConfig.onLoad(event.getConfig()); }

    @SubscribeEvent
    public static final void onConfigChanged(net.minecraftforge.fml.config.ModConfig.Reloading event)
    { ModConfig.onFileChange(event.getConfig()); }

    // @SubscribeEvent
    public final void onPlayerUpdateEvent(final LivingEvent.LivingUpdateEvent event)
    {
      //   if(!(event.getEntity() instanceof PlayerEntity)) return;
      //   final PlayerEntity player = (PlayerEntity)event.getEntity();
      //   if(player.world == null) return;
    }

    public static final void onSleepingLocationCheckEvent(net.minecraftforge.event.entity.player.SleepingLocationCheckEvent event)
    { SleepingBagItem.onSleepingLocationCheckEvent(event); }

    public static final void onPlayerWakeUpEvent(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event)
    { SleepingBagItem.onPlayerWakeUpEvent(event); }

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
