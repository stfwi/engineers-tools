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
  { super(properties.maxStackSize(64)); }

  @Override
  public boolean hasEffect(ItemStack stack)
  { return true; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
  {
    ItemStack stack = player.getHeldItem(hand);
    onUse(world, player, stack, 1);
    return ActionResult.func_233538_a_(stack, world.isRemote());
  }

  @Override
  public void onUse(World world, LivingEntity entity, ItemStack stack, int count)
  {
    if(!(entity instanceof PlayerEntity)) return;
    PlayerEntity player = (PlayerEntity)entity;
    if(world.isRemote()) {
      float rnd = (float)(world.getRandom().nextDouble()*.2);
      for(float pitch: new float[]{0.45f,0.4f,0.5f}) world.playSound(player, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.05f, pitch+rnd);
    } else {
      stack.shrink(1);
      player.addExperienceLevel(1);
      player.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
      player.extinguish();
      player.heal(player.getMaxHealth()/20);
    }
  }

}
