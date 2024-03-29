/*
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyanogenmod.account.setup;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ThemeUtils;
import android.content.res.CustomTheme;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.IWindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import android.widget.ViewSwitcher;
import com.cyanogenmod.account.R;
import com.cyanogenmod.account.ui.SetupPageFragment;
import com.cyanogenmod.account.util.CMAccountUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.cyanogenmod.hardware.KeyDisabler;

public class PersonalizationPage extends Page {

    private static final String TAG = PersonalizationPage.class.getSimpleName();
    private Bundle mPageState;

    public PersonalizationPage(Context context, SetupDataCallbacks callbacks, int titleResourceId) {
        super(context, callbacks, titleResourceId);
    }

    @Override
    public Fragment createFragment() {
        Bundle args = new Bundle();
        args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
        mPageState = getData();

        PersonalizationFragment fragment = new PersonalizationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getNextButtonResId() {
        return R.string.next;
    }

    public static class PersonalizationFragment extends SetupPageFragment {

        ViewSwitcher mSwitcher;

        public PersonalizationFragment() {
        }

        @Override
        protected void setUpPage() {
            mSwitcher = (ViewSwitcher) mRootView.findViewById(R.id.switcher);
            if (hideWhisperPush(getActivity())) {
                ViewGroup whisperPushLayout = (ViewGroup) mRootView.findViewById(R.id.whisperpush);
                if (whisperPushLayout != null) {
                    whisperPushLayout.setVisibility(View.GONE);
                }
            }
            Switch whisperPushSwitch = (Switch) mRootView.findViewById(R.id.whisperpush_switch);
            mPage.getData().putBoolean("register", whisperPushSwitch.isChecked());
            whisperPushSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mPage.getData().putBoolean("register", b);
                }
            });


            Switch defaultThemeSwitch = (Switch) mRootView.findViewById(R.id.apply_default_theme_switch);
            if (hideThemeSwitch(getActivity())) {
                ViewGroup themeLayout = (ViewGroup) mRootView.findViewById(R.id.apply_default_theme);
                if (themeLayout != null) {
                    themeLayout.setVisibility(View.GONE);
                }
            } else {
                defaultThemeSwitch.setChecked(true);
            }
            mPage.getData().putBoolean("apply_default_theme", defaultThemeSwitch.isChecked());
            defaultThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mPage.getData().putBoolean("apply_default_theme", b);
                    if (!b && mSwitcher.getDisplayedChild() == 0) {
                        mSwitcher.showNext();
                    } else  {
                        mSwitcher.showPrevious();
                    }
                }
            });

        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_personalization_page;
        }

        @Override
        protected int getTitleResource() {
            return R.string.setup_personalization;
        }
    }

    protected static boolean hideWhisperPush(Context context) {
        final int playServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        return playServicesAvailable != ConnectionResult.SUCCESS
                || (CMAccountUtils.isGSMPhone(context) && CMAccountUtils.isSimMissing(context));
    }


    protected static boolean hideThemeSwitch(Context context) {
        return ThemeUtils.getDefaultThemePackageName(context) == CustomTheme.HOLO_DEFAULT;
    }

    public static boolean skipPage(Context context) {
        return hideWhisperPush(context) && hideThemeSwitch(context);
    }
}
