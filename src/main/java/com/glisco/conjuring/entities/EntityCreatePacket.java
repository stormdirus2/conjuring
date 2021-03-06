package com.glisco.conjuring.entities;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class EntityCreatePacket {

    public static final Identifier ID = new Identifier("conjuring", "entity_create");

    public static Packet<?> create(Entity e) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeVarInt(Registry.ENTITY_TYPE.getRawId(e.getType()));
        buffer.writeVarInt(e.getEntityId());
        buffer.writeUuid(e.getUuid());
        buffer.writeDouble(e.getX());
        buffer.writeDouble(e.getY());
        buffer.writeDouble(e.getZ());
        buffer.writeByte(MathHelper.floor(e.pitch * 256f / 360f));
        buffer.writeByte(MathHelper.floor(e.yaw * 256f / 360f));
        return ServerSidePacketRegistry.INSTANCE.toPacket(ID, buffer);
    }

    @Environment(EnvType.CLIENT)
    public static void onPacket(PacketContext context, PacketByteBuf buffer) {
        EntityType<?> entityType = Registry.ENTITY_TYPE.get(buffer.readVarInt());
        int entityID = buffer.readVarInt();
        UUID uuid = buffer.readUuid();
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        float pitch = (buffer.readByte() * 360f) / 256f;
        float yaw = (buffer.readByte() * 360f) / 256f;

        ClientWorld world = MinecraftClient.getInstance().world;
        Entity e = entityType.create(world);

        context.getTaskQueue().execute(() -> {
            if (e != null) {
                e.setEntityId(entityID);
                e.setUuid(uuid);
                e.updatePosition(x, y, z);
                e.updateTrackedPosition(x, y, z);
                e.pitch = pitch;
                e.yaw = yaw;

                world.addEntity(entityID, e);

            }
        });
    }

}
