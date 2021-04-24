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

import net.minecraft.item.Item;
import wile.engineerstools.ModContent;
import wile.engineerstools.libmc.detail.Auxiliaries;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.loot.LootContext;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;


import net.minecraft.block.AbstractBlock;

public class AriadneCoalBlock extends Block
{
  public static final IntegerProperty ORIENTATION = IntegerProperty.create("orientation", 0, 15);

  final VoxelShape aabbs[];
  final Direction.Axis attachment_axis;

  public AriadneCoalBlock(AbstractBlock.Properties properties, Direction.Axis axis)
  {
    super(properties);
    registerDefaultState(this.getStateDefinition().any().setValue(ORIENTATION, 0));
    attachment_axis = axis;
    if(axis==Axis.X) {
      aabbs = new VoxelShape[] {
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 0.1,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(15.9,0,0, 16,16,16)),
      };
    } else if(axis==Axis.Y) {
      aabbs = new VoxelShape[] {
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0, 16,0.1,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,15.9,0, 16,16,16)),
      };
    } else {
      aabbs = new VoxelShape[] {
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,0   , 16,16,0.1)),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
        VoxelShapes.create(Auxiliaries.getPixeledAABB(0,0,15.9, 16,16,16 )),
      };
    }
  }

  @Override
  public Item asItem()
  { return ModContent.ARIADNE_COAL; }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag)
  { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

  @Override
  @SuppressWarnings("deprecation")
  public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext selectionContext)
  { return aabbs[state.getValue(ORIENTATION)]; }

  @Override
  @SuppressWarnings("deprecation")
  public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext)
  { return VoxelShapes.empty(); }

  @Override
  protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
  { builder.add(ORIENTATION); }

//  @Override
//  @SuppressWarnings("deprecation")
//  public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos)
//  { return false; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type)
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext)
  { return true; }

  @Override
  @SuppressWarnings("deprecation")
  public int getLightBlock(BlockState state, IBlockReader worldIn, BlockPos pos)
  { return 0; }

  @Override
  @SuppressWarnings("deprecation")
  public PushReaction getPistonPushReaction(BlockState state)
  { return PushReaction.DESTROY; }

  @Override
  @SuppressWarnings("deprecation")
  public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
  { return Collections.singletonList(ItemStack.EMPTY); }

  @Override
  public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid)
  { return world.removeBlock(pos, false); }

  @Override
  @SuppressWarnings("deprecation")
  public void attack(BlockState state, World world, BlockPos pos, PlayerEntity player)
  { world.removeBlock(pos, false); }

  @Override
  @SuppressWarnings("deprecation")
  public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
  {
    if(world.isClientSide) return;
    Direction facing = Direction.NORTH;
    switch(attachment_axis) {
      case X: facing = (state.getValue(ORIENTATION) <= 7) ? Direction.WEST: Direction.EAST; break;
      case Y: facing = (state.getValue(ORIENTATION) <= 7) ? Direction.DOWN: Direction.UP; break;
      case Z: facing = (state.getValue(ORIENTATION) <= 7) ? Direction.NORTH: Direction.SOUTH; break;
    }
    if(!pos.relative(facing).equals(fromPos)) return;
    if(Block.isFaceFull(world.getBlockState(fromPos).getCollisionShape(world, fromPos, ISelectionContext.empty()), facing.getOpposite())) return;
    world.removeBlock(pos, isMoving);
  }
}
