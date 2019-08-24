/*
 * @file ItemGrit.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Mod internal metal dusts, applied when no other mod
 * provides a similar dust. Explicitly not registered
 * to ore dict, only smelting. Other grits shall be preferred.
 */
package wile.engineerstools.items;

public class ItemGrit extends ItemTools
{
  public ItemGrit(String registryName)
  { super(registryName); setMaxStackSize(64); }
}
