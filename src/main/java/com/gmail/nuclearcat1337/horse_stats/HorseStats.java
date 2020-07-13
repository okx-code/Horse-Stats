package com.gmail.nuclearcat1337.horse_stats;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/*
Created by Mr_Little_Kitty on 12/17/2015
*/
@Mod(modid = HorseStats.MODID, name = HorseStats.MODNAME, version = HorseStats.MODVERSION)
public class HorseStats {
    public static final String MODID = "horsestats";
    public static final String MODNAME = "Horse Stats";
    public static final String MODVERSION = "1.3.0";

    public static final String DECIMAL_PLACES_KEY = "decimal-places";
    public static final String RENDER_KEY = "should-render";
    public static final String RENDER_DISTANCE_KEY = "render-distance";
    public static final String JUMP_KEY = "jump-threshold";
    public static final String SPEED_KEY = "speed-threshold";
    public static final String HEALTH_KEY = "health-threshold";

    private static Minecraft mc = Minecraft.getMinecraft();
    private static final File modSettingsFile = new File(mc.mcDataDir, "/mods/" + MODNAME + "/Settings.txt");

    @Mod.Instance(MODID)
    public static HorseStats instance;
    public static Logger logger = Logger.getLogger("HorseStats");

    private Settings settings;
    private DecimalFormat decimalFormat;

    private static final float MIN_TEXT_RENDER_SCALE = 0.02f;
    private static final float MAX_TEXT_RENDER_SCALE = 0.06f;

    private static final float scale_step = (MAX_TEXT_RENDER_SCALE - MIN_TEXT_RENDER_SCALE) / 30;

    @Mod.EventHandler
    public void preInitialize(FMLPreInitializationEvent event) {
        logger.info("HorseStats: pre-Initializing");

        initializeSettings();
    }

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        logger.info("HorseStats: Initializing");

        updateDecimalPlaces();

        //Self registers with forge to receive proper events
        new KeyHandler();

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void updateDecimalPlaces() {
        decimalFormat = Util.CreateDecimalFormat(settings.decimalPlaces);
    }

    public int getDecimalPlaces() {
        return settings.decimalPlaces;
    }

    public Settings getSettings() {
        return settings;
    }

    public boolean shouldRenderStats() {
        return settings.renderDistance > 0.1;
    }

    public float getRenderDistance() {
        return settings.renderDistance;
    }

    public float getRenderDistanceSquared() {
        return settings.renderDistance * settings.renderDistance;
    }

    public Threshold getSpeedThreshold() {
        return settings.speedThreshold;
    }

    public Threshold getJumpThreshold() {
        return settings.jumpThreshold;
    }

    public Threshold getHealthThreshold() {
        return settings.healthThreshold;
    }

    public void saveSettings() {
        final File parentDirectory = modSettingsFile.getParentFile();
        if (!parentDirectory.exists()) {
            parentDirectory.mkdir();
        }

        if (!modSettingsFile.exists()) {
            try {
                modSettingsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.write(gson.toJson(settings), modSettingsFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void RenderWorldLastEvent(RenderWorldLastEvent event) {
        if (mc.inGameHasFocus && shouldRenderStats() && !mc.gameSettings.hideGUI) {
            for (int i = 0; i < mc.world.loadedEntityList.size(); i++) {
                Entity object = mc.world.loadedEntityList.get(i);

                if (object instanceof AbstractHorse) {
                    RenderHorseInfoInWorld((AbstractHorse) object, event.getPartialTicks());
                }

            }
        }
    }

    public boolean canEntityBeSeen(Entity e) {
        // ray trace the bottom middle and top middle of the horse
        return trace(e.posX, e.posY + e.getEyeHeight(), e.posZ)
            || trace(e.posX, e.posY, e.posZ);
    }

    private boolean trace(double x, double y, double z) {
        return rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ),
            new Vec3d(x, y, z), false) == null;
    }

    private void RenderHorseInfoInWorld(AbstractHorse horse, float partialTickTime) {
        //if the player is in the world
        //and not looking at a menu
        //and F1 not pressed
        if ((mc.inGameHasFocus || mc.currentScreen == null || mc.currentScreen instanceof GuiChat)) {
            if (mc.player.isRidingHorse() && mc.player.getRidingEntity() == horse) {
                if (settings.renderWhileRiding) {
                    // if this is not constant it glitches on horseback
                    partialTickTime = 1;
                } else {
                    //don't render stats of the horse/animal we are currently riding
                    return;
                }
            }

            //only show entities that are close by
            double distanceFromMe = mc.player.getDistanceSq(horse);

            if (distanceFromMe <= getRenderDistanceSquared() && canEntityBeSeen(horse)) {
                RenderHorseOverlay(horse, partialTickTime);
            }
        }
    }

    protected void RenderHorseOverlay(AbstractHorse horse, float partialTickTime) {
        float x = (float) horse.posX;
        float y = (float) horse.posY;
        float z = (float) horse.posZ;

        List<String> overlayText = new ArrayList<>(3);

        if (settings.showSpeed) {
            overlayText.add(getSpeedThreshold().format(decimalFormat, Util.GetEntityMaxSpeed(horse)) + " m/s");
        }
        if (settings.showHealth) {
            overlayText.add(getHealthThreshold().format(decimalFormat, Util.GetEntityMaxHP(horse)) + " hp");
        }
        if (settings.showJump) {
            overlayText.add(getJumpThreshold().format(decimalFormat, Util.GetHorseMaxJump(horse)) + " jump");
        }

        RenderFloatingText(overlayText, x, y + 1.3f, z, 0xFFFFFF, true, partialTickTime);
    }

    public void RenderFloatingText(List<String> text, float x, float y, float z, int color, boolean renderBlackBackground, float partialTickTime) {
        if (text == null || text.isEmpty()) {
            return;
        }

        //Thanks to Electric-Expansion mod for the majority of this code
        //https://github.com/Alex-hawks/Electric-Expansion/blob/master/src/electricexpansion/client/render/RenderFloatingText.java

        RenderManager renderManager = mc.getRenderManager();

        float playerX = (float) (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTickTime);
        float playerY = (float) (mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTickTime);
        float playerZ = (float) (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTickTime);

        float dx = x - playerX;
        float dy = y - playerY;
        float dz = z - playerZ;
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float scale = MIN_TEXT_RENDER_SCALE + (distance * scale_step);//.01f; //Min font scale for max text render distance

        GL11.glColor4f(1f, 1f, 1f, 0.5f);
        GL11.glPushMatrix();
        GL11.glTranslatef(dx, dy, dz);
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int textWidth = 0;
        for (String thisMessage : text) {
            int thisMessageWidth = mc.fontRenderer.getStringWidth(thisMessage);

            if (thisMessageWidth > textWidth)
                textWidth = thisMessageWidth;
        }

        int lineHeight = 10;
        int initialValue = lineHeight * text.size();

        if (renderBlackBackground) {
            int stringMiddle = textWidth / 2;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexBuffer = tessellator.getBuffer();

            GlStateManager.disableTexture2D();

            //This code taken from 1.8.8 net.minecraft.client.renderer.entity.Render.renderLivingLabel()
            vertexBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            vertexBuffer.pos((double) (-stringMiddle - 1), (double) (-1) - initialValue, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexBuffer.pos((double) (-stringMiddle - 1), (double) (8 + lineHeight * (text.size() - 1)) - initialValue, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexBuffer.pos((double) (stringMiddle + 1), (double) (8 + lineHeight * (text.size() - 1)) - initialValue, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexBuffer.pos((double) (stringMiddle + 1), (double) (-1) - initialValue, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();

            tessellator.draw();

            GlStateManager.enableTexture2D();
        }

        int i = 0;
        for (String message : text) {
            mc.fontRenderer.drawString(message, -textWidth / 2, (i * lineHeight) - initialValue, color);
            i++;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }

    private void initializeSettings() {
        if (modSettingsFile.exists()) {
            try (FileReader reader = new FileReader(modSettingsFile)) {
                Gson gson = new Gson();
                settings = gson.fromJson(reader, Settings.class);
                logger.info("Loaded settings from disk");
            } catch (IOException e) {
                settings = new Settings();
                e.printStackTrace();
                logger.info("Error loading settings from disk. Loading default settings");
            }
        } else {
            logger.info("No settings found. Loading default settings");
            settings = new Settings();
            saveSettings();
        }
    }

    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean returnLastUncollidableBlock) {
        // copied from minecraft source code with some adjustments

        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int i = MathHelper.floor(vec32.x);
                int j = MathHelper.floor(vec32.y);
                int k = MathHelper.floor(vec32.z);
                int l = MathHelper.floor(vec31.x);
                int i1 = MathHelper.floor(vec31.y);
                int j1 = MathHelper.floor(vec31.z);
                BlockPos blockpos = new BlockPos(l, i1, j1);
                IBlockState iblockstate = mc.world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if ((iblockstate.getCollisionBoundingBox(mc.world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, false)) {
                    RayTraceResult raytraceresult = iblockstate.collisionRayTrace(mc.world, blockpos, vec31, vec32);

                    if (raytraceresult != null) {
                        return raytraceresult;
                    }
                }

                RayTraceResult raytraceresult2 = null;
                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return returnLastUncollidableBlock ? raytraceresult2 : null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double)l + 1.0D;
                    } else if (i < l) {
                        d0 = (double)l + 0.0D;
                    } else {
                        flag2 = false;
                    }

                    if (j > i1) {
                        d1 = (double)i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double)i1 + 0.0D;
                    } else {
                        flag = false;
                    }

                    if (k > j1) {
                        d2 = (double)j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double)j1 + 0.0D;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;

                    if (flag2) {
                        d3 = (d0 - vec31.x) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - vec31.y) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - vec31.z) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    EnumFacing enumfacing;

                    if (d3 < d4 && d3 < d5)
                    {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    }
                    else if (d4 < d5)
                    {
                        enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    }
                    else
                    {
                        enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    IBlockState iblockstate1 = mc.world.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();

                    if (iblockstate1.getCollisionBoundingBox(mc.world, blockpos) != Block.NULL_AABB
                        && iblockstate1.isOpaqueCube()) {

                        if (block1.canCollideCheck(iblockstate1, false)) {
                            RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(mc.world, blockpos, vec31, vec32);

                            if (raytraceresult1 != null) {
                                return raytraceresult1;
                            }
                        } else {
                            raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
                        }
                    }
                }

                return returnLastUncollidableBlock ? raytraceresult2 : null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
}
