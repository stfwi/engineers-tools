/*
 * @file ItemCrushingHammer.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Early game manual ore duping hammer. Not much use
 * for other stuff.
 */
package wile.engineerstools.items;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;
import wile.engineerstools.ModEngineersTools;

import java.util.ArrayList;
import java.util.List;

public class ItemCrushingHammer extends ItemTools
{
  private static String grit_preference_order = "immersiveengineering";   // --> grid mod preference, should be at least similarly configurable to IE.
  private static int hammer_wear_off = 2;       // --> mod config tweaks
  private static int num_output_stacks = 2;     // --> mod config tweaks

  public static void on_config(String grit_preference, int hammer_durability_loss, int output_count)
  {
    num_output_stacks = MathHelper.clamp(output_count, 2, 3);
    hammer_wear_off = MathHelper.clamp(hammer_durability_loss, (num_output_stacks>2) ? 3:1, 32);
    grit_preference_order = grit_preference.toLowerCase().replaceAll(";;", ";")
      .replaceFirst("^;+", "").replaceAll(";+$","").trim();
    if(grit_preference_order.isEmpty()) grit_preference_order = "immersiveengineering";
  }

  ItemCrushingHammer(String registryName)
  {
    super(registryName);
    setMaxStackSize(1);
    setMaxDamage(128);
    setNoRepair();
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Ore crushing recipe (inline, as explicitly bound to this item)
  //--------------------------------------------------------------------------------------------------------------------

  public static class CrushingHammerRecipe extends ShapelessOreRecipe
  {

    public CrushingHammerRecipe(String recipe_name, ItemStack grit_stack, String ore_name)
    {
      super(new ResourceLocation(ModEngineersTools.MODID, "hammer_crushing"), grit_stack, ore_name, new ItemStack(ModItems.CRUSHING_HAMMER, 1, OreDictionary.WILDCARD_VALUE));
      setRegistryName(new ResourceLocation(ModEngineersTools.MODID, recipe_name.toLowerCase().replace(" ", "")));
    }

    public CrushingHammerRecipe(String recipe_name, ItemStack grit_stack, Item ore_item)
    {
      super(new ResourceLocation(ModEngineersTools.MODID, "hammer_crushing"), grit_stack, ore_item, new ItemStack(ModItems.CRUSHING_HAMMER, 1, OreDictionary.WILDCARD_VALUE));
      setRegistryName(new ResourceLocation(ModEngineersTools.MODID, recipe_name.toLowerCase().replace(" ", "")));
    }

    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
    {
      NonNullList<ItemStack> remaining = net.minecraftforge.common.ForgeHooks.defaultRecipeGetRemainingItems(inv);
      final ItemStack reference_hammer = new ItemStack(ModItems.CRUSHING_HAMMER, 1);
      int hammer_position = -1;
      for(int i=0; i<inv.getSizeInventory(); ++i) {
        if(inv.getStackInSlot(i).isItemEqualIgnoreDurability(reference_hammer)) { hammer_position = i; break; }
      }
      if((hammer_position >= 0) && (hammer_position < remaining.size())) {
        ItemStack used_hammer = inv.getStackInSlot(hammer_position);
        int durability = used_hammer.getItemDamage() + hammer_wear_off;
        if(durability >= used_hammer.getMaxDamage()) return remaining;
        used_hammer.setItemDamage(durability);
        remaining.set(hammer_position, used_hammer.copy());
      }
      if(num_output_stacks > 1) {
        for(int i=0; i<remaining.size(); ++i) {
          if(remaining.get(i).isEmpty()) {
            ItemStack stack = getCraftingResult(inv);
            stack.setCount(num_output_stacks - 1);
            remaining.set(i, stack);
            break;
          }
        }
      }
      return remaining;
    }


    public static void registerAll(RegistryEvent.Register<IRecipe> event)
    {
      registerGritRecipes(event.getRegistry());
      registerGritFurnaceRecipes(event.getRegistry());
    }

    private static void registerGritRecipe(final IForgeRegistry<IRecipe> registry, final ItemStack grit_stack, String ore_name)
    {
      if(grit_stack.getMetadata()==OreDictionary.WILDCARD_VALUE) grit_stack.setItemDamage(0);
      grit_stack.setCount(1);
      if(ore_name.contains(":")) {
        Item ore_item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ore_name));
        if(ore_item == null) { ModEngineersTools.logger.error("Item for recipe registration not found: '" + ore_name + "'"); return; }
        ore_name = ore_name.replaceAll(":", "").toLowerCase();
        registry.register(new CrushingHammerRecipe(("crushinghammer_" + (ore_name.toLowerCase()) + "_to_"
          + grit_stack.getItem().getRegistryName().getNamespace() + grit_stack.getItem().getRegistryName().getPath()
          + (grit_stack.getHasSubtypes() ? ("_m" + ((grit_stack.getMetadata()!=OreDictionary.WILDCARD_VALUE) ? (grit_stack.getMetadata()) : (0) )) : (""))
          + "_n" + grit_stack.getCount()), grit_stack, ore_item));
      } else {
        registry.register(new CrushingHammerRecipe(("crushinghammer_" + (ore_name.toLowerCase()) + "_to_"
          + grit_stack.getItem().getRegistryName().getNamespace() + grit_stack.getItem().getRegistryName().getPath()
          + (grit_stack.getHasSubtypes() ? ("_m" + ((grit_stack.getMetadata()!=OreDictionary.WILDCARD_VALUE) ? (grit_stack.getMetadata()) : (0) )) : (""))
          + "_n" + grit_stack.getCount()), grit_stack, ore_name));
      }
    }

    public static void registerGritRecipes(final IForgeRegistry<IRecipe> registry)
    {
      boolean gold_grit_registered=false, iron_grit_registered=false;
      int num_crushing_hammer_recipes_registered = 0;
      final String[] grit_preference = grit_preference_order.toLowerCase().trim().split(";");
      for(int i=0; i<grit_preference.length; ++i) grit_preference[i] = grit_preference[i].trim();
      List<Tuple<String,String>> ingredient_result_pairs = new ArrayList<Tuple<String,String>>();
      for(final String ore_name:OreDictionary.getOreNames()) {
        if((!ore_name.startsWith("ore")) || (ore_name.length() <= 5)) continue;
        final String grit_name = "dust" + ore_name.substring(3);
        if(!OreDictionary.doesOreNameExist(ore_name) || OreDictionary.getOres(ore_name).isEmpty()) continue;
        if(!OreDictionary.doesOreNameExist(grit_name) || OreDictionary.getOres(grit_name).isEmpty()) continue;
        ItemStack preferred_stack = ItemStack.EMPTY;
        for(ItemStack ore : OreDictionary.getOres(grit_name)) {
          if(grit_preference.length == 0) {
            preferred_stack = ore;
          } else {
            for(int i = grit_preference.length-1; i >= 0; --i) {
              if(grit_preference[i].isEmpty()) continue;
              if(ore.getItem().getRegistryName().getNamespace().equals(grit_preference[i])) preferred_stack = ore;
            }
          }
        }
        if(ore_name == "oreIron") iron_grit_registered = true;
        if(ore_name == "oreGold") gold_grit_registered = true;
        if(preferred_stack.isEmpty()) continue;
        registerGritRecipe(registry, preferred_stack, ore_name);
        ++num_crushing_hammer_recipes_registered;
      }
      if(!iron_grit_registered) registerGritRecipe(registry, new ItemStack(ModItems.IRON_GRIT), "minecraft:iron_ore");
      if(!gold_grit_registered) registerGritRecipe(registry, new ItemStack(ModItems.GOLD_GRIT), "minecraft:gold_ore");
      ModEngineersTools.logger.info("Registered " + num_crushing_hammer_recipes_registered + " ore crashing hammer recipes, priority order: '" + grit_preference_order + "'");
    }

    public static void registerGritFurnaceRecipes(final IForgeRegistry<IRecipe> registry)
    {
      GameRegistry.addSmelting(ModItems.IRON_GRIT, new ItemStack(Items.IRON_INGOT), 0.8f);
      GameRegistry.addSmelting(ModItems.GOLD_GRIT, new ItemStack(Items.GOLD_INGOT), 1f);
    }
  }
}
