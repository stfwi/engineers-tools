/*
 * @file ItemSleepingBag.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Simple Tough Fabric sleeping bag (don't ask me why - I just wanna know how that works).
 */
package wile.engineerstools.items;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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

  @Override
  public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
  {
    if((!isSelected) || (world.isRemote) || (!(entity instanceof PlayerEntity))) return;
    PlayerEntity player = (PlayerEntity)entity;
    CompoundNBT nbt = player.getPersistentData();
    if(!nbt.contains("ETCorrectBedLocation")) return;
    nbt.remove("ETCorrectBedLocation");
    BlockPos pos = null;
    DimensionType dim = null;
    if(nbt.contains("ETOriginalBedLocation")) { pos = BlockPos.fromLong(nbt.getLong("ETOriginalBedLocation")); nbt.remove("ETOriginalBedLocation"); }
    if(nbt.contains("ETOriginalBedDimension")) { dim = DimensionType.getById(nbt.getInt("ETOriginalBedDimension")); nbt.remove("ETOriginalBedDimension"); }
    if((pos == null) || (dim != player.dimension)) return;
    if(!((SleepingBagItem)player.getHeldItem(Hand.MAIN_HAND).getItem()).respawn_at_bed) player.setSpawnPoint(pos, true, dim);
  }

  // -------------------------------------------------------------------------------------------------------------------

  private void onUse(PlayerEntity player, World world, BlockPos pos, Direction side)
  {
    if(side != Direction.UP) return;
    tryPlayerSleep(player, world, pos.up()).ifLeft(sr -> {
      switch(sr) {
        case TOO_FAR_AWAY: break;
        default: player.sendStatusMessage(sr.getMessage(), true);
      }
    });
  }

  private Either<PlayerEntity.SleepResult, Unit> tryPlayerSleep(PlayerEntity player, World world, BlockPos at)
  {
    BlockPos bl = player.getBedLocation(player.dimension);
    DimensionType dim = player.dimension;
    Optional<BlockPos> optAt = Optional.of(at);
    if(!world.isRemote()) {
      // Related to #8, tryPlayerSleep() handles sideness check in vanilla, but is effectively only invoked server side
      // when using beds -> also done here, as other mods hooking into the event may not expect client side usage.
      PlayerEntity.SleepResult ret = net.minecraftforge.event.ForgeEventFactory.onPlayerSleepInBed(player, optAt);
      if(ret!=null) return Either.left(ret);
    }
    if(!world.isRemote()) {
      if(player.isSleeping() || !player.isAlive()) return Either.left(PlayerEntity.SleepResult.OTHER_PROBLEM);
      if(!world.dimension.isSurfaceWorld()) return Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_HERE);
      if(!net.minecraftforge.event.ForgeEventFactory.fireSleepingTimeCheck(player, optAt)) return Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_NOW);
      if((!player.isCreative()) && (!(world.getEntitiesWithinAABB(MonsterEntity.class, new AxisAlignedBB(at.getX()-8, at.getY()-5, at.getZ()-8, at.getX()+8, at.getY()+5, at.getZ()+8), e->e.isPreventingPlayerRest(player)).isEmpty()))) return Either.left(PlayerEntity.SleepResult.NOT_SAFE);
      CompoundNBT nbt = player.getPersistentData();
      nbt.putShort("SleepTimer", (short)0);
      if(bl != null) {
        nbt.putLong("ETOriginalBedLocation", bl.toLong());
        nbt.putInt("ETOriginalBedDimension", dim.getId());
      }
      player.startSleeping(at);
      if((bl != null) && respawn_at_bed) player.setBedPosition(bl);
      if(world instanceof ServerWorld) ((ServerWorld)world).updateAllPlayersSleepingFlag();
    }
    return Either.right(Unit.INSTANCE);
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
