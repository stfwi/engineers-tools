/*
 * @file ItemStimPack.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Automatic healing injection when low health is detected.
 */
package wile.engineerstools.items;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import wile.engineerstools.ModEngineersTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.World;


public class DivingCapsuleItem extends EtItem
{
  private static int item_damage = 10;
  private static int trigger_threshold = 3;
  private static int instant_refresh = 6;

  public static void on_config(int maxuses, int trigger_threshold_bubbles, int instant_refresh_bubbles)
  {
    item_damage = Math.max(100/MathHelper.clamp(maxuses, 1, 100), 1);
    trigger_threshold = MathHelper.clamp(trigger_threshold_bubbles, 2, 7);
    instant_refresh = MathHelper.clamp(instant_refresh_bubbles, 1, 10);
    ModEngineersTools.LOGGER.info("Diving Capsule config: uses:"+maxuses+"(dmg "+item_damage+"), trigger:"+trigger_threshold+" bubbles, push:"+instant_refresh+" bubbles.");
  }

  // -------------------------------------------------------------------------------------------------------------------

  public DivingCapsuleItem(Item.Properties properties)
  { super(properties.stacksTo(1).defaultDurability(100).setNoRepair()); }

  @Override
  public int getEnchantmentValue()
  { return 0; }

  @Override
  public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair)
  { return false; }

  @Override
  public boolean canBeDepleted()
  { return true; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
  { return false; }

  @Override
  public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
  { if((entity instanceof PlayerEntity)) inventoryTick(stack, world, (PlayerEntity)entity, itemSlot); }

  private void inventoryTick(final ItemStack capsule, final World world, final PlayerEntity player, int itemSlot)
  {
    int air = player.getAirSupply();
    final int max_air = player.getMaxAirSupply();
    if(max_air <= 0) return; // can't help him.
    if((air > (trigger_threshold * max_air / 10)) || (!player.isAlive())) return;
    for(int i=0; i<player.inventory.getContainerSize(); ++i) {
      if(i == itemSlot) break;
      if(player.inventory.getItem(i).sameItemStackIgnoreDurability(capsule)) return;
    }
    if(!world.isClientSide) {
      player.setAirSupply(Math.min(player.getAirSupply()+(instant_refresh*max_air/10), player.getMaxAirSupply()));
      int dmg = capsule.getDamageValue() + item_damage;
      if(dmg >= capsule.getMaxDamage()) capsule.shrink(1); else capsule.setDamageValue(dmg);
    } else {
      if((capsule.getDamageValue()+item_damage) >= capsule.getMaxDamage()) {
        world.playSound(player, player.blockPosition(), SoundEvents.PLAYER_HURT_DROWN, SoundCategory.PLAYERS, 1.2f, 1.4f);
      } else {
        world.playSound(player, player.blockPosition(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.PLAYERS, 1.0f, 2.2f);
      }
    }
    if(!world.isClientSide) player.inventory.setChanged();
  }
}
