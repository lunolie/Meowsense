package dev.hez.meowsense.utils.mc;

import dev.hez.meowsense.utils.Utils;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class KeyboardUtil implements Utils {
    public static boolean isPressedOnKeyboard(KeyBinding keyBinding) {
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBinding.boundKey.getCode());
    }
}
