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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import com.mojang.datafixers.util.Either;

import java.util.Optional;


public class SleepingBagItem extends EtItem
{
  private final boolean respawn_at_bed = false;

  public SleepingBagItem(Item.Properties properties)
  { super(properties.maxStackSize(1).defaultMaxDamage(4096).setNoRepair()); }

  @Override
  public ActionResultType onItemUse(ItemUseContext context)
  {
    if(context.getWorld().getBlockState(context.getPos()).isBed(context.getWorld(), context.getPos(), context.getPlayer())) return ActionResultType.PASS;
    if(context.getWorld().isRemote()) return ActionResultType.CONSUME;
    onUse(context.getPlayer(), context.getWorld(), context.getPos(), context.getFace());
    return ActionResultType.SUCCESS;
  }

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

  private void onUse(PlayerEntity player, World world, BlockPos pos, Direction side)
  {
    if(world.isRemote() || (side != Direction.UP) || (!(player instanceof ServerPlayerEntity))) return;
    if(!world.getDimensionType().isNatural()) {
      ITextComponent msg = PlayerEntity.SleepResult.NOT_POSSIBLE_HERE.getMessage();
      if(msg != null) player.sendStatusMessage(msg, true);
      return;
    }
    boolean setspawn = true;
    tryServerPlayerSleep((ServerPlayerEntity)player, world, pos.up(), setspawn).ifLeft(sr -> {
      switch(sr) {
        case TOO_FAR_AWAY:
          break;
        default:
          if(sr.getMessage()!=null) player.sendStatusMessage(sr.getMessage(), true);
      }
    });
    if(!player.world.isDaytime()) {
      ((ServerWorld)player.world).updateAllPlayersSleepingFlag();
    }
  }

  private Either<PlayerEntity.SleepResult, Unit> tryServerPlayerSleep(ServerPlayerEntity player, World world, BlockPos at, boolean set_spawn_point)
  {
    if(!player.isAlive()) return Either.left(PlayerEntity.SleepResult.OTHER_PROBLEM);
    if(!player.world.getDimensionType().isNatural()) return Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_HERE);
    if(!player.isCreative()) {
      double d0=8, d1=5;
      if(!player.world.getEntitiesWithinAABB(MonsterEntity.class, new AxisAlignedBB(at.getX()-8, at.getY()-5, at.getZ()-8, at.getX()+8, at.getY()+5, at.getZ()+8), (en)->en.func_230292_f_(player)).isEmpty()) {
        return Either.left(PlayerEntity.SleepResult.NOT_SAFE);
      }
    }
    if(set_spawn_point) player.setBedPosition(at);
    if(player.world.isDaytime()) return Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_NOW);
    if(!player.isSleeping()) {
      Optional<BlockPos> optAt = Optional.of(at);
      PlayerEntity.SleepResult ret = net.minecraftforge.event.ForgeEventFactory.onPlayerSleepInBed(player, optAt);
      if(ret != null) return Either.left(ret);
      ((ServerPlayerEntity)player).startSleeping(at);
      return Either.right(Unit.INSTANCE);
    }
    return Either.left(PlayerEntity.SleepResult.OTHER_PROBLEM);
  }

  public static void onSleepingLocationCheckEvent(net.minecraftforge.event.entity.player.SleepingLocationCheckEvent event)
  {
    if(!(event.getEntity() instanceof PlayerEntity)) return;
    PlayerEntity player = (PlayerEntity)event.getEntity();
    if((player.world.isRemote) || (!(player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof SleepingBagItem))) return;
    event.setResult(net.minecraftforge.event.entity.player.SleepingLocationCheckEvent.Result.ALLOW);
  }

  public static void onPlayerWakeUpEvent(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event)
  {
    if(!(event.getEntity() instanceof PlayerEntity)) return;
    PlayerEntity player = (PlayerEntity)event.getEntity();
    if((player.world.isRemote) || ((!(player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof SleepingBagItem)))) return;
    CompoundNBT nbt = player.getPersistentData();
    nbt.putInt("ETCorrectBedLocation", 1);
  }

}
