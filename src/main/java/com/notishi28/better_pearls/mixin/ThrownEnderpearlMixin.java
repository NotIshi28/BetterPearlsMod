package com.notishi28.better_pearls.mixin;

import com.notishi28.better_pearls.PearlConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {

    @Unique private static final double BOUNCE_DAMPING = 0.7;
    
    @Unique private static final double MIN_VELOCITY = 0.2;
    
    @Unique private static final double WALL_BOUNCE_DAMPING = 0.8;
    
    @Unique private static final double FLUID_BOUNCE_DAMPING = 0.9;

    
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
        if (PearlConfig.getInstance().getCanRide() && self.getOwner() instanceof Player player) {
            player.startRiding(self, true);
        }
    }

    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
        Level level = self.level();
        Vec3 velocity = self.getDeltaMovement();

        if (velocity.lengthSqr() <= MIN_VELOCITY * MIN_VELOCITY) return;

        BlockPos pos = self.blockPosition();
        FluidState fluidState = level.getFluidState(pos);

        if (!fluidState.isEmpty() && PearlConfig.getInstance().getWaterBounce()) {
            level.playSound(null, self.getX(), self.getY(), self.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 2.0F, 1.0F);

            Vec3 newVelocity = new Vec3(
                    velocity.x * FLUID_BOUNCE_DAMPING,
                    -velocity.y * FLUID_BOUNCE_DAMPING,
                    velocity.z * FLUID_BOUNCE_DAMPING
            );

            self.setDeltaMovement(newVelocity);
            if (newVelocity.lengthSqr() <= MIN_VELOCITY * MIN_VELOCITY && self.getFirstPassenger() instanceof Player) {
                self.getFirstPassenger().stopRiding();
            }
        }
    }

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    
    private void onHit(HitResult hitResult, CallbackInfo ci) {
    
        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHit) {
            ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
            Level level = self.level();
            Vec3 velocity = self.getDeltaMovement();

            if (velocity.lengthSqr() <= MIN_VELOCITY * MIN_VELOCITY) return;

            Direction hitDirection = blockHit.getDirection();
            FluidState fluidState = level.getFluidState(blockHit.getBlockPos());
            boolean hitFluid = !fluidState.isEmpty();

            level.playSound(null, self.getX(), self.getY(), self.getZ(),
                    hitFluid ? SoundEvents.PLAYER_SPLASH : SoundEvents.SLIME_SQUISH,
                    SoundSource.PLAYERS, 2.0F, 1.0F);

            Vec3 newVelocity = switch (hitDirection.getAxis()) {
                case Y -> new Vec3(velocity.x, -velocity.y * (hitFluid ? FLUID_BOUNCE_DAMPING : BOUNCE_DAMPING), velocity.z);
                case Z -> new Vec3(velocity.x, velocity.y, -velocity.z * (hitFluid ? FLUID_BOUNCE_DAMPING : WALL_BOUNCE_DAMPING));
                case X -> new Vec3(-velocity.x * (hitFluid ? FLUID_BOUNCE_DAMPING : WALL_BOUNCE_DAMPING), velocity.y, velocity.z);
            };

            self.setDeltaMovement(newVelocity);
            if (newVelocity.lengthSqr() <= MIN_VELOCITY * MIN_VELOCITY && self.getFirstPassenger() instanceof Player) {
                self.getFirstPassenger().stopRiding();
            }

            ci.cancel();
        }
    }
}