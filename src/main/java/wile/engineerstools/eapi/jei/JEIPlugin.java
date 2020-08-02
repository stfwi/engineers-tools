/*
 * @file JEIPlugin.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * JEI plugin (see https://github.com/mezz/JustEnoughItems/wiki/Creating-Plugins)
 */
package wile.engineerstools.eapi.jei;
// public class JEIPlugin {}

import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.ModConfig;
import wile.engineerstools.ModContent;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements mezz.jei.api.IModPlugin
{
 @Override
 public ResourceLocation getPluginUid()
 { return new ResourceLocation(ModEngineersTools.MODID, "jei_plugin_uid"); }

 @Override
 public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
 {}

 @Override
 public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
 {
   HashSet<Item> blacklisted = new HashSet<>();
   for(Block e: ModContent.getRegisteredBlocks()) {
     if(ModConfig.isOptedOut(e) && (e.asItem().getRegistryName().getPath()).equals((e.getRegistryName().getPath()))) {
       blacklisted.add(e.asItem());
     }
   }
   for(Item e: ModContent.getRegisteredItems()) {
     if(ModConfig.isOptedOut(e) && (!(e instanceof BlockItem))) {
       blacklisted.add(e);
     }
   }
   if(!blacklisted.isEmpty()) {
     List<ItemStack> blacklist = blacklisted.stream().map(ItemStack::new).collect(Collectors.toList());
     try {
       jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, blacklist);
     } catch(Exception e) {
       ModEngineersTools.logger().warn("Exception in JEI opt-out processing: '" + e.getMessage() + "', skipping further JEI optout processing.");
     }
   }
 }
}
