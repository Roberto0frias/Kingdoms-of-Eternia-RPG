package dev.eternia.ui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.eternia.core.EterniaMod;
import dev.eternia.core.network.ActivateSkillPayload;
import dev.eternia.core.network.ReleaseSkillPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;

public final class EterniaClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(EterniaMod.id("main"));
    private static final KeyMapping OPEN_CHARACTER = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.eternia.open_character", InputConstants.Type.KEYSYM, InputConstants.KEY_P, CATEGORY));
    private static final KeyMapping ACTIVATE_SKILL = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.eternia.activate_skill", InputConstants.Type.KEYSYM, InputConstants.KEY_R, CATEGORY));
    private static boolean skillWasDown;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CHARACTER.consumeClick()) {
                if (client.player != null) {
                    client.gui.setScreen(new CharacterCreationScreen());
                }
            }
            boolean skillDown = client.player != null && client.gui.screen() == null && ACTIVATE_SKILL.isDown();
            if (client.getConnection() != null && skillDown != skillWasDown) {
                if (skillDown) {
                    ClientPlayNetworking.send(new ActivateSkillPayload(0));
                    if (client.player != null) client.player.swing(InteractionHand.MAIN_HAND);
                } else {
                    ClientPlayNetworking.send(new ReleaseSkillPayload());
                }
            }
            skillWasDown = skillDown;
        });
    }
}
