package dev.hez.meowsense.module.modules.ghost;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.Module;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BlockItem;

public class FastPlace extends Module {

    public FastPlace() {
        super("FastPlace", "Removes block placement delay for blocks only", 0, ModuleCategory.GHOST);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (mc.player == null || mc.world == null) return;

        ItemStack heldItem = mc.player.getMainHandStack();

        if (isBlock(heldItem)) {
            mc.itemUseCooldown = 0;
        }
    };

    private boolean isBlock(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() instanceof BlockItem;
    }
}