/*
 * @file ItemMusliBarPress.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Musli Power Bar food.
 */
package wile.engineerstools.items;

import com.mojang.blaze3d.matrix.MatrixStack;
import wile.engineerstools.ModContent;
import wile.engineerstools.libmc.detail.Auxiliaries;
import wile.engineerstools.libmc.detail.Networking;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import com.mojang.blaze3d.systems.RenderSystem;


public class MusliBarPressItem extends EtItem
{
  private static int max_seed_storage = 512;
  private static int max_food_storage = 128 * MusliBarItem.healing();
  private static int seeds_per_bar = 1;
  private static int food_per_bar = MusliBarItem.healing();

  public static void on_config(int seed_storage, int food_storage, int seeds_needed_per_bar, int food_value_needed_per_bar)
  {
    final int healing = Math.max(1, MusliBarItem.healing());
    max_seed_storage = MathHelper.clamp(seed_storage, 64, 1024);
    max_food_storage = MathHelper.clamp(food_storage, 64, 1024) * healing;
    seeds_per_bar = MathHelper.clamp(seeds_needed_per_bar, 1, 32);
    food_per_bar =  MathHelper.clamp(food_value_needed_per_bar, healing, 128);
    Auxiliaries.logInfo("Musli Press: storage:"+max_food_storage+"hunger/"+max_seed_storage+"seeds, bar:" + food_per_bar + "hunger/"+seeds_per_bar+"seeds");
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Block
  //--------------------------------------------------------------------------------------------------------------------

  public MusliBarPressItem(Item.Properties properties)
  { super(properties.stacksTo(1)); }

  @Override
  public ActionResultType useOn(ItemUseContext context)
  { return ActionResultType.PASS; }

  @Override
  public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
  {
    ItemStack stack = player.getItemInHand(hand);
    if(!world.isClientSide()) {
      NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
        @Override
        public ITextComponent getDisplayName()
        { return new TranslationTextComponent(getRegistryName().toString()); }

        @Override
        public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player)
        { return new MusliBarPressContainer(id, inventory, player); }
      });
    }
    return new ActionResult<>(ActionResultType.SUCCESS, stack);
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Container
  //--------------------------------------------------------------------------------------------------------------------

  private static boolean isValidFood(ItemStack stack)
  {
    if((!stack.getItem().isEdible()) || (stack.getItem().getFoodProperties()==null)) return false;
    if(stack.getItem() instanceof MusliBarItem) return false;
    if(stack.getItem().getTags().contains(new ResourceLocation(Auxiliaries.modid(), "musli_bar_food_blacklisted"))) return false;
    return true;
  }

  private static boolean isValidSeeds(ItemStack stack)
  {
    if(stack.getItem() == Items.WHEAT_SEEDS) return true; // fast branch and default item.
    if(stack.getItem().getTags().contains(new ResourceLocation(Auxiliaries.modid(), "musli_bar_seeds"))) return true;
    return false;
  }

  public static class MusliBarPressContainer extends Container implements Networking.INetworkSynchronisableContainer
  {
    private static final int INPUT_SLOTNO = 0;
    private static final int OUTPUT_SLOTNO = 1;
    private static final int PLAYER_INV_START_SLOTNO = 2;

    //------------------------------------------------------------------------------------------------------------------

    public static class InputSlot extends Slot
    {
      private final MusliBarPressContainer uicontainer;

      public InputSlot(MusliBarPressContainer uicontainer, IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); this.uicontainer = uicontainer; }

      @Override
      public boolean mayPlace(ItemStack stack)
      { return isValidFood(stack) || isValidSeeds(stack); }

      @Override
      public void setChanged()
      {
        container.setChanged();
        uicontainer.onSlotsChanged();
      }
    }

    public static class OutputSlot extends Slot
    {
      private final MusliBarPressContainer uicontainer;

      public OutputSlot(MusliBarPressContainer uicontainer, IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); this.uicontainer = uicontainer; }

      @Override
      public boolean mayPlace(ItemStack stack)
      { return false; }

      @Override
      public void setChanged()
      {
        container.setChanged();
        uicontainer.onSlotsChanged();
      }
    }

    public static class ReadonlySlot extends Slot
    {
      public ReadonlySlot(IInventory inventory, int index, int xpos, int ypos)
      { super(inventory, index, xpos, ypos); }

      @Override
      public boolean mayPlace(ItemStack stack)
      { return false; }

      public ItemStack remove(int amount)
      { return ItemStack.EMPTY; }

      public boolean mayPickup(PlayerEntity playerIn)
      { return false; }
    }

    //------------------------------------------------------------------------------------------------------------------

    private PlayerEntity player;
    private PlayerInventory player_inventory;
    private Inventory inventory_ = new Inventory(2);
    private final ItemStack muslipress;
    private ReadonlySlot muslipress_slot_ = null;
    private int seeds_stored = 0;
    private int food_stored = 0;

    // Container -------------------------------------------------------------------------------------------------------

    public MusliBarPressContainer(int cid, PlayerInventory player_inventory)
    { this(cid, player_inventory, player_inventory.player); }

    public MusliBarPressContainer(int cid, PlayerInventory player_inventory, PlayerEntity player)
    {
      super(ModContent.CT_MUSLI_BAR_PRESS, cid);
      this.player_inventory = player_inventory;
      this.player = player;
      if((player_inventory.selected < 0) || (player_inventory.selected >= player_inventory.getContainerSize())
        || (!(player_inventory.getItem(player_inventory.selected).getItem() instanceof MusliBarPressItem))
      ) {
        muslipress = new ItemStack(Items.AIR);
        return;
      }
      muslipress = player_inventory.getItem(player_inventory.selected);
      int i=-1;
      addSlot(new InputSlot(this, inventory_, ++i, 31, 45));
      addSlot(new OutputSlot(this, inventory_, ++i, 92, 45));
      // player slots
      for(int x=0; x<9; ++x) {
        int slot = x;
        if(player_inventory.selected != slot) {
          addSlot(new Slot(player_inventory, slot, 8+x*18, 144)); // player slots: 0..8
        } else {
          addSlot(muslipress_slot_ = new ReadonlySlot(player_inventory, slot, 8+x*18, 144));
        }
      }
      for(int y=0; y<3; ++y) {
        for(int x=0; x<9; ++x) {
          int slot = x+y*9+9;
          if(player_inventory.selected != slot) {
            addSlot(new Slot(player_inventory, slot, 8+x*18, 86+y*18)); // player slots: 9..35
          } else {
            addSlot(muslipress_slot_ = new ReadonlySlot(player_inventory, slot, 8+x*18, 86+y*18)); // player slots: 9..35
          }
        }
      }
      read(muslipress.getTag());
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
      if(index==INPUT_SLOTNO) {
        if(!moveItemStackTo(slot_stack, PLAYER_INV_START_SLOTNO, PLAYER_INV_START_SLOTNO+36, true)) return ItemStack.EMPTY;
      } else if(index==OUTPUT_SLOTNO) {
        if(!moveItemStackTo(slot_stack, PLAYER_INV_START_SLOTNO, PLAYER_INV_START_SLOTNO+36, true)) return ItemStack.EMPTY;
      } else if((index >= PLAYER_INV_START_SLOTNO) && (index < PLAYER_INV_START_SLOTNO+36)) {
        if((!isValidFood(slot_stack)) && (!isValidSeeds(slot_stack))) return ItemStack.EMPTY;
        if(!moveItemStackTo(slot_stack, INPUT_SLOTNO, INPUT_SLOTNO+1, true)) return ItemStack.EMPTY;
      } else {
        return ItemStack.EMPTY; // invalid slot
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
      muslipress.setTag(write(muslipress.getTag()));
    }

    // INetworkSynchronisableContainer ---------------------------------------------------------------------------------

    @Override
    public void onClientPacketReceived(int windowId, PlayerEntity player, CompoundNBT pkg_nbt)
    {}

    @Override
    public void onServerPacketReceived(int windowId, CompoundNBT pkg_nbt)
    {
      if(!(muslipress.getItem() instanceof MusliBarPressItem)) return;
      if(!pkg_nbt.contains("muslipress")) return;
      CompoundNBT nbt = pkg_nbt.getCompound("muslipress");
      read(nbt);
    }

    // Specific --------------------------------------------------------------------------------------------------------

    public CompoundNBT write(CompoundNBT nbt)
    {
      if(nbt==null) nbt = new CompoundNBT();
      nbt.putInt("seeds_stored", seeds_stored);
      nbt.putInt("food_stored", food_stored);
      nbt.put("output_slot", inventory_.getItem(OUTPUT_SLOTNO).save(new CompoundNBT()));
      nbt.put("input_slot", inventory_.getItem(INPUT_SLOTNO).save(new CompoundNBT()));
      return nbt;
    }

    public CompoundNBT read(CompoundNBT nbt)
    {
      if(nbt==null) return nbt;
      if(nbt.contains("seeds_stored")) seeds_stored = nbt.getInt("seeds_stored");
      if(nbt.contains("food_stored")) food_stored = nbt.getInt("food_stored");
      if(nbt.contains("output_slot")) inventory_.setItem(OUTPUT_SLOTNO, ItemStack.of(nbt.getCompound("output_slot")));
      if(nbt.contains("input_slot")) inventory_.setItem(INPUT_SLOTNO, ItemStack.of(nbt.getCompound("input_slot")));
      return nbt;
    }

    public void onSlotsChanged()
    {
      if(player.level.isClientSide()) return;
      ItemStack stack = inventory_.getItem(INPUT_SLOTNO);
      if(isValidSeeds(stack)) {
        seeds_stored += stack.getCount();
        if(seeds_stored > max_seed_storage) {
          stack.setCount(seeds_stored-max_seed_storage);
          seeds_stored = max_seed_storage;
        } else {
          stack = ItemStack.EMPTY;
        }
      } else if(isValidFood(stack)) {
        final int hunger = stack.getItem().getFoodProperties().getNutrition();
        if(hunger > 0) {
          food_stored += stack.getCount() * hunger;
          if(food_stored > max_food_storage) {
            stack.setCount((food_stored-max_food_storage)/hunger);
            food_stored = max_food_storage;
          } else {
            stack = ItemStack.EMPTY;
          }
        }
      }
      inventory_.setItem(INPUT_SLOTNO, stack);
      while(inventory_.getItem(OUTPUT_SLOTNO).getCount() < 64) {
        if((food_stored < food_per_bar) || (seeds_stored < seeds_per_bar)) break;
        food_stored -= food_per_bar;
        seeds_stored -= seeds_per_bar;
        if(inventory_.getItem(OUTPUT_SLOTNO).isEmpty()) {
          inventory_.setItem(OUTPUT_SLOTNO, new ItemStack(ModContent.MUSLI_BAR, 1));
        } else {
          inventory_.getItem(OUTPUT_SLOTNO).grow(1);
        }
      }
      // Sync client
      CompoundNBT nbt = write(new CompoundNBT());
      muslipress.setTag(nbt);
      player_inventory.setChanged();
      CompoundNBT pkg_nbt = new CompoundNBT();
      pkg_nbt.put("muslipress", nbt);
      Networking.PacketContainerSyncServerToClient.sendToPlayer(player, containerId, pkg_nbt);
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // GUI
  //--------------------------------------------------------------------------------------------------------------------

  @OnlyIn(Dist.CLIENT)
  public static class MusliBarPressGui extends ContainerScreen<MusliBarPressContainer>
  {
    private static final ResourceLocation BACKGROUND_IMAGE = new ResourceLocation(Auxiliaries.modid(), "textures/gui/musli_bar_press_gui.png");

    public MusliBarPressGui(MusliBarPressContainer uicontainer, PlayerInventory player_inventory, ITextComponent title)
    { super(uicontainer, player_inventory, title); }

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
      RenderSystem.color4f(1f, 1f, 1f, 1f);
      getMinecraft().getTextureManager().bind(BACKGROUND_IMAGE);
      final int x0=getGuiLeft(), y0=getGuiTop(), w=getXSize(), h=getYSize();
      blit(mx,x0, y0, 0, 0, w, h);
      blit(mx,x0+48, y0+45, 180, 45, seeds_px(43), 7);
      blit(mx,x0+48, y0+54, 180, 54, food_px(43), 7);
      {
        final Slot slot = getMenu().muslipress_slot_;
        if(slot != null) blit(mx,x0+slot.x, y0+slot.y, 181, 144, 16, 16);
      }
    }

    @Override
    protected void renderLabels(MatrixStack mx, int x, int y)
    {}

    private int seeds_px(int pixels)
    {
      final int v = MathHelper.clamp(getMenu().seeds_stored, 0, max_seed_storage);
      if((v <= 0) || (max_seed_storage <= 0)) return 0;
      return Math.max(1,(int)(Math.sqrt(v) / Math.sqrt(max_seed_storage) * pixels)); // -> ln() curve was too steep, so sqrt().
    }

    private int food_px(int pixels)
    {
      final int v = MathHelper.clamp(getMenu().food_stored, 0, max_food_storage);
      if((v <=0 ) || (max_food_storage <= 0)) return 0;
      return Math.max(1, (int)(Math.sqrt(v) / Math.sqrt(max_food_storage) * pixels));
    }

  }
}
