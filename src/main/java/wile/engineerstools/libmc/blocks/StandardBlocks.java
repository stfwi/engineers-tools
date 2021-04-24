/*
 * @file BlockDecorFull.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Common functionality class for decor blocks.
 * Mainly needed for:
 * - MC block defaults.
 * - Tooltip functionality
 * - Model initialisation
 */
package wile.engineerstools.libmc.blocks;

import wile.engineerstools.libmc.detail.Auxiliaries;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.loot.LootContext;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.*;


public class StandardBlocks
{
  public static final long CFG_DEFAULT                    = 0x0000000000000000L; // no special config
  public static final long CFG_CUTOUT                     = 0x0000000000000001L; // cutout rendering
  public static final long CFG_MIPPED                     = 0x0000000000000002L; // cutout mipped rendering
  public static final long CFG_TRANSLUCENT                = 0x0000000000000004L; // indicates a block/pane is glass like (transparent, etc)
  public static final long CFG_WATERLOGGABLE              = 0x0000000000000008L; // The derived block extends IWaterLoggable
  public static final long CFG_HORIZIONTAL                = 0x0000000000000010L; // horizontal block, affects bounding box calculation at construction time and placement
  public static final long CFG_LOOK_PLACEMENT             = 0x0000000000000020L; // placed in direction the player is looking when placing.
  public static final long CFG_FACING_PLACEMENT           = 0x0000000000000040L; // placed on the facing the player has clicked.
  public static final long CFG_OPPOSITE_PLACEMENT         = 0x0000000000000080L; // placed placed in the opposite direction of the face the player clicked.
  public static final long CFG_FLIP_PLACEMENT_IF_SAME     = 0x0000000000000100L; // placement direction flipped if an instance of the same class was clicked
  public static final long CFG_FLIP_PLACEMENT_SHIFTCLICK  = 0x0000000000000200L; // placement direction flipped if player is sneaking
  public static final long CFG_STRICT_CONNECTIONS         = 0x0000000000000400L; // blocks do not connect to similar blocks around (implementation details may vary a bit)

  public interface IStandardBlock
  {
    default boolean hasDynamicDropList()
    { return false; }

    default List<ItemStack> dropList(BlockState state, World world, BlockPos pos, boolean explosion)
    { return Collections.singletonList((!world.isClientSide()) ? (new ItemStack(state.getBlock().asItem())) : (ItemStack.EMPTY)); }

    enum RenderTypeHint { SOLID,CUTOUT,CUTOUT_MIPPED,TRANSLUCENT }

    default RenderTypeHint getRenderTypeHint()
    { return RenderTypeHint.SOLID; }
  }

  public static class BaseBlock extends Block implements IStandardBlock
  {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public final long config;
    public final VoxelShape vshape;

    public BaseBlock(long conf, AbstractBlock.Properties properties)
    { this(conf, properties, Auxiliaries.getPixeledAABB(0, 0, 0, 16, 16,16 )); }

    public BaseBlock(long conf, AbstractBlock.Properties properties, AxisAlignedBB aabb)
    { super(properties); config = conf; vshape = VoxelShapes.create(aabb); }

    public BaseBlock(long conf, AbstractBlock.Properties properties, VoxelShape voxel_shape)
    { super(properties); config = conf; vshape = voxel_shape; }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    { return ActionResultType.PASS; }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand)
    {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag)
    { Auxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

    @Override
    public RenderTypeHint getRenderTypeHint()
    { return ((config & CFG_CUTOUT)!=0) ? RenderTypeHint.CUTOUT : RenderTypeHint.SOLID; }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext selectionContext)
    { return vshape; }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos,  ISelectionContext selectionContext)
    { return vshape; }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
      BlockState state = super.getStateForPlacement(context);
      if((config & CFG_WATERLOGGABLE)!=0) {
        FluidState fs = context.getLevel().getFluidState(context.getClickedPos());
        state = state.setValue(WATERLOGGED,fs.getType()==Fluids.WATER);
      }
      return state;
    }

    @Override
    public boolean isPossibleToRespawnInThis()
    { return false; }

    @Override
    @SuppressWarnings("deprecation")
    public PushReaction getPistonPushReaction(BlockState state)
    { return PushReaction.NORMAL; }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
      if(state.hasTileEntity() && (state.getBlock() != newState.getBlock())) {
        world.removeBlockEntity(pos);
        world.updateNeighbourForOutputSignal(pos, this);
      }
    }

    public static boolean dropBlock(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player)
    {
      if(!(state.getBlock() instanceof IStandardBlock)) { world.removeBlock(pos, false); return true; }
      if(!world.isClientSide()) {
        if((player==null) || (!player.isCreative())) {
          ((IStandardBlock)state.getBlock()).dropList(state, world, pos, player==null).forEach(stack->world.addFreshEntity(new ItemEntity(world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, stack)));
        }
      }
      if(state.getBlock().hasTileEntity(state)) world.removeBlockEntity(pos);
      world.removeBlock(pos, false);
      return true;
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid)
    { return hasDynamicDropList() ? dropBlock(state, world, pos, player) : super.removedByPlayer(state, world,pos , player, willHarvest, fluid); }

    @Override
    public void wasExploded(World world, BlockPos pos, Explosion explosion)
    { if(hasDynamicDropList()) dropBlock(world.getBlockState(pos), world, pos, null); }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    { return hasDynamicDropList() ? Collections.singletonList(ItemStack.EMPTY) : super.getDrops(state, builder); }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
    {
      if((config & CFG_WATERLOGGABLE)!=0) {
        if(state.getValue(WATERLOGGED)) return false;
      }
      return super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state)
    {
      if((config & CFG_WATERLOGGABLE)!=0) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
      }
      return super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos)
    {
      if((config & CFG_WATERLOGGABLE)!=0) {
        if(state.getValue(WATERLOGGED)) world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      }
      return state;
    }
  }

  public static class WaterLoggable extends BaseBlock implements IWaterLoggable, IStandardBlock
  {
    public WaterLoggable(long config, AbstractBlock.Properties properties)
    { super(config|CFG_WATERLOGGABLE, properties); }

    public WaterLoggable(long config, AbstractBlock.Properties properties, AxisAlignedBB aabb)
    { super(config|CFG_WATERLOGGABLE, properties, aabb); }

    public WaterLoggable(long config, AbstractBlock.Properties properties, VoxelShape voxel_shape)
    { super(config|CFG_WATERLOGGABLE, properties, voxel_shape);  }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    { super.createBlockStateDefinition(builder); builder.add(WATERLOGGED); }
  }

  public static class Directed extends BaseBlock implements IStandardBlock
  {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    protected final ArrayList<VoxelShape> AABBs;

    public Directed(long config, AbstractBlock.Properties builder, final AxisAlignedBB unrotatedAABB)
    {
      super(config, builder);
      registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
      final boolean is_horizontal = ((config & CFG_HORIZIONTAL)!=0);
      AABBs = new ArrayList<VoxelShape>(Arrays.asList(
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.DOWN, is_horizontal)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.UP, is_horizontal)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.NORTH, is_horizontal)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.SOUTH, is_horizontal)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.WEST, is_horizontal)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.EAST, is_horizontal)),
        VoxelShapes.create(unrotatedAABB),
        VoxelShapes.create(unrotatedAABB)
      ));
    }

    @Override
    public boolean isPossibleToRespawnInThis()
    { return false; }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType)
    { return false; }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext selectionContext)
    { return AABBs.get((state.getValue(FACING)).get3DDataValue() & 0x7); }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext)
    { return getShape(state, world, pos, selectionContext); }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    { super.createBlockStateDefinition(builder); builder.add(FACING); }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
      Direction facing = context.getClickedFace();
      if((config & (CFG_HORIZIONTAL|CFG_LOOK_PLACEMENT)) == (CFG_HORIZIONTAL|CFG_LOOK_PLACEMENT)) {
        // horizontal placement in direction the player is looking
        facing = context.getHorizontalDirection();
      } else if((config & (CFG_HORIZIONTAL|CFG_LOOK_PLACEMENT)) == (CFG_HORIZIONTAL)) {
        // horizontal placement on a face
        if(((facing==Direction.UP)||(facing==Direction.DOWN))) return null;
      } else if((config & CFG_LOOK_PLACEMENT)!=0) {
        // placement in direction the player is looking, with up and down
        facing = context.getNearestLookingDirection();
      } else {
        // default: placement on the face the player clicking
      }
      if((config & CFG_OPPOSITE_PLACEMENT)!=0) facing = facing.getOpposite();
      if(((config & CFG_FLIP_PLACEMENT_SHIFTCLICK) != 0) && (context.getPlayer().isShiftKeyDown())) facing = facing.getOpposite();
      return super.getStateForPlacement(context).setValue(FACING, facing);
    }
  }

  public static class Horizontal extends BaseBlock implements IStandardBlock
  {
    public static final DirectionProperty HORIZONTAL_FACING = HorizontalBlock.FACING;
    protected final ArrayList<VoxelShape> AABBs;

    public Horizontal(long config, AbstractBlock.Properties builder, final AxisAlignedBB unrotatedAABB)
    {
      super(config|CFG_HORIZIONTAL, builder, unrotatedAABB);
      registerDefaultState(stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
      AABBs = new ArrayList<VoxelShape>(Arrays.asList(
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.DOWN, true)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.UP, true)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.NORTH, true)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.SOUTH, true)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.WEST, true)),
        VoxelShapes.create(Auxiliaries.getRotatedAABB(unrotatedAABB, Direction.EAST, true)),
        VoxelShapes.create(unrotatedAABB),
        VoxelShapes.create(unrotatedAABB)
      ));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext selectionContext)
    { return AABBs.get((state.getValue(HORIZONTAL_FACING)).get3DDataValue() & 0x7); }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext)
    { return getShape(state, world, pos, selectionContext); }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    { super.createBlockStateDefinition(builder); builder.add(HORIZONTAL_FACING); }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
      Direction facing = context.getClickedFace();
      if((config & CFG_LOOK_PLACEMENT) != 0) {
        // horizontal placement in direction the player is looking
        facing = context.getHorizontalDirection();
      } else {
        // horizontal placement on a face
        facing = ((facing==Direction.UP)||(facing==Direction.DOWN)) ? (context.getHorizontalDirection()) : facing;
      }
      if((config & CFG_OPPOSITE_PLACEMENT)!=0) facing = facing.getOpposite();
      if(((config & CFG_FLIP_PLACEMENT_SHIFTCLICK) != 0) && (context.getPlayer().isShiftKeyDown())) facing = facing.getOpposite();
      return super.getStateForPlacement(context).setValue(HORIZONTAL_FACING, facing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rot)
    { return state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING))); }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirrorIn)
    { return state.rotate(mirrorIn.getRotation(state.getValue(HORIZONTAL_FACING))); }
  }

  public static class DirectedWaterLoggable extends Directed implements IWaterLoggable, IStandardBlock
  {
    public DirectedWaterLoggable(long config, AbstractBlock.Properties properties, AxisAlignedBB aabb)
    { super(config|CFG_WATERLOGGABLE, properties, aabb); }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    { super.createBlockStateDefinition(builder); builder.add(WATERLOGGED); }
  }

  public static class HorizontalWaterLoggable extends Horizontal implements IWaterLoggable, IStandardBlock
  {
    public HorizontalWaterLoggable(long config, AbstractBlock.Properties properties, AxisAlignedBB aabb)
    { super(config|CFG_WATERLOGGABLE|CFG_HORIZIONTAL, properties, aabb); }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    { super.createBlockStateDefinition(builder); builder.add(WATERLOGGED); }
  }

}
