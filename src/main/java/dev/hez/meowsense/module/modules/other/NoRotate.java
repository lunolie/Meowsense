package dev.hez.meowsense.module.modules.other;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.network.EventPacket;
import dev.hez.meowsense.mixin.accesors.PlayerPositionLookS2CPacketAccessor;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.lang.reflect.Field;

public class NoRotate extends Module {

    public NoRotate() {
        super("NoRotate", "Prevents the server from rotating your head", 0, ModuleCategory.OTHER);
    }

    @EventLink
    public final Listener<EventPacket> eventPacketListener = event -> {
        if (isNull()) {
            return;
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            ((PlayerPositionLookS2CPacketAccessor) packet).setPitch(mc.player.getPitch());
            ((PlayerPositionLookS2CPacketAccessor) packet).setYaw(mc.player.getYaw());
        }
    };
}
