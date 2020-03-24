package com.gmail.nuclearcat1337.horse_stats.gui;

import net.minecraft.client.gui.GuiButton;

public class GuiToggleButton<T> extends GuiButton {
  private final String name;
  private final GuiToggleButton.GuiValueResponder<T> responder;
  private final GuiToggleButton.FormatHelper<T> formatter;
  private T value;

  public GuiToggleButton(GuiToggleButton.GuiValueResponder<T> responder, int id, int x, int y, T defaultValue, String name, GuiToggleButton.FormatHelper<T> formatter) {
    super(id, x, y, 150, 20, "");
    this.name = name;
    this.responder = responder;
    this.formatter = formatter;
    this.value = defaultValue;
    setDisplayString();
  }

  @Override
  public void mouseReleased(int mouseX, int mouseY) {
    value = responder.setEntryValue(id, value);
    setDisplayString();
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  private void setDisplayString() {
    this.displayString = formatter.getText(id, name, value);
  }

  public interface GuiValueResponder<T> {
    /**
     * Handles clicking of the {@link GuiToggleButton}
     * @param id the id of the button
     * @param currentValue the current value of the {@link GuiToggleButton}
     * @return the new value of the {@link GuiToggleButton}
     */
    T setEntryValue(int id, T currentValue);
  }

  public interface FormatHelper<T> {
    String getText(int id, String name, T value);
  }
}
