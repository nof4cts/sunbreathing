package com.sunbreathing.forms;

import com.sunbreathing.breath.BreathData;
import com.sunbreathing.config.SunConfig;
import com.sunbreathing.network.ModNetwork;
import com.sunbreathing.network.S2CFormCast;
import com.sunbreathing.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Server timelines for each form: movement, hit windows and sounds.
 *
 * Every timeline starts by broadcasting {@link S2CFormCast} with the yaw/pitch used for
 * hit detection, so the client's slash mesh is drawn exactly where hits are checked.
 * Timings here must match the matching client animation (see FormTiming).
 */
public final class ServerForms {
    private ServerForms() {}

    public static void cast(Form form, ServerPlayer player, BreathData data, SunConfig.FormStats stats) {
        switch (form) {
            case DANCE -> dance(player, stats);
            default -> { /* later stages */ }
        }
    }

    private static void broadcastCast(ServerPlayer p, Form form) {
        ModNetwork.sendToTrackingAndSelf(p, new S2CFormCast(p.getId(), form.ordinal(),
                p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot()));
    }

    private static boolean stillValid(ServerPlayer p) {
        return p.isAlive() && !p.hasDisconnected();
    }

    // ------------------------------------------------------------------ 1st Form: Dance
    /**
     * A single fast diagonal slash with a short forward step.
     * t0: windup (whoosh + ignite) | t1: lunge | t3: blade crosses the arc -> hit check
     */
    private static void dance(ServerPlayer p, SunConfig.FormStats stats) {
        ServerLevel level = p.serverLevel();
        final float yaw = p.getYRot();
        final float damage = stats.damage.get().floatValue();

        broadcastCast(p, Form.DANCE);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.DANCE_SWING.get(), SoundSource.PLAYERS,
                1.0f, 0.95f + level.random.nextFloat() * 0.1f);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.FLAME_IGNITE.get(), SoundSource.PLAYERS,
                0.7f, 1.1f);

        ServerScheduler.schedule(FormTiming.DANCE_LUNGE_TICK, () -> {
            if (!stillValid(p)) return;
            Vec3 fwd = Vec3.directionFromRotation(0f, yaw).scale(p.onGround() ? 0.9 : 0.45);
            p.setDeltaMovement(p.getDeltaMovement().add(fwd.x, 0.04, fwd.z));
            p.hurtMarked = true; // pushes the velocity change to the client
        });

        ServerScheduler.schedule(FormTiming.DANCE_HIT_TICK, () -> {
            if (!stillValid(p)) return;
            Vec3 origin = p.position().add(0, p.getBbHeight() * 0.55, 0);
            List<LivingEntity> targets = Combat.arc(p, origin, yaw, 4.6, 160, 2.4);
            boolean any = false;
            for (LivingEntity t : targets) {
                any |= Combat.strike(p, t, damage, Form.DANCE, yaw, 0.45);
            }
            if (any) Combat.playImpactLayers(level, targets.get(0).position(), Math.min(1f, damage / 15f));
        });
    }
}
