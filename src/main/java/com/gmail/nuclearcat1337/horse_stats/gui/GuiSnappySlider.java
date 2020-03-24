package com.gmail.nuclearcat1337.horse_stats.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSnappySlider extends GuiButton {

  private float sliderValue;
  public boolean dragging;
  private final SliderOption option;
  private final GuiPageButtonList.GuiResponder responder;
  private final GuiSlider.FormatHelper formatter;

  public GuiSnappySlider(GuiPageButtonList.GuiResponder responder, int buttonId, int x, int y,
      SliderOption optionIn, float defaultValue,
      GuiSlider.FormatHelper formatter) {
    super(buttonId, x, y, 150, 20, "");
    this.responder = responder;
    this.sliderValue = 1.0F;
    this.option = optionIn;
    this.sliderValue = optionIn.normalizeValue(defaultValue);
    this.displayString = formatter.getText(id, optionIn.getName(), optionIn.denormalizeValue(sliderValue));
    this.formatter = formatter;
  }

  /**
   * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if
   * it IS hovering over this button.
   */
  protected int getHoverState(boolean mouseOver) {
    return 0;
  }

  /**
   * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent
   * e).
   */
  protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      if (this.dragging) {
        setValue(mouseX);
      }

      mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)),
          this.y, 0, 66, 4, 20);
      this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)) + 4,
          this.y, 196, 66, 4, 20);
    }
  }

  /**
   * Returns true if the mouse has been pressed on this control. Equivalent of
   * MouseListener.mousePressed(MouseEvent e).
   */
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (super.mousePressed(mc, mouseX, mouseY)) {
      setValue(mouseX);
      return true;
    } else {
      return false;
    }
  }

  private void setValue(int mouseX) {
    this.sliderValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
    this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
    float f = this.option.denormalizeValue(this.sliderValue);
    responder.setEntryValue(id, f);
    this.sliderValue = this.option.normalizeValue(f);
    this.displayString = formatter.getText(id, option.getName(), f);
    this.dragging = true;
  }

  /**
   * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent
   * e).
   */
  public void mouseReleased(int mouseX, int mouseY) {
    this.dragging = false;
  }
}
