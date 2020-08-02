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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import wile.engineerstools.ModContent;
import wile.engineerstools.blocks.BlockAriadneCoal;


public class ItemAriadneCoal extends ItemTools
{
  public ItemAriadneCoal(String registryName)
  {
    super(registryName);
    setMaxStackSize(1);
    setMaxDamage(100);
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
  public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  {
    if(world.isRemote) return EnumActionResult.PASS;
    final ItemStack stack = player.getHeldItem(hand);
    final IBlockState state = world.getBlockState(pos);
    final BlockPos markpos = pos.offset(facing);
    if((!state.isSideSolid(world, pos, facing)) || ((!world.isAirBlock(markpos)) && (!(state.getBlock() instanceof BlockAriadneCoal))) || (stack.getItem()!=this)) {
      return EnumActionResult.PASS;
    }
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
    IBlockState setstate;
    switch(facing.getAxis()) {
      case X:  setstate = ModContent.ARIADNE_COAL_X.getDefaultState(); break;
      case Y:  setstate = ModContent.ARIADNE_COAL_Y.getDefaultState(); break;
      default: setstate = ModContent.ARIADNE_COAL_Z.getDefaultState(); break;
    }
    if(world.setBlockState(markpos, setstate.withProperty(BlockAriadneCoal.ORIENTATION, orientation), 1|2)) {
      stack.setItemDamage(stack.getItemDamage()+1);
      if(stack.getItemDamage() >= stack.getMaxDamage()) {
        player.setHeldItem(hand, ItemStack.EMPTY);
        world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 0.4f, 2f);
      } else {
        world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_HIT, SoundCategory.BLOCKS, 0.4f, 2f);
      }
      return EnumActionResult.SUCCESS;
    } else {
      return EnumActionResult.FAIL;
    }
  }
}
