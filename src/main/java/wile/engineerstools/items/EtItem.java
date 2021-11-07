/*
 * @file ItemTools.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Basic item functionality for mod items.
 */
package wile.engineerstools.items;

import wile.engineerstools.ModConfig;
import wile.engineerstools.libmc.detail.Auxiliaries;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.engineerstools.ModEngineersTools;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class EtItem extends Item
{
  public EtItem(Item.Properties properties)
  { super(properties.tab(ModEngineersTools.ITEMGROUP)); }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
  { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

  @Override
  public Collection<ItemGroup> getCreativeTabs()
  { return ModConfig.isOptedOut(this) ? (ModBlockItem.DISABLED_TABS) : (ModBlockItem.ENABLED_TABS); }

}
