/*
 * @file ItemStimPack.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Automatic healing injection when low health is detected.
 */
package wile.engineerstools.items;

import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.detail.ModResources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;


public class AutoStimPackItem extends EtItem
{
  private static int max_uses = 2;
  private static float trigger_threshold_health = 3;
  private static float instant_healing_health = 3;

  public static void on_config(int maxuses, int trigger_threshold_hearts, int instant_healing_hearts)
  {
    max_uses = MathHelper.clamp(maxuses, 1, 3);
    trigger_threshold_health = MathHelper.clamp(trigger_threshold_hearts, 2, 5);
    instant_healing_health = MathHelper.clamp(instant_healing_hearts, 1, 5);
    ModEngineersTools.LOGGER.info("Stimpack config: uses:"+max_uses+", trigger:"+trigger_threshold_health+" hearts, inst-heal:"+instant_healing_health+" hearts.");
  }

  // -------------------------------------------------------------------------------------------------------------------

  public AutoStimPackItem(Item.Properties properties)
  { super(properties.stacksTo(1).defaultDurability(2).setNoRepair()); }

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

  private void inventoryTick(final ItemStack stimpack, final World world, final PlayerEntity player, int itemSlot)
  {
    CompoundNBT nbt = stimpack.getOrCreateTag();
    if(nbt.contains("cd")) {
      // cooldown
      if(nbt.contains("ta")) nbt.remove("ta");
      int t = nbt.getInt("cd");
      if(t > 0) {
        nbt.putInt("cd", t-1);
      } else {
        nbt.remove("cd");
        if(nbt.size()==0) nbt = null;
      }
    } else {
      float health = player.getHealth();
      if((health > (trigger_threshold_health*2)) || (!player.isAlive())) return;
      int t = nbt.getInt("ta");
      if(t < 20) {
        if(t == 2) world.playSound(player, player.blockPosition(), ModResources.STIMPACK_INJECT_SOUND, SoundCategory.BLOCKS, 1f, 1f);
        nbt.putInt("ta", t+1);
      } else {
        nbt.remove("ta");
        if(!world.isClientSide) {
          int dmg = stimpack.getDamageValue() + 1;
          if(dmg >= stimpack.getMaxDamage()) stimpack.shrink(1); else stimpack.setDamageValue(dmg);
          player.setHealth(health+instant_healing_health*2); // setHealth already clamps to maxHealth.
          player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 400));
          player.addEffect(new EffectInstance(Effects.REGENERATION, 300));
          player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 200));
          player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 200));
        }
      }
      for(int i=0; i<player.inventory.getContainerSize(); ++i) {
        if(i == itemSlot) continue;
        ItemStack other = player.inventory.getItem(i);
        if(!other.sameItemStackIgnoreDurability(stimpack)) continue;
        CompoundNBT other_nbt = other.getOrCreateTag();
        other_nbt.remove("ta");
        other_nbt.putInt("cd", 30);
      }
      nbt.remove("cd");
      if(nbt.size()==0) nbt = null;
      if(!world.isClientSide) player.inventory.setChanged();
    }
    stimpack.setTag(nbt);
  }
}
