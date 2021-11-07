/*
 * @file Overlay.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Renders status messages in one line.
 */
package wile.engineerstools.libmc.detail;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

public class Overlay
{
  public static void register()
  {
    if(SidedProxy.mc() != null) {
      MinecraftForge.EVENT_BUS.register(new TextOverlayGui());
      Networking.OverlayTextMessage.setHandler(TextOverlayGui::show);
    }
  }

  public static void show(PlayerEntity player, final ITextComponent message)
  { Networking.OverlayTextMessage.sendToPlayer(player, message, 3000); }

  public static void show(PlayerEntity player, final ITextComponent message, int delay)
  { Networking.OverlayTextMessage.sendToPlayer(player, message, delay); }

  // -----------------------------------------------------------------------------
  // Client side handler
  // -----------------------------------------------------------------------------

  @Mod.EventBusSubscriber(Dist.CLIENT)
  @OnlyIn(Dist.CLIENT)
  public static class TextOverlayGui extends AbstractGui
  {
    private static double overlay_y_ = 0.75;
    private static int text_color_ = 0x00ffaa00;
    private static int border_color_ = 0xaa333333;
    private static int background_color1_ = 0xaa333333;
    private static int background_color2_ = 0xaa444444;
    private final Minecraft mc;
    private static long deadline_;
    private static String text_;

    public static void on_config(double overlay_y)
    {
      overlay_y_ = overlay_y;
      // currently const, just to circumvent "useless variable" warnings
      text_color_ = 0x00ffaa00;
      border_color_ = 0xaa333333;
      background_color1_ = 0xaa333333;
      background_color2_ = 0xaa444444;
    }

    public static synchronized String text()
    { return text_; }

    public static synchronized long deadline()
    { return deadline_; }

    public static synchronized void hide()
    { deadline_ = 0; text_ = ""; }

    public static synchronized void show(ITextComponent s, int displayTimeoutMs)
    { text_ = (s==null)?(""):(s.getString()); deadline_ = System.currentTimeMillis() + displayTimeoutMs; }

    public static synchronized void show(String s, int displayTimeoutMs)
    { text_ = (s == null) ? ("") : (s); deadline_ = System.currentTimeMillis() + displayTimeoutMs; }

    TextOverlayGui()
    { super(); mc = SidedProxy.mc(); }

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event)
    {
      if(event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
      if(deadline() < System.currentTimeMillis()) return;
      String txt = text();
      MatrixStack mxs = event.getMatrixStack();
      if(txt.isEmpty()) return;
      final MainWindow win = mc.getWindow();
      final FontRenderer fr = mc.font;
      final boolean was_unicode = fr.isBidirectional();
      try {
        final int cx = win.getGuiScaledWidth() / 2;
        final int cy = (int)(win.getGuiScaledHeight() * overlay_y_);
        final int w = fr.width(txt);
        final int h = fr.lineHeight;
        fillGradient(mxs,cx-(w/2)-3, cy-2, cx+(w/2)+2, cy+h+2, 0xaa333333, 0xaa444444);
        hLine(mxs,cx-(w/2)-3, cx+(w/2)+2, cy-2, 0xaa333333);
        hLine(mxs,cx-(w/2)-3, cx+(w/2)+2, cy+h+2, 0xaa333333);
        vLine(mxs,cx-(w/2)-3, cy-2, cy+h+2, 0xaa333333);
        vLine(mxs,cx+(w/2)+2, cy-2, cy+h+2, 0xaa333333);
        drawCenteredString(mxs, fr, txt, cx , cy+1, 0x00ffaa00); // drawCenteredString
      } finally {
        ; // fr.setBidiFlag(was_unicode);
      }
    }

  }

}
