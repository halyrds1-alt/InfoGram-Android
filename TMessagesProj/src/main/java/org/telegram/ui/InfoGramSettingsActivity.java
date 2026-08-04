package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.FakeLastSeenController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
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
        items.add(UItem.asSwitch(ROW_FAKE_LAST_SEEN, getString(R.string.FakeLastSeen))
                .setChecked(FakeLastSeenController.isEnabled()));
        items.add(UItem.asShadow(getString(R.string.FakeLastSeenInfo)));

        items.add(UItem.asSwitch(ROW_ANTI_DELETE, getString(R.string.AntiDelete))
                .setChecked(false));
        items.add(UItem.asShadow(getString(R.string.AntiDeleteInfo)));
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ROW_FAKE_LAST_SEEN) {
            FakeLastSeenController.setEnabled(!FakeLastSeenController.isEnabled());
            if (FakeLastSeenController.isEnabled() && FakeLastSeenController.getFakeTime() == 0) {
                FakeLastSeenController.setFakeTime(System.currentTimeMillis() - 3600000);
            }
            listView.adapter.update(true);
        } else if (item.id == ROW_ANTI_DELETE) {
            listView.adapter.update(true);
        }
    }
}
