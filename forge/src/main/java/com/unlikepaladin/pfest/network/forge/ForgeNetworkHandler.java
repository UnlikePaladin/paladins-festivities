package com.unlikepaladin.pfest.network.forge;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.network.ServerPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ForgeNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PaladinFestivitiesMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, CarvePacket.class, CarvePacket::encode, CarvePacket::decode, CarvePacket::handle);
        CHANNEL.registerMessage(id++, OccupiedPacket.class, OccupiedPacket::encode, OccupiedPacket::decode, OccupiedPacket::handle);
        CHANNEL.registerMessage(id++, FinishCarvePacket.class, FinishCarvePacket::encode, FinishCarvePacket::decode, FinishCarvePacket::handle);
        CHANNEL.registerMessage(id++, RotationPacket.class, RotationPacket::encode, RotationPacket::decode, RotationPacket::handle);
    }

    public static class CarvePacket {
        private final BlockPos pos;
        private final int faceIndex;
        private final byte[] faceData;

        public CarvePacket(BlockPos pos, int faceIndex, byte[] faceData) {
            this.pos = pos;
            this.faceIndex = faceIndex;
            this.faceData = faceData;
        }

        public static void encode(CarvePacket msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeVarInt(msg.faceIndex);
            buf.writeByteArray(msg.faceData);
        }

        public static CarvePacket decode(FriendlyByteBuf buf) {
            return new CarvePacket(buf.readBlockPos(), buf.readVarInt(), buf.readByteArray());
        }

        public static void handle(CarvePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    ServerPacketHandler.handleCarve(player, msg.pos, msg.faceIndex, msg.faceData);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class OccupiedPacket {
        private final BlockPos pos;
        private final boolean occupied;

        public OccupiedPacket(BlockPos pos, boolean occupied) {
            this.pos = pos;
            this.occupied = occupied;
        }

        public static void encode(OccupiedPacket msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeBoolean(msg.occupied);
        }

        public static OccupiedPacket decode(FriendlyByteBuf buf) {
            return new OccupiedPacket(buf.readBlockPos(), buf.readBoolean());
        }

        public static void handle(OccupiedPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    ServerPacketHandler.handleOccupied(player, msg.pos, msg.occupied);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class FinishCarvePacket {
        private final BlockPos pos;

        public FinishCarvePacket(BlockPos pos) {
            this.pos = pos;
        }

        public static void encode(FinishCarvePacket msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
        }

        public static FinishCarvePacket decode(FriendlyByteBuf buf) {
            return new FinishCarvePacket(buf.readBlockPos());
        }

        public static void handle(FinishCarvePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    ServerPacketHandler.handleFinishCarve(player, msg.pos);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class RotationPacket {
        private final BlockPos pos;
        private final float angle;

        public RotationPacket(BlockPos pos, float angle) {
            this.pos = pos;
            this.angle = angle;
        }

        public static void encode(RotationPacket msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeFloat(msg.angle);
        }

        public static RotationPacket decode(FriendlyByteBuf buf) {
            return new RotationPacket(buf.readBlockPos(), buf.readFloat());
        }

        public static void handle(RotationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    ServerPacketHandler.handleRotation(player, msg.pos, msg.angle);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
