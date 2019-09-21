/*
 * @file ItemStimPack.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 */
package wile.engineerstools.items;

import net.minecraft.entity.EntityLivingBase;
import wile.engineerstools.detail.ModResources;
import wile.engineerstools.eapi.baubles.IBaubleItem;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;


public class ItemStimPack extends ItemTools implements IBaubleItem
{
  private static int max_uses = 2;
  private static float trigger_threshold_health = 3;
  private static float instant_healing_health = 3;

  public ItemStimPack(String registryName)
  {
    super(registryName);
    setMaxStackSize(1);
    setMaxDamage(2);
    setNoRepair();
  }

  @Override
  public boolean isEnchantable(ItemStack stack)
  { return false; }

  @Override
  public boolean isRepairable()
  { return false; }

  @Override
  public int getItemBurnTime(ItemStack itemStack)
  { return 0; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
  { if((entity instanceof EntityPlayer)) inventoryTick(stack, world, (EntityPlayer)entity, itemSlot); }

  @Override
  public void onBaubleTick(ItemStack stack, EntityLivingBase entity)
  { if((entity instanceof EntityPlayer)) inventoryTick(stack, entity.getEntityWorld(), (EntityPlayer)entity, -1); }

  private void inventoryTick(final ItemStack stimpack, final World world, final EntityPlayer player, int itemSlot)
  {
    NBTTagCompound nbt = stimpack.getTagCompound();
    if(nbt==null) nbt = new NBTTagCompound();
    if(nbt.hasKey("cd")) {
      // cooldown
      if(nbt.hasKey("ta")) nbt.removeTag("ta");
      int t = nbt.getInteger("cd");
      if(t > 0) {
        nbt.setInteger("cd", t-1);
      } else {
        nbt.removeTag("cd");
        if(nbt.getSize()==0) nbt = null;
      }
    } else {
      float health = player.getHealth();
      if((health > (trigger_threshold_health*2)) || (player.isDead)) return;
      int t = nbt.getInteger("ta");
      if(t < 20) {
        if(t == 2) world.playSound(player, player.getPosition(), ModResources.STIMPACK_INJECT_SOUND, SoundCategory.BLOCKS, 1f, 1f);
        nbt.setInteger("ta", t+1);
      } else {
        nbt.removeTag("ta");
        if(!world.isRemote) {
          int dmg = stimpack.getItemDamage() + 1;
          if(dmg >= stimpack.getMaxDamage()) stimpack.shrink(1); else stimpack.setItemDamage(dmg);
          player.setHealth(health+instant_healing_health*2); // setHealth already clamps to maxHealth.
          player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 400));
          player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 300));
          player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 200));
          player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 200));
        }
      }
      for(int i=0; i<player.inventory.getSizeInventory(); ++i) {
        if(i == itemSlot) continue;
        ItemStack other = player.inventory.getStackInSlot(i);
        if(!other.isItemEqualIgnoreDurability(stimpack)) continue;
        NBTTagCompound other_nbt = other.getTagCompound();
        if(other_nbt==null) other_nbt = new NBTTagCompound();
        other_nbt.removeTag("ta");
        other_nbt.setInteger("cd", 30);
      }
      nbt.removeTag("cd");
      if(nbt.getSize()==0) stimpack.setTagCompound(null);
      if(!world.isRemote) player.inventory.markDirty();
    }
    stimpack.setTagCompound(nbt);
  }
}
