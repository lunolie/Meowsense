package dev.hez.meowsense.mixin.game;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.impl.input.EventHandleInput;
import dev.hez.meowsense.event.impl.player.EventTickPost;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.event.impl.world.EventWorldChange;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo info) {
        Client.INSTANCE.getEventManager().post(new EventTickPre());
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onPostTick(CallbackInfo info) {
        Client.INSTANCE.getEventManager().post(new EventTickPost());
    }

    /**
     * @author my aunt
     * @reason cool
     */

    @Overwrite
    private String getWindowTitle() {
        return "Meowsense | " + Client.version;
    }

    @Inject(method = "handleInputEvents", at = @At(value = "HEAD"))
    private void onHandleInputEvents(CallbackInfo info) {
        Client.INSTANCE.getEventManager().post(new EventHandleInput());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    void postWindowInit(RunArgs args, CallbackInfo ci) {
        try {
            Client.INSTANCE.getFontManager().initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void setWorldInject(ClientWorld world, CallbackInfo ci) {
        Client.INSTANCE.getEventManager().post(new EventWorldChange(world));
    }
}
