package com.gmail.nuclearcat1337.horse_stats.gui;

import com.gmail.nuclearcat1337.horse_stats.HorseStats;
import com.gmail.nuclearcat1337.horse_stats.Threshold;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;

import java.text.DecimalFormat;
import net.minecraft.client.gui.GuiSlider.FormatHelper;
import net.minecraftforge.fml.client.config.GuiCheckBox;

/**
 * Created by Mr_Little_Kitty on 1/10/2017.
 */
public class GuiHorseStats extends GuiScreen {
    private static final int BUTTON_WIDTH = GuiConstants.SMALL_BUTTON_WIDTH * 3;


    private final HorseStats horseStats;

    private GuiCheckBox toggleJump;
    private GuiCheckBox toggleHealth;
    private GuiCheckBox toggleSpeed;

    public GuiHorseStats(HorseStats stats) {
        this.horseStats = stats;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int xPos = (this.width / 2) - (BUTTON_WIDTH / 2) - (GuiConstants.STANDARD_SEPARATION_DISTANCE * 2) - BUTTON_WIDTH;
        int yPos = (this.height / 2) - (GuiConstants.STANDARD_BUTTON_HEIGHT / 2) - GuiConstants.STANDARD_SEPARATION_DISTANCE - GuiConstants.STANDARD_BUTTON_HEIGHT * 2; //the last *2 is so that the buttons are higher up
        int buttonYPos1 = yPos + GuiConstants.STANDARD_BUTTON_HEIGHT * 3 + GuiConstants.STANDARD_SEPARATION_DISTANCE * 3;
        int buttonYPos2 = buttonYPos1 + GuiConstants.STANDARD_BUTTON_HEIGHT + GuiConstants.STANDARD_SEPARATION_DISTANCE;

        toggleJump = layoutThresholdButtons(xPos, yPos, horseStats.getJumpThreshold(), "Jump", 2, 5.3f, 0.1f, new DecimalFormat("#.0"), horseStats.getSettings().showJump);

        buttonList.add(new GuiSnappySlider(renderDistanceResponder, 10, xPos, buttonYPos1, new SliderOption("Max Distance", 0, 50, 1), horseStats.getRenderDistance(), renderDistanceFormatter));

        xPos += BUTTON_WIDTH + GuiConstants.STANDARD_SEPARATION_DISTANCE * 2;

        toggleHealth = layoutThresholdButtons(xPos, yPos, horseStats.getHealthThreshold(), "Health", 18, 31, 1, new DecimalFormat("#"), horseStats.getSettings().showHealth);

        buttonList.add(new GuiSnappySlider(decimalPlacesResponder, 11, xPos, buttonYPos1, new SliderOption("Decimal Places", 0, 7, 1), horseStats.getDecimalPlaces(), decimalPlacesFormatter));
        buttonList.add(new GuiButton(1, xPos, buttonYPos2, BUTTON_WIDTH, GuiConstants.STANDARD_BUTTON_HEIGHT, "Done"));

        xPos += BUTTON_WIDTH + GuiConstants.STANDARD_SEPARATION_DISTANCE * 2;

        toggleSpeed = layoutThresholdButtons(xPos, yPos, horseStats.getSpeedThreshold(), "Speed", 8, 14.3F, 0.05F, new DecimalFormat("#.00"), horseStats.getSettings().showSpeed);

        buttonList.add(new GuiToggleButton<>(showRidingResponder, 12, xPos, buttonYPos1, horseStats.getSettings().renderWhileRiding, "Show Riding", showRidingFormatter));

        super.initGui();
    }

    private GuiCheckBox layoutThresholdButtons(int xPos, int yPos, Threshold threshold, String name, float min, float max, float step, DecimalFormat df, boolean show) {
        GuiCheckBox box = new GuiCheckBox(6, xPos, yPos - GuiConstants.STANDARD_BUTTON_HEIGHT / 2 - mc.fontRenderer.FONT_HEIGHT / 2,"Horse " + name, show);

        DecimalFormatHelper helper = new DecimalFormatHelper(df);
        GuiSnappySlider greatSlider = new GuiSnappySlider(new ThresholdRunnable(threshold, true), 7, xPos, yPos, new SliderOption(name + " Great", min, max, step), threshold.getGreat(), helper);
        greatSlider.width = BUTTON_WIDTH;

        yPos += greatSlider.height + GuiConstants.STANDARD_SEPARATION_DISTANCE;

        GuiSnappySlider averageSlider = new GuiSnappySlider(new ThresholdRunnable(threshold, false), 8, xPos, yPos, new SliderOption(name + " Avg", min, max, step), threshold.getAverage(), helper);
        averageSlider.width = BUTTON_WIDTH;

        buttonList.add(box);
        buttonList.add(greatSlider);
        buttonList.add(averageSlider);

        return box;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        /*int xPos = (this.width / 2) - (BUTTON_WIDTH / 2) - (GuiConstants.STANDARD_SEPARATION_DISTANCE * 2) - BUTTON_WIDTH;
        int yPos = (this.height / 2) - (GuiConstants.STANDARD_BUTTON_HEIGHT / 2) - GuiConstants.STANDARD_SEPARATION_DISTANCE - GuiConstants.STANDARD_BUTTON_HEIGHT * 2; //the last *2 is so that the buttons are higher up

        mc.fontRenderer.drawString(JUMP_STRING, xPos + BUTTON_WIDTH / 2 - jumpWidth / 2, yPos - GuiConstants.STANDARD_BUTTON_HEIGHT / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 16777215);

        xPos += BUTTON_WIDTH + GuiConstants.STANDARD_SEPARATION_DISTANCE * 2;

        mc.fontRenderer.drawString(HEALTH_STRING, xPos + BUTTON_WIDTH / 2 - healthWidth / 2, yPos - GuiConstants.STANDARD_BUTTON_HEIGHT / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 16777215);

        xPos += BUTTON_WIDTH + GuiConstants.STANDARD_SEPARATION_DISTANCE * 2;

        mc.fontRenderer.drawString(SPEED_STRING, xPos + BUTTON_WIDTH / 2 - speedWidth / 2, yPos - GuiConstants.STANDARD_BUTTON_HEIGHT / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 16777215);*/
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (!button.enabled)
            return;

        switch (button.id) {
            case 0:
                boolean nextState = !horseStats.shouldRenderStats();
                horseStats.getSettings().shouldRender = nextState;
                button.displayString = "Overlay Render: " + (nextState ? "On" : "Off");
                horseStats.saveSettings();
                break;
            case 1:
                horseStats.saveSettings();
                Minecraft.getMinecraft().displayGuiScreen(null);
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        horseStats.getSettings().showHealth = toggleHealth.isChecked();
        horseStats.getSettings().showJump = toggleJump.isChecked();
        horseStats.getSettings().showSpeed = toggleSpeed.isChecked();

        horseStats.saveSettings();
    }

    private final GuiPageButtonList.GuiResponder decimalPlacesResponder = new GuiPageButtonList.GuiResponder() {
        @Override
        public void setEntryValue(int id, boolean value) {

        }

        @Override
        public void setEntryValue(int id, float value) {
            horseStats.getSettings().decimalPlaces = (int) value;
            horseStats.updateDecimalPlaces();
        }

        @Override
        public void setEntryValue(int id, String value) {

        }
    };

    private final GuiPageButtonList.GuiResponder renderDistanceResponder = new GuiPageButtonList.GuiResponder() {
        @Override
        public void setEntryValue(int id, boolean value) {

        }

        @Override
        public void setEntryValue(int id, float value) {
            horseStats.getSettings().renderDistance = value;
        }

        @Override
        public void setEntryValue(int id, String value) {

        }
    };

    private final GuiToggleButton.GuiValueResponder<Boolean> showRidingResponder = new GuiToggleButton.GuiValueResponder<Boolean>() {
        @Override
        public Boolean setEntryValue(int id, Boolean currentValue) {
            boolean newValue = !currentValue;
            horseStats.getSettings().renderWhileRiding = newValue;
            return newValue;
        }
    };

    private final GuiToggleButton.FormatHelper<Boolean> showRidingFormatter = new GuiToggleButton.FormatHelper<Boolean>() {
        @Override
        public String getText(int id, String name, Boolean value) {
            return name + ": " + (value ? "YES" : "NO");
        }
    };

    private final GuiSlider.FormatHelper decimalPlacesFormatter = new GuiSlider.FormatHelper() {
        @Override
        public String getText(int id, String name, float value) {
            return name + ": " + (int) value + " d.p.";
        }
    };

    private final GuiSlider.FormatHelper renderDistanceFormatter = new GuiSlider.FormatHelper() {
        @Override
        public String getText(int id, String name, float value) {
            int iv = (int) value;
            String render;
            if (iv <= 0) {
                render = "None";
            } else if (iv == 1) {
                render = iv + " block";
            } else {
                render = iv + " blocks";
            }
            return name + ": " + render;
        }
    };
    class DecimalFormatHelper implements FormatHelper {

        private final DecimalFormat format;

        DecimalFormatHelper(DecimalFormat format) {
            this.format = format;
        }
        @Override
        public String getText(int id, String name, float value) {
            return name + ": " + format.format(value);
        }

    }

    private static class ThresholdRunnable implements GuiPageButtonList.GuiResponder {
        private final Threshold threshold;
        private final boolean great;

        public ThresholdRunnable(Threshold threshold, boolean great) {
            this.threshold = threshold;
            this.great = great;
        }

        @Override
        public void setEntryValue(int id, boolean value) {

        }

        @Override
        public void setEntryValue(int id, float value) {
            if (this.great)
                threshold.setGreat(value);
            else
                threshold.setAverage(value);
        }

        @Override
        public void setEntryValue(int id, String value) {

        }
    }
}
