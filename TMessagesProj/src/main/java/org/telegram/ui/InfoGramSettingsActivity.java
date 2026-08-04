package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.telegram.messenger.AntiDeleteController;
import org.telegram.messenger.AntiEditController;
import org.telegram.messenger.AntiForwardController;
import org.telegram.messenger.AnonymousForwardController;
import org.telegram.messenger.AutoReplyController;
import org.telegram.messenger.FakeLastSeenController;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;

public class InfoGramSettingsActivity extends BaseFragment {

    private static final int ROW_FAKE_LAST_SEEN = 100;
    private static final int ROW_ANTI_DELETE = 101;
    private static final int ROW_ANTI_EDIT = 102;
    private static final int ROW_AUTO_REPLY = 103;
    private static final int ROW_ANTI_FORWARD = 104;
    private static final int ROW_ANONYMOUS_FORWARD = 105;

    private UniversalRecyclerView listView;

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

        FrameLayout contentView = new FrameLayout(context);
        contentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray, resourceProvider));

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, null);
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        return fragmentView = contentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.InfoGramFeatures)));

        items.add(UItem.asSwitch(ROW_FAKE_LAST_SEEN, getString(R.string.FakeLastSeen))
                .setChecked(FakeLastSeenController.isEnabled()));
        items.add(UItem.asShadow(getString(R.string.FakeLastSeenInfo)));

        items.add(UItem.asSwitch(ROW_ANTI_DELETE, getString(R.string.AntiDelete))
                .setChecked(AntiDeleteController.isEnabled()));
        items.add(UItem.asShadow(getString(R.string.AntiDeleteInfo)));

        items.add(UItem.asSwitch(ROW_ANTI_EDIT, getString(R.string.AntiEdit))
                .setChecked(AntiEditController.isEnabled()));
        items.add(UItem.asShadow(getString(R.string.AntiEditInfo)));

        items.add(UItem.asSwitch(ROW_AUTO_REPLY, getString(R.string.AutoReply))
                .setChecked(AutoReplyController.isEnabled()));
        items.add(UItem.asShadow(getString(R.string.AutoReplyInfo)));

        items.add(UItem.asSwitch(ROW_ANTI_FORWARD, getString(R.string.AntiForward))
                .setChecked(AntiForwardController.isEnabled()));
        items.add(UItem.asShadow(getString(R.string.AntiForwardInfo)));

        items.add(UItem.asSwitch(ROW_ANONYMOUS_FORWARD, getString(R.string.AnonymousForward))
                .setChecked(AnonymousForwardController.isEnabled()));
        items.add(UItem.asShadow(getString(R.string.AnonymousForwardInfo)));
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ROW_FAKE_LAST_SEEN) {
            FakeLastSeenController.setEnabled(!FakeLastSeenController.isEnabled());
            if (FakeLastSeenController.isEnabled() && FakeLastSeenController.getFakeTime() == 0) {
                FakeLastSeenController.setFakeTime(System.currentTimeMillis() - 3600000);
            }
            listView.adapter.update(true);
        } else if (item.id == ROW_ANTI_DELETE) {
            AntiDeleteController.setEnabled(!AntiDeleteController.isEnabled());
            listView.adapter.update(true);
        } else if (item.id == ROW_ANTI_EDIT) {
            AntiEditController.setEnabled(!AntiEditController.isEnabled());
            listView.adapter.update(true);
        } else if (item.id == ROW_AUTO_REPLY) {
            if (!AutoReplyController.isEnabled()) {
                showAutoReplyDialog();
            } else {
                AutoReplyController.setEnabled(false);
                listView.adapter.update(true);
            }
        } else if (item.id == ROW_ANTI_FORWARD) {
            AntiForwardController.setEnabled(!AntiForwardController.isEnabled());
            listView.adapter.update(true);
        } else if (item.id == ROW_ANONYMOUS_FORWARD) {
            AnonymousForwardController.setEnabled(!AnonymousForwardController.isEnabled());
            listView.adapter.update(true);
        }
    }

    private void showAutoReplyDialog() {
        if (getParentActivity() == null) return;
        android.widget.EditText editText = new android.widget.EditText(getParentActivity());
        editText.setText(AutoReplyController.getReplyText());
        editText.setHint(R.string.AutoReplyText);
        editText.setSingleLine(false);
        editText.setMinLines(3);
        editText.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(10), AndroidUtilities.dp(20), AndroidUtilities.dp(10));

        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourceProvider);
        builder.setTitle(getString(R.string.AutoReply));
        builder.setView(editText);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String text = editText.getText().toString().trim();
            if (!text.isEmpty()) {
                AutoReplyController.setReplyText(text);
                AutoReplyController.setEnabled(true);
                listView.adapter.update(true);
                try {
                    Toast.makeText(getParentActivity(), getString(R.string.AutoReplyEnabled), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    org.telegram.messenger.FileLog.e(e);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        showDialog(builder.create());
    }
}
