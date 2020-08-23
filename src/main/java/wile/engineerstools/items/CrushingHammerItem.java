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

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


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

  @Override
  public int getBurnTime(ItemStack stack)
  { return 0; }

  @Override
  public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker)
  { return true; }

  @Override
  public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity target)
  {
    if(!(target instanceof LivingEntity)) return true;
    World world = player.getEntityWorld();
    if(world.isRemote()) return true;
    boolean hard = (target instanceof MonsterEntity) && (((MonsterEntity)target).getAttackTarget() != null);
    ((LivingEntity)target).applyKnockback(hard ? 1.2f : 0.3f, Math.sin(Math.PI/180 * player.rotationYaw), -Math.cos(Math.PI/180 * player.rotationYaw));
    if(hard) {
      if(world.getRandom().nextInt(1) == 0) stack.damageItem(1, player, p->p.sendBreakAnimation(player.getActiveHand()));
      world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.BLOCK_ANVIL_PLACE, player.getSoundCategory(), 0.2f, 0.05f); // ITEM_TRIDENT_HIT_GROUND
    } else {
      world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.BLOCK_BAMBOO_HIT, player.getSoundCategory(), 0.5f, 0.3f);
    }
    return true;
  }

  @Override
  public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity player)
  {
    if((world.isRemote()) || (!(player instanceof PlayerEntity))) return true;
    if(state.getBlockHardness(world, pos) > 0.5) stack.damageItem(1, player, p->p.sendBreakAnimation(player.getActiveHand()));
    return false;
  }
}
