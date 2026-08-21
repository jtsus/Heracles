package earth.terrarium.heracles.api.rewards.client.defaults;

import com.teamresourceful.resourcefullib.client.scissor.ScissorBoxStack;
import com.teamresourceful.resourcefullib.client.screens.CursorScreen;
import com.teamresourceful.resourcefullib.client.utils.CursorUtils;
import earth.terrarium.heracles.api.client.ItemDisplayWidget;
import earth.terrarium.heracles.api.client.WidgetUtils;
import earth.terrarium.heracles.api.quests.QuestIcon;
import earth.terrarium.heracles.client.widgets.buttons.ThemedButton;
import earth.terrarium.heracles.common.constants.ConstantComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface BaseItemRewardWidget extends ItemDisplayWidget {

    int ACTION_HEIGHT = 16;
    int ACTION_PAD = 6;
    int COLLECT_TEXT = 0xFF111111;
    int LOCKED_TEXT = 0xFF6A6A6A;

    QuestIcon<?> getIconOverride();

    ItemStack getIcon();

    boolean canClaim();

    void claimReward();

    boolean isInteractive();

    @Override
    default ItemStack getCurrentItem() {
        return getIcon();
    }

    default boolean isClaimed() {
        return false;
    }

    static Component actionLabel(boolean canClaim, boolean claimed) {
        if (canClaim) {
            return ConstantComponents.Rewards.COLLECT;
        }
        if (claimed) {
            return ConstantComponents.Rewards.COLLECTED;
        }
        return ConstantComponents.Rewards.LOCKED;
    }

    default Component actionLabel() {
        return actionLabel(canClaim(), isClaimed());
    }

    default int actionButtonWidth() {
        Font font = Minecraft.getInstance().font;
        return Math.max(52, font.width(actionLabel()) + 14);
    }

    @Override
    default void render(GuiGraphics graphics, ScissorBoxStack scissor, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        int iconSize = 32;
        if (!getIconOverride().render(graphics, x + 5, y + 5, iconSize, iconSize)) {
            WidgetUtils.drawItemIconWithTooltip(graphics, getIcon(), x + 5, y + 5, iconSize, this::getTooltip, mouseX, mouseY);
        }
        if (isInteractive()) {
            Font font = Minecraft.getInstance().font;
            Component label = actionLabel();
            int buttonWidth = actionButtonWidth();
            int buttonX = x + width - buttonWidth - ACTION_PAD;
            int buttonY = y + 13;
            boolean buttonHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth
                && mouseY >= buttonY && mouseY < buttonY + ACTION_HEIGHT;
            if (canClaim()) {
                graphics.blitSprite(buttonHovered ? ThemedButton.SPRITE_COMPLETABLE_HOVERED : ThemedButton.SPRITE_COMPLETABLE, buttonX, buttonY, buttonWidth, ACTION_HEIGHT);
            } else {
                graphics.blitSprite(ThemedButton.SPRITE_DISABLED, buttonX, buttonY, buttonWidth, ACTION_HEIGHT);
            }
            int textX = buttonX + (buttonWidth - font.width(label)) / 2;
            int textY = buttonY + (ACTION_HEIGHT - font.lineHeight) / 2;
            graphics.drawString(font, label, textX, textY, canClaim() ? COLLECT_TEXT : LOCKED_TEXT, false);
            if (buttonHovered) {
                CursorUtils.setCursor(true, canClaim() ? CursorScreen.Cursor.POINTER : CursorScreen.Cursor.DISABLED);
            }
        }
    }

    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int mouseButton, int width) {
        if (ItemDisplayWidget.super.mouseClicked(mouseX, mouseY, mouseButton, width)) return true;
        if (isInteractive()) {
            int buttonWidth = actionButtonWidth();
            int buttonX = width - buttonWidth - ACTION_PAD;
            int buttonY = 13;
            boolean buttonHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth
                && mouseY >= buttonY && mouseY < buttonY + ACTION_HEIGHT;
            if (buttonHovered && canClaim()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                claimReward();
                return true;
            }
        }
        return false;
    }

    @Override
    default int getHeight(int width) {
        return 42;
    }

    default List<Component> getTooltip() {
        return Screen.getTooltipFromItem(Minecraft.getInstance(), getIcon());
    }
}
