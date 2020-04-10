package com.gmail.nuclearcat1337.horse_stats;

public class Settings {
    public boolean showJump = true;
    public boolean showHealth = true;
    public boolean showSpeed = true;

    public Threshold jumpThreshold = new Threshold(3, 4);
    public Threshold speedThreshold = new Threshold(11, 13);
    public Threshold healthThreshold = new Threshold(24, 28);

    public float renderDistance = 15.0F;
    public boolean shouldRender = true;
    public int decimalPlaces = 3;

    public boolean renderWhileRiding = false;
}
