package dev.hez.meowsense.mixin.game;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.impl.input.EventHandleInput;
import dev.hez.meowsense.event.impl.player.EventTickPost;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.event.impl.world.EventWorldChange;
import dev.hez.meowsense.gui.CustomTitleScreen;
import dev.hez.meowsense.gui.clickgui.imgui.ImGuiImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.TitleScreen;

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
            ImGuiImpl.initialize(MinecraftClient.getInstance().getWindow().getHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void setWorldInject(ClientWorld world, CallbackInfo ci) {
        Client.INSTANCE.getEventManager().post(new EventWorldChange(world));
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void replaceTitleScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof TitleScreen && !(screen instanceof CustomTitleScreen)) {
            MinecraftClient.getInstance().setScreen(new CustomTitleScreen());
            ci.cancel();
        }
    }
}
