/*
 * @file ExtendedShapelessRecipe.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
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
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
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

  @Override
  public boolean isDynamic()
  { return false; }

  @Override
  public IRecipeSerializer<?> getSerializer()
  { return ExtendedShapelessRecipe.SERIALIZER; }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
  {
    String tool_name = aspects.getString("tool");
    int tool_damage = MathHelper.clamp(aspects.getInt("tool_damage"), 1, 128);
    NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    for(int i=0; i<remaining.size(); ++i) {
      final ItemStack stack = inv.getStackInSlot(i);
      if(stack.getItem().getRegistryName().toString().equals(tool_name)) {
        if(!stack.isDamageable()) {
          remaining.set(i, stack);
        } else {
          ItemStack rstack = stack.copy();
          rstack.setDamage(rstack.getDamage()+tool_damage);
          if(rstack.getDamage() < rstack.getMaxDamage()) {
            remaining.set(i, rstack);
          }
        }
      } else if(stack.hasContainerItem()) {
        remaining.set(i, stack.getContainerItem());
      }
    }
    return remaining;
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ExtendedShapelessRecipe>
  {
    private static int MAX_WIDTH = 3;
    private static int MAX_HEIGHT = 3;

    public Serializer()
    {}

    @Override
    public ExtendedShapelessRecipe read(ResourceLocation recipeId, JsonObject json)
    {
      ResourceLocation resultTag = null;
      String group = JSONUtils.getString(json, "group", "");
      NonNullList<Ingredient> list = NonNullList.create();
      JsonArray ingredients = JSONUtils.getJsonArray(json, "ingredients");
      for(int i = 0; i < ingredients.size(); ++i) {
        Ingredient ingredient = Ingredient.deserialize(ingredients.get(i));
        if (!ingredient.hasNoMatchingItems()) list.add(ingredient);
      }
      if (list.isEmpty()) throw new JsonParseException("No ingredients for "+this.getRegistryName().getPath()+" recipe");
      if (list.size() > MAX_WIDTH * MAX_HEIGHT) throw new JsonParseException("Too many ingredients for crafting_tool_shapeless recipe the max is " + (MAX_WIDTH * MAX_HEIGHT));
      // Tag based item picking
      final JsonObject res = JSONUtils.getJsonObject(json, "result");
      if(res.has("tag")) {
        ResourceLocation rl = new ResourceLocation(res.get("tag").getAsString());
        Tag<Item> tag = ItemTags.getCollection().getTagMap().getOrDefault(rl, null);
        if(tag==null) throw new JsonParseException(this.getRegistryName().getPath() + ": Result tag does not exist: #" + rl);
        if(tag.getAllElements().isEmpty()) throw new JsonParseException(this.getRegistryName().getPath() + ": Result tag has no items: #" + rl);
        if(res.has("item")) res.remove("item");
        resultTag = rl;
        List<Item> lst = Lists.newArrayList(tag.getAllElements());
        res.addProperty("item", lst.get(0).getRegistryName().toString());
      }
      ItemStack stack = ShapedRecipe.deserializeItem(res);
      //--------------------
      CompoundNBT nbt = new CompoundNBT();
      if(json.get("aspects")==null) return new ExtendedShapelessRecipe(recipeId, group, stack, list, nbt, resultTag);
      final JsonObject aspects = JSONUtils.getJsonObject(json, "aspects");
      if(aspects.size() > 0) {
        try {
          nbt = JsonToNBT.getTagFromJson( (((new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()).toJson(aspects))) );
        } catch(Exception ex) {
          throw new JsonParseException(this.getRegistryName().getPath() + ": Failed to parse the 'aspects' object:" + ex.getMessage());
        }
      }
      return new ExtendedShapelessRecipe(recipeId, group, stack, list, nbt, resultTag);
    }

    @Override
    public ExtendedShapelessRecipe read(ResourceLocation recipeId, PacketBuffer pkt)
    {
      String group = pkt.readString(0x7fff);
      final int size = pkt.readVarInt();
      NonNullList<Ingredient> list = NonNullList.withSize(size, Ingredient.EMPTY);
      for(int i=0; i<list.size(); ++i) list.set(i, Ingredient.read(pkt));
      ItemStack stack = pkt.readItemStack();
      CompoundNBT aspects = pkt.readCompoundTag();
      ResourceLocation resultTag = pkt.readResourceLocation();
      return new ExtendedShapelessRecipe(recipeId, group, stack, list, aspects, resultTag);
    }

    @Override
    public void write(PacketBuffer pkt, ExtendedShapelessRecipe recipe)
    {
      pkt.writeString(recipe.getGroup());
      pkt.writeVarInt(recipe.getIngredients().size());
      for(Ingredient ingredient : recipe.getIngredients()) ingredient.write(pkt);
      pkt.writeItemStack(recipe.getRecipeOutput());
      pkt.writeCompoundTag(recipe.getAspects());
      pkt.writeResourceLocation(recipe.resultTag);
    }
  }
}
