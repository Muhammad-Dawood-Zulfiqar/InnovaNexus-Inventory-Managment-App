package com.example.pelagiahotelapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout bottomTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupViewPager();
        setupTabLayout();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        bottomTabLayout = findViewById(R.id.bottomTabLayout);
        bottomTabLayout.setSelectedTabIndicator(null);
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);
    }

    private void setupTabLayout() {
        // 1. Attach the Mediator (Connects ViewPager to Tabs)
        new TabLayoutMediator(bottomTabLayout, viewPager, (tab, position) -> {
            // Inflate the Custom View for THIS tab
            View customView = LayoutInflater.from(this).inflate(R.layout.item_tab, null);

            // Get references to the icon and text inside the custom view
            ImageView icon = customView.findViewById(R.id.tab_icon);
            TextView text = customView.findViewById(R.id.tab_text);

            // Set Data based on position
            switch (position) {
                case 0:
                    icon.setImageResource(R.drawable.ic_home);
                    text.setText("Hotels");
                    // FORCE VISIBLE for the first item immediately
                    text.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    icon.setImageResource(R.drawable.icons8request50);
                    text.setText("Requests");
                    text.setVisibility(View.GONE);
                    break;
                case 2:
                    icon.setImageResource(R.drawable.ic_edit);
                    text.setText("Edit Hotels");
                    text.setVisibility(View.GONE);
                    break;
                case 3:
                    icon.setImageResource(R.drawable.ic_profile);
                    text.setText("Profile");
                    text.setVisibility(View.GONE);
                    break;
            }

            // CRITICAL: Attach the custom view to the tab
            tab.setCustomView(customView);

        }).attach();

        // 2. Add Listener to handle clicking (Expand/Collapse animation)
        bottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view != null) {
                    TextView text = view.findViewById(R.id.tab_text);
                    text.setVisibility(View.VISIBLE); // Expand
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view != null) {
                    TextView text = view.findViewById(R.id.tab_text);
                    text.setVisibility(View.GONE); // Collapse
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
    }}