/*
 * @file ChargedLapisSqueezerItem.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * XP storage consumable.
 */
package wile.engineerstools.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.world.World;
import wile.engineerstools.ModContent;
import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.libmc.detail.Auxiliaries;
import wile.engineerstools.libmc.detail.Inventories;
import wile.engineerstools.libmc.detail.Overlay;

public class ChargedLapisSqueezerItem extends EtItem
{
  public ChargedLapisSqueezerItem(Item.Properties properties)
  { super(properties.stacksTo(64)); }

  @Override
  public boolean isFoil(ItemStack stack)
  { return false; }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment)
  { return false; }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book)
  { return false; }

  @Override
  public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
  {
    ItemStack stack = player.getItemInHand(hand);
    if(world.isClientSide()) return ActionResult.success(stack);
    onUseTick(world, player, stack, 1);
    return ActionResult.success(stack);
  }

  @Override
  public void onUseTick(World world, LivingEntity entity, ItemStack squeezer, int count)
  {
    if(!(entity instanceof PlayerEntity) || (world.isClientSide())) return;
    final PlayerEntity player = (PlayerEntity)entity;
    if(player.experienceLevel < 1) {
      Overlay.show(player, Auxiliaries.localizable("item."+ModEngineersTools.MODID+".charged_lapis_squeezer.msg.noxp"));
      world.playSound(null, player.blockPosition(), SoundEvents.WOOL_BREAK, SoundCategory.PLAYERS, 1f, 0.6f);
      return;
    }
    if(player.getHealth() <= (player.getMaxHealth()/10)) {
      Overlay.show(player, Auxiliaries.localizable("item."+ModEngineersTools.MODID+".charged_lapis_squeezer.msg.lowhealth"));
      world.playSound(null, player.blockPosition(), SoundEvents.WOOL_BREAK, SoundCategory.PLAYERS, 1f, 0.6f);
      return;
    }
    final ItemStack lapis = Inventories.extract(Inventories.itemhandler(player), new ItemStack(Items.LAPIS_LAZULI), 1, false);
    if(lapis.isEmpty()) {
      Overlay.show(player, Auxiliaries.localizable("item."+ModEngineersTools.MODID+".charged_lapis_squeezer.msg.nolapis"));
      world.playSound(null, player.blockPosition(), SoundEvents.WOOL_BREAK, SoundCategory.PLAYERS, 1f, 0.6f);
      return;
    }
    world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT, SoundCategory.PLAYERS, 0.2f, 1.4f);
    world.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.5f, 1.4f);
    Inventories.give(player, new ItemStack(ModContent.CHARGED_LAPIS));
    player.giveExperienceLevels(-1);
    player.causeFoodExhaustion(4f);
    player.setHealth(player.getHealth()-(player.getMaxHealth()/10));
    player.addEffect(new EffectInstance(Effects.BLINDNESS, 20, 0));
  }

}
