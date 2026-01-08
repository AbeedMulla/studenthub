package com.studenthub.ui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.studenthub.R;
import com.studenthub.data.local.entity.ClassEntity;
import com.studenthub.data.repository.DataRepository;
import com.studenthub.util.DateTimeUtils;

/**
 * Schedule fragment with Today and Week view tabs.
 */
public class ScheduleFragment extends Fragment {

    private TabLayout viewTabs;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAddClass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewPager();
        setupListeners();
    }

    private void initViews(View view) {
        viewTabs = view.findViewById(R.id.view_tabs);
        viewPager = view.findViewById(R.id.view_pager);
        fabAddClass = view.findViewById(R.id.fab_add_class);
    }

    private void setupViewPager() {
        SchedulePagerAdapter adapter = new SchedulePagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(viewTabs, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? getString(R.string.today_view) : getString(R.string.week_view));
        }).attach();
    }

    private void setupListeners() {
        fabAddClass.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ClassEditorActivity.class);
            startActivity(intent);
        });
    }

    private static class SchedulePagerAdapter extends FragmentStateAdapter {
        SchedulePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new TodayScheduleFragment() : new WeekScheduleFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    public static class TodayScheduleFragment extends Fragment {
        private RecyclerView recyclerView;
        private View emptyState;
        private DataRepository repository;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_schedule_day, container, false);
            recyclerView = view.findViewById(R.id.classes_list);
            emptyState = view.findViewById(R.id.empty_state);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            repository = DataRepository.getInstance(requireContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            loadTodayClasses();
        }

        @Override
        public void onResume() {
            super.onResume();
            loadTodayClasses();
        }

        private void loadTodayClasses() {
            int today = DateTimeUtils.getCurrentDayOfWeek();
            repository.getClassesForDay(today).observe(getViewLifecycleOwner(), classes -> {
                if (classes == null || classes.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                    ClassAdapter adapter = new ClassAdapter(classes, this::openClassEditor);
                    recyclerView.setAdapter(adapter);
                }
            });
        }

        private void openClassEditor(ClassEntity classEntity) {
            Intent intent = new Intent(requireContext(), ClassEditorActivity.class);
            intent.putExtra(ClassEditorActivity.EXTRA_CLASS_ID, classEntity.getId());
            startActivity(intent);
        }
    }

    public static class WeekScheduleFragment extends Fragment {
        private RecyclerView recyclerView;
        private View emptyState;
        private DataRepository repository;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_schedule_day, container, false);
            recyclerView = view.findViewById(R.id.classes_list);
            emptyState = view.findViewById(R.id.empty_state);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            repository = DataRepository.getInstance(requireContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            loadAllClasses();
        }

        @Override
        public void onResume() {
            super.onResume();
            loadAllClasses();
        }

        private void loadAllClasses() {
            repository.getAllClasses().observe(getViewLifecycleOwner(), classes -> {
                if (classes == null || classes.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                    ClassAdapter adapter = new ClassAdapter(classes, this::openClassEditor);
                    recyclerView.setAdapter(adapter);
                }
            });
        }

        private void openClassEditor(ClassEntity classEntity) {
            Intent intent = new Intent(requireContext(), ClassEditorActivity.class);
            intent.putExtra(ClassEditorActivity.EXTRA_CLASS_ID, classEntity.getId());
            startActivity(intent);
        }
    }
}
