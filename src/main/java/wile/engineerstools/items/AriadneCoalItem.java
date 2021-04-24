/*
 * @file ItemAriadneCoal.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Mod internal metal dusts, applied when no other mod
 * provides a similar dust. Explicitly not registered
 * to ore dict, only smelting. Other grits shall be preferred.
 */
package wile.engineerstools.items;

import wile.engineerstools.ModContent;
import wile.engineerstools.blocks.AriadneCoalBlock;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;


public class AriadneCoalItem extends EtItem
{
  public AriadneCoalItem(Item.Properties properties)
  { super(properties
    .stacksTo(1)
    .defaultDurability(100)
    .setNoRepair()
    );
  }

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
  public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
  {
    if(context.getLevel().isClientSide) return ActionResultType.PASS;
    final PlayerEntity player = context.getPlayer();
    final Hand hand = context.getHand();
    final BlockPos pos = context.getClickedPos();
    final Direction facing = context.getClickedFace();
    final World world = context.getLevel();
    final BlockState state = world.getBlockState(pos);
    final BlockPos markpos = pos.relative(facing);
    if(((!world.isEmptyBlock(markpos)) && (!(state.getBlock() instanceof AriadneCoalBlock))) || (stack.getItem()!=this)) return ActionResultType.PASS;
    if(!Block.isFaceFull(state.getCollisionShape(world, pos, ISelectionContext.of(player)), facing)) return ActionResultType.PASS;
    final double hitX = context.getClickLocation().x() - pos.getX();
    final double hitY = context.getClickLocation().y() - pos.getY();
    final double hitZ = context.getClickLocation().z() - pos.getZ();
    Vector3d v;
    switch(facing) {
      case WEST:  v = new Vector3d(0.0+hitZ, hitY, 0); break;
      case EAST:  v = new Vector3d(1.0-hitZ, hitY, 0); break;
      case SOUTH: v = new Vector3d(0.0+hitX, hitY, 0); break;
      case NORTH: v = new Vector3d(1.0-hitX, hitY, 0); break;
      default:    v = new Vector3d(0.0+hitX, hitZ, 0); break; // UP/DOWN
    }
    v = v.subtract(0.5, 0.5, 0);
    final int orientation = (((int)(Math.rint(4.0/Math.PI * Math.atan2(v.y, v.x) + 16) ) % 8) + ((facing.getAxisDirection()==AxisDirection.NEGATIVE) ? 8 : 0)) & 0xf;
    BlockState setstate;
    switch(facing.getAxis()) {
      case X:  setstate = ModContent.ARIADNE_COAL_X.defaultBlockState(); break;
      case Y:  setstate = ModContent.ARIADNE_COAL_Y.defaultBlockState(); break;
      default: setstate = ModContent.ARIADNE_COAL_Z.defaultBlockState(); break;
    }
    if(world.setBlock(markpos, setstate.setValue(AriadneCoalBlock.ORIENTATION, orientation), 1|2)) {
      stack.setDamageValue(stack.getDamageValue()+1);
      if(stack.getDamageValue() >= stack.getMaxDamage()) {
        player.setItemInHand(hand, ItemStack.EMPTY);
        world.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundCategory.BLOCKS, 0.4f, 2f);
      } else {
        world.playSound(null, pos, SoundEvents.GRAVEL_HIT, SoundCategory.BLOCKS, 0.4f, 2f);
      }
      return ActionResultType.SUCCESS;
    } else {
      return ActionResultType.FAIL;
    }
  }
}
