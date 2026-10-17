package dev.mehbur.garipseyler.network;

import dev.mehbur.garipseyler.GaripSeylerMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server → client: Vecna ({@code vecnaId}) takes over the player's mind for {@code ticks}: the camera is
 * dragged towards him and movement keys are reversed.
 */
public record VecnaMindControlPayload(int vecnaId, int ticks) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<VecnaMindControlPayload> TYPE = new CustomPacketPayload.Type<>(GaripSeylerMod.id("vecna_mind_control"));
	public static final StreamCodec<ByteBuf, VecnaMindControlPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, VecnaMindControlPayload::vecnaId,
			ByteBufCodecs.VAR_INT, VecnaMindControlPayload::ticks,
			VecnaMindControlPayload::new);

	@Override
	public CustomPacketPayload.Type<VecnaMindControlPayload> type() {
		return TYPE;
	}
}
