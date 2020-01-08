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
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.tuple.Pair;
import wile.engineerstools.blocks.BlockAriadneCoal;
import wile.engineerstools.items.ItemRediaTool;
import wile.engineerstools.items.ItemStimPack;
import wile.engineerstools.items.ModBlockItem;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ModConfig
{
  private static final Logger LOGGER = ModEngineersTools.LOGGER;
  private static final String MODID = ModEngineersTools.MODID;
  public static final CommonConfig COMMON;
  public static final ServerConfig SERVER;
  public static final ClientConfig CLIENT;
  public static final ForgeConfigSpec COMMON_CONFIG_SPEC;
  public static final ForgeConfigSpec SERVER_CONFIG_SPEC;
  public static final ForgeConfigSpec CLIENT_CONFIG_SPEC;

  static {
    final Pair<CommonConfig, ForgeConfigSpec> common_ = (new ForgeConfigSpec.Builder()).configure(CommonConfig::new);
    COMMON_CONFIG_SPEC = common_.getRight();
    COMMON = common_.getLeft();
    final Pair<ServerConfig, ForgeConfigSpec> server_ = (new ForgeConfigSpec.Builder()).configure(ServerConfig::new);
    SERVER_CONFIG_SPEC = server_.getRight();
    SERVER = server_.getLeft();
    final Pair<ClientConfig, ForgeConfigSpec> client_ = (new ForgeConfigSpec.Builder()).configure(ClientConfig::new);
    CLIENT_CONFIG_SPEC = client_.getRight();
    CLIENT = client_.getLeft();
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static void onLoad(final net.minecraftforge.fml.config.ModConfig config)
  {
    try {
      apply();
      LOGGER.info("Loaded config file {}", config.getFileName());
    } catch(Exception ex) {
      LOGGER.error("Failed to apply config file data {}", config.getFileName());
    }
  }

  public static void onFileChange(final net.minecraftforge.fml.config.ModConfig config)
  {
    LOGGER.info("Config file changed {}", config.getFileName());
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static class ClientConfig
  {
    ClientConfig(ForgeConfigSpec.Builder builder)
    {
      builder.comment("Settings not loaded on servers.")
        .push("client");
      // --- OPTOUTS ------------------------------------------------------------
      {
      }
      builder.pop();
    }
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static class ServerConfig
  {
    ServerConfig(ForgeConfigSpec.Builder builder)
    {
      builder.comment("Settings not loaded on clients.")
        .push("server");
      builder.pop();
    }
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static class CommonConfig
  {
    // Optout
    public final ForgeConfigSpec.ConfigValue<String> pattern_excludes;
    public final ForgeConfigSpec.ConfigValue<String> pattern_includes;
    public final ForgeConfigSpec.BooleanValue without_crushing_hammer;
    public final ForgeConfigSpec.BooleanValue without_redia_tool;
    // Tweaks
    public final ForgeConfigSpec.IntValue redia_tool_durability;
    public final ForgeConfigSpec.IntValue redia_tool_initial_durability_percent;
    public final ForgeConfigSpec.ConfigValue<String> redia_tool_efficiency_curve;
    public final ForgeConfigSpec.ConfigValue<String> redia_tool_furtune_curve;
    public final ForgeConfigSpec.BooleanValue without_safe_attacking;
    public final ForgeConfigSpec.IntValue redia_tool_attack_cooldown_ms;
    // Misc
    public final ForgeConfigSpec.BooleanValue with_experimental;

    CommonConfig(ForgeConfigSpec.Builder builder)
    {
      builder.comment("Settings affecting the logical server side, but are also configurable in single player.")
        .push("server");
      // --- OPTOUTS ------------------------------------------------------------
      {
        builder.comment("Opt-out settings")
          .push("optout");
        pattern_excludes = builder
          .translation(MODID + ".config.pattern_excludes")
          .comment("Opt-out any block by its registry name ('*' wildcard matching, "
            + "comma separated list, whitespaces ignored. You must match the whole name, "
            + "means maybe add '*' also at the begin and end. Example: '*wood*,*steel*' "
            + "excludes everything that has 'wood' or 'steel' in the registry name. "
            + "The matching result is also traced in the log file. ")
          .define("pattern_excludes", "");
        pattern_includes = builder
          .translation(MODID + ".config.pattern_includes")
          .comment("Prevent blocks from being opt'ed by registry name ('*' wildcard matching, "
            + "comma separated list, whitespaces ignored. Evaluated before all other opt-out checks. "
            + "You must match the whole name, means maybe add '*' also at the begin and end. Example: "
            + "'*wood*,*steel*' includes everything that has 'wood' or 'steel' in the registry name."
            + "The matching result is also traced in the log file.")
          .define("pattern_includes", "");
        without_crushing_hammer = builder
          .translation(MODID + ".config.without_crushing_hammer")
          .comment("Completely disable the crushing hammer.")
          .define("without_crushing_hammer", false);
        without_redia_tool = builder
          .translation(MODID + ".config.without_redia_tool")
          .comment("Completely disable the REDIA tool.")
          .define("without_redia_tool", false);
        builder.pop();
      }
      // --- MISC ---------------------------------------------------------------
      {
        builder.comment("Miscellaneous settings")
          .push("miscellaneous");
        with_experimental = builder
          .translation(MODID + ".config.with_experimental")
          .comment("Enables experimental features. Use at own risk.")
          .define("with_experimental", false);
        builder.pop();
      }
      // --- TWEAKS -------------------------------------------------------------
      {
        builder.comment("Settings to tweak the performance, or use cases normally no change should be required here.")
          .push("tweaks");
        redia_tool_durability = builder
          .translation(MODID + ".config.redia_tool_durability")
          .comment("Durability (maximum item damage) of the REDIA tool.")
          .defineInRange("redia_tool_durability", 3000, 750, 4000);
        redia_tool_initial_durability_percent = builder
          .translation(MODID + ".config.redia_tool_initial_durability_percent")
          .comment("Durability of the REDIA tool in percent, which the tool has when it is crafted. " +
                   "Allows to tune initial repairing investments for getting efficiency and furtune.")
          .defineInRange("redia_tool_initial_durability_percent", 100, 25, 100);
        redia_tool_efficiency_curve = builder
          .translation(MODID + ".config.redia_tool_efficiency_curve")
          .comment(
            "Defines the efficiency scaling depending on the durability. ",
            "Ten values have to given in precent, (between 10 and 250), " +
            "and the curve must be rising left-to-right. 100% corresponds " +
            "to vanilla diamond tools. The first number specifies the efficiency " +
            "factor between 0% and 10% durability, second 10% to 20%, last 90% to 100%."
          )
          .define("redia_tool_efficiency_curve", "10,60,90,100,120,140,170,200,220,230");
        redia_tool_furtune_curve = builder
          .translation(MODID + ".config.redia_tool_furtune_curve")
          .comment(
            "Defines the fortune depending on the durability. ",
            "Ten values have to given as integer numbers, (between 0 and 3), " +
            "and the curve must be rising left-to-right."
          )
          .define("redia_tool_furtune_curve", "0,0,0,0,0,1,2,2,3,3");
        without_safe_attacking = builder
          .translation(MODID + ".config.without_safe_attacking")
          .comment("Disable the REDIA tool feature to prevent accidentally hitting own pets, villagers, or bloody zombie pigmen.")
          .define("without_safe_attacking", false);
        redia_tool_attack_cooldown_ms = builder
          .translation(MODID + ".config.redia_tool_attack_cooldown_ms")
          .comment("If safe attacking is enabled, this defines in milliseconds how long you cannot accidentally hit passive " +
            "non-agressive mobs when breaking blocks. The time does not affect preventing to hit villagers, own pets, or zombie pigmen.")
          .defineInRange("redia_tool_attack_cooldown_ms", 0, 10, 2500);
        builder.pop();
      }
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Optout checks
  //--------------------------------------------------------------------------------------------------------------------

  public static final boolean isOptedOut(final @Nullable Block block)
  { return isOptedOut(block, false); }

  public static final boolean isOptedOut(final @Nullable Item item)
  { return isOptedOut(item, false); }

  public static final boolean isOptedOut(final @Nullable Block block, boolean with_log_details)
  {
    if(block == null) return true;
    if(COMMON == null) return false;
    try {
      if(!with_experimental) {
        //if(block instanceof ModAuxiliaries.IExperimentalFeature) return true;
        //if(ModContent.getExperimentalBlocks().contains(block)) return true;
      }
      final String rn = block.getRegistryName().getPath();
      // Force-include/exclude pattern matching
      try {
        for(String e:includes_) {
          if(rn.matches(e)) {
            if(with_log_details) LOGGER.info("Optout force include: " + rn);
            return false;
          }
        }
        for(String e:excludes_) {
          if(rn.matches(e)) {
            if(with_log_details) LOGGER.info("Optout force exclude: " + rn);
            return true;
          }
        }
      } catch(Throwable ex) {
        LOGGER.error("optout include pattern failed, disabling.");
        includes_.clear();
        excludes_.clear();
      }
    } catch(Exception ex) {
      LOGGER.error("Exception evaluating the optout config: '" + ex.getMessage() + "'");
    }
    return false;
  }

  public static final boolean isOptedOut(final @Nullable Item item, boolean with_log_details)
  {
    if(item == null) return true;
    if((item instanceof ModBlockItem) && (((ModBlockItem)item).getBlock() instanceof BlockAriadneCoal)) return true;
    if(COMMON == null) return false;
    try {
      final String rn = item.getRegistryName().getPath();
      // Force-include/exclude pattern matching
      try {
        for(String e:includes_) {
          if(rn.matches(e)) {
            if(with_log_details) LOGGER.info("Optout force include: " + rn);
            return false;
          }
        }
        for(String e:excludes_) {
          if(rn.matches(e)) {
            if(with_log_details) LOGGER.info("Optout force exclude: " + rn);
            return true;
          }
        }
      } catch(Throwable ex) {
        LOGGER.error("optout include pattern failed, disabling.");
        includes_.clear();
        excludes_.clear();
      }
    } catch(Exception ex) {
      LOGGER.error("Exception evaluating the optout config: '" + ex.getMessage() + "'");
    }
    return false;
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Cache
  //--------------------------------------------------------------------------------------------------------------------
  private static final ArrayList<String> includes_ = new ArrayList<String>();
  private static final ArrayList<String> excludes_ = new ArrayList<String>();
  public static boolean with_experimental = false;

  public static final void apply()
  {
    with_experimental = COMMON.with_experimental.get();
    {
      String inc = COMMON.pattern_includes.get().toLowerCase().replaceAll(MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
      if(COMMON.pattern_includes.get() != inc) COMMON.pattern_includes.set(inc);
      if(!inc.isEmpty()) LOGGER.info("Pattern includes: '" + inc + "'");
      String[] incl = inc.split(",");
      includes_.clear();
      for(int i=0; i< incl.length; ++i) {
        incl[i] = incl[i].replaceAll("[*]", ".*?");
        if(!incl[i].isEmpty()) includes_.add(incl[i]);
      }
    }
    {
      String exc = COMMON.pattern_includes.get().toLowerCase().replaceAll(MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
      if(!exc.isEmpty()) LOGGER.info("Pattern excludes: '" + exc + "'");
      String[] excl = exc.split(",");
      excludes_.clear();
      for(int i=0; i< excl.length; ++i) {
        excl[i] = excl[i].replaceAll("[*]", ".*?");
        if(!excl[i].isEmpty()) excludes_.add(excl[i]);
      }
    }
    ItemRediaTool.on_config(
      false,
      false,
      false,
      COMMON.redia_tool_durability.get(),
      COMMON.redia_tool_furtune_curve.get(),
      COMMON.redia_tool_furtune_curve.get(),
      COMMON.redia_tool_initial_durability_percent.get(),
      COMMON.redia_tool_attack_cooldown_ms.get(),
      COMMON.without_safe_attacking.get()
    );
    ItemStimPack.on_config( //@todo: make config
      2,
      3,
      3
    );
  }
}
