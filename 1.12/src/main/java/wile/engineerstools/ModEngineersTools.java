/*
 * @file ModEngineersTools.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main mod class.
 */
package wile.engineerstools;

import wile.engineerstools.detail.*;
import wile.engineerstools.items.*;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nonnull;


@Mod(
  modid = ModEngineersTools.MODID,
  name = ModEngineersTools.MODNAME,
  version = ModEngineersTools.MODVERSION,
  dependencies = "required-after:forge@[14.23.5.2768,);before:immersiveengineering",
  useMetadata = true,
  updateJSON = "https://raw.githubusercontent.com/stfwi/engineerstools/develop/meta/update.json",
  certificateFingerprint = ((ModEngineersTools.MODFINGERPRINT==("@"+"MOD_SIGNSHA1"+"@")) ? "" : ModEngineersTools.MODFINGERPRINT)
)
@SuppressWarnings({"unused", "ConstantConditions"})
public class ModEngineersTools
{
  public static final String MODID = "engineerstools";
  public static final String MODNAME = "Engineer's Tools";
  public static final String MODVERSION = "@MOD_VERSION@";
  public static final String MODMCVERSION = "@MOD_MCVERSION@";
  public static final String MODFINGERPRINT = "@MOD_SIGNSHA1@";
  public static final String MODBUILDID = "@MOD_BUILDID@";
  public static Logger logger;

  @Mod.Instance
  public static ModEngineersTools instance;

  //--------------------------------------------------------------------------------------------------------------------
  // Side handling
  //--------------------------------------------------------------------------------------------------------------------

  @SidedProxy(clientSide = "wile.engineerstools.detail.ClientProxy", serverSide = "wile.engineerstools.detail.ServerProxy")
  public static IProxy proxy;

  public interface IProxy
  {
    default void preInit(final FMLPreInitializationEvent e) {}
    default void init(final FMLInitializationEvent e) {}
    default void postInit(final FMLPostInitializationEvent e) {}
    default World getWorlClientSide() { return null; }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Init
  //--------------------------------------------------------------------------------------------------------------------

  @Mod.EventHandler
  public void preInit(final FMLPreInitializationEvent event)
  {
    logger = event.getModLog();
    logger.info(MODNAME + ": Version " + MODMCVERSION + "-" + MODVERSION + ( (MODBUILDID=="@"+"MOD_BUILDID"+"@") ? "" : (" "+MODBUILDID) ) + ".");
    if(MODFINGERPRINT=="@"+"MOD_SIGNSHA1"+"@") {
      logger.warn(MODNAME + ": Mod is NOT signed by the author.");
    } else {
      logger.info(MODNAME + ": Found valid fingerprint " + MODFINGERPRINT + ".");
    }
    proxy.preInit(event);
    // MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
  }

  @Mod.EventHandler
  public void init(final FMLInitializationEvent event)
  { proxy.init(event); }

  @Mod.EventHandler
  public void postInit(final FMLPostInitializationEvent event)
  {
    ModConfig.onPostInit(event);
    proxy.postInit(event);
    if(RecipeCondModSpecific.num_skipped > 0) logger.info("Excluded " + RecipeCondModSpecific.num_skipped + " recipes due to config opt-out.");
    if(ModConfig.zmisc.with_experimental) logger.info("Included experimental features due to mod config.");
  }

  @Mod.EventBusSubscriber
  public static final class RegistrationSubscriptions
  {
    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event)
    { ModItems.registerItems(event); }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    { ItemCrushingHammer.CrushingHammerRecipe.registerAll(event); }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(final ModelRegistryEvent event)
    { ModItems.initModels(); }
  }

  public static final CreativeTabs CREATIVE_TAB_ENGINEERSTOOLS = (new CreativeTabs("tabengineerstools") {
    @Override
    @SideOnly(Side.CLIENT)
    public @Nonnull ItemStack createIcon()
    { return new ItemStack(ModItems.CRUSHING_HAMMER); }
  });

  //--------------------------------------------------------------------------------------------------------------------
  // Player interaction/notification
  //--------------------------------------------------------------------------------------------------------------------

  @Mod.EventBusSubscriber
  public static class PlayerEventHandler
  {
    @SubscribeEvent
    public void update(final LivingEvent.LivingUpdateEvent event)
    {
      if(!(event.getEntity() instanceof EntityPlayer)) return;
      final EntityPlayer player = (EntityPlayer)event.getEntity();
      if(player.world == null) return;
    }
  }

}
