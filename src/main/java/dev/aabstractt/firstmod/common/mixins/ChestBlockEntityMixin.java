package dev.aabstractt.firstmod.common.mixins;

import dev.aabstractt.firstmod.common.ChestBlockEntityUsable;
import dev.aabstractt.firstmod.server.ExampleModServer;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin implements ChestBlockEntityUsable, ChestBlockEntityAccessor {

    private boolean used = false;

    public boolean isUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Inject(at = @At("RETURN"), method = "writeNbt")
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (!this.used) {
            return;
        }

        nbt.putBoolean("used", true);
    }

    @Inject(at = @At("RETURN"), method = "readNbt")
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        System.out.println("read nbt");

        try {
            this.used = nbt.getBoolean("used");
        } catch (Exception e) {
            ExampleModServer.LOGGER.error("Error reading NBT", e);
        }
    }
}