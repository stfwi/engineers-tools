/*
 * @file ModConfig.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main class for module settings. Handles reading and
 * saving the config file.
 */
package wile.engineerstools.detail;

import wile.engineerstools.ModContent;
import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.items.*;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
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

    @Config.Comment({ "Disable upward vein chopping of the REDIA tool." })
    @Config.Name("Without REDIA tree chopping")
    public boolean without_redia_tree_chopping = false;

    @Config.Comment({ "Disable hoeing function of the REDIA tool." })
    @Config.Name("Without REDIA hoeing")
    public boolean without_redia_hoeing = false;

    @Config.Comment({ "Disable torch placing function of the REDIA tool." })
    @Config.Name("Without REDIA torch placing")
    public boolean without_redia_torchplacing = false;
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

    @Config.Comment({ "Defines how much durability REDIA tool has." })
    @Config.Name("REDIA tool: Durability")
    @Config.RangeInt(min=800, max=3000)
    public int redia_durability = 2200;

    @Config.Comment({
      "Defines the efficiency scaling depending on the durability. ",
      "Ten values have to given in precent, (between 10 and 250), " +
      "and the curve must be rising left-to-right. 100% corresponds " +
      "to vanilla diamond tools. The first number specifies the efficiency " +
      "factor between 0% and 10% durability, second 10% to 20%, last 90% to 100%."
    })
    @Config.Name("REDIA tool: Dur-Eff curve")
    public String redia_efficiency_curve = "10,60,90,100,120,140,170,200,220,230";

    @Config.Comment({
      "Defines the fortune depending on the durability. ",
      "Ten values have to given as integer numbers, (between 0 and 3), " +
      "and the curve must be rising left-to-right."
    })
    @Config.Name("REDIA tool: Dur-Fortune curve")
    public String redia_fortune_curve = "0,0,0,0,0,1,1,1,2,3";

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

  public static final boolean isWithoutRecipes()
  { return (zmisc==null) || (zmisc.without_recipes); }

  public static final boolean isOptedOut(final @Nullable Item item)
  {
    if((item == null) || (optout == null)) return true;
    if((optout.without_crushing_hammer) && (item == ModContent.CRUSHING_HAMMER)) return true;
    return false;
  }

  public static final void apply()
  {
    ItemCrushingHammer.on_config("immersiveengineering", tweaks.crushing_hammer_wearoff, 2);
    ItemRediaTool.on_config(optout.without_redia_torchplacing, optout.without_redia_hoeing, optout.without_redia_tree_chopping,
                           tweaks.redia_durability, tweaks.redia_efficiency_curve, tweaks.redia_fortune_curve);
  }

}
