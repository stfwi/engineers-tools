/*
 * @file TrackerItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.engineerstools.items;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.detail.ModRenderers;
import wile.engineerstools.libmc.detail.Auxiliaries;
import wile.engineerstools.libmc.detail.Overlay;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class TrackerItem extends EtItem
{
  public TrackerItem(Item.Properties properties)
  { super(properties.maxStackSize(1).setNoRepair().setISTER(TrackerItem::createIster)); }

  //--------------------------------------------------------------------------------------------------------------------
  // Item / IForgeItem
  //--------------------------------------------------------------------------------------------------------------------

  private static java.util.concurrent.Callable<net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer> createIster()
  { return ModRenderers.TrackerIster::new; }

  @OnlyIn(Dist.CLIENT)
  public void registerModels()
  {
    net.minecraftforge.client.model.ModelLoader.addSpecialModel(new ModelResourceLocation(new ResourceLocation(ModEngineersTools.MODID, "tracker_model"), "inventory"));
    net.minecraftforge.client.model.ModelLoader.addSpecialModel(new ModelResourceLocation(new ResourceLocation(ModEngineersTools.MODID, "tracker_pointer_model"), "inventory"));
  }

  @Override
  public boolean hasEffect(ItemStack stack)
  { return false; }

  @Override
  public boolean isEnchantable(ItemStack stack)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean isRepairable(ItemStack stack)
  { return false; }

  @Override
  public boolean showDurabilityBar(ItemStack stack)
  { return false; }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
  {
    if(Auxiliaries.Tooltip.helpCondition() || Auxiliaries.Tooltip.extendedTipCondition()) {
      super.addInformation(stack, world, tooltip, flag);
      return;
    }
    CompoundNBT nbt = stack.getChildTag("trackerdata");
    if(nbt==null) return;
    int distance = -1;
    String dimension_name = "";
    String text = "";
    if(nbt.contains("target")) {
      BlockPos target_pos = BlockPos.fromLong(nbt.getLong("target"));
      if(nbt.contains("playerpos")) distance = (int)Math.sqrt(BlockPos.fromLong(nbt.getLong("playerpos")).distanceSq(target_pos));
    } else {
      dimension_name = nbt.getString("dimensionid");
    }
    if(nbt.contains("location")) {
      BlockPos pos = BlockPos.fromLong(nbt.getLong("location"));
      text = "["+pos.getX()+","+pos.getY()+","+pos.getZ()+"]";
      text = Auxiliaries.localizable("item."+ModEngineersTools.MODID+".tracker.tip.target.location", null, text).getString();
    } else if(nbt.contains("entityname")) {
      text = nbt.getString("entityname");
      if(text.isEmpty()) return;
      text = Auxiliaries.localizable("item."+ModEngineersTools.MODID+".tracker.tip.target.entity", null, text).getString();
    }
    if(distance >= 0) {
      if(distance > 0) {
        text += Auxiliaries.localizable("item."+ModEngineersTools.MODID+".tracker.tip.target.distance", null, distance).getString();
      }
    } else if(!dimension_name.isEmpty()) {
      text += Auxiliaries.localizable("item."+ModEngineersTools.MODID+".tracker.tip.dimension."+dimension_name, null, distance).getString();
    }
    tooltip.add(new StringTextComponent(text));
  }

  @Override
  public ActionResultType onItemUse(ItemUseContext context)
  {
    World world = context.getWorld();
    if(world.isRemote()) return ActionResultType.CONSUME;
    if(!checkOverwrite(context.getItem(), context.getPlayer())) return ActionResultType.FAIL;
    CompoundNBT nbt = new CompoundNBT();
    nbt.putLong("location", context.getPos().toLong());
    nbt.putInt("dimension", dimensionIdentifier(world));
    nbt.putString("dimensionid", dimensionName(world));
    context.getItem().setTagInfo("trackerdata", nbt);
    Overlay.show(context.getPlayer(), Auxiliaries.localizable("item."+ModEngineersTools.MODID+".tracker.msg.locationset"));
    return ActionResultType.SUCCESS;
  }

  @Override
  public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand)
  {
    if(player.getEntityWorld().isRemote()) return ActionResultType.CONSUME;
    if(!checkOverwrite(stack, player)) return ActionResultType.FAIL;
    CompoundNBT nbt = new CompoundNBT();
    nbt.putUniqueId("entity", target.getUniqueID());
    nbt.putString("entityname", target.getDisplayName().getString());
    stack.setTagInfo("trackerdata", nbt);
    Overlay.show(player, Auxiliaries.localizable("item."+ModEngineersTools.MODID+".tracker.msg.entityset", null, target.getDisplayName()));
    return ActionResultType.SUCCESS;
  }

  @Override
  public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
  {
    if(!(entity instanceof PlayerEntity)) return;
    if(world instanceof ServerWorld) {
      serverTick(stack, (ServerWorld)world, (PlayerEntity)entity, slot, selected);
    } else {
      clientTick(stack, world, (PlayerEntity)entity, slot, selected);
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Tracker
  //--------------------------------------------------------------------------------------------------------------------

  private boolean checkOverwrite(ItemStack stack, PlayerEntity player)
  {
    CompoundNBT nbt = stack.getChildTag("trackerdata");
    if((nbt==null) || (nbt.isEmpty())) return true;
    if(player.getLookVec().y < -0.98) {
      stack.setTag(null);
      Overlay.show(player, Auxiliaries.localizable("item."+ModEngineersTools.MODID+".tracker.hint.cleared"));
    } else {
      Overlay.show(player, Auxiliaries.localizable("item."+ModEngineersTools.MODID+".tracker.hint.clearfirst"));
    }
    return false;
  }

  //Dist.CLIENT
  private static final ConcurrentHashMap<Integer, Tuple<Integer,Integer>> tracker_angles = new ConcurrentHashMap<Integer, Tuple<Integer,Integer>>();

  //Dist.CLIENT
  public static Optional<Tuple<Integer,Integer>> getUiAngles(ItemStack stack)
  {
    final CompoundNBT nbt = stack.getChildTag("trackerdata");
    if((nbt==null) || (!nbt.contains("id"))) return Optional.empty();
    Tuple<Integer,Integer> rot = tracker_angles.getOrDefault(nbt.getInt("id"), null);
    return (rot==null) ? Optional.empty() : Optional.of(rot);
  }

  private int dimensionIdentifier(World world)
  { return dimensionName(world).hashCode(); }

  private String dimensionName(World world)
  {
    if(world.getDimensionKey() == World.OVERWORLD) return "overworld";
    if(world.getDimensionKey() == World.THE_NETHER) return "nether";
    if(world.getDimensionKey() == World.THE_END) return "end";
    return "other";
  }

  //Dist.CLIENT
  private void clientTick(ItemStack stack, World world, PlayerEntity player, int slot, boolean selected)
  {
    final CompoundNBT nbt = stack.getChildTag("trackerdata");
    if((nbt==null) || nbt.isEmpty() || (!nbt.contains("id"))) {
      return;
    } else if(!nbt.contains("target")) {
      tracker_angles.remove(nbt.getInt("id"));
      return;
    } else {
      BlockPos pos = BlockPos.fromLong(nbt.getLong("target"));
      Vector3d gdv = (new Vector3d(pos.getX(), pos.getY(), pos.getZ())).subtract(player.getPositionVec());
      final double dsq = gdv.lengthSquared();
      if(dsq < 0.3) return;
      final double xz_distance = (new Vector3d(gdv.x, 0, gdv.z)).length();
      final double y_distance = Math.abs(gdv.y);
      gdv = gdv.normalize();
      final Vector3d ldv = player.getLookVec();
      double ry = (Math.atan2(ldv.z, ldv.x) - Math.atan2(gdv.z, gdv.x)) * 180./Math.PI;
      double rx = (y_distance+5 > xz_distance) ? ((Math.acos(ldv.y)-Math.acos(gdv.y)) * 180./Math.PI) : (0);
      final double inc = 10;
      if(rx > 180) rx -= 360;
      if(ry > 180) ry -= 360;
      if(Math.abs(rx) < 30) rx = 0;
      nbt.putLong("playerpos", new BlockPos(player.getPositionVec()).toLong());
      tracker_angles.put(nbt.getInt("id"), new Tuple<Integer,Integer>((int)rx,(int)ry));
    }
  }

  private void serverTick(ItemStack stack, ServerWorld world, PlayerEntity player, int slot, boolean selected)
  {
    if((world.getGameTime() & 0x7) != 0) return;
    if((stack.getTag()==null) || (stack.getTag().isEmpty())) return;
    boolean changed = false;
    final CompoundNBT nbt = stack.getOrCreateChildTag("trackerdata");
    if(!nbt.contains("id")) {
      nbt.putInt("id", world.getRandom().nextInt());
      if(tracker_angles.size() > 128) {
        tracker_angles.clear();
      }
    }
    if(nbt.contains("location")) {
      long pos = nbt.getLong("location");
      long uipos = nbt.getLong("target");
      if(dimensionIdentifier(world) != nbt.getLong("dimension")) {
        if(nbt.contains("target")) {
          nbt.remove("target");
          changed = true;
        }
      } else if(pos != uipos) {
        nbt.putLong("target", pos);
        changed = true;
      }
    } else if(nbt.contains("entity")) {
      Entity target = world.getEntityByUuid(nbt.getUniqueId("entity"));
      if((target == null) || (target.getEntityWorld()==null) || (dimensionIdentifier(target.getEntityWorld()) != dimensionIdentifier(player.getEntityWorld()))) {
        if(nbt.contains("target")) {
          nbt.remove("target");
          changed = true;
        }
      } else {
        BlockPos uipos = BlockPos.fromLong(nbt.getLong("target"));
        if(player.getDistanceSq(target) > 200) {
          if(uipos.distanceSq(target.getPositionVec(), false) > 10) {
            nbt.putLong("target", (new BlockPos(target.getPositionVec())).toLong());
            changed = true;
          }
        } else {
          if(uipos.distanceSq(target.getPositionVec(), false) > 2.78) {
            nbt.putLong("target", (new BlockPos(target.getPositionVec())).toLong());
            changed = true;
          }
        }
      }
      if(changed && (target != null) && (target.getEntityWorld()!=null)) {
        String target_dimension = dimensionName(target.getEntityWorld());
        if(!nbt.getString("dimensionid").equals(target_dimension)) {
          nbt.putString("dimensionid", target_dimension);
          changed = true;
        }
      }
    }
    if(changed) {
      stack.setTagInfo("trackerdata", nbt); // client sync only when needed
    }
  }

}
