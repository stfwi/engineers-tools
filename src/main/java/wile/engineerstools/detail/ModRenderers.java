/*
 * @file ModRenderers.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.engineerstools.detail;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.matrix.MatrixStack;
import wile.engineerstools.ModEngineersTools;
import wile.engineerstools.items.TrackerItem;
import java.util.Optional;


public class ModRenderers
{
  @OnlyIn(Dist.CLIENT)
  public static class TrackerIster extends ItemStackTileEntityRenderer
  {
    @Override
    public void func_239207_a_/*render*/(ItemStack stack, ItemCameraTransforms.TransformType ctt, MatrixStack mx, IRenderTypeBuffer buf, int combinedLight, int combinedOverlay)
    {
      if(ctt == TransformType.FIRST_PERSON_LEFT_HAND || ctt == TransformType.FIRST_PERSON_RIGHT_HAND) return;
      Optional<Tuple<Integer,Integer>> rotations = TrackerItem.getUiAngles(stack);
      mx.push();
      final ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
      IVertexBuilder vb = ItemRenderer.getBuffer(buf, RenderType.getCutout(), true, false);
      {
        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(new ResourceLocation(ModEngineersTools.MODID, "tracker_model"), "inventory"));
        ir.renderModel(model, stack, combinedLight, combinedOverlay, mx, vb);
      }
      {
        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(new ResourceLocation(ModEngineersTools.MODID, "tracker_pointer_model"), "inventory"));
        mx.translate(0.5,0.5,0.5);
        if(!rotations.isPresent()) {
          mx.rotate(Vector3f.YP.rotationDegrees(180));
        } else {
          mx.rotate(Vector3f.YP.rotationDegrees(rotations.get().getB()));
          mx.rotate(Vector3f.XP.rotationDegrees(rotations.get().getA()));
        }
        mx.scale(0.6f, 0.6f, 0.6f);
        mx.translate(-0.5,-0.5,-0.5);
        ir.renderModel(model, stack, combinedLight, combinedOverlay, mx, vb);
      }
      mx.pop();
    }
  }
}
