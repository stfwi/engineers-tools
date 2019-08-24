/*
 * @file BlockAriadneCoal.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Common functionality class for decor blocks.
 * Mainly needed for:
 * - MC block defaults.
 * - Tooltip functionality
 * - Model initialisation
 * - Accumulating "deprecated" warnings from Block where "overriding/implementing is fine".
 */
package wile.engineerstools.blocks;

import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.detail.ModAuxiliaries;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockAriadneCoal extends Block
{
  public static final PropertyInteger ORIENTATION = PropertyInteger.create("orientation", 0, 15);

  final AxisAlignedBB aabbs[];
  final EnumFacing.Axis attachment_axis;

  public BlockAriadneCoal(@Nonnull String registryName, EnumFacing.Axis axis)
  {
    super(Material.CIRCUITS);
    setCreativeTab(null);
    setRegistryName(ModEngineersTools.MODID, registryName);
    setTranslationKey(ModEngineersTools.MODID + "." + registryName);
    setTickRandomly(false);
    setHardness(0.1f);
    setResistance(0.1f);
    setSoundType(SoundType.STONE);
    setLightOpacity(0);
    attachment_axis = axis;
    if(axis==Axis.X) {
      aabbs = new AxisAlignedBB[] {
        ModAuxiliaries.getPixeledAABB(0,0,0, 0.1,16,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 0.1,16,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 0.1,16,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 0.1,16,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 0.1,16,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 0.1,16,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 0.1,16,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 0.1,16,16),
        ModAuxiliaries.getPixeledAABB(15.9,0,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(15.9,0,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(15.9,0,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(15.9,0,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(15.9,0,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(15.9,0,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(15.9,0,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(15.9,0,0, 16,16,16),
      };
    } else if(axis==Axis.Y) {
      aabbs = new AxisAlignedBB[] {
        ModAuxiliaries.getPixeledAABB(0,0,0, 16,0.1,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 16,0.1,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 16,0.1,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 16,0.1,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 16,0.1,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 16,0.1,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 16,0.1,16),
        ModAuxiliaries.getPixeledAABB(0,0,0, 16,0.1,16),
        ModAuxiliaries.getPixeledAABB(0,15.9,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(0,15.9,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(0,15.9,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(0,15.9,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(0,15.9,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(0,15.9,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(0,15.9,0, 16,16,16),
        ModAuxiliaries.getPixeledAABB(0,15.9,0, 16,16,16),
      };
    } else {
      aabbs = new AxisAlignedBB[] {
        ModAuxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1),
        ModAuxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1),
        ModAuxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1),
        ModAuxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1),
        ModAuxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1),
        ModAuxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1),
        ModAuxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1),
        ModAuxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1),
        ModAuxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 ),
        ModAuxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 ),
        ModAuxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 ),
        ModAuxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 ),
        ModAuxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 ),
        ModAuxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 ),
        ModAuxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 ),
        ModAuxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 ),
      };
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
  { ModAuxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

  @Override
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer()
  { return BlockRenderLayer.CUTOUT; }

  @Override
  @SideOnly(Side.CLIENT)
  @SuppressWarnings("deprecation")
  public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face)
  { return BlockFaceShape.UNDEFINED; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isFullCube(IBlockState state)
  { return false; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isNormalCube(IBlockState state)
  { return false; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isOpaqueCube(IBlockState state)
  { return false; }

  @Override
  public boolean canSpawnInBlock()
  { return false; }

  @Override
  public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
  { return false; }

  @Override
  public boolean hasTileEntity(IBlockState state)
  { return false; }

  @Override
  public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public EnumPushReaction getPushReaction(IBlockState state)
  { return EnumPushReaction.DESTROY; }

  @Override
  @SuppressWarnings("deprecation")
  public IBlockState getStateFromMeta(int meta)
  { return getDefaultState().withProperty(ORIENTATION, meta & 0xf); }

  @Override
  public int getMetaFromState(IBlockState state)
  { return state.getValue(ORIENTATION); }

  @Override
  protected BlockStateContainer createBlockState()
  { return new BlockStateContainer(this, ORIENTATION); }

  @Override
  @SuppressWarnings("deprecation")
  public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
  { return state; }

  @Override
  @SuppressWarnings("deprecation")
  public IBlockState withRotation(IBlockState state, Rotation rot)
  { return state; }

  @Override
  @SuppressWarnings("deprecation")
  public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
  { return state; }

  @Override
  @Nullable
  @SuppressWarnings("deprecation")
  public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
  { return NULL_AABB; }

  @Override
  @SuppressWarnings("deprecation")
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
  { return aabbs[state.getValue(ORIENTATION)]; }

  @SideOnly(Side.CLIENT)
  @SuppressWarnings("deprecation")
  public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
  { return state.getBoundingBox(world, pos).offset(pos); }

  @Override
  @SuppressWarnings("deprecation")
  public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
  {
    if(world.isRemote) return;
    EnumFacing facing = EnumFacing.NORTH;
    switch(attachment_axis) {
      case X: facing = (state.getValue(ORIENTATION) <= 7) ? EnumFacing.WEST: EnumFacing.EAST; break;
      case Y: facing = (state.getValue(ORIENTATION) <= 7) ? EnumFacing.DOWN: EnumFacing.UP; break;
      case Z: facing = (state.getValue(ORIENTATION) <= 7) ? EnumFacing.NORTH: EnumFacing.SOUTH; break;
    }
    if(!pos.offset(facing).equals(fromPos)) return;
    if(world.getBlockState(fromPos).isSideSolid(world, fromPos, facing.getOpposite())) return;
    world.setBlockToAir(pos);
  }

  @Override
  public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
  { world.setBlockToAir(pos); }

}
