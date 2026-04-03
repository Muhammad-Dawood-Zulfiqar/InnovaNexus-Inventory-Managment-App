package com.example.pelagiahotelapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pelagiahotelapp.Main.HotelBookingsFragment;
import com.example.pelagiahotelapp.Main.HotelsEditFragment;
import com.example.pelagiahotelapp.Main.ProfileFragment;
import com.example.pelagiahotelapp.Main.RequestFragment;


public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HotelBookingsFragment();
            case 1:
                return new RequestFragment();
            case 2:
            return new HotelsEditFragment();
            case 3:
                return new ProfileFragment();
            default:
                return new HotelBookingsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}