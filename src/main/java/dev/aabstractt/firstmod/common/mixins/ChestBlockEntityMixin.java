package dev.aabstractt.firstmod.common.mixins;

import dev.aabstractt.firstmod.common.ChestBlockEntityUsable;
import lombok.Data;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class) @Data
public final class ChestBlockEntityMixin implements ChestBlockEntityUsable {

    private boolean used = false;

    @Inject(at = @At("HEAD"), method = "writeNbt")
    protected void injectWriteMethod(NbtCompound nbt, CallbackInfo ci) {
        if (!this.used) {
            return;
        }

        nbt.putBoolean("used", true);
    }

    @Inject(at = @At("HEAD"), method = "readNbt")
    protected void injectReadMethod(NbtCompound nbt, CallbackInfo ci) {
        this.used = nbt.contains("used");
    }
}