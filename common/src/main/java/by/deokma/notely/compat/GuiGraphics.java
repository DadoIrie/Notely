package by.deokma.notely.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.18.2 compatibility shim that emulates the 1.20+ GuiGraphics API
 * on top of a PoseStack + direct GuiComponent/Font calls.
 */
public final class GuiGraphics {

    private final PoseStack pose;

    // Optional panel transform used so scissor regions respect a translated/scaled UI.
    private float tOffX = 0f, tOffY = 0f, tScale = 1f;

    public GuiGraphics(PoseStack pose) {
        this.pose = pose;
    }

    public PoseStack pose() {
        return pose;
    }

    /** Tells the scissor logic about an active translate+scale applied to {@code pose}. */
    public void setTransform(float offsetX, float offsetY, float scale) {
        this.tOffX = offsetX;
        this.tOffY = offsetY;
        this.tScale = scale;
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(pose, x1, y1, x2, y2, color);
    }

    public void drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        if (shadow) font.drawShadow(pose, text, x, y, color);
        else font.draw(pose, text, x, y, color);
    }

    public void drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        if (shadow) font.drawShadow(pose, text, x, y, color);
        else font.draw(pose, text, x, y, color);
    }

    public void drawString(Font font, FormattedText text, int x, int y, int color, boolean shadow) {
        drawString(font, text.getString(), x, y, color, shadow);
    }

    public void blit(ResourceLocation texture, int x, int y, int u, int v, int w, int h, int texW, int texH) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(pose, x, y, (float) u, (float) v, w, h, texW, texH);
    }

    public void enableScissor(int x1, int y1, int x2, int y2) {
        Minecraft mc = Minecraft.getInstance();
        double scale = mc.getWindow().getGuiScale();
        int sh = mc.getWindow().getHeight();
        // Map panel-local coords through the active transform into GUI-scaled space.
        float fx1 = tOffX + x1 * tScale;
        float fy1 = tOffY + y1 * tScale;
        float fx2 = tOffX + x2 * tScale;
        float fy2 = tOffY + y2 * tScale;
        int sx = (int) (fx1 * scale);
        int sy = (int) (sh - fy2 * scale);
        int sw = (int) ((fx2 - fx1) * scale);
        int sht = (int) ((fy2 - fy1) * scale);
        RenderSystem.enableScissor(sx, sy, sw, sht);
    }

    public void disableScissor() {
        RenderSystem.disableScissor();
    }
}
