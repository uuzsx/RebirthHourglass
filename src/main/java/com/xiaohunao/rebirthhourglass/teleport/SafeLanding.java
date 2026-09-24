package com.xiaohunao.rebirthhourglass.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public final class SafeLanding {
    private SafeLanding() {}

    public static Optional<Vec3> find(ServerLevel level, ServerPlayer player, Vec3 death, int radius) {
        BlockPos center = BlockPos.containing(death);
        int baseY = Math.clamp(center.getY(), level.getMinY() + 1, level.getMaxY() - 2);
        for (int ring = 0; ring <= radius; ring++) {
            for (int dy = 0; dy <= 16; dy++) {
                for (int sign = 0; sign < (dy == 0 ? 1 : 2); sign++) {
                    int y = baseY + (sign == 0 ? dy : -dy);
                    for (int dx = -ring; dx <= ring; dx++) {
                        for (int dz = -ring; dz <= ring; dz++) {
                            if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                            BlockPos feet = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                            if (safe(level, player, feet)) return Optional.of(Vec3.atBottomCenterOf(feet));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean safe(ServerLevel level, ServerPlayer player, BlockPos feet) {
        if (feet.getY() <= level.getMinY() || feet.getY() + 2 > level.getMaxY()
                || !level.getWorldBorder().isWithinBounds(feet)) return false;
        level.getChunk(feet.getX() >> 4, feet.getZ() >> 4);
        BlockPos floor = feet.below();
        if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) return false;
        for (BlockPos pos : new BlockPos[]{floor, feet, feet.above()}) {
            if (!level.getFluidState(pos).isEmpty() || hazardous(level.getBlockState(pos))) return false;
        }
        var dimensions = player.getDimensions(Pose.STANDING);
        double half = dimensions.width() / 2.0;
        AABB box = new AABB(feet.getX() + .5 - half, feet.getY(), feet.getZ() + .5 - half,
                feet.getX() + .5 + half, feet.getY() + dimensions.height(), feet.getZ() + .5 + half);
        return level.getWorldBorder().isWithinBounds(box) && level.noCollision(player, box) && !level.containsAnyLiquid(box);
    }

    private static boolean hazardous(BlockState state) {
        return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.CACTUS) || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE)
                || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.POINTED_DRIPSTONE);
    }
}
