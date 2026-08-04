package org.telegram.ui;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.GhostModeController;
import org.telegram.messenger.AnonymousForwardController;
import org.telegram.messenger.LocalPremiumController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;

public class GhostModeActivity extends BaseFragment {

    private UniversalRecyclerView listView;

    private static final int GHOST_MODE = 1;
    private static final int HIDE_READ = 2;
    private static final int HIDE_TYPING = 3;
    private static final int ANTI_DELETE = 4;
    private static final int ANTI_EDIT = 5;
    private static final int SCREENSHOT_BYPASS = 6;
    private static final int ANONYMOUS_FORWARD = 7;
    private static final int LOCAL_PREMIUM = 8;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("InfoGram Settings");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, null);
        listView.adapter.setApplyBackground(false);
        listView.setPadding(0, AndroidUtilities.statusBarHeight + AndroidUtilities.dp(12), 0, AndroidUtilities.navigationBarHeight + AndroidUtilities.dp(12));
        listView.setClipToPadding(false);

        return listView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("InfoGram Ghost Mode"));

        UItem ghostMode = UItem.asExpandableSwitch(GHOST_MODE, "Ghost Mode",
                "Hides your online status. Others will not see when you are online or were last seen recently.");
        ghostMode.checked = GhostModeController.isEnabled();
        items.add(ghostMode);

        UItem hideRead = UItem.asExpandableSwitch(HIDE_READ, "Hide Read Receipts",
                "Prevents the server from knowing you have read messages. Others won't see blue ticks.");
        hideRead.checked = GhostModeController.isHideReadReceipts();
        items.add(hideRead);

        UItem hideTyping = UItem.asExpandableSwitch(HIDE_TYPING, "Hide Typing Status",
                "Hides the typing indicator so others won't see when you are typing a message.");
        hideTyping.checked = GhostModeController.isHideTypingStatus();
        items.add(hideTyping);

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Anti-Recall & Anti-Edit"));

        UItem antiDelete = UItem.asExpandableSwitch(ANTI_DELETE, "Anti-Delete",
                "Saves deleted messages locally so you can still see them even after the sender removes them.");
        antiDelete.checked = GhostModeController.isAntiDelete();
        items.add(antiDelete);

        UItem antiEdit = UItem.asExpandableSwitch(ANTI_EDIT, "Anti-Edit",
                "Keeps a history of edited messages so you can see the original version before edits.");
        antiEdit.checked = GhostModeController.isAntiEdit();
        items.add(antiEdit);

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Privacy Tweaks"));

        UItem screenshotBypass = UItem.asExpandableSwitch(SCREENSHOT_BYPASS, "Screenshot Bypass",
                "Allows screenshots even in secret chats, media viewers, and other protected screens.");
        screenshotBypass.checked = GhostModeController.isScreenshotBypassEnabled();
        items.add(screenshotBypass);

        UItem anonForward = UItem.asExpandableSwitch(ANONYMOUS_FORWARD, "Anonymous Forward",
                "Strips forward headers when forwarding messages so the original source is hidden.");
        anonForward.checked = GhostModeController.isAnonymousForwardEnabled();
        items.add(anonForward);

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Premium Bypass"));

        UItem localPremium = UItem.asExpandableSwitch(LOCAL_PREMIUM, "Local Premium",
                "Bypasses client-side premium restrictions like larger uploads, premium stickers, and more.");
        localPremium.checked = LocalPremiumController.isLocalPremiumEnabled();
        items.add(localPremium);
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case GHOST_MODE:
                item.checked = !item.checked;
                GhostModeController.setEnabled(item.checked);
                listView.adapter.update(true);
                break;
            case HIDE_READ:
                item.checked = !item.checked;
                GhostModeController.setHideReadReceipts(item.checked);
                listView.adapter.update(true);
                break;
            case HIDE_TYPING:
                item.checked = !item.checked;
                GhostModeController.setHideTypingStatus(item.checked);
                listView.adapter.update(true);
                break;
            case ANTI_DELETE:
                item.checked = !item.checked;
                GhostModeController.setAntiDelete(item.checked);
                listView.adapter.update(true);
                break;
            case ANTI_EDIT:
                item.checked = !item.checked;
                GhostModeController.setAntiEdit(item.checked);
                listView.adapter.update(true);
                break;
            case SCREENSHOT_BYPASS:
                item.checked = !item.checked;
                GhostModeController.setScreenshotBypassEnabled(item.checked);
                listView.adapter.update(true);
                break;
            case ANONYMOUS_FORWARD:
                item.checked = !item.checked;
                AnonymousForwardController.setAnonymousForwardEnabled(item.checked);
                listView.adapter.update(true);
                break;
            case LOCAL_PREMIUM:
                item.checked = !item.checked;
                LocalPremiumController.setLocalPremiumEnabled(item.checked);
                listView.adapter.update(true);
                break;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }
}
