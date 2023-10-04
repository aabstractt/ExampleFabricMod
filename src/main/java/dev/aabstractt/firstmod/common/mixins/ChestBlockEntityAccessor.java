package dev.aabstractt.firstmod.common.mixins;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChestBlockEntity.class)
public interface ChestBlockEntityAccessor {

    @Invoker("writeNbt")
    void writeNbt(NbtCompound nbt);

    @Invoker("readNbt")
    void readNbt(NbtCompound nbt);
}