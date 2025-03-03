package dev.hez.meowsense.module.modules.other;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.network.EventPacket;
import dev.hez.meowsense.event.impl.player.EventMotionPre;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.event.impl.world.EventWorldChange;
import dev.hez.meowsense.mixin.accesors.CommonPongC2SPacketAccessor;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.movement.Speed;
import dev.hez.meowsense.module.modules.player.Scaffold;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.utils.mc.DelayData;
import dev.hez.meowsense.utils.mc.PacketUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;
import dev.hez.meowsense.utils.render.notifications.impl.Notification;
import dev.hez.meowsense.utils.render.notifications.impl.NotificationMoode;
import lombok.Getter;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class Disabler extends Module {

    public static final BooleanSetting sprint = new BooleanSetting("Sprinting", false);
    public static final BooleanSetting vulcanScaffold = new BooleanSetting("Vulcan scaffold", false);
    public static final BooleanSetting verusCombat = new BooleanSetting("Verus combat", false);
    public static final BooleanSetting pingSpoof = new BooleanSetting("Ping Spoof", false);
    public static final NumberSetting pingSpoofDelay = new NumberSetting("Ping Spoof Delay", 0, 10000, 2500, 10);
    public static final BooleanSetting watchdogMotion = new BooleanSetting("Watchdog motion", false);
    public static final BooleanSetting balance = new BooleanSetting("Balance", false);
    public static final BooleanSetting cancelC00 = new BooleanSetting("Miniblox (WIP)", false); // New setting
    public static final BooleanSetting cubecraftDisabler = new BooleanSetting("Cubecraft Disabler", false); // New setting for Cubecraft Disabler

    public Disabler() {
        super("Disabler", "Disables anticheats", 0, ModuleCategory.OTHER);
        this.addSettings(sprint, vulcanScaffold, verusCombat, pingSpoof, pingSpoofDelay, watchdogMotion, balance, cancelC00, cubecraftDisabler); // Add cancelC00 and cubecraftDisabler
        pingSpoofDelay.addDependency(pingSpoof, true);
    }

    // Verus Reach
    private boolean verusbool = false;

    // PingSpoof
    private final Set<DelayData> packetQueue = new LinkedHashSet<>();

    // Vulcan
    private boolean co = false;

    // Watchdog motion
    private boolean jump;
    private boolean disabled;
    public boolean canLowHop;
    private int testTicks;

    // Test
    private boolean cancelReceive = false;
    public final ArrayList<Packet<?>> receivePackets = new ArrayList<>();

    // Cubecraft Disabler
    private final CubecraftDisabler cubecraftDisablerInstance = new CubecraftDisabler();

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }

        if (vulcanScaffold.getValue()) {
            if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) {
                if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).actualBlocksPlaced % 8 == 0) {
                    if (!co) {
                        PacketUtils.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                        co = true;
                    }
                } else if (co) {
                    PacketUtils.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    co = false;
                }
            } else if (co) {
                PacketUtils.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                co = false;
            }
        }

        if (pingSpoof.getValue()) {
            if (mc.isIntegratedServerRunning()) {
                setEnabled(false);
                return;
            }
            sendPacketsByOrder(false);
        }

        // Cubecraft Disabler tick logic
        if (cubecraftDisabler.getValue()) {
            cubecraftDisablerInstance.onTick(event);
        }
    };

    @EventLink
    public final Listener<EventPacket> eventPacketListener = event -> {
        if (isNull()) {
            return;
        }
        Packet<?> packet = event.getPacket();

        // Cancel C00 packet
        if (cancelC00.getValue() && packet instanceof KeepAliveC2SPacket) {
            event.cancel();
            System.out.println("Cancelled C00 packet: " + ((KeepAliveC2SPacket) packet).getId());

            // Send /resync command
            if (mc.player != null) {
                mc.player.networkHandler.sendChatCommand("resync"); // Send /resync command
            }
        }

        if (balance.getValue()) {
            if (packet instanceof PlayerMoveC2SPacket c03) {
                if (!c03.changeLook && !c03.changePosition) {
                    event.cancel();
                }
            }
        }

        if (watchdogMotion.getValue()) {
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
                testTicks++;
                if (testTicks == 35) {
                    disabled = false;
                    testTicks = 0;

                    canLowHop = true;
                    mc.player.jump();
                    Client.INSTANCE.getNotificationManager().addNewNotification(new Notification("Disabled motion checks", 2000, NotificationMoode.INFORMATION));
                }
            }
        }

        if (sprint.getValue()) {
            if (event.getPacket() instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket) event.getPacket()).getEntityId() == mc.player.getId()) {
                if (((ClientCommandC2SPacket) event.getPacket()).getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING || ((ClientCommandC2SPacket) event.getPacket()).getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
                    event.cancel();
                }
            }
        }

        if (verusCombat.getValue()) {
            if (mc.player.age <= 20) {
                return;
            }
            if (packet instanceof CommonPingS2CPacket pingS2CPacket) {
                pingS2CPacket.parameter = verusbool ? -1 : 1;
                verusbool = !verusbool;
            }
        }

        if (pingSpoof.getValue()) {
            if (mc.player.isDead() || event.isCancelled()) {
                return;
            }

            if (packet instanceof KeepAliveS2CPacket || packet instanceof CommonPingS2CPacket) {
                event.cancel();

                synchronized (packetQueue) {
                    packetQueue.add(new DelayData(packet, System.currentTimeMillis()));
                }
            }
        }

        // Cubecraft Disabler packet logic
        if (cubecraftDisabler.getValue()) {
            cubecraftDisablerInstance.onSendPacket(event);
        }
    };

    @EventLink
    public final Listener<EventMotionPre> eventMotionPreListener = event -> {
        if (isNull()) {
            return;
        }
        if (watchdogMotion.getValue()) {
            if (mc.player.age > 25) {
                if (mc.player.isOnGround() && jump) {
                    jump = false;
                    disabled = true;
                    mc.player.jump();
                } else if (disabled && PlayerUtil.inAirTicks() >= 10) {
                    if (PlayerUtil.inAirTicks() % 2 == 0) {
                        event.onGround = false;
                        event.x += 0.095f;
                    }

                    if (Client.INSTANCE.getModuleManager().getModule(Speed.class).isEnabled()) {
                        Client.INSTANCE.getModuleManager().getModule(Speed.class).setEnabled(false);
                    }

                    mc.player.getVelocity().x = mc.player.getVelocity().z = mc.player.getVelocity().y = 0;
                }
            }
        }

        // Cubecraft Disabler motion logic
        if (cubecraftDisabler.getValue()) {
            cubecraftDisablerInstance.onMotion(event);
        }
    };

    @Override
    public void onEnable() {
        testTicks = 0;
        disabled = false;
        jump = true;
        canLowHop = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (pingSpoof.getValue()) {
            reset();
        }
        co = false;
    }

    @EventLink
    public final Listener<EventWorldChange> eventWorldChangeListener = event -> {
        if (pingSpoof.getValue()) {
            if (event.getWorld() == null) {
                synchronized (packetQueue) {
                    packetQueue.clear();
                }
            }
        }

        if (watchdogMotion.getValue()) {
            jump = true;
            disabled = false;

            canLowHop = false;
        }

        // Cubecraft Disabler world change logic
        if (cubecraftDisabler.getValue()) {
            cubecraftDisablerInstance.onWorld(event);
        }
    };

    private void sendPacketsByOrder(boolean all) {
        synchronized (packetQueue) {
            packetQueue.removeIf(data -> {
                if (all || data.getDelay() <= System.currentTimeMillis() - pingSpoofDelay.getValue()) {
                    PacketUtils.handlePacket(data.getPacket());
                    return true;
                }
                return false;
            });
        }
    }

    private void reset() {
        sendPacketsByOrder(true);
    }

    // Inner class for Cubecraft Disabler
    private class CubecraftDisabler {
        public static int godTick = 0;

        public void onTick(EventTickPre event) {
            // Implement tick logic here
        }

        public void onMotion(EventMotionPre event) {
            if (mc.player == null || mc.player.age <= 20) return;

            if (godTick == 0 && mc.player.age % 80 == 0)
                Client.INSTANCE.getNotificationManager().addNewNotification(new Notification("Disabler hook failed", 2000, NotificationMoode.INFORMATION));
        }

        public void onSendPacket(EventPacket event) {
            if (mc.player == null || mc.player.age <= 20) return;

            Packet<?> packet = event.getPacket();

            if (godTick == 1) {
                Client.INSTANCE.getNotificationManager().addNewNotification(new Notification("Sentinel bypasses has activated", 2000, NotificationMoode.INFORMATION));
                godTick++;
            }

            if (godTick >= 3) {
                if (packet instanceof PlayerInputC2SPacket) {
                    event.cancel();
                    PacketUtils.sendPacket(new PlayerInputC2SPacket(0.0F, 0.0F, false, false));
                }
                if ((godTick >= 328 || godTick <= 5) && packet instanceof PlayerMoveC2SPacket)
                    event.cancel();
                else if (godTick % 55 == 0)
                    PacketUtils.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }

            if (packet instanceof CommonPongC2SPacket) {
                int parameter = ((CommonPongC2SPacket) packet).getParameter();
                if (parameter < 0 && (godTick >= 3 || godTick == 0)) {
                    godTick++;
                    PacketUtils.sendPacket(new CommonPongC2SPacket(parameter + 1));
                } else if (parameter != 0) {
                    event.cancel();
                    if (godTick >= 330 || godTick == 2) {
                        PacketUtils.sendPacket(new CommonPongC2SPacket(parameter + 3));
                        godTick = 3;
                    }
                }
            }
        }

        public void onWorld(EventWorldChange event) {
            godTick = 0;
        }
    }
}