package dev.mehbur.garipseyler.network;

import dev.mehbur.garipseyler.GaripSeylerMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server → client: "a red lightning bolt strikes at (x, floorY, z), reaching {@code height} blocks up to the ceiling".
 */
public record RedLightningPayload(double x, double floorY, double z, float height) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RedLightningPayload> TYPE = new CustomPacketPayload.Type<>(GaripSeylerMod.id("red_lightning"));
	public static final StreamCodec<ByteBuf, RedLightningPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, RedLightningPayload::x,
			ByteBufCodecs.DOUBLE, RedLightningPayload::floorY,
			ByteBufCodecs.DOUBLE, RedLightningPayload::z,
			ByteBufCodecs.FLOAT, RedLightningPayload::height,
			RedLightningPayload::new);

	@Override
	public CustomPacketPayload.Type<RedLightningPayload> type() {
		return TYPE;
	}
}
