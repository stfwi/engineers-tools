/*
 * @file ChargedLapisItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * XP storage consumable.
 */
package wile.engineerstools.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.world.World;


public class ChargedLapisItem extends EtItem
{
  public ChargedLapisItem(Item.Properties properties)
  { super(properties.stacksTo(64)); }

  @Override
  public boolean isFoil(ItemStack stack)
  { return true; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
  {
    ItemStack stack = player.getItemInHand(hand);
    onUseTick(world, player, stack, 1);
    return ActionResult.sidedSuccess(stack, world.isClientSide());
  }

  @Override
  public void onUseTick(World world, LivingEntity entity, ItemStack stack, int count)
  {
    if(!(entity instanceof PlayerEntity)) return;
    PlayerEntity player = (PlayerEntity)entity;
    if(world.isClientSide()) {
      float rnd = (float)(world.getRandom().nextDouble()*.2);
      for(float pitch: new float[]{0.45f,0.4f,0.5f}) world.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.05f, pitch+rnd);
    } else {
      stack.shrink(1);
      player.giveExperienceLevels(1);
      player.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
      player.clearFire();
      player.heal(player.getMaxHealth()/20);
    }
  }

}
