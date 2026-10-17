package dev.mehbur.garipseyler.network;

import dev.mehbur.garipseyler.GaripSeylerMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client → server: the telekinesis key was pressed. The server decides whether that grabs or throws.
 */
public record TelekinesisPayload() implements CustomPacketPayload {
	public static final TelekinesisPayload INSTANCE = new TelekinesisPayload();
	public static final CustomPacketPayload.Type<TelekinesisPayload> TYPE = new CustomPacketPayload.Type<>(GaripSeylerMod.id("telekinesis"));
	public static final StreamCodec<ByteBuf, TelekinesisPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public CustomPacketPayload.Type<TelekinesisPayload> type() {
		return TYPE;
	}
}
