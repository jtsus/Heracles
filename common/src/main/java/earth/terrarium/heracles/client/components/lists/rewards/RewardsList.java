package earth.terrarium.heracles.client.components.lists.rewards;

import earth.terrarium.heracles.api.client.DisplayWidget;
import earth.terrarium.heracles.api.rewards.QuestReward;
import earth.terrarium.heracles.api.rewards.client.QuestRewardWidgets;
import earth.terrarium.heracles.client.ui.UIConstants;
import earth.terrarium.heracles.client.components.lists.HeadingListEntry;
import earth.terrarium.heracles.client.components.lists.ListEntry;
import earth.terrarium.heracles.client.components.lists.QuestList;
import earth.terrarium.heracles.client.components.lists.QuestValueEntry;
import earth.terrarium.heracles.client.handlers.ClientQuests;
import earth.terrarium.heracles.common.menus.quest.QuestContent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RewardsList extends QuestList<QuestReward<?>> {

    private static final ListEntry<QuestReward<?>> LOCKED = new HeadingListEntry<>(Component.translatable("quest.heracles.locked"), UIConstants.LOCKED_HEADING_LEFT, UIConstants.LOCKED_HEADING_RIGHT);
    private static final ListEntry<QuestReward<?>> AVAILABLE = new HeadingListEntry<>(Component.translatable("quest.heracles.available"), UIConstants.CLAIMABLE_HEADING_LEFT, UIConstants.CLAIMABLE_HEADING_RIGHT);
    private static final ListEntry<QuestReward<?>> CLAIMED = new HeadingListEntry<>(Component.translatable("quest.heracles.claimed"), UIConstants.CLAIMED_HEADING_LEFT, UIConstants.CLAIMED_HEADING_RIGHT);

    public RewardsList(@Nullable QuestList<QuestReward<?>> list, int width, int height, QuestContent content) {
        super(list, width, height, content);
    }

    public RewardsList(int width, int height, QuestContent content) {
        super(width, height, content);
    }

    @Override
    public ListEntry<QuestReward<?>> create(QuestReward<?> reward, DisplayWidget widget) {
        return new QuestValueEntry<>(reward, widget);
    }

    @Override
    public void update(String group) {
        this.clear();
        collect(this.content(), true).forEach(this::add);
    }

    /** Reward rows without Locked/Available/Claimed headings. */
    public static List<ListEntry<QuestReward<?>>> modalEntries(QuestContent content) {
        return collect(content, false);
    }

    private static List<ListEntry<QuestReward<?>>> collect(QuestContent content, boolean headings) {
        List<ListEntry<QuestReward<?>>> out = new ArrayList<>();
        ClientQuests.QuestEntry entry = ClientQuests.get(content.id()).orElse(null);
        if (entry == null) {
            return out;
        }

        List<ListEntry<QuestReward<?>>> locked = new ArrayList<>();
        List<ListEntry<QuestReward<?>>> available = new ArrayList<>();
        List<ListEntry<QuestReward<?>>> claimed = new ArrayList<>();

        for (var reward : entry.value().rewards().values()) {
            DisplayWidget widget = QuestRewardWidgets.create(reward);
            if (widget == null) continue;
            ListEntry<QuestReward<?>> rewardsEntry = new QuestValueEntry<>(reward, widget);
            if (content.progress().canClaim(reward.id())) {
                available.add(rewardsEntry);
            } else if (content.progress().isComplete()) {
                claimed.add(rewardsEntry);
            } else {
                locked.add(rewardsEntry);
            }
        }

        if (headings && !locked.isEmpty()) {
            out.add(LOCKED);
        }
        out.addAll(locked);
        if (headings && !available.isEmpty()) {
            out.add(AVAILABLE);
        }
        out.addAll(available);
        if (headings && !claimed.isEmpty()) {
            out.add(CLAIMED);
        }
        out.addAll(claimed);
        return out;
    }
}
