package org.dave.bonsaitrees.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.bonsaitrees.misc.RenderTickCounter;
import org.dave.bonsaitrees.tile.TileBonsaiPot;
import org.dave.bonsaitrees.trees.TreeBlockAccess;
import org.dave.bonsaitrees.trees.TreeShape;
import org.dave.bonsaitrees.utility.Logz;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class TESRBonsaiPot extends TileEntitySpecialRenderer<TileBonsaiPot> {
    private IBlockAccess blockAccess;
    private TreeShape treeShape;

    @Override
    public void render(TileBonsaiPot te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        //super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        if(te.getBonsaiShapeName() == null) {
            return;
        }



        treeShape = te.getBonsaiShape();

        List<BlockPos> toRender = treeShape.getToRenderPositions();
        if(toRender.isEmpty()) {
            return;
        }

        blockAccess = new TreeBlockAccess(treeShape, te.getWorld(), te.getPos());
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        // Init GlStateManager
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();

        GlStateManager.enableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();

        float angle = RenderTickCounter.renderTicks * 45.0f / 64.0f;

        float rotateOffsetX = (float)(treeShape.getWidth()+1) / 2.0f;
        float rotateOffsetY = 0.0f;
        float rotateOffsetZ = (float)(treeShape.getDepth()+1) / 2.0f;

        GlStateManager.translate(0.5f, 0.0f, 0.5f);


        double progress = (double)te.getProgress() / (double)te.getTreeType().getGrowTime();

        double scale = treeShape.getScaleRatio();

        GlStateManager.translate(0.0d, 0.10d, 0.0d);

        //GlStateManager.translate(-rotateOffsetX, -rotateOffsetY, -rotateOffsetZ);

        //GlStateManager.translate(rotateOffsetX, rotateOffsetY, rotateOffsetZ);


        //GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);
        GlStateManager.scale(scale, scale, scale);

        if(RenderTickCounter.renderTicks % 100 == 0) {
            //Logz.info("ProgressScale: %.2f", progress);
        }
        GlStateManager.scale(progress, progress, progress);


        GlStateManager.translate(-rotateOffsetX, -rotateOffsetY, -rotateOffsetZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        // Aaaand render
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        GlStateManager.disableAlpha();
        this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.SOLID, toRender);
        GlStateManager.enableAlpha();
        this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT_MIPPED, toRender);
        this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT, toRender);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.TRANSLUCENT, toRender);

        tessellator.draw();

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void renderLayer(BlockRendererDispatcher blockrendererdispatcher, BufferBuilder buffer, BlockRenderLayer renderLayer, List<BlockPos> toRender) {
        for (BlockPos pos : toRender) {
            IBlockState state = treeShape.getStateAtPos(pos);
            if (!state.getBlock().canRenderInLayer(state, renderLayer)) {
                continue;
            }

            ForgeHooksClient.setRenderLayer(renderLayer);
            try {
                //Logz.info("Rendering: %s", state);
                blockrendererdispatcher.renderBlock(state, pos, blockAccess, buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ForgeHooksClient.setRenderLayer(null);
        }
    }
}