/*
 * @file ItemCrushingHammer.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Early game manual ore duping hammer. Not much use
 * for other stuff.
 */
package wile.engineerstools.items;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class CrushingHammerItem extends EtItem
{
  public CrushingHammerItem(Item.Properties properties)
  { super(properties
    .maxStackSize(1)
    .defaultMaxDamage(640)
    .setNoRepair()
    );
  }

  @Override
  public int getItemEnchantability()
  { return 0; }

  @Override
  public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
  { return false; }

  @Override
  public boolean isDamageable()
  { return true; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
  { return false; }

}
