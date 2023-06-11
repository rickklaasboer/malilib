package fi.dy.masa.malilib.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;

public class GuiTextFieldGeneric extends TextFieldWidget {

  protected int x;
  protected int y;
  protected int width;
  protected int height;
  protected int zLevel;

  public GuiTextFieldGeneric(
    int x,
    int y,
    int width,
    int height,
    TextRenderer textRenderer
  ) {
    super(textRenderer, x, y, width, height, ScreenTexts.EMPTY);
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    this.setMaxLength(256);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);

    if (mouseButton == 1 && this.isMouseOver((int) mouseX, (int) mouseY)) {
      this.setText("");
      this.setFocused(true);
      return true;
    }

    return ret;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public boolean isMouseOver(int mouseX, int mouseY) {
    return (
      mouseX >= this.x &&
      mouseX < this.x + this.width &&
      mouseY >= this.y &&
      mouseY < this.y + this.height
    );
  }

  // Make public
  @Override
  public void setFocused(boolean isFocusedIn) {
    super.setFocused(isFocusedIn);
  }

  public int getCursorPosition() {
    return this.getCursor();
  }

  public void setCursorPosition(int pos) {
    this.setCursor(pos);
  }

  public void setCursorPositionZero() {
    this.setCursorToStart();
  }

  public void setCursorPositionEnd() {
    this.setCursorToEnd();
  }

  public GuiTextFieldGeneric setZLevel(int zLevel) {
    this.zLevel = zLevel;
    return this;
  }

  @Override
  public void render(
    DrawContext drawContext,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    if (this.zLevel != 0) {
      drawContext.getMatrices().push();
      drawContext.getMatrices().translate(0, 0, this.zLevel);

      super.render(drawContext, mouseX, mouseY, partialTicks);

      drawContext.getMatrices().pop();
    } else {
      super.render(drawContext, mouseX, mouseY, partialTicks);
    }
  }
}
