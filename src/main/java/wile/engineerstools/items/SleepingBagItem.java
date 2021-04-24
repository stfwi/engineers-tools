/*
 * @file ItemSleepingBag.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Simple Tough Fabric sleeping bag (don't ask me why - I just wanna know how that works).
 */
package wile.engineerstools.items;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;


public class SleepingBagItem extends EtItem
{
  public SleepingBagItem(Item.Properties properties)
  { super(properties.stacksTo(1).defaultDurability(4096).setNoRepair()); }

  @Override
  public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
  {
    if(context.getLevel().getBlockState(context.getClickedPos()).isBed(context.getLevel(), context.getClickedPos(), context.getPlayer())) return ActionResultType.PASS;
    onUse(context.getPlayer(), context.getLevel(), context.getClickedPos(), context.getClickedFace());
    return context.getLevel().isClientSide() ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
  }

  @Override
  public ActionResultType useOn(ItemUseContext context)
  { return ActionResultType.FAIL; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
  { return false; }

  @Override
  public boolean showDurabilityBar(ItemStack stack)
  { return false; }

  // -------------------------------------------------------------------------------------------------------------------

  private boolean onUse(PlayerEntity player, World world, BlockPos pos, Direction side)
  {
    if(side != Direction.UP) return false;
    final PlayerEntity.SleepResult sr = trySleep(player, world, pos.above(), true);
    if(sr == null) return true;
    if((sr == PlayerEntity.SleepResult.TOO_FAR_AWAY) || (sr.getMessage()==null)) return false;
    if(!world.isClientSide()) player.displayClientMessage(sr.getMessage(), true);
    return false;
  }

  private PlayerEntity.SleepResult trySleep(PlayerEntity player, World world, BlockPos at, boolean set_spawn_point)
  {
    if(player.isSpectator() || player.isCreative()) return null;
    if(player.isSleeping()) return null;
    if(!player.isAlive()) return PlayerEntity.SleepResult.OTHER_PROBLEM;
    if(!world.dimensionType().natural()) return PlayerEntity.SleepResult.NOT_POSSIBLE_HERE;
    double d0=8, d1=5;
    if(!world.getEntitiesOfClass(MonsterEntity.class, new AxisAlignedBB(at.getX()-8, at.getY()-5, at.getZ()-8, at.getX()+8, at.getY()+5, at.getZ()+8), (en)->en.isPreventingPlayerRest(player)).isEmpty()) return PlayerEntity.SleepResult.NOT_SAFE;
    if(!net.minecraftforge.event.ForgeEventFactory.fireSleepingTimeCheck(player, Optional.of(at))) return PlayerEntity.SleepResult.NOT_POSSIBLE_NOW;
    if(world.isClientSide() || !(player instanceof ServerPlayerEntity)) return null;
    ((ServerPlayerEntity)player).setSleepingPos(at);
    Optional<BlockPos> optAt = Optional.of(at);
    PlayerEntity.SleepResult ret = net.minecraftforge.event.ForgeEventFactory.onPlayerSleepInBed(player, optAt);
    if(ret != null) return ret;
    player.startSleeping(at);
    ((ServerWorld)world).updateSleepingPlayerList();
    return null;
  }

  public static void onSleepingLocationCheckEvent(net.minecraftforge.event.entity.player.SleepingLocationCheckEvent event)
  {
    if(!(event.getEntity() instanceof PlayerEntity)) return;
    PlayerEntity player = (PlayerEntity)event.getEntity();
    if((player.level.isClientSide) || (!(player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof SleepingBagItem))) return;
    event.setResult(net.minecraftforge.event.entity.player.SleepingLocationCheckEvent.Result.ALLOW);
  }

  public static void onPlayerWakeUpEvent(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event)
  {
    if(!(event.getEntity() instanceof PlayerEntity)) return;
    PlayerEntity player = (PlayerEntity)event.getEntity();
    if((player.level.isClientSide) || ((!(player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof SleepingBagItem)))) return;
    //CompoundNBT nbt = player.getPersistentData();
    //nbt.putInt("ETCorrectBedLocation", 1);
  }

}
