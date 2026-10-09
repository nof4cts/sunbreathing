package com.sunbreathing.client.fx;

import com.sunbreathing.client.anim.PlayerAnims;
import com.sunbreathing.config.SunConfig;
import com.sunbreathing.forms.Form;
import com.sunbreathing.forms.FormTiming;
import com.sunbreathing.network.S2CFormCast;
import com.sunbreathing.network.S2CImpact;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Client presentation for each form: body animation, slash meshes and particles, all keyed off
 * the cast packet, plus the shared impact response (flash, hit-stop, shake, FOV punch).
 */
public final class FormVfx {
    private FormVfx() {}

    public static void onCast(S2CFormCast msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(msg.entityId);
        if (!(e instanceof AbstractClientPlayer player)) return;
        Form form = Form.byIndex(msg.form);

        PlayerAnims.play(player, form);

        switch (form) {
            case DANCE -> dance(player, msg);
            default -> { }
        }
    }

    // ------------------------------------------------------------------ 1st Form: Dance
    /**
     * One diagonal crescent from high-right to low-left. The arc is anchored to the player when it
     * spawns (after the lunge has started) so it stays glued to the body instead of the cast spot.
     */
    private static void dance(AbstractClientPlayer player, S2CFormCast msg) {
        final float yaw = msg.yaw, pitch = msg.pitch;

        // Windup: a few embers gather at the blade hand.
        Vec3 hand = handPos(player, yaw);
        for (int i = 0; i < SunFx.count(6); i++) {
            SunFx.ember(hand.add(SunFx.randomUnit().scale(0.25)), new Vec3(0, 0.03, 0));
        }

        ClientScheduler.schedule(FormTiming.DANCE_ARC_TICK, () -> {
            if (player.isRemoved()) return;
            Vec3 center = player.position().add(0, player.getBbHeight() * 0.62, 0)
                    .add(Vec3.directionFromRotation(0f, yaw).scale(0.15));
            SlashArc core = SlashArc.facing(center, yaw, pitch, 38f, 2.35f, 0.95f, 165f,
                    120f, 430f, 1.0f, 0.42f, 0.08f);
            SlashArcRenderer.add(core);
            // Thin inner echo, slightly delayed and smaller: gives the slash depth.
            SlashArc echo = SlashArc.facing(center, yaw, pitch, 30f, 1.75f, 0.45f, 140f,
                    150f, 380f, 0.85f, 0.16f, 0.04f);
            SlashArcRenderer.add(echo);
            SunFx.flamesAlongArc(core, 0f, 1f, 1.0f);
        });
        // Follow-up flames licking off the trail as it fades.
        ClientScheduler.schedule(FormTiming.DANCE_ARC_TICK + 2, () -> {
            if (player.isRemoved()) return;
            Vec3 center = player.position().add(0, player.getBbHeight() * 0.62, 0);
            SlashArc ghost = SlashArc.facing(center, yaw, pitch, 38f, 2.35f, 0.95f, 165f, 1f, 1f, 1f, 1f, 1f);
            SunFx.flamesAlongArc(ghost, 0.35f, 1f, 0.5f);
        });
    }

    private static Vec3 handPos(AbstractClientPlayer p, float yaw) {
        Vec3 fwd = Vec3.directionFromRotation(0f, yaw);
        Vec3 right = fwd.cross(new Vec3(0, 1, 0)).normalize();
        return p.position().add(0, p.getBbHeight() * 0.7, 0).add(right.scale(0.4)).add(fwd.scale(0.3));
    }

    // ------------------------------------------------------------------ impacts
    public static void onImpact(S2CImpact msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Vec3 at = new Vec3(msg.x, msg.y, msg.z);
        float heaviness = Mth.clamp(msg.damage / 15f, 0.15f, 1f);
        Vec3 dir = Vec3.directionFromRotation(0f, msg.yaw);

        SunFx.impactBurst(at, dir, heaviness);

        boolean iAmAttacker = msg.attackerId == mc.player.getId();
        boolean iAmVictim = msg.targetId == mc.player.getId();
        if (!iAmAttacker && !iAmVictim) return;

        // Hit-stop: ~2 frames for light hits up to ~4 frames (at 60 fps) for heavy ones.
        if (SunConfig.CLIENT.hitStop.get()) {
            VfxClock.freeze(33.0 + 34.0 * heaviness);
        }
        CameraFx.addTrauma(iAmVictim ? 0.35f + 0.5f * heaviness : 0.18f + 0.42f * heaviness);
        if (heaviness >= 0.55f) CameraFx.punchFov(heaviness);
    }
}
