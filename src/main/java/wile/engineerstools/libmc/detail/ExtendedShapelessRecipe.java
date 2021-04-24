/*
 * @file ExtendedShapelessRecipe.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.engineerstools.libmc.detail;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import com.google.common.collect.Lists;
import com.google.gson.*;
import java.util.List;

public class ExtendedShapelessRecipe extends ShapelessRecipe implements ICraftingRecipe
{
  public interface IRepairableToolItem
  {
    ItemStack onShapelessRecipeRepaired(ItemStack toolStack, int previousDamage, int repairedDamage);
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static final ExtendedShapelessRecipe.Serializer SERIALIZER = ((ExtendedShapelessRecipe.Serializer)(
    (new ExtendedShapelessRecipe.Serializer()).setRegistryName(Auxiliaries.modid(), "crafting_extended_shapeless")
  ));

  //--------------------------------------------------------------------------------------------------------------------

  private final CompoundNBT aspects;
  private final ResourceLocation resultTag;

  public ExtendedShapelessRecipe(ResourceLocation id, String group, ItemStack output, NonNullList<Ingredient> ingredients, CompoundNBT aspects, ResourceLocation resultTag)
  { super(id, group, output, ingredients); this.aspects=aspects; this.resultTag = resultTag; }

  public CompoundNBT getAspects()
  { return aspects.copy(); }

  private int getToolDamage()
  {
    if(aspects.contains("tool_repair")) return (-MathHelper.clamp(aspects.getInt("tool_repair"), 0, 4096));
    if(aspects.contains("tool_damage")) return (MathHelper.clamp(aspects.getInt("tool_damage"), 1, 1024));
    return 0;
  }

  private boolean isRepair()
  { return getToolDamage() < 0; }

  @Override
  public boolean isSpecial()
  { return isRepair() || aspects.getBoolean("dynamic"); }

  @Override
  public IRecipeSerializer<?> getSerializer()
  { return ExtendedShapelessRecipe.SERIALIZER; }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
  {
    final String tool_name = aspects.getString("tool");
    final int tool_damage = getToolDamage();
    NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    for(int i=0; i<remaining.size(); ++i) {
      final ItemStack stack = inv.getItem(i);
      if(stack.getItem().getRegistryName().toString().equals(tool_name)) {
        if(!stack.isDamageableItem()) {
          remaining.set(i, stack);
        } else if(!isRepair()) {
          ItemStack rstack = stack.copy();
          rstack.setDamageValue(rstack.getDamageValue()+tool_damage);
          if(rstack.getDamageValue() < rstack.getMaxDamage()) {
            remaining.set(i, rstack);
          }
        }
      } else if(stack.hasContainerItem()) {
        remaining.set(i, stack.getContainerItem());
      }
    }
    return remaining;
  }

  @Override
  public ItemStack assemble(CraftingInventory inv)
  {
    if(!isRepair()) return super.assemble(inv);
    // Tool repair
    final String tool_name = aspects.getString("tool");
    for(int i=0; i<inv.getContainerSize(); ++i) {
      final ItemStack stack = inv.getItem(i);
      if(!stack.getItem().getRegistryName().toString().equals(tool_name)) continue;
      ItemStack rstack = stack.copy();
      final int dmg = rstack.getDamageValue();
      final int repair_negative_dmg = Math.min(-1, getToolDamage() * rstack.getMaxDamage() / 100);
      rstack.setDamageValue(Math.max(dmg+repair_negative_dmg, 0));
      if((rstack.getItem() instanceof IRepairableToolItem)) {
        rstack = ((IRepairableToolItem)(rstack.getItem())).onShapelessRecipeRepaired(rstack, dmg, rstack.getDamageValue());
      }
      return rstack;
    }
    return ItemStack.EMPTY; // in doubt prevent duping, people will complain soon enough.
  }

  @Override
  public ItemStack getResultItem()
  { return isSpecial() ? ItemStack.EMPTY : super.getResultItem(); }

  //--------------------------------------------------------------------------------------------------------------------

  public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ExtendedShapelessRecipe>
  {
    private static int MAX_WIDTH = 3;
    private static int MAX_HEIGHT = 3;

    public Serializer()
    {}

    @Override
    public ExtendedShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json)
    {
      ResourceLocation resultTag = new ResourceLocation("libmc", "none"); // just no null
      String group = JSONUtils.getAsString(json, "group", "");
      // Recipe ingredients
      NonNullList<Ingredient> list = NonNullList.create();
      JsonArray ingredients = JSONUtils.getAsJsonArray(json, "ingredients");
      for(int i = 0; i < ingredients.size(); ++i) {
        Ingredient ingredient = Ingredient.fromJson(ingredients.get(i));
        if (!ingredient.isEmpty()) list.add(ingredient);
      }
      if(list.isEmpty()) throw new JsonParseException("No ingredients for "+this.getRegistryName().getPath()+" recipe");
      if(list.size() > MAX_WIDTH * MAX_HEIGHT) throw new JsonParseException("Too many ingredients for crafting_tool_shapeless recipe the max is " + (MAX_WIDTH * MAX_HEIGHT));
      // Extended recipe aspects
      CompoundNBT aspects_nbt = new CompoundNBT();
      if(json.get("aspects")!=null) {
        final JsonObject aspects = JSONUtils.getAsJsonObject(json, "aspects");
        if(aspects.size() > 0) {
          try {
            aspects_nbt = JsonToNBT.parseTag( (((new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()).toJson(aspects))) );
          } catch(Exception ex) {
            throw new JsonParseException(this.getRegistryName().getPath() + ": Failed to parse the 'aspects' object:" + ex.getMessage());
          }
        }
      }
      // Recipe result
      final JsonObject res = JSONUtils.getAsJsonObject(json, "result");
      if(res.has("tag")) {
        // Tag based item picking
        ResourceLocation rl = new ResourceLocation(res.get("tag").getAsString());
        ITag<Item> tag = TagCollectionManager.getInstance().getItems().getAllTags().getOrDefault(rl, null);
        if(tag==null) throw new JsonParseException(this.getRegistryName().getPath() + ": Result tag does not exist: #" + rl);
        if(tag.getValues().isEmpty()) throw new JsonParseException(this.getRegistryName().getPath() + ": Result tag has no items: #" + rl);
        if(res.has("item")) res.remove("item");
        resultTag = rl;
        List<Item> lst = Lists.newArrayList(tag.getValues());
        res.addProperty("item", lst.get(0).getRegistryName().toString());
      }
      ItemStack result_stack = ShapedRecipe.itemFromJson(res);
      return new ExtendedShapelessRecipe(recipeId, group, result_stack, list, aspects_nbt, resultTag);
    }

    @Override
    public ExtendedShapelessRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer pkt)
    {
      String group = pkt.readUtf(0x7fff);
      final int size = pkt.readVarInt();
      NonNullList<Ingredient> list = NonNullList.withSize(size, Ingredient.EMPTY);
      for(int i=0; i<list.size(); ++i) list.set(i, Ingredient.fromNetwork(pkt));
      ItemStack stack = pkt.readItem();
      CompoundNBT aspects = pkt.readNbt();
      ResourceLocation resultTag = pkt.readResourceLocation();
      return new ExtendedShapelessRecipe(recipeId, group, stack, list, aspects, resultTag);
    }

    @Override
    public void toNetwork(PacketBuffer pkt, ExtendedShapelessRecipe recipe)
    {
      pkt.writeUtf(recipe.getGroup());
      pkt.writeVarInt(recipe.getIngredients().size());
      for(Ingredient ingredient : recipe.getIngredients()) ingredient.toNetwork(pkt);
      pkt.writeItem(recipe.getResultItem());
      pkt.writeNbt(recipe.getAspects());
      pkt.writeResourceLocation(recipe.resultTag);
    }
  }
}
