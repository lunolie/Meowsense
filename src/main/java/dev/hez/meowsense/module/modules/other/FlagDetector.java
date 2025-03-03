package dev.hez.meowsense.module.modules.other;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.network.EventPacket;
import dev.hez.meowsense.event.types.TransferOrder;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.utils.render.notifications.impl.Notification;
import dev.hez.meowsense.utils.render.notifications.impl.NotificationMoode;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class FlagDetector extends Module {
    public FlagDetector() {
        super("FlagDetector", "Detects flags", 0, ModuleCategory.OTHER);
    }

    @EventLink
    public final Listener<EventPacket> eventPacketListener = event -> {
        if (isNull()) {
            return;
        }
        if (event.getOrder() == TransferOrder.RECEIVE) {
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
                Client.INSTANCE.getNotificationManager().addNewNotification(new Notification("You flagged", 1000, NotificationMoode.WARNING));
            }
        }
    };
}
