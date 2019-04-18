/*
 * @file ModConfig.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main class for module settings. Handles reading and
 * saving the config file.
 */
package wile.engineerstools.detail;

import wile.engineerstools.ModEngineersTools;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import wile.engineerstools.items.ItemCrushingHammer;
import wile.engineerstools.items.ModItems;
import javax.annotation.Nullable;

@Config(modid = ModEngineersTools.MODID)
@Config.LangKey("engineerstools.config.title")
public class ModConfig
{
  @Config.Comment({"Allows disabling specific features."})
  @Config.Name("Feature opt-outs")
  public static final SettingsOptouts optout = new SettingsOptouts();
  public static final class SettingsOptouts
  {
    @Config.Comment({"Disable ore-duping crushing hammer."})
    @Config.Name("Without crushing hammer")
    @Config.RequiresMcRestart
    public boolean without_crushing_hammer = false;
  }

  @Config.Comment({
    "Settings for beta testing and trouble shooting. Some of the settings " +
    "may be moved to other categories after testing."
  })
  @Config.Name("Miscellaneous")
  public static final SettingsZTesting zmisc = new SettingsZTesting();
  public static final class SettingsZTesting
  {
    @Config.Comment({ "Enables experimental features. Use at own risk." })
    @Config.Name("With experimental")
    @Config.RequiresMcRestart
    public boolean with_experimental = false;

    @Config.Comment({ "Disable all internal recipes, allowing to use alternative pack recipes." })
    @Config.Name("Without recipes")
    @Config.RequiresMcRestart
    public boolean without_recipes = false;

    @Config.Comment({"Disable registration of opt'ed out items. That is normally not a good idea. Your choice."})
    @Config.Name("Without opt-out registration")
    @Config.RequiresMcRestart
    public boolean without_optout_registration = false;
  }

  @Config.Comment({"Tweaks and item behaviour adaptions."})
  @Config.Name("Tweaks")
  public static final SettingsTweaks tweaks = new SettingsTweaks();
  public static final class SettingsTweaks
  {
    @Config.Comment({
      "Defines how much durability the ore crushing hammer loses for each ore " +
      "block it processes to grit."
    })
    @Config.Name("Crushing hammer: Wear-off")
    @Config.RangeInt(min=1, max=32)
    public int crushing_hammer_wearoff = 2;
  }

  @SuppressWarnings("unused")
  @Mod.EventBusSubscriber(modid=ModEngineersTools.MODID)
  private static final class EventHandler
  {
    @SubscribeEvent
    public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
      if(!event.getModID().equals(ModEngineersTools.MODID)) return;
      ConfigManager.sync(ModEngineersTools.MODID, Config.Type.INSTANCE);
      apply();
    }
  }

  @SuppressWarnings("unused")
  public static final void onPostInit(FMLPostInitializationEvent event)
  { apply(); }

  public static final boolean isWithoutOptOutRegistration()
  { return (zmisc!=null) && (zmisc.without_optout_registration); }

  public static final boolean isWithoutRecipes()
  { return (zmisc==null) || (zmisc.without_recipes); }

  public static final boolean isOptedOut(final @Nullable Item item)
  {
    if((item == null) || (optout == null)) return true;
    if((!zmisc.with_experimental) && (item == ModItems.CRUSHING_HAMMER)) return true;
    return false;
  }

  public static final void apply()
  {
    ItemCrushingHammer.on_config("immersiveengineering", tweaks.crushing_hammer_wearoff, 2);
  }

}
