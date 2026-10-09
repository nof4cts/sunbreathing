package com.sunbreathing.forms;

/**
 * Shared timeline constants (ticks, 20 per second) so the server's hit window, the client's
 * slash mesh and the Player Animator keyframes line up. If you retime an animation in
 * Blockbench, update these numbers to match.
 */
public final class FormTiming {
    private FormTiming() {}

    // 1st Form: Dance  (animation "form_dance", 0.6s)
    public static final int DANCE_LUNGE_TICK = 1;
    /** Client: slash mesh starts sweeping on this tick (blade leaves the shoulder). */
    public static final int DANCE_ARC_TICK = 2;
    /** Server: hit check when the blade is mid-sweep. */
    public static final int DANCE_HIT_TICK = 3;
}
