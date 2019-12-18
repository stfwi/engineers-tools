/*
 * @file ModAuxiliaries.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * General commonly used functionality.
 */
package wile.engineerstools.detail;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.ModList;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import wile.engineerstools.ModEngineersTools;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModAuxiliaries
{
  private static final Logger LOGGER = ModEngineersTools.LOGGER;

  // -------------------------------------------------------------------------------------------------------------------
  // Sideness, system/environment, tagging interfaces
  // -------------------------------------------------------------------------------------------------------------------

  public interface IExperimentalFeature {}

  public static boolean isClientSide()
  { return ModEngineersTools.proxy.mc() != null; }

  public static final boolean isModLoaded(final String registry_name)
  { return ModList.get().isLoaded(registry_name); }

  // -------------------------------------------------------------------------------------------------------------------
  // Logging
  // -------------------------------------------------------------------------------------------------------------------

  public static final void logInfo(final String msg)
  { LOGGER.info(msg); }

  public static final void logWarn(final String msg)
  { LOGGER.warn(msg); }

  public static final void logError(final String msg)
  { LOGGER.error(msg); }

  // -------------------------------------------------------------------------------------------------------------------
  // Block handling
  // -------------------------------------------------------------------------------------------------------------------

  public static final AxisAlignedBB getPixeledAABB(double x0, double y0, double z0, double x1, double y1, double z1)
  { return new AxisAlignedBB(x0/16.0, y0/16.0, z0/16.0, x1/16.0, y1/16.0, z1/16.0); }

  // -------------------------------------------------------------------------------------------------------------------
  // Localization, text formatting
  // -------------------------------------------------------------------------------------------------------------------

  /**
   * Text localisation wrapper, implicitly prepends `ModRsGauges.MODID` to the
   * translation keys. Forces formatting argument, nullable if no special formatting shall be applied..
   */
  public static TranslationTextComponent localizable(String modtrkey, @Nullable TextFormatting color, Object... args)
  {
    TranslationTextComponent tr = new TranslationTextComponent(ModEngineersTools.MODID+"."+modtrkey, args);
    if(color!=null) tr.getStyle().setColor(color);
    return tr;
  }

  @OnlyIn(Dist.CLIENT)
  public static String localize(String translationKey, Object... args)
  {
    TranslationTextComponent tr = new TranslationTextComponent(translationKey, args);
    tr.getStyle().setColor(TextFormatting.RESET);
    final String ft = tr.getFormattedText();
    if(ft.contains("${")) {
      // Non-recursive, non-argument lang file entry cross referencing.
      Pattern pt = Pattern.compile("\\$\\{([\\w\\.]+)\\}");
      Matcher mt = pt.matcher(ft);
      StringBuffer sb = new StringBuffer();
      while(mt.find()) mt.appendReplacement(sb, (new TranslationTextComponent(mt.group(1))).getFormattedText().trim());
      mt.appendTail(sb);
      return sb.toString();
    } else {
      return ft;
    }
  }

  /**
   * Returns true if a given key is translated for the current language.
   */
  @OnlyIn(Dist.CLIENT)
  public static boolean hasTranslation(String key)
  { return net.minecraft.client.resources.I18n.hasKey(key); }

  public static final class Tooltip
  {
    @OnlyIn(Dist.CLIENT)
    public static boolean extendedTipCondition()
    {
      /*
      return (
        InputMappings.isKeyDown(ModEngineersTools.proxy.mc().mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
          InputMappings.isKeyDown(ModEngineersTools.proxy.mc().mainWindow.getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)
      );
      */

      return (
        InputMappings.isKeyDown(ModEngineersTools.proxy.mc().   func_228018_at_().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
        InputMappings.isKeyDown(ModEngineersTools.proxy.mc().func_228018_at_().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)
      );
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean helpCondition()
    {
      /*
      return extendedTipCondition() && (
        InputMappings.isKeyDown(ModEngineersTools.proxy.mc().mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) ||
          InputMappings.isKeyDown(ModEngineersTools.proxy.mc().mainWindow.getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL)
      );
      */
      return extendedTipCondition() && (
        InputMappings.isKeyDown(ModEngineersTools.proxy.mc().func_228018_at_().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) ||
        InputMappings.isKeyDown(ModEngineersTools.proxy.mc().func_228018_at_().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL)
      );
    }

    /**
     * Adds an extended tooltip or help tooltip depending on the key states of CTRL and SHIFT.
     * Returns true if the localisable help/tip was added, false if not (either not CTL/SHIFT or
     * no translation found).
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean addInformation(@Nullable String advancedTooltipTranslationKey, @Nullable String helpTranslationKey, List<ITextComponent> tooltip, ITooltipFlag flag, boolean addAdvancedTooltipHints)
    {
      // Note: intentionally not using keybinding here, this must be `control` or `shift`. MC uses lwjgl Keyboard,
      //       so using this also here should be ok.
      final boolean help_available = (helpTranslationKey != null) && ModAuxiliaries.hasTranslation(helpTranslationKey + ".help");
      final boolean tip_available = (advancedTooltipTranslationKey != null) && ModAuxiliaries.hasTranslation(helpTranslationKey + ".tip");
      if((!help_available) && (!tip_available)) return false;
      if(helpCondition()) {
        if(!help_available) return false;
        String s = localize(helpTranslationKey + ".help");
        if(s.isEmpty()) return false;
        tooltip.add(new StringTextComponent(s));
        return true;
      } else if(extendedTipCondition()) {
        if(!tip_available) return false;
        String s = localize(advancedTooltipTranslationKey + ".tip");
        if(s.isEmpty()) return false;
        tooltip.add(new StringTextComponent(s));
        return true;
      } else if(addAdvancedTooltipHints) {
        String s = "";
        if(tip_available) s += localize(ModEngineersTools.MODID + ".tooltip.hint.extended") + (help_available ? " " : "");
        if(help_available) s += localize(ModEngineersTools.MODID + ".tooltip.hint.help");
        tooltip.add(new StringTextComponent(s));
      }
      return false;
    }

    /**
     * Adds an extended tooltip or help tooltip for a given stack depending on the key states of CTRL and SHIFT.
     * Format in the lang file is (e.g. for items): "item.MODID.REGISTRYNAME.tip" and "item.MODID.REGISTRYNAME.help".
     * Return value see method pattern above.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag, boolean addAdvancedTooltipHints)
    { return addInformation(stack.getTranslationKey(), stack.getTranslationKey(), tooltip, flag, addAdvancedTooltipHints); }
  }

  //public static final boolean isModLoaded(final String registry_name)
  //{ return ModList.get().isLoaded(registry_name); }

}
