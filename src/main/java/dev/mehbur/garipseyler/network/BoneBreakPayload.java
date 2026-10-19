package dev.mehbur.garipseyler.network;

import dev.mehbur.garipseyler.GaripSeylerMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client → server: the bone-break key was pressed. The server checks the glove tier, target and cooldown.
 */
public record BoneBreakPayload() implements CustomPacketPayload {
	public static final BoneBreakPayload INSTANCE = new BoneBreakPayload();
	public static final CustomPacketPayload.Type<BoneBreakPayload> TYPE = new CustomPacketPayload.Type<>(GaripSeylerMod.id("bone_break"));
	public static final StreamCodec<ByteBuf, BoneBreakPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public CustomPacketPayload.Type<BoneBreakPayload> type() {
		return TYPE;
	}
}
