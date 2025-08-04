package dev.hez.meowsense.mixin.render;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.modules.combat.KillAura;
import dev.hez.meowsense.module.modules.render.Animations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void renderFirstPersonItemInject(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        final Animations module = Client.INSTANCE.getModuleManager().getModule(Animations.class);

        if (module.isEnabled()) {
            if (Hand.MAIN_HAND == hand && player.getMainHandStack().getItem() instanceof SwordItem) {
                matrices.translate(Animations.xOffset.getValue(), Animations.yOffset.getValue(), Animations.itemScale.getValue());
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(0));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0));
            }
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"), cancellable = true)
    private void renderIteminject(LivingEntity player, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        final Module module = Client.INSTANCE.getModuleManager().getModule(Animations.class);

        if (!module.isEnabled()) {
            return;
        }

        if (!(stack.getItem() instanceof ShieldItem)) {
            return;
        }

        ci.cancel();
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;",
            ordinal = 0
    ))
    private UseAction renderFirstPersonItemInject(ItemStack instance) {
        var item = instance.getItem();
        if (item instanceof SwordItem && (Client.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled() &&
                Client.INSTANCE.getModuleManager().getModule(KillAura.class).shouldRenderFakeAnim() || MinecraftClient.getInstance().options.useKey.isPressed()) && Client.INSTANCE.getModuleManager().getModule(Animations.class).isEnabled()) {
            return UseAction.BLOCK;
        }

        return instance.getUseAction();
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingItem()Z",
            ordinal = 1
    ))
    private boolean renderFirstPersonItemInject(AbstractClientPlayerEntity instance) {
        var item = instance.getMainHandStack().getItem();

        if (item instanceof SwordItem && (Client.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled() &&
                Client.INSTANCE.getModuleManager().getModule(KillAura.class).shouldRenderFakeAnim() || MinecraftClient.getInstance().options.useKey.isPressed()) && Client.INSTANCE.getModuleManager().getModule(Animations.class).isEnabled()) {
            return true;
        }

        return instance.isUsingItem();
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getActiveHand()Lnet/minecraft/util/Hand;",
            ordinal = 1
    ))
    private Hand renderFirstPersonItemInject2(AbstractClientPlayerEntity instance) {
        var item = instance.getMainHandStack().getItem();

        if (item instanceof SwordItem && (Client.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled() &&
                Client.INSTANCE.getModuleManager().getModule(KillAura.class).shouldRenderFakeAnim() || MinecraftClient.getInstance().options.useKey.isPressed()) && Client.INSTANCE.getModuleManager().getModule(Animations.class).isEnabled()) {
            return Hand.MAIN_HAND;
        }

        return instance.getActiveHand();
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getItemUseTimeLeft()I",
            ordinal = 2
    ))
    private int renderFirstPersonItemInject3(AbstractClientPlayerEntity instance) {
        var item = instance.getMainHandStack().getItem();

        if (item instanceof SwordItem && (Client.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled() &&
                Client.INSTANCE.getModuleManager().getModule(KillAura.class).shouldRenderFakeAnim() || MinecraftClient.getInstance().options.useKey.isPressed()) && Client.INSTANCE.getModuleManager().getModule(Animations.class).isEnabled()) {
            return 7200;
        }

        return instance.getItemUseTimeLeft();
    }

    @ModifyArg(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V",
            ordinal = 4
    ), index = 2)
    private float renderFirstPersonItemInject4(float equipProgress) {
        if (Client.INSTANCE.getModuleManager().getModule(Animations.class).isEnabled()) {
            return 0.0F;
        }
        return equipProgress;
    }

    @Inject(method = "renderFirstPersonItem",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2, shift = At.Shift.AFTER))
    private void renderFirstPersonItemInject5(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Client.INSTANCE.getModuleManager().getModule(Animations.class).isEnabled() && item.getItem() instanceof SwordItem) {
            final Arm arm = (hand == Hand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
            transform(matrices, arm, swingProgress);
        }
    }

    private void transform(MatrixStack matrices, Arm arm, float swingProgress) {
        if (Animations.mode.isMode("1.7")) {
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.1f);
            applySwingOffsetCustom(matrices, arm, swingProgress);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Exhibition")) { // Renamed from "Exhi" - gentle swing motion
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.0f);
            float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * g * 10.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -35.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Overhead")) { // Renamed from "Russian" - overhead blocking style
            matrices.translate(-0.02f, 0.05f, 0.0f);
            transformFirstPersonItem(matrices, swingProgress, 0.0f);
            doBlockTransformations(matrices);
            matrices.translate(-0.05f, 0.2f, 0.2f);
            float swingValue = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.of(new Vector3f(-8.0f, 0.0f, 9.0f)).rotationDegrees(-swingValue * 70.0f / 2.0f));
            matrices.multiply(RotationAxis.of(new Vector3f(1.0f, -0.4f, 0.0f)).rotationDegrees(-swingValue * 70.0f));
        }

        if (Animations.mode.isMode("Push")) { // Name fits - pushing motion
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.15f, -0.05f);
            float pushSwing = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pushSwing * -40.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * pushSwing * 25.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        // REMOVED "Smooth" - too similar to Exhibition

        if (Animations.mode.isMode("Spin")) { // Name fits - spinning motion
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.0f);
            float spinProgress = swingProgress * 2.0f;
            float spinAngle = spinProgress * 360.0f;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * spinAngle));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Wiggle")) { // Name fits - wiggling motion
            matrices.translate(arm == Arm.RIGHT ? -0.08f : 0.08f, 0.16f, 0.05f);
            float time = System.currentTimeMillis() * 0.01f;
            float wiggleX = MathHelper.sin(time) * 5.0f;
            float wiggleY = MathHelper.cos(time * 1.5f) * 3.0f;
            float swingWiggle = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(wiggleX + swingWiggle * -20.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (wiggleY + swingWiggle * 15.0f)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Stab")) { // Name fits - stabbing motion
            matrices.translate(arm == Arm.RIGHT ? -0.05f : 0.05f, 0.1f, -0.3f);
            float stabProgress = MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.translate(0.0f, 0.0f, stabProgress * 0.4f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(stabProgress * -60.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * stabProgress * 10.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(10.0f));
        }

        if (Animations.mode.isMode("Slash")) { // Name fits - slashing motion
            matrices.translate(arm == Arm.RIGHT ? -0.15f : 0.15f, 0.25f, 0.1f);
            float slashSwing = MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * slashSwing * 90.0f));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? -1 : 1) * slashSwing * 45.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(slashSwing * -30.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Helicopter")) { // Name fits - helicopter blade motion
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.0f);
            float heliTime = System.currentTimeMillis() * 0.02f;
            float heliRotation = (heliTime % 360.0f) * (swingProgress > 0.1f ? 10.0f : 1.0f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(heliRotation));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(10.0f));
        }

        if (Animations.mode.isMode("Bounce")) { // Name fits - bouncing motion
            float bounceHeight = Math.abs(MathHelper.sin(System.currentTimeMillis() * 0.008f)) * 0.15f;
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f + bounceHeight, 0.0f);
            float bounceSwing = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * bounceSwing * 30.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(bounceSwing * -25.0f + bounceHeight * 50.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Wave")) { // Name fits - wave-like motion
            matrices.translate(arm == Arm.RIGHT ? -0.12f : 0.12f, 0.18f, 0.05f);
            float waveTime = System.currentTimeMillis() * 0.005f;
            float wave1 = MathHelper.sin(waveTime) * 10.0f;
            float wave2 = MathHelper.sin(waveTime + 1.0f) * 8.0f;
            float wave3 = MathHelper.sin(waveTime + 2.0f) * 6.0f;
            float swingWave = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(wave1 + swingWave * -40.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (wave2 + swingWave * 20.0f)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * wave3));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Flip")) { // Name fits - flipping motion
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.0f);
            float flipProgress = swingProgress * 2.0f;
            if (flipProgress > 1.0f) flipProgress = 2.0f - flipProgress;
            float flipAngle = flipProgress * 180.0f;
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(flipAngle));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * flipProgress * 20.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Pulse")) { // Name fits - pulsing/scaling motion
            float pulseScale = 1.0f + MathHelper.sin(System.currentTimeMillis() * 0.015f) * 0.1f;
            float swingPulse = 1.0f + MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI) * 0.3f;
            matrices.scale(pulseScale * swingPulse, pulseScale * swingPulse, pulseScale * swingPulse);
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.0f);
            float pulseSwing = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * pulseSwing * 25.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pulseSwing * -35.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Shake")) { // Name fits - shaking motion
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.0f);
            float shakeIntensity = swingProgress * 8.0f;
            float shakeX = (float) (Math.random() - 0.5) * shakeIntensity;
            float shakeY = (float) (Math.random() - 0.5) * shakeIntensity;
            float shakeZ = (float) (Math.random() - 0.5) * shakeIntensity;
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(shakeX));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(shakeY));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(shakeZ));
            float baseSwing = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * baseSwing * 15.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(baseSwing * -25.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        if (Animations.mode.isMode("Windmill")) { // Name fits - windmill rotation
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.15f, 0.1f);
            float windmillTime = System.currentTimeMillis() * 0.01f;
            float windmillAngle = (windmillTime % 360.0f) * (1.0f + swingProgress * 3.0f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * windmillAngle));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45.0f));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * 30.0f));
        }

        if (Animations.mode.isMode("Figure8")) { // Name fits - figure-8 pattern
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.0f);
            float fig8Time = System.currentTimeMillis() * 0.008f + swingProgress * 5.0f;
            float fig8X = MathHelper.sin(fig8Time) * 20.0f;
            float fig8Y = MathHelper.sin(fig8Time * 2.0f) * 15.0f;
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(fig8Y));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * fig8X));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
        }

        // CLIENT-INSPIRED ANIMATIONS (keeping unique ones)
        if (Animations.mode.isMode("Classic")) { // Renamed from "Sigma" - classic smooth blocking
            matrices.translate(arm == Arm.RIGHT ? -0.08f : 0.08f, 0.25f, -0.1f);
            float classicSwing = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f + classicSwing * -15.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (25.0f + classicSwing * 10.0f)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (15.0f + classicSwing * 5.0f)));
        }

        if (Animations.mode.isMode("Float")) { // Renamed from "Flux" - floating motion
            matrices.translate(arm == Arm.RIGHT ? -0.05f : 0.05f, 0.3f, -0.15f);
            float floatTime = System.currentTimeMillis() * 0.003f;
            float floatSwing = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            float floatOscillation = MathHelper.sin(floatTime) * 3.0f;
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-95.0f + floatOscillation + floatSwing * -10.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (30.0f + floatSwing * 8.0f)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (20.0f + floatOscillation * 0.5f)));
        }

        // REMOVED several similar client animations (Astolfo, Novoline, Rise, Intave, Liquid, Tenacity, Kotlin, Avatar, Moon, Flush, Keystrokes)
        // as they were too similar to each other and the ones above

        if (Animations.mode.isMode("Zoom")) { // Name fits - zooming effect
            float zoomScale = 1.0f + MathHelper.sin(swingProgress * (float) Math.PI) * 0.4f;
            matrices.scale(zoomScale, zoomScale, zoomScale);
            matrices.translate(arm == Arm.RIGHT ? -0.09f : 0.09f, 0.17f, 0.02f);
            float zoomSwing = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-97.0f + zoomSwing * -21.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (23.0f + zoomSwing * 14.0f)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (73.0f + zoomSwing * 11.0f)));
        }

        if (Animations.mode.isMode("Jitter")) { // Name fits - high-frequency jittery motion
            matrices.translate(arm == Arm.RIGHT ? -0.08f : 0.08f, 0.21f, 0.01f);
            float jitterTime = System.currentTimeMillis() * 0.05f;
            float jitterX = MathHelper.sin(jitterTime) * 3.0f;
            float jitterY = MathHelper.sin(jitterTime * 1.3f) * 2.5f;
            float jitterZ = MathHelper.sin(jitterTime * 0.7f) * 2.0f;
            float jitterSwing = MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-95.0f + jitterX + jitterSwing * -17.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (27.0f + jitterY + jitterSwing * 15.0f)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (74.0f + jitterZ + jitterSwing * 8.0f)));
        }

        if (Animations.mode.isMode("Glitch")) { // Name fits - glitchy motion
            matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.2f, 0.0f);
            long glitchSeed = System.currentTimeMillis() / 100;
            float glitchX = ((glitchSeed * 1234567) % 100 - 50) * 0.2f;
            float glitchY = ((glitchSeed * 7654321) % 100 - 50) * 0.15f;
            float glitchZ = ((glitchSeed * 9876543) % 100 - 50) * 0.1f;
            float glitchSwing = MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-92.0f + glitchX + glitchSwing * -20.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (25.0f + glitchY + glitchSwing * 18.0f)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (69.0f + glitchZ + glitchSwing * 12.0f)));
        }

        if (Animations.mode.isMode("Swirl")) { // Name fits - swirling motion
            matrices.translate(arm == Arm.RIGHT ? -0.09f : 0.09f, 0.19f, 0.03f);
            float swirlTime = System.currentTimeMillis() * 0.006f;
            float swirlX = MathHelper.sin(swirlTime) * 12.0f;
            float swirlY = MathHelper.sin(swirlTime * 1.5f) * 8.0f;
            float swirlZ = MathHelper.sin(swirlTime * 0.8f) * 15.0f;
            float swirlSwing = MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-96.0f + swirlX + swirlSwing * -16.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (29.0f + swirlY + swirlSwing * 14.0f)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * (75.0f + swirlZ + swirlSwing * 10.0f)));
        }
    }
    // Helper method for first person item transformation (equivalent to transformFirstPersonItem call)
    private void transformFirstPersonItem(MatrixStack matrices, float swingProgress, float equipProgress) {
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g * 70.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(g * -20.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f * -20.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * -10.0f));
    }

    // Helper method for block transformations (equivalent to doBlockTransformations call)
    private void doBlockTransformations(MatrixStack matrices) {
        matrices.translate(-0.5f, 0.2f, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0f));
    }

    private void applySwingOffsetCustom(MatrixStack matrices, Arm arm, float swingProgress) {
        int armSide = (arm == Arm.RIGHT) ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide * (45.0f + f * -20.0f)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armSide * g * -20.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide * -45.0f));
    }
}