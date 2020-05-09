/*
 * @file ItemAriadneCoal.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Mod internal metal dusts, applied when no other mod
 * provides a similar dust. Explicitly not registered
 * to ore dict, only smelting. Other grits shall be preferred.
 */
package wile.engineerstools.items;

import wile.engineerstools.ModContent;
import wile.engineerstools.blocks.BlockAriadneCoal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


public class ItemAriadneCoal extends ItemTools
{
  public ItemAriadneCoal(Item.Properties properties)
  { super(properties
    .maxStackSize(1)
    .defaultMaxDamage(100)
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
  public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
  {
    if(context.getWorld().isRemote) return ActionResultType.PASS;
    final PlayerEntity player = context.getPlayer();
    final Hand hand = context.getHand();
    final BlockPos pos = context.getPos();
    final Direction facing = context.getFace();
    final World world = context.getWorld();
    final BlockState state = world.getBlockState(pos);
    final BlockPos markpos = pos.offset(facing);
    if(((!world.isAirBlock(markpos)) && (!(state.getBlock() instanceof BlockAriadneCoal))) || (stack.getItem()!=this)) return ActionResultType.PASS;
    if(!Block.doesSideFillSquare(state.getCollisionShape(world, pos, ISelectionContext.forEntity(player)), facing)) return ActionResultType.PASS;
    final double hitX = context.getHitVec().getX() - pos.getX();
    final double hitY = context.getHitVec().getY() - pos.getY();
    final double hitZ = context.getHitVec().getZ() - pos.getZ();
    Vec3d v;
    switch(facing) {
      case WEST:  v = new Vec3d(0.0+hitZ, hitY, 0); break;
      case EAST:  v = new Vec3d(1.0-hitZ, hitY, 0); break;
      case SOUTH: v = new Vec3d(0.0+hitX, hitY, 0); break;
      case NORTH: v = new Vec3d(1.0-hitX, hitY, 0); break;
      default:    v = new Vec3d(0.0+hitX, hitZ, 0); break; // UP/DOWN
    }
    v = v.subtract(0.5, 0.5, 0);
    final int orientation = (((int)(Math.rint(4.0/Math.PI * Math.atan2(v.y, v.x) + 16) ) % 8) + ((facing.getAxisDirection()==AxisDirection.NEGATIVE) ? 8 : 0)) & 0xf;
    BlockState setstate;
    switch(facing.getAxis()) {
      case X:  setstate = ModContent.ARIADNE_COAL_X.getDefaultState(); break;
      case Y:  setstate = ModContent.ARIADNE_COAL_Y.getDefaultState(); break;
      default: setstate = ModContent.ARIADNE_COAL_Z.getDefaultState(); break;
    }
    if(world.setBlockState(markpos, setstate.with(BlockAriadneCoal.ORIENTATION, orientation), 1|2)) {
      stack.setDamage(stack.getDamage()+1);
      if(stack.getDamage() >= stack.getMaxDamage()) {
        player.setHeldItem(hand, ItemStack.EMPTY);
        world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 0.4f, 2f);
      } else {
        world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_HIT, SoundCategory.BLOCKS, 0.4f, 2f);
      }
      return ActionResultType.SUCCESS;
    } else {
      return ActionResultType.FAIL;
    }
  }
}
