/*
 * @file ModResources.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Common extended functionality dealing with resource
 * files and corresponding settings/usage options.
 */
package wile.engineerstools.detail;

import wile.engineerstools.ModEngineersTools;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;

public class ModResources
{
  public static final SoundEvent STIMPACK_INJECT_SOUND = SoundRegistry.createSoundEvent("stimpack_inject_sound");

  /**
   * Registry event handling for the sounds listed above.
   */
  @Mod.EventBusSubscriber(modid=ModEngineersTools.MODID)
  public static final class SoundRegistry
  {
    private static LinkedList<SoundEvent> created_sounds_ = new LinkedList<>();

    public static SoundEvent createSoundEvent(String name)
    {
      final ResourceLocation rl = new ResourceLocation(ModEngineersTools.MODID, name);
      SoundEvent se = new SoundEvent(rl).setRegistryName(rl);
      created_sounds_.push(se);
      return se;
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onRegistryEvent(RegistryEvent.Register<SoundEvent> event)
    {
      for(SoundEvent se:created_sounds_) {event.getRegistry().register(se);}
      created_sounds_.clear();
    }
  }
}
