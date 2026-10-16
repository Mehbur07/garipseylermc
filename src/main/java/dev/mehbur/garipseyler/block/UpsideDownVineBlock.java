package dev.mehbur.garipseyler.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Upside Down vine: clings to block faces like glow lichen. Placed with {@code age=0} by
 * {@link VineGrowth}, it grows through three stages (sprout → young → full) on scheduled ticks,
 * which gives the emerging animation. It never spreads on its own.
 */
public class UpsideDownVineBlock extends MultifaceBlock {
	public static final MapCodec<UpsideDownVineBlock> CODEC = simpleCodec(UpsideDownVineBlock::new);
	public static final int MAX_AGE = 2;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
	/** Ticks between growth stages. */
	public static final int GROWTH_STEP_TICKS = 5;

	public UpsideDownVineBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(AGE, 0));
	}

	@Override
	public MapCodec<UpsideDownVineBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AGE);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age < MAX_AGE) {
			level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
			if (age + 1 < MAX_AGE) {
				level.scheduleTick(pos, this, GROWTH_STEP_TICKS);
			}
		}
	}
}
