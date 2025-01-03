package com.notishi28.better_pearls.mixin;

import com.notishi28.better_pearls.PearlConfig;
import net.minecraft.world.item.EnderpearlItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EnderpearlItem.class)
public class EnderPearlItemMixin {
    @ModifyConstant(method = "use", constant = @Constant(intValue = 20))
    private int modifyCooldown(int original) {
        return PearlConfig.getInstance().getPearlCooldown();
    }
}