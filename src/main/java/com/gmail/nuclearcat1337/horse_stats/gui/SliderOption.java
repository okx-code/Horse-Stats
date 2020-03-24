package com.gmail.nuclearcat1337.horse_stats.gui;

import net.minecraft.util.math.MathHelper;

public class SliderOption {
  private final String name;
  private final float valueStep;
  private float valueMin;
  private float valueMax;

  public SliderOption(String name, float min, float max, float step) {
    this.name = name;
    this.valueMin = min;
    this.valueMax = max;
    this.valueStep = step;
  }

  public String getName() {
    return this.name;
  }

  public float getValueMin() {
    return this.valueMin;
  }

  public float getValueMax() {
    return this.valueMax;
  }

  public void setValueMax(float value) {
    this.valueMax = value;
  }

  /**
   * turns it from a value between min and max to a value 0 and 1
   */
  public float normalizeValue(float value) {
    return MathHelper
        .clamp((this.snapToStepClamp(value) - this.valueMin) / (this.valueMax - this.valueMin),
            0.0F, 1.0F);
  }

  /**
   * turns it from a value between 0 and 1 to a value between min and max
   */
  public float denormalizeValue(float value) {
    return this.snapToStepClamp(
        this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp(value, 0.0F, 1.0F));
  }

  public float snapToStepClamp(float value) {
    value = this.snapToStep(value);
    return MathHelper.clamp(value, this.valueMin, this.valueMax);
  }

  private float snapToStep(float value) {
    if (this.valueStep > 0.0F) {
      value = this.valueStep * (float) Math.round(value / this.valueStep);
    }

    return value;
  }
}
