package dev.hez.meowsense.utils.render.notifications;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.event.impl.render.EventRender2D;
import dev.hez.meowsense.module.modules.client.Notifications;
import dev.hez.meowsense.utils.Utils;
import dev.hez.meowsense.utils.render.notifications.impl.Notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements Utils {

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        notifications.removeIf(Notification::shouldDisappear);
    };

    @EventLink
    public final Listener<EventRender2D> eventRender2DListener = event -> {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (!Client.INSTANCE.getModuleManager().getModule(Notifications.class).isEnabled()) {
            return;
        }
        int yOffset = 0;
        for (Notification notification : notifications) {
            notification.render(event, yOffset);
            yOffset += 17;
        }
    };

    public void addNewNotification(Notification notification) {
        notifications.add(notification);
    }
}
