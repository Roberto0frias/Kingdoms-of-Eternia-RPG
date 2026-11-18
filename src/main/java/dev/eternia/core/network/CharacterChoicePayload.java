package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CharacterChoicePayload(String raceId, String classId) implements CustomPacketPayload {
    public static final Type<CharacterChoicePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EterniaMod.MOD_ID, "character_choice"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterChoicePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CharacterChoicePayload::raceId,
            ByteBufCodecs.STRING_UTF8, CharacterChoicePayload::classId,
            CharacterChoicePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
