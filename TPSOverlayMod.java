package com.example.tpsoverlay;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Displays a rolling‑average server TPS in the upper‑left corner of the HUD.
 * Works on Minecraft 1.8.9 + Forge 11.15.1.2318.
 * <p>
 * v1.1 – adds a robust fallback path that estimates TPS from the cadence of
 * S00PacketKeepAlive packets when S03PacketTimeUpdate is throttled or absent
 * (common on large minigame networks such as Hypixel).
 */
@Mod(
        modid = TPSOverlayMod.MODID,
        name = TPSOverlayMod.NAME,
        version = TPSOverlayMod.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.9]"
)
public class TPSOverlayMod {

    public static final String MODID   = "tpsoverlay";
    public static final String NAME    = "TPS Overlay";
    public static final String VERSION = "1.1";

    /* ------------------------------------------------------------------ */
    /*  sampling & maths                                                  */
    /* ------------------------------------------------------------------ */

    private static final int SAMPLE_SIZE = 100;      // sliding‑window length

    /** thread‑safe buffer of recent TPS samples                         */
    private final Deque<Double> samples = new ConcurrentLinkedDeque<>();

    /** number read directly by the render thread                        */
    private volatile double displayTps = 20.0;

    // ── state for S03‑based sampling ──────────────────────────────────────
    private long lastWorldTime       = -1;
    private long lastTimeUpdateNanos = -1;

    // ── state for S00‑based fallback sampling ────────────────────────────
    private long   lastKeepAliveNanos        = -1;
    /** learned “normal” keep‑alive interval for this connection (ns) */
    private Double keepAliveBaseIntervalNanos = null;

    /**
     * Called for every S03PacketTimeUpdate that advances the day‑cycle.
     * Calculates an instantaneous TPS value using tick‑delta ÷ real‑time.
     */
    private void recordTimeUpdate(long worldTime) {
        final long now = System.nanoTime();

        if (lastWorldTime != -1 && worldTime > lastWorldTime) {          // ignore frozen daylight‑cycle
            long   ticksAdvanced = worldTime - lastWorldTime;            // normally 20
            long   nanosElapsed  = now - lastTimeUpdateNanos;
            if (nanosElapsed > 0L) {
                double tps = ticksAdvanced * 1_000_000_000D / nanosElapsed;
                addSample(tps);
            }
        }
        lastWorldTime       = worldTime;
        lastTimeUpdateNanos = now;
    }

    /**
     * Called for every S00PacketKeepAlive (arrives once per server tick or
     * once per second, depending on the fork/config).   We derive an estimated
     * TPS by comparing the observed interval with the *ideal* 50 ms tick.
     */
private void recordKeepAlive() {
    final long now = System.nanoTime();

    if (lastKeepAliveNanos != -1) {
        long nanosElapsed = now - lastKeepAliveNanos;

        // ‑‑ learn or slowly re‑learn the server’s usual cadence
        if (keepAliveBaseIntervalNanos == null) {
            keepAliveBaseIntervalNanos = (double) nanosElapsed;         // first sample
        } else {
            // low‑pass filter: 90 % old + 10 % new, so big spikes don’t distort the baseline
            keepAliveBaseIntervalNanos =
                    keepAliveBaseIntervalNanos * 0.9 + nanosElapsed * 0.1;
        }

        // TPS = (ideal 20 TPS) × (baseline / observed interval)
        double tps = keepAliveBaseIntervalNanos / nanosElapsed * 20D;
        addSample(tps);
    }

    lastKeepAliveNanos = now;
}


    /** Common aggregation path for both sampling strategies. */
    private void addSample(double tps) {
        if (tps > 20D) tps = 20D;                          // hard cap – vanilla max
        samples.addLast(tps);
        while (samples.size() > SAMPLE_SIZE) samples.pollFirst();
        displayTps = samples.stream().mapToDouble(d -> d).average().orElse(20D);
    }

    /* ------------------------------------------------------------------ */
    /*  Forge lifecycle                                                   */
    /* ------------------------------------------------------------------ */

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(this);           // HUD overlay
        MinecraftForge.EVENT_BUS.register(new NetHook());  // packet taps
    }

    /* ------------------------------------------------------------------ */
    /*  HUD overlay                                                       */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Text evt) {
        String text = String.format("TPS: %.2f", displayTps);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, 2, 2, 0xFFFFFF);
    }

/* ------------------------------------------------------------------ */
/*  Netty injection                                                   */
/* ------------------------------------------------------------------ */

private final class NetHook {

    /** Install the handler once for each new connection               */
    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent evt) {
        attach(evt.manager);
    }

    private void attach(NetworkManager mgr) {
        if (mgr == null || mgr.channel() == null) return;
        if (mgr.channel().pipeline().get("tpsoverlay") != null) return; // already there

        // ── ❶ RESET our learned keep‑alive cadence for this connection ─────────
        TPSOverlayMod.this.keepAliveBaseIntervalNanos = null;
        TPSOverlayMod.this.lastKeepAliveNanos        = -1;
        // ───────────────────────────────────────────────────────────────────────

        mgr.channel().pipeline().addAfter(
                "packet_handler",
                "tpsoverlay",
                new SimpleChannelInboundHandler<Packet<?>>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> pkt) throws Exception {
                        if (pkt instanceof S03PacketTimeUpdate) {
                            long worldTime = ((S03PacketTimeUpdate) pkt).getWorldTime();
                            recordTimeUpdate(worldTime);
                        } else if (pkt instanceof S00PacketKeepAlive) {
                            recordKeepAlive();
                        }
                        ctx.fireChannelRead(pkt); // keep Forge happy
                    }
                });
    }
}

}
