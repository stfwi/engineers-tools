/*
 * @file ItemRediaTool.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * REDia combi tool.
 */
package wile.engineerstools.items;

import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.detail.ModAuxiliaries;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemAxe;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraftforge.common.IShearable;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;
import java.util.*;


public class ItemRediaTool extends ItemAxe
{
  private static int enchantability = 15;
  private static int max_damage_ = 2200;
  private static boolean with_torch_placing = true;
  private static boolean with_hoeing = true;
  private static boolean with_tree_felling = true;
  private static boolean with_shearing = true;
  private static double efficiency_decay[] = { 0.1, 0.8, 0.9, 1.0, 1.0, 1.3, 1.6, 1.8, 2.0, 2.0 }; // index: 0% .. 100% durability
  private static int fortune_decay[] = { 0, 0, 0, 0, 0, 0, 1, 1, 2, 3 }; // index: 0% .. 100% durability

  public static void on_config(boolean without_redia_torchplacing, boolean without_redia_hoeing,
                               boolean without_redia_tree_chopping, int durability, String efficiency_curve,
                               String fortune_curve)
  {
    with_torch_placing = !without_redia_torchplacing;
    with_hoeing = !without_redia_hoeing;
    with_tree_felling = !without_redia_tree_chopping;
    max_damage_ = MathHelper.clamp(durability, 800, 3000);
    ModEngineersTools.logger.info("REDIA tool config: "
            + (with_torch_placing?"":"no-") + "torch-placing, "
            + (with_hoeing?"":"no-") + "hoeing, "
            + (with_tree_felling?"":"no-") + "tree-felling."
    );
    // Efficiency
    {
      String[] sc = efficiency_curve.replaceAll("^[,0-9]", "").split(",");
      if(sc.length > 0) {
        ArrayList<Double> dc = new ArrayList<Double>();
        for(int i=0; (i<sc.length) && (i<efficiency_decay.length); ++i) dc.add(MathHelper.clamp(Double.parseDouble(sc[i]), 20, 250));
        for(int i=1; i<dc.size(); ++i) {
          if(dc.get(i) < dc.get(i-1)) dc.set(i, dc.get(i-1));
        }
        while(dc.size() < efficiency_decay.length) dc.add(dc.get(dc.size()-1));
        for(int i=0; i<dc.size(); ++i) efficiency_decay[i] = dc.get(i)/100;
      }
      StringBuilder confout = new StringBuilder();
      confout.append("REDIA tool efficiency curve: [");
      for(int i=0; i<efficiency_decay.length; ++i) confout.append(Math.round(efficiency_decay[i]*100)).append(",");
      confout.deleteCharAt(confout.length()-1).append("]");
      ModEngineersTools.logger.info(confout.toString());
    }
    // Fortune
    {
      String[] sc = efficiency_curve.replaceAll("^[,0-9]", "").split(",");
      if(sc.length > 0) {
        ArrayList<Integer> dc = new ArrayList<Integer>();
        for(int i=0; (i<sc.length) && (i<fortune_decay.length); ++i) dc.add(MathHelper.clamp(Integer.parseInt(sc[i]), 0, 3));
        for(int i=1; i<dc.size(); ++i) {
          if(dc.get(i) < dc.get(i-1)) dc.set(i, dc.get(i-1));
        }
        while(dc.size() < fortune_decay.length) dc.add(dc.get(dc.size()-1));
      }
      StringBuilder confout = new StringBuilder();
      confout.append("REDIA tool fortune curve: [");
      for(int i=0; i<fortune_decay.length; ++i) confout.append(Math.round(fortune_decay[i])).append(",");
      confout.deleteCharAt(confout.length()-1).append("]");
      ModEngineersTools.logger.info(confout.toString());
    }
  }

  // -------------------------------------------------------------------------------------------------------------------

  ItemRediaTool(String registryName)
  {
    super(ToolMaterial.DIAMOND);
    setRegistryName(ModEngineersTools.MODID, registryName);
    setTranslationKey(ModEngineersTools.MODID + "." + registryName);
    setCreativeTab(ModEngineersTools.CREATIVE_TAB_ENGINEERSTOOLS);
    setMaxStackSize(1);
    setHasSubtypes(false);
    setHarvestLevel("pickaxe", 3);
    setHarvestLevel("axe", 3);
    setHarvestLevel("shovel", 3);
    setMaxDamage(max_damage_);
//    this.attackSpeed = -4f;
//    this.attackDamage = 9;
  }

  @SideOnly(Side.CLIENT)
  public boolean isFull3D()
  { return true; }

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
  { ModAuxiliaries.Tooltip.addInformation(stack, world, tooltip, flag, true); }

  @SideOnly(Side.CLIENT)
  public boolean hasEffect(ItemStack stack)
  { return false; } // don't show enchantment glint, looks awful. Also nice to cause some confusion ;-)

  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public int getItemEnchantability()
  { return enchantability; }

  public String getToolMaterialName()
  { return toolMaterial.toString(); }

  @Override
  public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
  {
    ItemStack mat = toolMaterial.getRepairItemStack();
    if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) return true;
    return super.getIsRepairable(toRepair, repair);
  }

  @Override
  @SuppressWarnings("deprecation")
  public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
  {
    Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
    if(slot == EntityEquipmentSlot.MAINHAND)  {
      // That messes up rendering?! Why that?
      //multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double)this.attackDamage, 0));
      //multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", (double)this.attackSpeed, 0));
    }
    return multimap;
  }

  @Override
  public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState state)
  {
    switch(toolClass) {
      case "axe":
      case "pickaxe":
      case "shovel":
        return 3; // diamond
      default:
        return 2;
    }
  }

  @Override
  public Set<String> getToolClasses(ItemStack stack)
  { return com.google.common.collect.ImmutableSet.of("axe", "pickaxe", "shovel"); }

  @Override
  public boolean isDamageable()
  { return true; }

  @Override
  public boolean canHarvestBlock(IBlockState block)
  { return true; }

  @Override
  public int getItemBurnTime(ItemStack itemStack)
  { return 0; }

  @Override
  public float getSmeltingExperience(ItemStack item)
  { return 0; }

  @Override
  public boolean canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity, EntityLivingBase attacker)
  { return true; }

  @Override
  public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity)
  { return false; }

  @Override
  @SuppressWarnings("deprecation")
  public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot)
  {
    Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);
    if(slot != EntityEquipmentSlot.MAINHAND) return multimap;
    multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)this.attackDamage, 0));
    multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", (double)this.attackSpeed, 0));
    return multimap;
  }

  @Override
  public boolean isBookEnchantable(ItemStack tool, ItemStack book)
  { return true; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
  {
    if(enchantment == Enchantments.FORTUNE) return false;
    if(enchantment == Enchantments.EFFICIENCY) return false;
    if(enchantment == Enchantments.KNOCKBACK) return true;
    if(enchantment == Enchantments.LOOTING) return true;
    if(enchantment == Enchantments.SHARPNESS) return true;
    if(enchantment == Enchantments.FIRE_ASPECT) return true;
    return enchantment.type.canEnchantItem(stack.getItem());
  }

  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
  {

    stack.damageItem(2, attacker);
    return true;
  }

  @Override
  public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
  { return (entity instanceof EntityVillager); } // Cancel attacks for villagers.

  @Override
  public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  {
    EnumActionResult rv = EnumActionResult.PASS;
    if(player.isSneaking()) {
      rv = tryPlantSnipping(player, world, pos, hand, facing, hitX, hitY, hitZ);
      if(rv != EnumActionResult.PASS) return rv;
      if(facing == EnumFacing.UP) {
        rv = tryDigOver(player, world, pos, hand, facing, hitX, hitY, hitZ);
        if(rv != EnumActionResult.PASS) return rv;
      }
    } else {
      rv = tryTorchPlacing(player, world, pos, hand, facing, hitX, hitY, hitZ);
      if(rv != EnumActionResult.PASS) return rv;
    }
    return rv;
  }

  @Override
  public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player)
  {
    if(!player.world.isRemote) setFortune(stack);
    return false;
  }

  @Override
  public boolean onBlockDestroyed(ItemStack tool, World world, IBlockState state, BlockPos pos, EntityLivingBase player)
  {
    if(!world.isRemote) {
      if(state.getBlockHardness(world, pos) != 0.0f) tool.damageItem(1, player);
      {
        NBTTagCompound nbt = tool.getTagCompound();
        if(nbt==null) nbt = new NBTTagCompound();
        nbt.setInteger("lhbh", state.getBlock().hashCode());
        tool.setTagCompound(nbt);
      }
      if(with_tree_felling && (player instanceof EntityPlayer) && (player.isSneaking())) {
        if(tryTreeFelling(world, state, pos, player)) return true;
      }
    }
    resetFortune(tool);
    return true;
  }

  @Override
  public float getDestroySpeed(ItemStack stack, IBlockState state)
  {
    double ramp_scaler = 1.0;
    {
      final int hitcount_max = 5;
      NBTTagCompound nbt = stack.getTagCompound();
      if(nbt==null) nbt = new NBTTagCompound();
      int lasthitblock = nbt.getInteger("lhbh");
      if(lasthitblock != 0) { // this also means it's on the server (`onBlockDestroyed()`)
        int hitcount = nbt.getInteger("lhbc");
        int hit_id = state.getBlock().hashCode();
        if(lasthitblock==hit_id) {
          hitcount = Math.min(hitcount+1, hitcount_max);
        } else {
          lasthitblock = hit_id;
          hitcount = 0;
        }
        nbt.setInteger("lhbh", lasthitblock);
        nbt.setInteger("lhbc", hitcount);
        stack.setTagCompound(nbt);
        ramp_scaler = 0.5 + 0.5 * ((double)hitcount) / hitcount_max;
      }
    }
    return (float)(
      ((double)efficiency) * ramp_scaler *
      efficiency_decay[
        (int)MathHelper.clamp(relativeDurability(stack) * efficiency_decay.length,0,efficiency_decay.length-1)
      ]
    );
  }

  @Override
  public boolean itemInteractionForEntity(ItemStack tool, EntityPlayer player, EntityLivingBase entity, EnumHand hand)
  {
    if(entity.world.isRemote) return false;
    setFortune(player.getHeldItem(hand));
    boolean handled = false
      || tryEntityShearing(tool, player, entity, hand)
      ;
    resetFortune(player.getHeldItem(hand));
    return handled;
  }

  // -------------------------------------------------------------------------------------------------------------------

  private void setFortune(ItemStack stack)
  {
    int fortune = durabilityDependentFortune(stack);
    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
    if(fortune > 0) {
      enchantments.put(Enchantments.FORTUNE, fortune);
    } else if(enchantments.containsKey(Enchantments.FORTUNE)) {
      enchantments.remove(Enchantments.FORTUNE);
    }
    EnchantmentHelper.setEnchantments(enchantments, stack);
  }

  private void resetFortune(ItemStack stack)
  {
    int fortune = durabilityDependentFortune(stack);
    if(fortune==0) return;
    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
    enchantments.remove(Enchantments.FORTUNE);
    EnchantmentHelper.setEnchantments(enchantments, stack);
  }

  private double relativeDurability(ItemStack stack)
  { return MathHelper.clamp(((double)(getMaxDamage(stack)-getDamage(stack)))/((double)getMaxDamage(stack)), 0,1.0); }

  private int durabilityDependentFortune(ItemStack stack)
  { return fortune_decay[MathHelper.clamp((int)(relativeDurability(stack)*fortune_decay.length), 0, fortune_decay.length-1)]; }

  private boolean tryEntityShearing(ItemStack tool, EntityPlayer player, EntityLivingBase entity, EnumHand hand)
  {
    if(!(entity instanceof IShearable)) return false;
    IShearable target = (IShearable)entity;
    BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
    if(!target.isShearable(tool, entity.world, pos)) return false;
    List<ItemStack> drops = target.onSheared(tool, entity.world, pos, durabilityDependentFortune(tool));
    Random rand = new Random();
    for(ItemStack stack:drops) {
      EntityItem e = entity.entityDropItem(stack, 1f);
      e.motionY += rand.nextFloat() * 0.05f;
      e.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1f;
      e.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1f;
    }
    tool.damageItem(1, entity);
    return true;
  }

  private EnumActionResult tryPlantSnipping(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  {
    if(!with_shearing) return EnumActionResult.PASS;
    final IBlockState state = world.getBlockState(pos);
    if(state.getBlock() instanceof IShearable) {
      if(((IShearable)state.getBlock()).isShearable(new ItemStack(Items.SHEARS), world, pos)) {
        final ItemStack tool = player.getHeldItem(hand);
        if(tool.getItem()==this) {
          world.playSound(player, pos, SoundEvents.ENTITY_MOOSHROOM_SHEAR, SoundCategory.BLOCKS, 0.8f, 1.1f);
          List<ItemStack> stacks = ((IShearable)state.getBlock()).onSheared(new ItemStack(Items.SHEARS), world, pos, durabilityDependentFortune(tool));
          if(!world.isRemote) {
            for(ItemStack stack:stacks) {
              EntityItem ie = new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, stack);
              ie.setDefaultPickupDelay();
              world.spawnEntity(ie);
            }
          }
          state.getBlock().onBlockHarvested(world, pos, state, player);
          world.setBlockState(pos, Blocks.AIR.getDefaultState(), 1|2|8); //
          tool.damageItem(1, player);
        }
      }
    }
    return EnumActionResult.PASS;
  }

  private EnumActionResult tryTorchPlacing(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  {
    if(!with_torch_placing) return EnumActionResult.PASS;
    for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
      ItemStack stack = player.inventory.getStackInSlot(i);
      if((!stack.isEmpty()) && (stack.getItem()== Item.getItemFromBlock(Blocks.TORCH))) {
        ItemStack tool = player.getHeldItem(hand);
        player.setHeldItem(hand, stack);
        EnumActionResult r = stack.getItem().onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        player.setHeldItem(hand, tool);
        return r;
      }
    }
    return EnumActionResult.PASS;
  }

  private EnumActionResult tryDigOver(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  {
    if(!with_hoeing) return EnumActionResult.PASS;
    if(world.getTileEntity(pos) != null) return EnumActionResult.PASS;
    final IBlockState state = world.getBlockState(pos);
    IBlockState replaced = state;
    final Block block = state.getBlock();
    if((block instanceof BlockGrass) || ((block==Blocks.DIRT) && (state.getValue(BlockDirt.VARIANT) != BlockDirt.DirtType.COARSE_DIRT))) {
      replaced = Blocks.FARMLAND.getDefaultState();
    } else if(block instanceof BlockFarmland) {
      replaced = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    } else if((block==Blocks.DIRT) && (block.getMetaFromState(state)==1)) {
      replaced = Blocks.GRASS_PATH.getDefaultState();
    } else if(block instanceof BlockGrassPath) {
      replaced = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, DirtType.DIRT);
    }
    if(replaced != state) {
      world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 0.8f, 1.1f);
      if(!world.isRemote)
      {
        world.setBlockState(pos, replaced,1|2);
        ItemStack stack = player.getHeldItem(hand);
        if(stack.getItem() == this) stack.damageItem(1, player); // just to ensure, check likely not needed
      }
      return EnumActionResult.SUCCESS;
    } else {
      return EnumActionResult.PASS;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Fun algorithm coding ,)
  // -------------------------------------------------------------------------------------------------------------------

  private boolean tryTreeFelling(World world, IBlockState state, BlockPos pos, EntityLivingBase player)
  {
    if((!state.isFullBlock()) || (state.getMaterial() != Material.WOOD)) return false;
    if(world.isRemote) return true;
    Item item = Item.getItemFromBlock(state.getBlock());
    if(item==null) return false;
    int[] oids = OreDictionary.getOreIDs(new ItemStack(item));
    for(int i=0; i<oids.length; ++i) {
      if(OreDictionary.getOreName(oids[i]).matches("^log[A-Z].*$")) {
        chopTree(world, state, pos, player);
        return true;
      }
    }
    return false;
  }

  private static final List<Vec3i> hoffsets = ImmutableList.of(
    new Vec3i( 1,0, 0), new Vec3i( 1,0, 1), new Vec3i( 0,0, 1),
    new Vec3i(-1,0, 1), new Vec3i(-1,0, 0), new Vec3i(-1,0,-1),
    new Vec3i( 0,0,-1), new Vec3i( 1,0,-1)
  );

  private List<BlockPos> findBlocksAround(final World world, final BlockPos centerPos, final IBlockState leaf_type_state, final Set<BlockPos> checked, int recursion_left)
  {
    ArrayList<BlockPos> to_decay = new ArrayList<BlockPos>();
    for(int y=-1; y<=1; ++y) {
      final BlockPos layer = centerPos.add(0,y,0);
      for(Vec3i v:hoffsets) {
        BlockPos pos = layer.add(v);
        if((!checked.contains(pos)) && (world.getBlockState(pos).getBlock()==leaf_type_state.getBlock())) {
          checked.add(pos);
          to_decay.add(pos);
          if(recursion_left > 0) {
            to_decay.addAll(findBlocksAround(world, pos, leaf_type_state, checked, recursion_left-1));
          }
        }
      }
    }
    return to_decay;
  }

  private static boolean isSameLog(IBlockState a, IBlockState b)
  {
    // very strange  ...
    if(a.getBlock()!=b.getBlock()) {
      return false;
    } else if(a.getBlock() instanceof BlockNewLog) {
      return a.getValue(BlockNewLog.VARIANT) == b.getValue(BlockNewLog.VARIANT);
    } else if(a.getBlock() instanceof BlockOldLog) {
      return a.getValue(BlockOldLog.VARIANT) == b.getValue(BlockOldLog.VARIANT);
    } else {
      return false;
    }
  }

  private void chopTree(World world, IBlockState broken_state, BlockPos startPos, EntityLivingBase player)
  {
    final Block broken_block = broken_state.getBlock();
    if(!(broken_block instanceof BlockLog)) return;
    ItemStack tool = player.getHeldItemMainhand();
    if(tool.getItem() != this) tool = player.getHeldItemOffhand();
    if(tool.getItem() != this) return;
    final int max_broken_blocks = (tool.getMaxDamage()-tool.getItemDamage()) * 2/3;
    final long ymin = startPos.getY();
    final long max_leaf_distance = 6;
    Set<BlockPos> checked = new HashSet<BlockPos>();
    ArrayList<BlockPos> to_break = new ArrayList<BlockPos>();
    ArrayList<BlockPos> to_decay = new ArrayList<BlockPos>();
    checked.add(startPos);
    // Initial simple layer-up search of same logs. This forms the base corpus, and only leaves and
    // leaf-enclosed logs attached to this corpus may be broken/decayed.
    {
      LinkedList<BlockPos> queue = new LinkedList<BlockPos>();
      LinkedList<BlockPos> upqueue = new LinkedList<BlockPos>();
      queue.add(startPos);
      int cutlevel = 0;
      int steps_left = 64;
      while(!queue.isEmpty() && (--steps_left >= 0)) {
        final BlockPos pos = queue.removeFirst();
        // Vertical search
        final BlockPos uppos = pos.up();
        final IBlockState upstate = world.getBlockState(uppos);
        if(!checked.contains(uppos)) {
          checked.add(uppos);
          if(isSameLog(upstate, broken_state)) {
            // Up is log
            upqueue.add(uppos);
            to_break.add(uppos);
            steps_left = 64;
          } else {
            boolean isleaf = (upstate.getBlock() instanceof BlockLeaves) || (upstate.getBlock().isLeaves(upstate, world, uppos));
            if(isleaf || world.isAirBlock(uppos) || (upstate.getBlock() instanceof BlockVine)) {
              if(isleaf) to_decay.add(uppos);
              // Up is air, check adjacent for diagonal up (e.g. Accacia)
              for(Vec3i v:hoffsets) {
                final BlockPos p = uppos.add(v);
                if(checked.contains(p)) continue;
                checked.add(p);
                final IBlockState st = world.getBlockState(p);
                final Block bl = st.getBlock();
                if(isSameLog(st, broken_state)) {
                  queue.add(p);
                  to_break.add(p);
                } else if((bl instanceof BlockLeaves) || (bl.isLeaves(st, world, p))) {
                  to_decay.add(p);
                }
              }
            }
          }
        }
        // Lateral search
        for(Vec3i v:hoffsets) {
          final BlockPos p = pos.add(v);
          if(checked.contains(p)) continue;
          checked.add(p);
          if(p.distanceSq(new BlockPos(startPos.getX(), p.getY(), startPos.getZ())) > (3+cutlevel*cutlevel)) continue;
          final IBlockState st = world.getBlockState(p);
          final Block bl = st.getBlock();
          if(isSameLog(st, broken_state)) {
            queue.add(p);
            to_break.add(p);
          } else if((bl instanceof BlockLeaves) || (bl.isLeaves(st, world, p))) {
            to_decay.add(p);
          }
        }
        if(queue.isEmpty() && (!upqueue.isEmpty())) {
          queue = upqueue;
          upqueue = new LinkedList<BlockPos>();
          ++cutlevel;
        }
      }
    }
    {
      // Determine lose logs between the leafs
      for(BlockPos pos:to_decay) {
        int dist = 1;
        to_break.addAll(findBlocksAround(world, pos, broken_state, checked, dist));
      }
    }
    if(!to_decay.isEmpty()) {
      final IBlockState leaf_type_state = world.getBlockState(to_decay.get(0));
      final ArrayList<BlockPos> leafs = to_decay;
      to_decay = new ArrayList<BlockPos>();
      for(BlockPos pos:leafs) {
        int dist = 2;
        to_decay.add(pos);
        to_decay.addAll(findBlocksAround(world, pos, leaf_type_state, checked, dist));
      }
    }
    checked.remove(startPos);
    for(BlockPos pos:to_break) {
      IBlockState state = world.getBlockState(pos);
      world.setBlockToAir(pos);
      state.getBlock().dropBlockAsItem(world, pos, state, 0);
    }
    for(BlockPos pos:to_decay) {
      IBlockState state = world.getBlockState(pos);
      world.setBlockToAir(pos);
      state.getBlock().dropBlockAsItem(world, pos, state, 0);
    }
    {
      // And now the bill.
      int dmg = (to_break.size()*3/2)+(to_decay.size()/8) - 1;
      if(dmg < 1) dmg = 1;
      tool.damageItem(dmg, player);
    }
  }
}
