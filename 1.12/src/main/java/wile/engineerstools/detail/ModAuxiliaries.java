/*
 * @file ModAuxiliaries.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * General commonly used functionality.
 */
package wile.engineerstools.detail;

import wile.engineerstools.ModEngineersTools;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModAuxiliaries
{
  /**
   * Text localisation wrapper, implicitly prepends `ModEngineersTools.MODID` to the
   * translation keys. Forces formatting argument, nullable if no special formatting shall be applied..
   */
  public static TextComponentTranslation localizable(String modtrkey, @Nullable TextFormatting color, Object... args)
  {
    TextComponentTranslation tr = new TextComponentTranslation(ModEngineersTools.MODID+"."+modtrkey, args);
    if(color!=null) tr.getStyle().setColor(color);
    return tr;
  }

  @SideOnly(Side.CLIENT)
  public static String localize(String translationKey, Object... args)
  {
    TextComponentTranslation tr = new TextComponentTranslation(translationKey, args);
    tr.getStyle().setColor(TextFormatting.RESET);
    final String ft = tr.getFormattedText();
    if(ft.contains("${")) {
      // Non-recursive, non-argument lang file entry cross referencing.
      Pattern pt = Pattern.compile("\\$\\{([\\w\\.]+)\\}");
      Matcher mt = pt.matcher(ft);
      StringBuffer sb = new StringBuffer();
      while(mt.find()) mt.appendReplacement(sb, (new TextComponentTranslation(mt.group(1))).getFormattedText().trim());
      mt.appendTail(sb);
      return sb.toString();
    } else {
      return ft;
    }
  }

  /**
   * Returns true if a given key is translated for the current language.
   */
  @SideOnly(Side.CLIENT)
  public static boolean hasTranslation(String key)
  { return net.minecraft.client.resources.I18n.hasKey(key); }

  public static final class Tooltip
  {
    @SideOnly(Side.CLIENT)
    public static boolean extendedTipCondition()
    { return (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)); }

    @SideOnly(Side.CLIENT)
    public static boolean helpCondition()
    { return extendedTipCondition() && ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)||Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))); }

    /**
     * Adds an extended tooltip or help tooltip depending on the key states of CTRL and SHIFT.
     * Returns true if the localisable help/tip was added, false if not (either not CTL/SHIFT or
     * no translation found).
     */
    @SideOnly(Side.CLIENT)
    public static boolean addInformation(@Nullable String advancedTooltipTranslationKey, @Nullable String helpTranslationKey, List<String> tooltip, ITooltipFlag flag, boolean addAdvancedTooltipHints)
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
        tooltip.add(s);
        return true;
      } else if(extendedTipCondition()) {
        if(!tip_available) return false;
        String s = localize(advancedTooltipTranslationKey + ".tip");
        if(s.isEmpty()) return false;
        tooltip.add(s);
        return true;
      } else if(addAdvancedTooltipHints) {
        String s = "";
        if(tip_available) s += localize(ModEngineersTools.MODID + ".tooltip.hint.extended") + (help_available ? " " : "");
        if(help_available) s += localize(ModEngineersTools.MODID + ".tooltip.hint.help");
        tooltip.add(s);
      }
      return false;
    }

    /**
     * Adds an extended tooltip or help tooltip for a given stack depending on the key states of CTRL and SHIFT.
     * Format in the lang file is (e.g. for items): "item.MODID.REGISTRYNAME.tip" and "item.MODID.REGISTRYNAME.help".
     * Return value see method pattern above.
     */
    @SideOnly(Side.CLIENT)
    public static boolean addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag, boolean addAdvancedTooltipHints)
    { return addInformation(stack.getTranslationKey(), stack.getTranslationKey(), tooltip, flag, addAdvancedTooltipHints); }
  }
}
