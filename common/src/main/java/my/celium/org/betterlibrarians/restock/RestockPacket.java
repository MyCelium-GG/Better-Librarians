package my.celium.org.betterlibrarians.restock;

import java.util.ArrayList;
import java.util.List;

import my.celium.org.betterlibrarians.BetterLibrariansMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server to client, once a second: the stock of the villagers near the player. */
public record RestockPacket(List<RestockInfo> villagers) implements CustomPacketPayload {
    public static final int MAX = 48;
    public static final Type<RestockPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(BetterLibrariansMod.ID, "restock"));
    public static final StreamCodec<FriendlyByteBuf, RestockPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.villagers.size());
                for (RestockInfo info : packet.villagers) {
                    info.write(buf);
                }
            },
            buf -> {
                int size = Math.min(MAX, buf.readVarInt());
                List<RestockInfo> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(RestockInfo.read(buf));
                }
                return new RestockPacket(list);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
