package dev.hez.meowsense.module.modules.player;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.input.EventMovementInput;
import dev.hez.meowsense.event.impl.network.EventPacket;
import dev.hez.meowsense.event.impl.player.EventSilentRotation;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.event.impl.render.EventRender2D;
import dev.hez.meowsense.event.types.TransferOrder;
import dev.hez.meowsense.mixin.accesors.KeyBindingAccessor;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.other.Disabler;
import dev.hez.meowsense.module.modules.player.scaffold.tower.ConstantMotionTower;
import dev.hez.meowsense.module.modules.player.scaffold.tower.NormalTower;
import dev.hez.meowsense.module.modules.player.scaffold.tower.VulcanTower;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;
import dev.hez.meowsense.module.setting.impl.ModeSetting;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;
import dev.hez.meowsense.utils.math.MathUtils;
import dev.hez.meowsense.utils.math.RandomUtil;
import dev.hez.meowsense.utils.mc.*;
import dev.hez.meowsense.utils.rotation.RotationUtils;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.UUID;

import lombok.Getter;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {
    public static final ModeSetting rotMode = new ModeSetting("Rotation Mode", "Normal", "Normal", "Grim 1.17", "Back", "Sideways", "StaticSideways", "None", "Godbridge");

    public static final BooleanSetting sprint = new BooleanSetting("Sprint", false);
    public static final ModeSetting sprintMode = new ModeSetting("Sprint Mode", "Normal", "Normal", "NoPacket", "Watchdog Slow", "oNCP");
    public static final BooleanSetting keepY = new BooleanSetting("KeepY", false);
    public static final ModeSetting keepYMode = new ModeSetting("KeepY Mode", "Normal", "Normal", "Watchdog");
    public static final BooleanSetting keepYLowHop = new BooleanSetting("KeepY LowHop", false);
    public static final BooleanSetting tower = new BooleanSetting("Tower", false);

    public final NewModeSetting towerMode = new NewModeSetting("Tower Mode", "Normal",
            new NormalTower("Normal", this),
            new ConstantMotionTower("Constant Motion", this),
            new VulcanTower("Vulcan", this)
    );

    public static final BooleanSetting towerSprint = new BooleanSetting("Tower Sprint", false);

    public static final NumberSetting constantMotionValue = new NumberSetting("ConstantMotion", 0.1, 1, 0.42f, 0.01);
    public static final NumberSetting constantMotionJumpGroundValue = new NumberSetting("ConstantMotionJump", 0.76, 1, 0.79, 0.01);

    public static final BooleanSetting extraCps = new BooleanSetting("Extra CPS", false);
    public static final BooleanSetting telly = new BooleanSetting("Telly", false);
    public static final BooleanSetting modernWatchdog = new BooleanSetting("ModernWatchdog", false);
    public static final BooleanSetting andromeda = new BooleanSetting("Andromeda", false);
    public static final BooleanSetting eagle = new BooleanSetting("Eagle", false);
    public static final NumberSetting eagleEveryXBlocks = new NumberSetting("Sneak every X blocks", 1, 10, 1, 1);
    public static final BooleanSetting raycat = new BooleanSetting("Raycast", false);
    public static final BooleanSetting strictRaycast = new BooleanSetting("Strict Raycast", false);
    public static final BooleanSetting supastrictraycast = new BooleanSetting("Supa Strict Raycast", false);
    public static final BooleanSetting blockCounter = new BooleanSetting("Block Counter", true);

    public Scaffold() {
        super("Scaffold", "Bridges for you", 0, ModuleCategory.PLAYER);
        this.addSettings(rotMode, sprint, sprintMode, keepY, keepYMode, keepYLowHop, tower, towerMode, towerSprint, constantMotionValue, constantMotionJumpGroundValue, extraCps, telly, modernWatchdog, andromeda, eagle, eagleEveryXBlocks, raycat, strictRaycast, supastrictraycast, blockCounter);
        sprintMode.addDependency(sprint, true);
        keepYMode.addDependency(keepY, true);
        keepYLowHop.addDependency(keepY, true);
        keepYLowHop.addDependency(keepYMode, "Watchdog");
        towerMode.addDependency(tower, true);
        towerSprint.addDependency(tower, true);

        strictRaycast.addDependency(raycat, true);
        supastrictraycast.addDependency(raycat, true);

        constantMotionValue.addDependency(towerMode, "Constant Motion");
        constantMotionJumpGroundValue.addDependency(towerMode, "Constant Motion");

        constantMotionValue.addDependency(tower, true);
        constantMotionJumpGroundValue.addDependency(tower, true);

        eagleEveryXBlocks.addDependency(eagle, true);
        modernWatchdog.addDependency(telly, true);

    }

    public BlockData blockData;
    private float[] rotations;
    public int actualBlocksPlaced;

    public int scaffYCoord;
    public int lastyPlaced;

    // hypixel keepy
    public int blocksPlaced;
    private int hypixelKeepYDelay;
    private boolean hypixelSprint;

    private boolean rotated = false;
    private static final UUID SPEED_BOOST_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    
    @Override
    public void onDisable() {
        if (eagle.getValue()) {
            setShift(false);
        }
        super.onDisable();
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        blockData = null;
        rotations = new float[]{mc.player.getYaw(), mc.player.getPitch()};
        scaffYCoord = (int) (mc.player.getPos().y - 1);
        blocksPlaced = 0;
        hypixelKeepYDelay = 0;
        lastyPlaced = 0;
        hypixelSprint = false;

        if (sprint.getValue()) {
            switch (sprintMode.getMode()) {
                case "NoPacket":
                    PacketUtils.sendPacketSilently(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                    break;
                case "oNCP":
                    mc.player.setSprinting(true);
                    break;

                default:
                    mc.player.setSprinting(false);
            }
        } else {
            mc.player.setSprinting(false);
        }

        actualBlocksPlaced = 0;
        rotated = false;
        changeBlockSlot();
        super.onEnable();
    }

    @EventLink
    public final Listener<EventRender2D> eventRender2DListener = event -> {
        if (isNull() || !blockCounter.getValue()) {
            return;
        }

        // Get block count from inventory
        int blockCount = getBlockCount();
        
        if (blockCount <= 0) {
            return;
        }

        String counterText = String.valueOf(blockCount);
        
        // Get screen dimensions
        int screenWidth = event.getWidth();
        int screenHeight = event.getHeight();
        
        // Calculate center position under crosshair
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        // Position the counter under the crosshair
        int textWidth = mc.textRenderer.getWidth(counterText);
        int textX = centerX - textWidth / 2;
        int textY = centerY + 20; // 20 pixels below crosshair
        
        // Render background
        // event.getContext().fill(textX - 4, textY - 2, textX + textWidth + 4, textY + mc.textRenderer.fontHeight + 2, 0x80000000);
        
        // Render text with color based on block count
        int color = getBlockCountColor(blockCount);
        event.getContext().drawTextWithShadow(mc.textRenderer, counterText, textX, textY, color);
    };

    private int getBlockCount() {
        if (mc.player == null) {
            return 0;
        }
        
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && InventoryUtils.isBlockPlaceable(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int getBlockCountColor(int count) {
        if (count > 128) {
            return 0xFF55FF55; // Green
        } else if (count > 64) {
            return 0xFFFFFF55; // Yellow
        } else if (count > 32) {
            return 0xFFFF8855; // Orange
        } else {
            return 0xFFFF5555; // Red
        }
    }

    @EventLink
    public final Listener<EventPacket> eventPacketListener = event -> {
        if (isNull() || !(mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem)) {
            return;
        }

        if (event.getOrder() == TransferOrder.SEND) {
            if (sprint.getValue()) {
                if (sprintMode.isMode("NoPacket")) {
                    if (event.getPacket() instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket) event.getPacket()).getEntityId() == mc.player.getId()) {
                        if (((ClientCommandC2SPacket) event.getPacket()).getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                            event.cancel();
                        }
                    }
                }
            }
        }
    };

    @EventLink
    public final Listener<EventMovementInput> eventMovementInputListener = event -> {
        if (isNull() || !(mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem)) {
            return;
        }

        if (blockData == null) {
            return;
        }

        if (!rotated) {
            return;
        }

        if (MoveUtils.isMoving2()) {
            if (telly.getValue() || andromeda.getValue()) {
                if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed() && mc.player.hurtTime == 0 && MoveUtils.isMoving2()) {
                    event.setJumping(andromeda.getValue() ? !mc.world.getBlockState(mc.player.getBlockPos().up().up()).isAir() : true);
                }
            } else {
                if (keepY.getValue() && keepYMode.isMode("Normal")) {
                    if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                        event.setJumping(true);
                    }
                }
                if (keepY.getValue() && keepYMode.isMode("Watchdog") && MoveUtils.isMoving2() && blocksPlaced >= 1) {
                    if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                        event.setJumping(true);
                        hypixelKeepYDelay = 3;
                    }
                }
            }
        }
    };

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            blockData = null;
            rotations = null;
            scaffYCoord = 0;
            blocksPlaced = 0;
            hypixelSprint = false;
            return;
        }

        if (hypixelKeepYDelay > 0) {
            hypixelKeepYDelay--;
        }

        if (eagle.getValue()) {
            if (mc.options.jumpKey.isPressed()) {
                setShift(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.sneakKey.boundKey.getCode()));
            } else {
                setShift((PlayerUtil.playerOverAir() && mc.player.isOnGround() || PlayerUtil.playerOverAir()) && actualBlocksPlaced % eagleEveryXBlocks.getValueInt() == 0);
            }
        }

        changeBlockSlot();

        if (!(mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem)) {
            return;
        }

        updateScaffoldYCoord();
        updateBlockPos();

        setSprint();

        if (blockData == null) {
            return;
        }

        if (keepY.getValue() && keepYMode.isMode("Watchdog") && MoveUtils.isMoving2() && blocksPlaced >= 1) {
            if (keepYLowHop.getValue() && Client.INSTANCE.getModuleManager().getModule(Disabler.class).isEnabled() && Client.INSTANCE.getModuleManager().getModule(Disabler.class).watchdogMotion.getValue() && Client.INSTANCE.getModuleManager().getModule(Disabler.class).canLowHop) {
                switch (PlayerUtil.inAirTicks()) {
                    case 3 -> mc.player.getVelocity().y -= 0.0025;
                    case 4 -> mc.player.getVelocity().y -= 0.04;
                    case 5 -> mc.player.getVelocity().y -= 0.1905189780583944;
                    case 6 -> mc.player.getVelocity().multiply(0.1);
                }
            }
        }

        place();
    };

    private void setShift(boolean sh) {
        KeyBinding.setKeyPressed(mc.options.sneakKey.boundKey, sh);
    }

    @EventLink
    public final Listener<EventSilentRotation> eventSilentRotationListener = event -> {
        if (isNull()) {
            return;
        }

        if (!(mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem)) {
            return;
        }

        if (blockData == null) {
            return;
        }

        if (rotMode.isMode("None") || rotMode.isMode("Grim 1.17")) {
            rotated = true;
            return;
        }

        if (doTellyShit() || doModernWatchdogTellyShit()) {
            return;
        }

        if (rotations == null) {
            return;
        }

        event.setSpeed(180f);
        event.setYaw(rotations[0]);
        event.setPitch(rotations[1]);

        rotated = true;
    };

    private void updateScaffoldYCoord() {
        if (keepY.getValue()) {
            if (keepYMode.isMode("Normal")) {
                if (!mc.options.jumpKey.isPressed()) {
                    if (mc.player.isOnGround()) {
                        scaffYCoord = (int) (mc.player.getPos().y - 1);
                    }
                } else {
                    scaffYCoord = (int) (mc.player.getPos().y - 1);
                }
            }
            if (keepYMode.isMode("Watchdog")) {
                if (mc.options.jumpKey.isPressed() || mc.player.getPos().y < scaffYCoord) {
                    scaffYCoord = (int) (mc.player.getPos().y - 1);
                    blocksPlaced = 0;
                    hypixelSprint = false;
                }
            }

        } else {
            if (andromeda.getValue()) {
                if (keepYMode.isMode("Normal")) {
                    if (!mc.options.jumpKey.isPressed()) {
                        if (mc.player.isOnGround()) {
                            scaffYCoord = (int) (mc.player.getPos().y - 1);
                        }
                    } else {
                        scaffYCoord = (int) (mc.player.getPos().y - 1);
                    }
                }
            } else {
                scaffYCoord = (int) (mc.player.getPos().y - 1);
            }
        }
    }

    private void changeBlockSlot() {
        if (mc.player != null) {
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && InventoryUtils.isBlockPlaceable(stack) && stack.getCount() >= 3) {
                    mc.player.getInventory().selectedSlot = i;
                    break;
                }
            }
        }
    }

    private void updateBlockPos() {
        if (ScaffoldUtils.getBlockData() != null) {
            blockData = ScaffoldUtils.getBlockData();
        }
    }

   private void updateRots() {
       if (rotMode.isMode("None")) {
           return;
       }

       if (hypixelKeepYDelay > 0) {
           rotations = new float[]{(float) (mc.player.getYaw() - getRandom() * 5), MathUtils.clamp_float((float) (mc.player.getPitch() - getRandom() * 5), -90, 90)};
           return;
       }

       if (blockData != null) {
           if (rotMode.isMode("Normal") || rotMode.isMode("Grim 1.17")) {
               getRotations();
           }

           if (rotMode.isMode("Back")) {
               rotations = new float[]{mc.player.getYaw() + 180, 83.8f};
           }

           if (rotMode.isMode("StaticSideways")) {
               float yaw = 90;
               float pitch = 90;
           }

           if (rotMode.isMode("Sideways")) {
               float yaw = mc.player.getYaw() + 90;
               float pitch = 90;
               rotations = new float[]{yaw, pitch};
           }
       }
   }

    public void getRotations() {
        if (RayTraceUtils.isLookingAtBlock(blockData.getFacing(), blockData.getPosition(), true, 5, rotations[0], rotations[1])) {
            return;
        }

        boolean found = false;

        for (float possibleYaw = mc.player.getYaw() - 180; possibleYaw <= mc.player.getYaw() + 360 - 180 && !found; possibleYaw += 45) {
            for (float possiblePitch = 90; possiblePitch > 30 && !found; possiblePitch -= possiblePitch > (mc.player.hasStatusEffect(StatusEffects.SPEED) ? 60 : 80) ? 1 : 10) {
                if (RayTraceUtils.isLookingAtBlock(blockData.getFacing(), blockData.getPosition(), true, 5, possibleYaw, possiblePitch)) {
                    this.rotations[0] = possibleYaw;
                    this.rotations[1] = possiblePitch;
                    found = true;
                }
            }
        }

        if (found) {
            return;
        }

        final float[] rotations = RotationUtils.getRotationToBlock(blockData.getPosition(), blockData.getFacing());

        if (RayTraceUtils.isLookingAtBlock(blockData.getFacing(), blockData.getPosition(), true, 5, rotations[0], rotations[1])) {
            this.rotations[0] = rotations[0];
            this.rotations[1] = rotations[1];
        }
    }


    private void place() {
        doHypixelPlace();

        if (ScaffoldUtils.getBlockData() == null) {
            if (extraCps.getValue()) {
                if (Math.random() > 0.5) {
                    BlockHitResult blockHitResult = RayTraceUtils.rayTrace(4.5f, rotations[0], rotations[1]);

                    if (blockHitResult != null && blockHitResult.getBlockPos() != null) {
                        PacketUtils.sendSequencedPacketSilently(it -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, it));
                    }
                }
            }

            if (andromeda.getValue()) {
                if (ScaffoldUtils.getBlockData(0, 3, 0) != null) {
                    blockData = ScaffoldUtils.getBlockData(0, 3, 0);
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        updateRots();

        if (hypixelKeepYDelay > 0) {
            return;
        }

        if (doTellyShit()) {
            return;
        }

        boolean STOP = false;

        if (raycat.getValue()) {
            if (supastrictraycast.getValue()) {
                STOP = true;
                HitResult hitResult = mc.crosshairTarget;
                if (hitResult == null) {
                    return;
                }
                if (hitResult.getType() != HitResult.Type.BLOCK) {
                    return;
                }

                BlockHitResult blockHitResult = (BlockHitResult) hitResult;

                if (!(blockHitResult.getBlockPos().getX() == blockData.getPosition().getX() && blockHitResult.getBlockPos().getY() == blockData.getPosition().getY() && blockHitResult.getBlockPos().getZ() == blockData.getPosition().getZ())) {
                    return;
                }

                if (!(blockHitResult.getSide() == blockData.getFacing())) {
                    return;
                }

                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(blockHitResult.getPos(), blockHitResult.getSide(), blockHitResult.getBlockPos(), false));

                mc.player.swingHand(Hand.MAIN_HAND);

                lastyPlaced = blockData.getPosition().getY();
                blocksPlaced++;
                actualBlocksPlaced++;
            } else {
                BlockHitResult blockHitResult = RayTraceUtils.rayTrace(4.5f, rotations[0], rotations[1]);
                if (blockHitResult == null) {
                    STOP = true;
                }
                if (!(blockHitResult.getBlockPos().getX() == blockData.getPosition().getX() && blockHitResult.getBlockPos().getY() == blockData.getPosition().getY() && blockHitResult.getBlockPos().getZ() == blockData.getPosition().getZ())) {
                    STOP = true;
                }
                if (strictRaycast.getValue()) {
                    if (blockHitResult.getSide() != blockData.getFacing()) {
                        STOP = true;
                    }
                }
            }
        }

        if (STOP) {
            return;
        }

        if (rotMode.isMode("Grim 1.17"))
            PacketUtils.sendPacketSilently(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), rotations[0], rotations[1], mc.player.isOnGround()));

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(ScaffoldUtils.getNewVector(blockData), blockData.getFacing(), blockData.getPosition(), false));

        mc.player.swingHand(Hand.MAIN_HAND);

        if (rotMode.isMode("Grim 1.17"))
            PacketUtils.sendPacketSilently(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));

        lastyPlaced = blockData.getPosition().getY();
        actualBlocksPlaced++;
        blocksPlaced++;
    }

    private void doHypixelPlace() {
        if (doTellyShit()) {
            return;
        }

        // Only place extra block for Watchdog mode, not Washdog
        if (keepY.getValue() && keepYMode.isMode("Watchdog") && MoveUtils.isMoving2() && blocksPlaced >= 1) {
            if (mc.player.getVelocity().y + mc.player.getPos().y < scaffYCoord + 2.0 && mc.player.getVelocity().y < -0.15 && !mc.options.jumpKey.isPressed()) {
                BlockData blockData1 = ScaffoldUtils.getBlockData(0, 1, 0);
                if (blockData1 != null) {
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(ScaffoldUtils.getNewVector(blockData1), blockData1.getFacing(), blockData1.getPosition(), false));
                    mc.player.swingHand(Hand.MAIN_HAND);
                    hypixelSprint = true;
                }
            }
        }
        
        // ModernWatchdog: Telly-like behavior that keeps same Y level and only goes forward for about 2 ticks
        if (telly.getValue() && modernWatchdog.getValue() && MoveUtils.isMoving2()) {
            int airTicks = PlayerUtil.inAirTicks();
            boolean modernWatchdogCondition = airTicks <= 2; // Limited to 2 ticks as requested
            
            // Check if we're moving forward (not sideways or backwards)
            boolean movingForward = mc.player.input.movementForward > 0.8f && Math.abs(mc.player.input.movementSideways) < 0.1f;
            
            // Keep same Y level by maintaining scaffold Y coordinate
            if (modernWatchdogCondition && movingForward) {
                // Maintain the same Y level as when we started scaffolding
                double targetY = scaffYCoord + 1.0;
                if (Math.abs(mc.player.getY() - targetY) > 0.1) {
                    // Gently adjust Y position to maintain level
                    double yDiff = targetY - mc.player.getY();
                    mc.player.getVelocity().y = Math.max(-0.1, Math.min(0.1, yDiff * 0.5));
                }
            }
        }
    }

    private void setSprint() {
        if (canTower()) {
            mc.player.setSprinting(towerSprint.getValue());
            KeyBinding.setKeyPressed(((KeyBindingAccessor) mc.options.sprintKey).getBoundKey(), towerSprint.getValue());
        } else if (keepY.getValue() && keepYMode.isMode("Watchdog")) {
            mc.player.setSprinting(hypixelSprint);
            KeyBinding.setKeyPressed(((KeyBindingAccessor) mc.options.sprintKey).getBoundKey(), hypixelSprint);
        } else if (sprint.getValue()) {
            doNormalSprint();
        } else {
            mc.player.setSprinting(false);
            KeyBinding.setKeyPressed(((KeyBindingAccessor) mc.options.sprintKey).getBoundKey(), false);
        }
    }

    private void doNormalSprint() {
        if (sprint.getValue()) {
            switch (sprintMode.getMode()) {
                case "Normal", "NoPacket":
                    mc.player.setSprinting(true);
                    KeyBinding.setKeyPressed(((KeyBindingAccessor) mc.options.sprintKey).getBoundKey(), true);
                    break;
                case "Watchdog Slow":
                    mc.player.setSprinting(false);
                    KeyBinding.setKeyPressed(((KeyBindingAccessor) mc.options.sprintKey).getBoundKey(), false);

                    if (MoveUtils.isMoving()) {
                        mc.player.getVelocity().x *= 0.96f;
                        mc.player.getVelocity().z *= 0.96f;
                    }
                    break;
            }
        }
    }

    private boolean doTellyShit() {
        if (!MoveUtils.isMoving2() || mc.player.hurtTime != 0) {
            return false;
        } else if (!keepY.getValue() || mc.options.jumpKey.isPressed()) {
            return telly.getValue() && PlayerUtil.inAirTicks() <= 3;
        } else {
            return telly.getValue() && PlayerUtil.inAirTicks() <= 5;
        }
    }

    private boolean doModernWatchdogTellyShit() {
        if (!MoveUtils.isMoving2() || mc.player.hurtTime != 0) {
            return false;
        }
        
        // ModernWatchdog telly-like behavior: prevent rotations when in air for more than 2 ticks
        if (telly.getValue() && modernWatchdog.getValue()) {
            return PlayerUtil.inAirTicks() > 2;
        }
        
        return false;
    }

    public double getRandom() {
        return RandomUtil.randomBetween(-90, 90) / 100.0;
    }

    public boolean canTower() {
        return tower.getValue() && mc.options.jumpKey.isPressed() && !mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem && blockData != null;
    }

    @Getter
    public static class BlockData {
        private BlockPos position;
        private Direction facing;

        public BlockData(final BlockPos position, final Direction facing) {
            this.position = position;
            this.facing = facing;
        }
    }
}