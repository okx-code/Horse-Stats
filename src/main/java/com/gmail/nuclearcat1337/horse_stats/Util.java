package com.gmail.nuclearcat1337.horse_stats;

import java.text.DecimalFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.AbstractHorse;

/**
 * Created by Mr_Little_Kitty on 1/10/2017.
 */
public class Util {
    /**
     * Gets the amount of decimals that should be displayed with a DecimalFormat object.
     *
     * @return
     */
    public static DecimalFormat CreateDecimalFormat(int numberOfDecimalsDisplayed) {
        if (numberOfDecimalsDisplayed < 1)
            return new DecimalFormat("#");

        String format = "#.";
        for (int i = 1; i <= numberOfDecimalsDisplayed; i++)
            format += "#";

        return new DecimalFormat(format);
    }

    /**
     * Gets the max height a horse can jump when the jump bar is fully charged.
     *
     * @param horse
     * @return e.x. 1.2?-5.5?
     */
    public static float GetHorseMaxJump(AbstractHorse horse) {
        double x = horse.getHorseJumpStrength();
        return (float) (-0.1817584952 * x*x*x + 3.689713992 * x*x + 2.128599134 * x - 0.343930367);
    }

    /**
     * Gets an entity's max run speed in meters(blocks) per second
     *
     * @param entity
     * @return e.x. Steve = 4.3 m/s. Horses ~7-13
     */
    public static float GetEntityMaxSpeed(EntityLivingBase entity) {
        return (float) entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 42.15778758471F;
    }

    /**
     * Gets an entity's max hit points
     *
     * @param entity
     * @return e.x. Steve = 20 hit points
     */
    public static int GetEntityMaxHP(EntityLivingBase entity) {
        return (int) entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() + 1;
    }

    /**
     * Gets the baby horses age ranging from 0 to 100.
     *
     * @param horse
     * @return
     */
    public static int GetHorseBabyGrowingAgeAsPercent(AbstractHorse horse) {
        float horseGrowingAge = horse.getHorseSize();    //horse size ranges from 0.5 to 1
        return (int) ((horseGrowingAge - 0.5f) * 2.0f * 100f);
    }
}
