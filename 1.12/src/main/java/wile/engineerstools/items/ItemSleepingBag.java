/*
 * @file ItemSleepingBag.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Simple Tough Fabric sleeping bag (don't ask me why - I just wanna know how that works).
 */
package wile.engineerstools.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;


public class ItemSleepingBag extends ItemTools
{
  public ItemSleepingBag(String registryName)
  {
    super(registryName);
    setMaxStackSize(1);
    setMaxDamage(4096);
    setNoRepair();
  }

  @Override
  public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  { onUse(player, world, pos, facing); return EnumActionResult.SUCCESS; }

  @Override
  public boolean isEnchantable(ItemStack stack)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player)
  { return false; }

  @Override
  public boolean showDurabilityBar(ItemStack stack)
  { return false; }

  // -------------------------------------------------------------------------------------------------------------------

  private void onUse(EntityPlayer player, World world, BlockPos pos, EnumFacing side)
  {
    if(world.isRemote || (side != EnumFacing.UP)) return;
    BlockPos bed_pos = player.bedLocation;
    switch(world.provider.canSleepAt(player, pos.up())) {
      case ALLOW: break;
      default:
        player.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep", new Object[0]), true);
        return;
    }
    EntityPlayer.SleepResult sr = player.trySleep(pos.up());
    switch(sr) {
      case OK:
      case TOO_FAR_AWAY:
        break;
      case NOT_SAFE:
        player.sendStatusMessage(new TextComponentTranslation("tile.bed.notSafe", new Object[0]), true);
        break;
      case NOT_POSSIBLE_HERE:
      case OTHER_PROBLEM:
      case NOT_POSSIBLE_NOW:
      default:
        player.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep", new Object[0]), true);
        break;
    }
  }

  public static void onBedCheck(SleepingLocationCheckEvent event)
  {
    if(!(event.getEntity() instanceof EntityPlayer)) return;
    if(event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSleepingBag) event.setResult(Result.ALLOW);
  }

}
