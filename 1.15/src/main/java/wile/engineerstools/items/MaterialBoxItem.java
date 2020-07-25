/*
 * @file MaterialBagItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Musli Power Bar food.
 */
package wile.engineerstools.items;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
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
  public void addInformation(final ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
  {
    if(!Auxiliaries.Tooltip.extendedTipCondition() || Auxiliaries.Tooltip.helpCondition()) {
      super.addInformation(stack, world, tooltip, flag);
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
      Auxiliaries.localize(getTranslationKey()+".tip", new Object[] {
        num_used_slots,
        MaterialBoxContainer.NUM_OF_STORGE_SLOTS - num_used_slots
      })
    ));
    // Details
    if(!stats.isEmpty()) {
      List<ITextComponent> elems = stats.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(e->new TranslationTextComponent(e.getKey().getTranslationKey()))
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
          if((slen+e.getUnformattedComponentText().length()) > MAX_TOOLTIP_ITEM_LINE_LENGTH) {
            tooltip.add(item_list);
            item_list = new StringTextComponent("");
            slen = 0;
          } else if(slen > 0) {
            item_list.appendSibling(new TranslationTextComponent(" | ")).applyTextStyle(TextFormatting.DARK_GRAY);
          }
          item_list.appendSibling(e.applyTextStyle(TextFormatting.GRAY));
          slen += e.getUnformattedComponentText().length();
        }
        if(slen > 0) tooltip.add(item_list);
      }
    }
  }

  @Override
  public ActionResultType onItemUse(ItemUseContext context)
  { return ActionResultType.PASS; }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
  {
    ItemStack stack = player.getHeldItem(hand);
    if(world.isRemote()) {
      if((stack.getCount() > 1)) {
        world.playSound(player, player.getPosition(), SoundEvents.BLOCK_CHEST_LOCKED,SoundCategory.NEUTRAL, 0.4f, 3f);
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
  public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
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
    ItemEntity e = new MaterialBoxEntity(world, oe.getPosX(),oe.getPosY(),oe.getPosZ(), stack);
    if(oe instanceof ItemEntity)
    e.setDefaultPickupDelay();
    e.setSilent(true);
    e.setMotion(oe.getMotion());
    if(oe instanceof ItemEntity) { // you never know who hooks into that ...
      e.setOwnerId(((ItemEntity)oe).getOwnerId());
      e.setThrowerId(((ItemEntity)oe).getThrowerId());
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
      private final MaterialBoxContainer container;

      public StorageSlot(MaterialBoxContainer container, IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); this.container = container; }

      @Override
      public boolean isItemValid(ItemStack stack)
      { return !is_filled_box(stack); }

      @Override
      public void onSlotChanged()
      {
        inventory.markDirty();
        container.onSlotsChanged();
      }
    }

    private static class PlayerSlot extends Slot
    {
      private final MaterialBoxContainer container;

      public PlayerSlot(MaterialBoxContainer container, IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); this.container = container; }

      @Override
      public void onSlotChanged()
      {
        inventory.markDirty();
        container.onSlotsChanged();
      }
    }

    private static class ReadonlySlot extends Slot
    {
      public ReadonlySlot(IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); }

      @Override
      public boolean isItemValid(ItemStack stack)
      { return false; }

      public ItemStack decrStackSize(int amount)
      { return ItemStack.EMPTY; }

      public boolean canTakeStack(PlayerEntity player)
      { return false; }
    }

    private static class StorageInventory extends Inventory
    {
      public StorageInventory(int nslots)
      { super(nslots); }

      @Override
      public boolean isItemValidForSlot(int index, ItemStack stack)
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
      if((player_inventory.currentItem < 0) || (player_inventory.currentItem >= player_inventory.getSizeInventory())
        || (!(player_inventory.getStackInSlot(player_inventory.currentItem).getItem() instanceof MaterialBoxItem))
      ) {
        bag = new ItemStack(Items.AIR);
        return;
      }
      bag = player_inventory.getStackInSlot(player_inventory.currentItem);
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
        if(player_inventory.currentItem != slot) {
          addSlot(new PlayerSlot(this, player_inventory, slot, 28+x*18, 183)); // player slots: 0..8
        } else {
          addSlot(bag_slot_ = new ReadonlySlot(player_inventory, slot, 28+x*18, 183));
        }
      }
      for(int y=0; y<3; ++y) {
        for(int x=0; x<9; ++x) {
          int slot = x+y*9+9;
          if(player_inventory.currentItem != slot) {
            addSlot(new PlayerSlot(this, player_inventory, slot, 28+x*18, 125+y*18)); // player slots: 9..35
          } else {
            addSlot(bag_slot_ = new ReadonlySlot(player_inventory, slot, 28+x*18, 125+y*18));
          }
        }
      }
      read(bag.getTag());
    }

    @Override
    public boolean canInteractWith(PlayerEntity player)
    { return player == this.player; }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index)
    {
      Slot slot = getSlot(index);
      if((slot==null) || (!slot.getHasStack())) return ItemStack.EMPTY;
      ItemStack slot_stack = slot.getStack();
      ItemStack transferred = slot_stack.copy();
      if((index>=0) && (index<PLAYER_SLOT_BEGIN)) {
        // Crate slots
        if(!mergeItemStack(slot_stack, PLAYER_SLOT_BEGIN, PLAYER_SLOT_END, false)) return ItemStack.EMPTY;
      } else if((index >= PLAYER_SLOT_BEGIN) && (index <= PLAYER_SLOT_END)) {
        // Player slot
        if(!mergeItemStack(slot_stack, STORAGE_SLOT_BEGIN, STORAGE_SLOT_END, false)) return ItemStack.EMPTY;
      } else {
        // Invalid slot
        return ItemStack.EMPTY;
      }
      if(slot_stack.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }
      if(slot_stack.getCount() == transferred.getCount()) return ItemStack.EMPTY;
      slot.onTake(player, slot_stack);
      return transferred;
    }

    @Override
    public void onContainerClosed(PlayerEntity player)
    {
      super.onContainerClosed(player);
      if(player.world.isRemote()) return;
      CompoundNBT nbt = write(bag.getTag());
      if((nbt!=null) && (nbt.isEmpty())) nbt = null;
      bag.setTag(nbt);
    }

    // INetworkSynchronisableContainer ---------------------------------------------------------------------------------

    @OnlyIn(Dist.CLIENT)
    public void onGuiAction(String message, CompoundNBT nbt)
    {
      nbt.putString("action", message);
      Networking.PacketContainerSyncClientToServer.sendToServer(windowId, nbt);
    }

    @Override
    public void onClientPacketReceived(int windowId, PlayerEntity player, CompoundNBT nbt)
    {
      boolean changed = false;
      if(!nbt.contains("action")) return;
      final int slotId = nbt.contains("slot") ? nbt.getInt("slot") : -1;
      if((bag_slot_!=null) && (slotId == bag_slot_.slotNumber)) return;
      switch(nbt.getString("action")) {
        case QUICK_MOVE_ALL: {
          if((slotId >= STORAGE_SLOT_BEGIN) && (slotId < STORAGE_SLOT_END) && (getSlot(slotId).getHasStack())) {
            final Slot slot = getSlot(slotId);
            ItemStack remaining = slot.getStack();
            slot.putStack(ItemStack.EMPTY);
            final ItemStack ref_stack = remaining.copy();
            ref_stack.setCount(ref_stack.getMaxStackSize());
            for(int i=storage_slot_range.end_slot-storage_slot_range.start_slot; (i>0) && (!remaining.isEmpty()); --i) {
              remaining = player_slot_range.insert(remaining, false, 0, true, true);
              if(!remaining.isEmpty()) break;
              remaining = storage_slot_range.extract(ref_stack);
            }
            if(!remaining.isEmpty()) {
              slot.putStack(remaining); // put back
            }
          } else if((slotId >= PLAYER_SLOT_BEGIN) && (slotId < PLAYER_SLOT_END) && (getSlot(slotId).getHasStack())) {
            final Slot slot = getSlot(slotId);
            ItemStack remaining = slot.getStack();
            slot.putStack(ItemStack.EMPTY);
            final ItemStack ref_stack = remaining.copy();
            ref_stack.setCount(ref_stack.getMaxStackSize());
            for(int i=player_slot_range.end_slot-player_slot_range.start_slot; (i>0) && (!remaining.isEmpty()); --i) {
              remaining = storage_slot_range.insert(remaining, false, 0, false, true);
              if(!remaining.isEmpty()) break;
              remaining = player_slot_range.extract(ref_stack);
            }
            if(!remaining.isEmpty()) {
              slot.putStack(remaining); // put back
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
        inventory_.markDirty();
        player.inventory.markDirty();
        detectAndSendChanges();
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
      for(int i=0; i<inventory_.getSizeInventory(); ++i) stacks.add(inventory_.getStackInSlot(i));
      if(!stacks.stream().allMatch(ItemStack::isEmpty)) {
        nbt = Inventories.writeNbtStacks(nbt, "stacks", stacks, true);
      } else if((nbt!=null) && (nbt.contains("stacks"))) {
        nbt.remove("stacks");
      }
      return nbt;
    }

    public CompoundNBT read(CompoundNBT nbt)
    {
      NonNullList<ItemStack> stacks = Inventories.readNbtStacks(nbt, "stacks", inventory_.getSizeInventory());
      for(int i=0; i<inventory_.getSizeInventory(); ++i) inventory_.setInventorySlotContents(i, stacks.get(i));
      return nbt;
    }

    public void onSlotsChanged()
    {
      if(player.world.isRemote()) return;
      // Sync client
      CompoundNBT nbt = write(new CompoundNBT());
      bag.setTag(nbt);
      player_inventory.markDirty();
      CompoundNBT pkg_nbt = new CompoundNBT();
      pkg_nbt.put("bag", nbt);
      Networking.PacketContainerSyncServerToClient.sendToPlayer(player, windowId, pkg_nbt);
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

    public MaterialBoxGui(MaterialBoxContainer container, PlayerInventory player_inventory, ITextComponent title)
    {
      super(container, player_inventory, title);
      player = player_inventory.player;
      xSize = 213;
      ySize = 206;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
      renderBackground();
      super.render(mouseX, mouseY, partialTicks);
      renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
      RenderSystem.color4f(1f, 1f, 1f, 1f);
      getMinecraft().getTextureManager().bindTexture(BACKGROUND_IMAGE);
      final int x0=getGuiLeft(), y0=getGuiTop(), w=getXSize(), h=getYSize();
      blit(x0, y0, 0, 0, w, h);
      {
        final Slot slot = getContainer().bag_slot_;
        if(slot != null) blit(x0+slot.xPos, y0+slot.yPos, 240, 183, 16, 16);
      }
    }

    //------------------------------------------------------------------------------------------------------------------

    protected void action(String message)
    { action(message, new CompoundNBT()); }

    protected void action(String message, CompoundNBT nbt)
    { getContainer().onGuiAction(message, nbt); }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int button, ClickType type)
    {
      if(!with_gui_mouse_handling) {
        super.handleMouseClick(slot, slotId, button, type);
      } else if((type == ClickType.QUICK_MOVE) && (slot!=null) && slot.getHasStack() && Auxiliaries.isShiftDown() && Auxiliaries.isCtrlDown()) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("slot", slotId);
        action(MaterialBoxContainer.QUICK_MOVE_ALL, nbt);
      } else {
        super.handleMouseClick(slot, slotId, button, type);
      }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double wheel_inc)
    {
      if(!with_gui_mouse_handling) return super.mouseScrolled(mouseX, mouseY, wheel_inc);
      final Slot slot = getSlotUnderMouse();
      if(!slot.getHasStack()) return true;
      final int count = slot.getStack().getCount();
      int limit = (Auxiliaries.isShiftDown() ? 2 : 1) * (Auxiliaries.isCtrlDown() ? 4 : 1);
      if(wheel_inc > 0.1) {
        if(count > 0) {
          if((count < slot.getStack().getMaxStackSize()) && (count < slot.getSlotStackLimit())) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("slot", slot.slotNumber);
            if(limit > 1) nbt.putInt("limit", limit);
            action(MaterialBoxContainer.INCREASE_STACK, nbt);
          }
        }
      } else if(wheel_inc < -0.1) {
        if(count > 0) {
          CompoundNBT nbt = new CompoundNBT();
          nbt.putInt("slot", slot.slotNumber);
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
      lifespan = filled ? 18000 : 6000;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
      if(!filled) return super.attackEntityFrom(source, amount);
      if(world.isRemote || removed) return false;
      lifespan = Math.max(lifespan-200, 200);
      return false;
    }
  }
}
