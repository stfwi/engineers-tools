/*
 * @file ModConfig.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main class for module settings. Handles reading and
 * saving the config file.
 */
package wile.engineerstools;

import wile.engineerstools.blocks.*;
import wile.engineerstools.items.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.tuple.Pair;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;


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
      LOGGER.info("Loading config file {}", config.getFileName());
      apply();
    } catch(Exception ex) {
      LOGGER.error("Failed to apply config file data {}", config.getFileName());
    }
  }

  public static void onFileChange(final net.minecraftforge.fml.config.ModConfig config)
  {
    try {
      LOGGER.info("Config file changed {}", config.getFileName());
      apply();
    } catch(Exception ex) {
      LOGGER.error("Failed to apply config file data {}", config.getFileName());
    }
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

  public static class CommonConfig
  {
    CommonConfig(ForgeConfigSpec.Builder builder)
    {
      builder.comment("!Server side config had to be moved to engineerstools-server.toml in the 'serverconfig' directory of the game!")
        .push("common");
      builder.pop();
    }
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static class ServerConfig
  {
    // Optout
    public final ForgeConfigSpec.ConfigValue<String> pattern_excludes;
    public final ForgeConfigSpec.ConfigValue<String> pattern_includes;
    public final ForgeConfigSpec.BooleanValue without_crushing_hammer;
    public final ForgeConfigSpec.BooleanValue without_redia_tool;
    public final ForgeConfigSpec.BooleanValue without_stimpack;
    public final ForgeConfigSpec.BooleanValue without_diving_capsule;
    public final ForgeConfigSpec.BooleanValue without_ariadne_coal;
    public final ForgeConfigSpec.BooleanValue without_sleeping_bag;
    public final ForgeConfigSpec.BooleanValue without_musli_bar;
    public final ForgeConfigSpec.BooleanValue without_material_box;
    // Tweaks
    public final ForgeConfigSpec.IntValue redia_tool_durability;
    public final ForgeConfigSpec.IntValue redia_tool_initial_durability_percent;
    public final ForgeConfigSpec.ConfigValue<String> redia_tool_efficiency_curve;
    public final ForgeConfigSpec.ConfigValue<String> redia_tool_furtune_curve;
    public final ForgeConfigSpec.BooleanValue without_safe_attacking;
    // Misc
    public final ForgeConfigSpec.BooleanValue with_experimental;

    ServerConfig(ForgeConfigSpec.Builder builder)
    {
      builder.comment("Settings affecting the logical server side, also valid for single player games.")
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
        // @Config.Name("Without indicators")
        without_redia_tool = builder
          .translation(MODID + ".config.without_redia_tool")
          .comment("Completely disable the REDIA tool.")
          .define("without_redia_tool", false);
        without_stimpack = builder
          .translation(MODID + ".config.without_stimpack")
          .comment("Completely disable the Auto Stim Pack.")
          .define("without_stimpack", false);
        without_diving_capsule = builder
          .translation(MODID + ".config.without_diving_capsule")
          .comment("Completely disable the Diving Air Capsule.")
          .define("without_diving_capsule", false);
        without_ariadne_coal = builder
          .translation(MODID + ".config.without_ariadne_coal")
          .comment("Completely disable the Ariadne Coal.")
          .define("without_ariadne_coal", false);
        without_sleeping_bag = builder
          .translation(MODID + ".config.without_sleeping_bag")
          .comment("Completely disable the Sleeping Bag.")
          .define("without_sleeping_bag", false);
        without_musli_bar = builder
          .translation(MODID + ".config.without_musli_bar")
          .comment("Completely disable the Muslee Bar and Muslee Bar Press.")
          .define("without_musli_bar", false);
        without_material_box = builder
          .translation(MODID + ".config.without_material_box")
          .comment("Completely disable the Material Box.")
          .define("without_material_box", false);
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
        // @Config.Name("Performance and usability tweaks")
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
          .defineInRange("redia_tool_initial_durability_percent", 100, 50, 100);
        redia_tool_efficiency_curve = builder
          .translation(MODID + ".config.redia_tool_efficiency_curve")
          .comment(
            "Defines the efficiency scaling depending on the durability. " +
            "Ten values have to given as integer numbers, (between 0 and 4), " +
            "and the curve must be rising left-to-right. 0 corresponds " +
            "to vanilla diamond tools. The first number specifies the efficiency " +
            "between 0% and 10% durability, second 10% to 20%, last 90% to 100%."
          )
          .define("redia_tool_efficiency_curve", "0,1,1,2,2,3,3,3,3,4");
        redia_tool_furtune_curve = builder
          .translation(MODID + ".config.redia_tool_furtune_curve")
          .comment(
            "Defines the fortune depending on the durability. " +
            "Ten values have to given as integer numbers, (between 0 and 3), " +
            "and the curve must be rising left-to-right. The first number specifies the furtune " +
            "between 0% and 10% durability, second 10% to 20%, last 90% to 100%."
          )
          .define("redia_tool_furtune_curve", "0,0,0,0,1,1,1,1,2,3");
        without_safe_attacking = builder
          .translation(MODID + ".config.without_safe_attacking")
          .comment("Disable the REDIA tool feature to prevent accidentally hitting own pets, villagers, or bloody zombie pigmen.")
          .define("without_safe_attacking", false);
        builder.pop();
      }
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Optout checks
  //--------------------------------------------------------------------------------------------------------------------

  public static final boolean isOptedOut(final @Nullable Block block)
  { return isOptedOut(block.asItem()); }

  public static final boolean isOptedOut(final @Nullable Item item)
  { return (item!=null) && optouts_.contains(item.getRegistryName().getPath()); }

  //--------------------------------------------------------------------------------------------------------------------
  // Cache
  //--------------------------------------------------------------------------------------------------------------------

  private static final CompoundNBT server_config_ = new CompoundNBT();
  private static HashSet<String> optouts_ = new HashSet<>();
  public static boolean with_experimental = false;

  public static final CompoundNBT getServerConfig() // config that may be synchronized from server to client via net pkg.
  { return server_config_; }

  private static final void updateOptouts()
  {
    if(SERVER==null) return;
    final ArrayList<String> includes_ = new ArrayList<String>();
    final ArrayList<String> excludes_ = new ArrayList<String>();
    with_experimental = SERVER.with_experimental.get();
    {
      String inc = SERVER.pattern_includes.get().toLowerCase().replaceAll(MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
      if(SERVER.pattern_includes.get() != inc) SERVER.pattern_includes.set(inc);
      if(!inc.isEmpty()) LOGGER.info("Pattern includes: '" + inc + "'");
      String[] incl = inc.split(",");
      includes_.clear();
      for(int i=0; i< incl.length; ++i) {
        incl[i] = incl[i].replaceAll("[*]", ".*?");
        if(!incl[i].isEmpty()) includes_.add(incl[i]);
      }
    }
    {
      String exc = SERVER.pattern_excludes.get().toLowerCase().replaceAll(MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
      if(!exc.isEmpty()) LOGGER.info("Pattern excludes: '" + exc + "'");
      String[] excl = exc.split(",");
      excludes_.clear();
      for(int i=0; i< excl.length; ++i) {
        excl[i] = excl[i].replaceAll("[*]", ".*?");
        if(!excl[i].isEmpty()) excludes_.add(excl[i]);
      }
    }
    {
      boolean with_log_details = false;
      HashSet<String> optouts = new HashSet<>();
      ModContent.getRegisteredItems().stream().filter((Item item) -> {
        if(item==null) return true;
        if((item instanceof ModBlockItem)&&(((ModBlockItem)item).getBlock() instanceof AriadneCoalBlock)) return true;
        if(SERVER.without_crushing_hammer.get()&&(item instanceof CrushingHammerItem)) return true;
        if(SERVER.without_crushing_hammer.get()&&(item instanceof GritItem)) return true;
        if(SERVER.without_redia_tool.get()&&(item instanceof RediaToolItem)) return true;
        if(SERVER.without_ariadne_coal.get()&&(item instanceof AriadneCoalItem)) return true;
        if(SERVER.without_diving_capsule.get()&&(item instanceof DivingCapsuleItem)) return true;
        if(SERVER.without_stimpack.get()&&(item instanceof AutoStimPackItem)) return true;
        if(SERVER.without_sleeping_bag.get()&&(item instanceof SleepingBagItem)) return true;
        if(SERVER.without_musli_bar.get()&&((item instanceof MusliBarItem)||(item instanceof MusliBarPressItem))) return true;
        if(SERVER.without_material_box.get()&&((item instanceof MaterialBoxItem))) return true;
        try {
          final String rn = item.getRegistryName().getPath();
          // Force-include/exclude pattern matching
          try {
            for(String e : includes_) {
              if(rn.matches(e)) {
                if(with_log_details) LOGGER.info("Optout force include: "+rn);
                return false;
              }
            }
            for(String e : excludes_) {
              if(rn.matches(e)) {
                if(with_log_details) LOGGER.info("Optout force exclude: "+rn);
                return true;
              }
            }
          } catch(Throwable ex) {
            LOGGER.error("optout include pattern failed, disabling.");
            includes_.clear();
            excludes_.clear();
          }
        } catch(Exception ex) {
          LOGGER.error("Exception evaluating the optout config: '"+ex.getMessage()+"'");
        }
        return false;
      }).forEach(e -> optouts.add(e.getRegistryName().getPath()));

      ModContent.getRegisteredBlocks().stream().filter(e->(e==null)||isOptedOut(e.asItem())).forEach(e->optouts.add(e.getRegistryName().getPath()));
      optouts_ = optouts;
    }
    {
      String s = String.join(",", optouts_);
      server_config_.putString("optout", s);
      if(!s.isEmpty()) LOGGER.info("Opt-outs:" + s);
    }
  }

  public static final void apply()
  {
    if(SERVER==null) return;
    updateOptouts();
    if(SERVER.redia_tool_efficiency_curve.get().equals("10,60,90,100,120,140,170,200,220,230")) {
      SERVER.redia_tool_efficiency_curve.set("0,1,1,2,2,3,3,3,3,4");
    }
    RediaToolItem.on_config(
      false,
      false,
      false,
      SERVER.redia_tool_durability.get(),
      SERVER.redia_tool_efficiency_curve.get(),
      SERVER.redia_tool_furtune_curve.get(),
      SERVER.redia_tool_initial_durability_percent.get(),
      SERVER.without_safe_attacking.get()
    );
    AutoStimPackItem.on_config(
      2,
      3,
      3
    );
    DivingCapsuleItem.on_config(
      10,
      3,
      7
    );
    MusliBarItem.on_config(
      6,
      1.2
    );
    MusliBarPressItem.on_config(
      512,
      128,
      1,
      6+2 // efficiency loss 1/4
    );
  }
}
