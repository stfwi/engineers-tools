/*
 * @file ClientProxy.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Client side only initialisation.
 */
package wile.engineerstools.detail;

import net.minecraft.world.World;
import wile.engineerstools.ModEngineersTools;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy implements ModEngineersTools.IProxy
{
  @Override
  public void preInit(FMLPreInitializationEvent e)
  { OBJLoader.INSTANCE.addDomain(ModEngineersTools.MODID); }

  @Override
  public World getWorldClientSide()
  { return Minecraft.getMinecraft().world; }
}
