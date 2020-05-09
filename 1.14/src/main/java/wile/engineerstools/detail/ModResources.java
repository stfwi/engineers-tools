/*
 * @file ModResources.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
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
import java.util.LinkedList;


public class ModResources
{
  private static LinkedList<SoundEvent> created_sounds_ = new LinkedList<>();

  private static final SoundEvent createSoundEvent(String name)
  {
    final ResourceLocation rl = new ResourceLocation(ModEngineersTools.MODID, name);
    SoundEvent se = new SoundEvent(rl).setRegistryName(rl);
    created_sounds_.push(se);
    return se;
  }

  public static final void registerSoundEvents(RegistryEvent.Register<SoundEvent> event)
  { for(SoundEvent se:created_sounds_) {event.getRegistry().register(se);} }

  public static final SoundEvent STIMPACK_INJECT_SOUND = createSoundEvent("stimpack_inject_sound");

}
