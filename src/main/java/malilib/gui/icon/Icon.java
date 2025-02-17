package malilib.gui.icon;

import com.google.gson.JsonObject;

import malilib.render.RenderUtils;
import malilib.render.ShapeRenderUtils;
import malilib.util.data.Identifier;

public interface Icon
{
    /**
     * @return the width of this icon in pixels
     */
    int getWidth();

    /**
     * @return the height of this icon in pixels
     */
    int getHeight();

    /**
     * @return the texture pixel u-coordinate (x-coordinate) of this icon's top left corner
     */
    int getU();

    /**
     * @return the texture pixel v-coordinate (y-coordinate) of this icon's top left corner
     */
    int getV();

    /**
     * @return the u-coordinate for the given icon variant
     */
    int getVariantU(int variantIndex);

    /**
     * @return the v-coordinate for the given icon variant
     */
    int getVariantV(int variantIndex);

    /**
     * @return the texture sheet width this icon is on
     */
    int getTextureSheetWidth();

    /**
     * @return the texture sheet height this icon is on
     */
    int getTextureSheetHeight();

    /**
     * @return the relative width of one pixel in the texture sheet
     */
    float getTexturePixelWidth();

    /**
     * @return the relative height of one pixel in the texture sheet
     */
    float getTexturePixelHeight();

    /**
     * @return the identifier/location of the texture sheet used for this icon
     */
    Identifier getTexture();

    /**
     * @return The icon serialized into a JSON object
     */
    JsonObject toJson();

    /**
     * Renders this icon at the given position
     */
    default void renderAt(int x, int y, float z)
    {
        this.renderScaledAt(x, y, z, this.getWidth(), this.getHeight(), 0);
    }

    /**
     * Renders the icon at the given location, using the given icon variant index.
     * The variant index is basically an offset from the base UV location.
     * The implementation can define where and how the position is offset
     * from the base location.
     */
    default void renderAt(int x, int y, float z, int variantIndex)
    {
        this.renderScaledAt(x, y, z, this.getWidth(), this.getHeight(), variantIndex);
    }

     /**
     * Renders a possibly scaled/stretched version of this icon, with the given rendered width and height
     */
    default void renderScaledAt(int x, int y, float z, int renderWidth, int renderHeight)
    {
        this.renderScaledAt(x, y, z, renderWidth, renderHeight, 0);
    }

    /**
     * Renders a possibly scaled/stretched version of this icon, with the given
     * rendered width and height, using the given icon variant index.
     * The variant index is basically an offset from the base UV location.
     * The implementation can define where and how the position is offset
     * from the base location.
     */
    default void renderScaledAt(int x, int y, float z, int renderWidth, int renderHeight, int variantIndex)
    {
        int width = this.getWidth();
        int height = this.getHeight();

        if (width == 0 || height == 0)
        {
            return;
        }

        int u = this.getVariantU(variantIndex);
        int v = this.getVariantV(variantIndex);
        float pw = this.getTexturePixelWidth();
        float ph = this.getTexturePixelHeight();

        RenderUtils.color(1f, 1f, 1f, 1f);
        RenderUtils.bindTexture(this.getTexture());
        RenderUtils.setupBlend();

        ShapeRenderUtils.renderScaledTexturedRectangle(x, y, z, u, v, renderWidth, renderHeight,
                                                       width, height, pw, ph);
    }

    /**
     * Renders this icon at the given position, with a tint color
     */
    default void renderTintedAt(int x, int y, float z, int backgroundTintColor)
    {
        int width = this.getWidth();
        int height = this.getHeight();

        if (width == 0 || height == 0)
        {
            return;
        }

        int u = this.getU();
        int v = this.getV();
        float pw = this.getTexturePixelWidth();
        float ph = this.getTexturePixelHeight();

        RenderUtils.color(1f, 1f, 1f, 1f);
        RenderUtils.bindTexture(this.getTexture());
        RenderUtils.setupBlend();

        ShapeRenderUtils.renderScaledTintedTexturedRectangle(x, y, z, u, v, width, height,
                                                             width, height, pw, ph, backgroundTintColor);
    }

    /** 
     * Renders a composite (smaller) icon by using a rectangular area
     * of each of the 4 corners of the texture. The width and height
     * arguments define what size texture is going to be rendered.
     * @param width the width of the icon to render
     * @param height the height of the icon to render
     */
    default void renderFourSplicedAt(int x, int y, float z, int width, int height)
    {
        this.renderFourSplicedAt(x, y, z, width, height, 0);
    }

    default void renderFourSplicedAt(int x, int y, float z, int width, int height, int variantIndex)
    {
        int textureWidth = this.getWidth();
        int textureHeight = this.getHeight();

        if (textureWidth == 0 || textureHeight == 0)
        {
            return;
        }

        int u = this.getVariantU(variantIndex);
        int v = this.getVariantV(variantIndex);
        int w1 = width / 2;
        int w2 = (width & 0x1) != 0 ? w1 + 1 : w1;
        int h1 = height / 2;
        int h2 = (height & 0x1) != 0 ? h1 + 1 : h1;
        int uRight = u + textureWidth - w2;
        int vBottom = v + textureHeight - h2;
        float pw = this.getTexturePixelWidth();
        float ph = this.getTexturePixelHeight();

        RenderUtils.color(1f, 1f, 1f, 1f);
        RenderUtils.bindTexture(this.getTexture());

        ShapeRenderUtils.renderTexturedRectangle(x, y     , z, u, v      , w1, h1, pw, ph); // top left
        ShapeRenderUtils.renderTexturedRectangle(x, y + h1, z, u, vBottom, w1, h2, pw, ph); // bottom left

        ShapeRenderUtils.renderTexturedRectangle(x + w1, y     , z, uRight, v      , w2, h1, pw, ph); // top right
        ShapeRenderUtils.renderTexturedRectangle(x + w1, y + h1, z, uRight, vBottom, w2, h2, pw, ph); // bottom right
    }
}
