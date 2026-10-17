package dev.mehbur.garipseyler.network;

import dev.mehbur.garipseyler.GaripSeylerMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server → client: "Vecna's vision is on you for {@code ticks}" — the client pulses a red vignette.
 */
public record VecnaVisionPayload(int ticks) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<VecnaVisionPayload> TYPE = new CustomPacketPayload.Type<>(GaripSeylerMod.id("vecna_vision"));
	public static final StreamCodec<ByteBuf, VecnaVisionPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, VecnaVisionPayload::ticks,
			VecnaVisionPayload::new);

	@Override
	public CustomPacketPayload.Type<VecnaVisionPayload> type() {
		return TYPE;
	}
}
