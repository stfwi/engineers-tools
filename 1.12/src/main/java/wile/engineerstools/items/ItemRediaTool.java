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
import wile.engineerstools.detail.BlockCategories;
import wile.engineerstools.detail.ModAuxiliaries;
import wile.engineerstools.detail.TreeCutting;
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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
import com.google.common.collect.Multimap;

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
  private static final int max_block_tracking_hitcount = 5;

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

  public ItemRediaTool(String registryName)
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
  public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState state)
  { return 3; } // diamond

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
    if(player.isSneaking() && tryPlantSnipping(player, world, pos, hand, facing, hitX, hitY, hitZ)) return EnumActionResult.SUCCESS;
    if(player.isSneaking() && (facing == EnumFacing.UP) && tryDigOver(player, world, pos, hand, facing, hitX, hitY, hitZ)) return EnumActionResult.SUCCESS;
    if(((!player.isSneaking()) || (facing.getAxis().isHorizontal())) && (tryTorchPlacing(player, world, pos, hand, facing, hitX, hitY, hitZ))) return EnumActionResult.SUCCESS;
    return EnumActionResult.PASS;
  }

  @Override
  public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player)
  { if(!player.world.isRemote){setFortune(stack);} return false; }

  private World a_world = null; // hate doing this, worse performance alternative would be to hook into the global onblockclicked player event.

  @Override
  public boolean onBlockDestroyed(ItemStack tool, World world, IBlockState state, BlockPos pos, EntityLivingBase player)
  {
    a_world = world;
    resetFortune(tool);
    if(world.isRemote) return true;
    if(state.getBlockHardness(world, pos) != 0f) tool.damageItem(1, player);
    if(with_tree_felling && (player instanceof EntityPlayer) && (player.isSneaking())) {
      if(tryTreeFelling(world, state, pos, player)) return true;
    }
    return true;
  }

  @Override
  public float getDestroySpeed(ItemStack stack, IBlockState state)
  {
    float hardness = 0.9f;
    try {
      if(a_world != null) hardness = MathHelper.clamp(state.getBlockHardness(a_world, BlockPos.ORIGIN), 0, 2);
    } catch(Throwable e) {
      a_world = null;
    }
    if(hardness >= 1) {
      return (float)((double)efficiency * efficiency_decay[(int)MathHelper.clamp(relativeDurability(stack) * efficiency_decay.length,0,efficiency_decay.length-1)]);
    } else {
      return efficiency;
    }
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

  private boolean tryPlantSnipping(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  {
    if(!with_shearing) return false;
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
          return true;
        }
      }
    }
    return false;
  }

  private boolean tryTorchPlacing(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  {
    if(!with_torch_placing) return false;
    for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
      ItemStack stack = player.inventory.getStackInSlot(i);
      if((!stack.isEmpty()) && (stack.getItem()== Item.getItemFromBlock(Blocks.TORCH))) {
        ItemStack tool = player.getHeldItem(hand);
        player.setHeldItem(hand, stack);
        EnumActionResult r = stack.getItem().onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        player.setHeldItem(hand, tool);
        return r!=EnumActionResult.PASS;
      }
    }
    return false;
  }

  private boolean tryDigOver(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
  {
    if(!with_hoeing) return false;
    if(world.getTileEntity(pos) != null)  return false;
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
      return true;
    } else {
      return false;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Fun algorithm coding ,)
  // -------------------------------------------------------------------------------------------------------------------

  private boolean tryTreeFelling(World world, IBlockState state, BlockPos pos, EntityLivingBase player)
  {
    if((!state.isFullBlock()) || (state.getMaterial() != Material.WOOD)) return false;
    if(world.isRemote) return true;
    if(BlockCategories.isLog(state)) {
      ItemStack tool = player.getHeldItemMainhand();
      if(tool.getItem() != this) tool = player.getHeldItemOffhand();
      if(tool.getItem() != this) return false;
      final int max_broken_blocks = (tool.getMaxDamage()-tool.getItemDamage()) * 2/3;
      final int dmg = TreeCutting.chopTree(world, state, pos, player, max_broken_blocks);
      tool.damageItem(dmg, player);
      if(player instanceof EntityPlayer) {
        float exhaustion = MathHelper.clamp(((float)dmg) / 8, 0.5f, 20f);
        ((EntityPlayer)player).addExhaustion(exhaustion);
      }
      return true;
    }
    return false;
  }
}
