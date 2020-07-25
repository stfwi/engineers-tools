/*
 * @file ItemMusliBar.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Musli Power Bar food.
 */
package wile.engineerstools.items;

import wile.engineerstools.libmc.detail.Auxiliaries;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;


public class MusliBarItem extends EtItem
{
  private static int healing_ = 6;
  private static float saturation_ = 1.2f;
  private static Food musli_bar_food = (new Food.Builder()).hunger(healing_).saturation(saturation_).fastToEat().setAlwaysEdible().build();

  public static void on_config(int hunger, double saturation)
  {
    healing_ = MathHelper.clamp(hunger, 1, 8);
    saturation_ = (float)MathHelper.clamp(saturation, 0.2, 2);
    musli_bar_food = (new Food.Builder()).hunger(healing_).saturation(saturation_).fastToEat().setAlwaysEdible().build();
    Auxiliaries.logInfo("Musli Bar: hunger:" + healing_ + ", saturation:" + saturation_);
  }

  public static int healing()
  { return healing_; }

  public static float saturation()
  { return saturation_; }

  public MusliBarItem(Item.Properties properties)
  { super(properties.maxStackSize(64).food(musli_bar_food)); }

  @Override
  public Food getFood()
  { return musli_bar_food; }
}
