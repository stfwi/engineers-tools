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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import com.mojang.datafixers.util.Either;

import java.util.List;
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
    if(!world.func_230315_m_().func_241510_j_()) { // world.dimension().can_sleep_in_or_so()
      player.sendStatusMessage(new TranslationTextComponent("block.minecraft.bed.no_sleep"), true);
      return;
    }
    boolean setspawn = true;
    tryServerPlayerSleep((ServerPlayerEntity)player, world, pos.up(), setspawn).ifLeft(sr -> {
      switch(sr) {
        case TOO_FAR_AWAY: break;
        default: player.sendStatusMessage(sr.getMessage(), true);
      }
    });
    ((ServerWorld)player.world).updateAllPlayersSleepingFlag();
  }

  private Either<PlayerEntity.SleepResult, Unit> tryServerPlayerSleep(ServerPlayerEntity player, World world, BlockPos at, boolean set_spawn_point)
  {
    Optional<BlockPos> optAt = Optional.of(at);
    PlayerEntity.SleepResult ret = net.minecraftforge.event.ForgeEventFactory.onPlayerSleepInBed(player, optAt);
    if (ret != null) return Either.left(ret);
    if (!player.isSleeping() && player.isAlive()) {
      if (!player.world.func_230315_m_().func_236043_f_()) {
        return Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_HERE);
      } else {
        if(set_spawn_point) player.setBedPosition(at);
        if (player.world.isDaytime()) {
          return Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_NOW);
        } else {
          if (!player.isCreative()) {
            double d0 = 8.0D;
            double d1 = 5.0D;
            Vector3d vector3d = Vector3d.func_237492_c_(at);
            List<MonsterEntity> list = player.world.getEntitiesWithinAABB(MonsterEntity.class, new AxisAlignedBB(vector3d.getX() - 8.0D, vector3d.getY() - 5.0D, vector3d.getZ() - 8.0D, vector3d.getX() + 8.0D, vector3d.getY() + 5.0D, vector3d.getZ() + 8.0D), (p_241146_1_) -> {
              return p_241146_1_.func_230292_f_(player);
            });
            if (!list.isEmpty()) {
              return Either.left(PlayerEntity.SleepResult.NOT_SAFE);
            }
          }
          ((ServerPlayerEntity)player).startSleeping(at);
          return Either.right(Unit.INSTANCE);
        }
      }
    } else {
      return Either.left(PlayerEntity.SleepResult.OTHER_PROBLEM);
    }
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
