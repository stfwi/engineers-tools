/*
 * @file MaterialBagItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Musli Power Bar food.
 */
package wile.engineerstools.items;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.*;
import wile.engineerstools.ModContent;
import wile.engineerstools.libmc.detail.Auxiliaries;
import wile.engineerstools.libmc.detail.Inventories;
import wile.engineerstools.libmc.detail.Inventories.SlotRange;
import wile.engineerstools.libmc.detail.Networking;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import com.mojang.blaze3d.systems.RenderSystem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;


public class MaterialBoxItem extends EtItem
{
  private static boolean with_gui_mouse_handling = true;

  public static void on_config(boolean without_gui_mouse_handling, String item_blacklist)
  {
    with_gui_mouse_handling = !without_gui_mouse_handling;
    Auxiliaries.logInfo("Bag: " + (without_gui_mouse_handling ? "no-mouse-tweaks" : "") + (item_blacklist.isEmpty() ? "" : (", blacklist:'"+item_blacklist+"'")));
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Item
  //--------------------------------------------------------------------------------------------------------------------

  public MaterialBoxItem(Item.Properties properties)
  { super(properties); }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(final ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
  {
    if(!Auxiliaries.Tooltip.extendedTipCondition() || Auxiliaries.Tooltip.helpCondition()) {
      super.appendHoverText(stack, world, tooltip, flag);
      return;
    }
    int num_used_slots = 0;
    HashMap<Item, Integer> stats = new HashMap<>();
    if(stack.hasTag()) {
      NonNullList<ItemStack> items = Inventories.readNbtStacks(stack.getTag(), "stacks", MaterialBoxContainer.NUM_OF_STORGE_SLOTS);
      for(int i = 0; i < MaterialBoxContainer.NUM_OF_STORGE_SLOTS; ++i) {
        final ItemStack st = items.get(i);
        if(st.isEmpty()) continue;
        stats.put(st.getItem(), stats.getOrDefault(st.getItem(), 0) + 1); // actually the slot count is more interesting st.getCount()
        ++num_used_slots;
      }
    }
    tooltip.add(new StringTextComponent(
      Auxiliaries.localize(getDescriptionId()+".tip", new Object[] {
        num_used_slots,
        MaterialBoxContainer.NUM_OF_STORGE_SLOTS - num_used_slots
      })
    ));
    // Details
    if(!stats.isEmpty()) {
      List<ITextComponent> elems = stats.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(e->new TranslationTextComponent(e.getKey().getDescriptionId()))
        .collect(Collectors.toList());
      Collections.reverse(elems);
      final int MAX_TOOLTIP_ITEM_LINE_LENGTH = 40;
      final int MAX_TOOLTIP_ITEMS = 20;
      StringTextComponent item_list = new StringTextComponent("");
      int slen = 0;
      if(!elems.isEmpty()) {
        tooltip.add(new StringTextComponent(""));
        int nleft = MAX_TOOLTIP_ITEMS;
        for(ITextComponent e:elems) {
          if(--nleft < 0) break;
          if((slen+e.getContents().length()) > MAX_TOOLTIP_ITEM_LINE_LENGTH) {
            tooltip.add(item_list);
            item_list = new StringTextComponent("");
            slen = 0;
          } else if(slen > 0) {
            item_list.append(new TranslationTextComponent(" | ")).withStyle(TextFormatting.DARK_GRAY);
          }
          item_list.append(
            ((e instanceof IFormattableTextComponent) ? (((IFormattableTextComponent)e).withStyle(TextFormatting.GRAY)) : (e))
          );
          slen += e.getContents().length();
        }
        if(slen > 0) tooltip.add(item_list);
      }
    }
  }

  @Override
  public ActionResultType useOn(ItemUseContext context)
  { return ActionResultType.PASS; }

  @Override
  public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
  {
    ItemStack stack = player.getItemInHand(hand);
    if(world.isClientSide()) {
      if((stack.getCount() > 1)) {
        world.playSound(player, player.blockPosition(), SoundEvents.CHEST_LOCKED,SoundCategory.NEUTRAL, 0.4f, 3f);
        return new ActionResult<>(ActionResultType.FAIL, stack);
      }
    } else if((stack.getCount() == 1)) {
      NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
        @Override
        public ITextComponent getDisplayName()
        { return new TranslationTextComponent(getRegistryName().toString()); }

        @Override
        public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player)
        { return new MaterialBoxContainer(id, inventory, player); }
      });
    }
    return new ActionResult<>(ActionResultType.SUCCESS, stack);
  }

  @Override
  public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity)
  { return true; }

  @Override
  public boolean isEnchantable(ItemStack stack)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public boolean canAttackBlock(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
  { return false; }

  @Override
  @SuppressWarnings("deprecation")
  public int getItemStackLimit(ItemStack stack)
  { return stack.hasTag() ? 1 : getMaxStackSize(); }

  @Override
  public boolean hasCustomEntity(ItemStack stack)
  { return true; }

  @Nullable
  public Entity createEntity(World world, final Entity oe, ItemStack stack)
  {
    final boolean is_filled = ((stack.getCount() == 1) && (stack.hasTag()));
    ItemEntity e = new MaterialBoxEntity(world, oe.getX(),oe.getY(),oe.getZ(), stack);
    if(oe instanceof ItemEntity)
    e.setDefaultPickUpDelay();
    e.setSilent(true);
    e.setDeltaMovement(oe.getDeltaMovement());
    if(oe instanceof ItemEntity) { // you never know who hooks into that ...
      e.setOwner(((ItemEntity)oe).getOwner());
      e.setThrower(((ItemEntity)oe).getThrower());
    }
    return e;
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Container
  //--------------------------------------------------------------------------------------------------------------------

  public static class MaterialBoxContainer extends Container implements Networking.INetworkSynchronisableContainer
  {
    protected static final int NUM_OF_STORGE_SLOTS = 54;
    protected static final int STORAGE_SLOT_BEGIN = 0;
    protected static final int STORAGE_SLOT_END = (STORAGE_SLOT_BEGIN+NUM_OF_STORGE_SLOTS);
    protected static final int PLAYER_SLOT_BEGIN = STORAGE_SLOT_END;
    protected static final int PLAYER_SLOT_END = (PLAYER_SLOT_BEGIN+36);

    protected static final String QUICK_MOVE_ALL = "quick-move-all";
    protected static final String INCREASE_STACK = "increase-stack";
    protected static final String DECREASE_STACK = "decrease-stack";

    //------------------------------------------------------------------------------------------------------------------

    private static boolean is_filled_box(ItemStack stack)
    { return (stack.getItem() instanceof MaterialBoxItem) && (stack.hasTag()); }

    private static class StorageSlot extends Slot
    {
      private final MaterialBoxContainer uicontainer;

      public StorageSlot(MaterialBoxContainer uicontainer, IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); this.uicontainer = uicontainer; }

      @Override
      public boolean mayPlace(ItemStack stack)
      { return !is_filled_box(stack); }

      @Override
      public void setChanged()
      {
        container.setChanged();
        uicontainer.onSlotsChanged();
      }
    }

    private static class PlayerSlot extends Slot
    {
      private final MaterialBoxContainer uicontainer;

      public PlayerSlot(MaterialBoxContainer uicontainer, IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); this.uicontainer = uicontainer; }

      @Override
      public void setChanged()
      {
        container.setChanged();
        uicontainer.onSlotsChanged();
      }
    }

    private static class ReadonlySlot extends Slot
    {
      public ReadonlySlot(IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); }

      @Override
      public boolean mayPlace(ItemStack stack)
      { return false; }

      public ItemStack remove(int amount)
      { return ItemStack.EMPTY; }

      public boolean mayPickup(PlayerEntity player)
      { return false; }
    }

    private static class StorageInventory extends Inventory
    {
      public StorageInventory(int nslots)
      { super(nslots); }

      @Override
      public boolean canPlaceItem(int index, ItemStack stack)
      { return !is_filled_box(stack); }
    }

    //------------------------------------------------------------------------------------------------------------------

    private PlayerEntity player;
    private PlayerInventory player_inventory;
    private StorageInventory inventory_ = new StorageInventory(NUM_OF_STORGE_SLOTS);
    private final ItemStack bag;
    private final SlotRange player_slot_range;
    private final SlotRange storage_slot_range;
    private ReadonlySlot bag_slot_ = null;

    // Container -------------------------------------------------------------------------------------------------------

    public MaterialBoxContainer(int cid, PlayerInventory player_inventory)
    { this(cid, player_inventory, player_inventory.player); }

    public MaterialBoxContainer(int cid, PlayerInventory player_inventory, PlayerEntity player)
    {
      super(ModContent.CT_MATERIAL_BAG, cid);
      this.player_inventory = player_inventory;
      this.player = player;
      storage_slot_range = new SlotRange(inventory_, 0, NUM_OF_STORGE_SLOTS);
      player_slot_range = new SlotRange(player_inventory, 0, 36);
      if((player_inventory.selected < 0) || (player_inventory.selected >= player_inventory.getContainerSize())
        || (!(player_inventory.getItem(player_inventory.selected).getItem() instanceof MaterialBoxItem))
      ) {
        bag = new ItemStack(Items.AIR);
        return;
      }
      bag = player_inventory.getItem(player_inventory.selected);
      int i=-1;
      // storage slots (stacks 0 to 53)
      for(int y=0; y<6; ++y) {
        for(int x=0; x<9; ++x) {
          int xpos = 28+x*18, ypos = 10+y*18;
          addSlot(new StorageSlot(this, inventory_, ++i, xpos, ypos));
        }
      }
      // player slots
      for(int x=0; x<9; ++x) {
        int slot = x;
        if(player_inventory.selected != slot) {
          addSlot(new PlayerSlot(this, player_inventory, slot, 28+x*18, 183)); // player slots: 0..8
        } else {
          addSlot(bag_slot_ = new ReadonlySlot(player_inventory, slot, 28+x*18, 183));
        }
      }
      for(int y=0; y<3; ++y) {
        for(int x=0; x<9; ++x) {
          int slot = x+y*9+9;
          if(player_inventory.selected != slot) {
            addSlot(new PlayerSlot(this, player_inventory, slot, 28+x*18, 125+y*18)); // player slots: 9..35
          } else {
            addSlot(bag_slot_ = new ReadonlySlot(player_inventory, slot, 28+x*18, 125+y*18));
          }
        }
      }
      read(bag.getTag());
    }

    @Override
    public boolean stillValid(PlayerEntity player)
    { return player == this.player; }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index)
    {
      Slot slot = getSlot(index);
      if((slot==null) || (!slot.hasItem())) return ItemStack.EMPTY;
      ItemStack slot_stack = slot.getItem();
      ItemStack transferred = slot_stack.copy();
      if((index>=0) && (index<PLAYER_SLOT_BEGIN)) {
        // Crate slots
        if(!moveItemStackTo(slot_stack, PLAYER_SLOT_BEGIN, PLAYER_SLOT_END, false)) return ItemStack.EMPTY;
      } else if((index >= PLAYER_SLOT_BEGIN) && (index <= PLAYER_SLOT_END)) {
        // Player slot
        if(!moveItemStackTo(slot_stack, STORAGE_SLOT_BEGIN, STORAGE_SLOT_END, false)) return ItemStack.EMPTY;
      } else {
        // Invalid slot
        return ItemStack.EMPTY;
      }
      if(slot_stack.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
      if(slot_stack.getCount() == transferred.getCount()) return ItemStack.EMPTY;
      slot.onTake(player, slot_stack);
      return transferred;
    }

    @Override
    public void removed(PlayerEntity player)
    {
      super.removed(player);
      if(player.level.isClientSide()) return;
      CompoundNBT nbt = write(bag.getTag());
      if((nbt!=null) && (nbt.isEmpty())) nbt = null;
      bag.setTag(nbt);
    }

    // INetworkSynchronisableContainer ---------------------------------------------------------------------------------

    @OnlyIn(Dist.CLIENT)
    public void onGuiAction(String message, CompoundNBT nbt)
    {
      nbt.putString("action", message);
      Networking.PacketContainerSyncClientToServer.sendToServer(containerId, nbt);
    }

    @Override
    public void onClientPacketReceived(int windowId, PlayerEntity player, CompoundNBT nbt)
    {
      boolean changed = false;
      if(!nbt.contains("action")) return;
      final int slotId = nbt.contains("slot") ? nbt.getInt("slot") : -1;
      if((bag_slot_!=null) && (slotId == bag_slot_.index)) return;
      switch(nbt.getString("action")) {
        case QUICK_MOVE_ALL: {
          if((slotId >= STORAGE_SLOT_BEGIN) && (slotId < STORAGE_SLOT_END) && (getSlot(slotId).hasItem())) {
            final Slot slot = getSlot(slotId);
            ItemStack remaining = slot.getItem();
            slot.set(ItemStack.EMPTY);
            final ItemStack ref_stack = remaining.copy();
            ref_stack.setCount(ref_stack.getMaxStackSize());
            for(int i=storage_slot_range.end_slot-storage_slot_range.start_slot; (i>0) && (!remaining.isEmpty()); --i) {
              remaining = player_slot_range.insert(remaining, false, 0, true, true);
              if(!remaining.isEmpty()) break;
              remaining = storage_slot_range.extract(ref_stack);
            }
            if(!remaining.isEmpty()) {
              slot.set(remaining); // put back
            }
          } else if((slotId >= PLAYER_SLOT_BEGIN) && (slotId < PLAYER_SLOT_END) && (getSlot(slotId).hasItem())) {
            final Slot slot = getSlot(slotId);
            ItemStack remaining = slot.getItem();
            slot.set(ItemStack.EMPTY);
            final ItemStack ref_stack = remaining.copy();
            ref_stack.setCount(ref_stack.getMaxStackSize());
            for(int i=player_slot_range.end_slot-player_slot_range.start_slot; (i>0) && (!remaining.isEmpty()); --i) {
              remaining = storage_slot_range.insert(remaining, false, 0, false, true);
              if(!remaining.isEmpty()) break;
              remaining = player_slot_range.extract(ref_stack);
            }
            if(!remaining.isEmpty()) {
              slot.set(remaining); // put back
            }
          }
          changed = true;
        } break;
        case INCREASE_STACK: {
        } break;
        case DECREASE_STACK: {
        } break;
      }
      if(changed) {
        inventory_.setChanged();
        player.inventory.setChanged();
        broadcastChanges();
      }
    }

    @Override
    public void onServerPacketReceived(int windowId, CompoundNBT pkg_nbt)
    {
      if(!(bag.getItem() instanceof MaterialBoxItem)) return;
      if(!pkg_nbt.contains("bag")) return;
      CompoundNBT nbt = pkg_nbt.getCompound("bag");
      read(nbt);
    }

    // Specific --------------------------------------------------------------------------------------------------------

    public CompoundNBT write(@Nullable CompoundNBT nbt)
    {
      NonNullList<ItemStack> stacks = NonNullList.create();
      for(int i=0; i<inventory_.getContainerSize(); ++i) stacks.add(inventory_.getItem(i));
      if(!stacks.stream().allMatch(ItemStack::isEmpty)) {
        nbt = Inventories.writeNbtStacks(nbt, "stacks", stacks, true);
      } else if((nbt!=null) && (nbt.contains("stacks"))) {
        nbt.remove("stacks");
      }
      return nbt;
    }

    public CompoundNBT read(CompoundNBT nbt)
    {
      NonNullList<ItemStack> stacks = Inventories.readNbtStacks(nbt, "stacks", inventory_.getContainerSize());
      for(int i=0; i<inventory_.getContainerSize(); ++i) inventory_.setItem(i, stacks.get(i));
      return nbt;
    }

    public void onSlotsChanged()
    {
      if(player.level.isClientSide()) return;
      // Sync client
      CompoundNBT nbt = write(new CompoundNBT());
      bag.setTag(nbt);
      player_inventory.setChanged();
      CompoundNBT pkg_nbt = new CompoundNBT();
      pkg_nbt.put("bag", nbt);
      Networking.PacketContainerSyncServerToClient.sendToPlayer(player, containerId, pkg_nbt);
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // GUI
  //--------------------------------------------------------------------------------------------------------------------

  @OnlyIn(Dist.CLIENT)
  public static class MaterialBoxGui extends ContainerScreen<MaterialBoxContainer>
  {
    private static final ResourceLocation BACKGROUND_IMAGE = new ResourceLocation(Auxiliaries.modid(), "textures/gui/material_box_gui.png");
    private final PlayerEntity player;

    public MaterialBoxGui(MaterialBoxContainer uicontainer, PlayerInventory player_inventory, ITextComponent title)
    {
      super(uicontainer, player_inventory, title);
      player = player_inventory.player;
      imageWidth = 213;
      imageHeight = 206;
    }

    @Override
    public void render(MatrixStack mx, int mouseX, int mouseY, float partialTicks)
    {
      renderBackground(mx);
      super.render(mx, mouseX, mouseY, partialTicks);
      renderTooltip(mx, mouseX, mouseY);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void renderBg(MatrixStack mx, float partialTicks, int mouseX, int mouseY)
    {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      getMinecraft().getTextureManager().bind(BACKGROUND_IMAGE);
      final int x0=getGuiLeft(), y0=getGuiTop(), w=getXSize(), h=getYSize();
      blit(mx, x0, y0, 0, 0, w, h);
      {
        final Slot slot = getMenu().bag_slot_;
        if(slot != null) blit(mx, x0+slot.x, y0+slot.y, 240, 183, 16, 16);
      }
    }

    @Override
    protected void renderLabels(MatrixStack mx, int x, int y)
    {}

    //------------------------------------------------------------------------------------------------------------------

    protected void action(String message)
    { action(message, new CompoundNBT()); }

    protected void action(String message, CompoundNBT nbt)
    { getMenu().onGuiAction(message, nbt); }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType type)
    {
      if(!with_gui_mouse_handling) {
        super.slotClicked(slot, slotId, button, type);
      } else if((type == ClickType.QUICK_MOVE) && (slot!=null) && slot.hasItem() && Auxiliaries.isShiftDown() && Auxiliaries.isCtrlDown()) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("slot", slotId);
        action(MaterialBoxContainer.QUICK_MOVE_ALL, nbt);
      } else {
        super.slotClicked(slot, slotId, button, type);
      }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double wheel_inc)
    {
      if(!with_gui_mouse_handling) return super.mouseScrolled(mouseX, mouseY, wheel_inc);
      final Slot slot = getSlotUnderMouse();
      if(!slot.hasItem()) return true;
      final int count = slot.getItem().getCount();
      int limit = (Auxiliaries.isShiftDown() ? 2 : 1) * (Auxiliaries.isCtrlDown() ? 4 : 1);
      if(wheel_inc > 0.1) {
        if(count > 0) {
          if((count < slot.getItem().getMaxStackSize()) && (count < slot.getMaxStackSize())) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("slot", slot.index);
            if(limit > 1) nbt.putInt("limit", limit);
            action(MaterialBoxContainer.INCREASE_STACK, nbt);
          }
        }
      } else if(wheel_inc < -0.1) {
        if(count > 0) {
          CompoundNBT nbt = new CompoundNBT();
          nbt.putInt("slot", slot.index);
          if(limit > 1) nbt.putInt("limit", limit);
          action(MaterialBoxContainer.DECREASE_STACK, nbt);
        }
      }
      return true;
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Entity
  //--------------------------------------------------------------------------------------------------------------------

  private static class MaterialBoxEntity extends ItemEntity
  {
    private final boolean filled;

    public MaterialBoxEntity(World world, double x, double y, double z, ItemStack stack)
    {
      super(world,x,y,z,stack);
      filled = ((getItem().getCount() == 1) && (getItem().hasTag()));
      setInvulnerable(filled);
      lifespan = filled ? 72000 : 6000;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hurt(DamageSource source, float amount)
    {
      if(!filled) return super.hurt(source, amount);
      if(level.isClientSide || removed) return false;
      lifespan = Math.max(lifespan-200, 200);
      return false;
    }
  }
}
