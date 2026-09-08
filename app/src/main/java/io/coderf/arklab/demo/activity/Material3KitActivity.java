package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.HeroCarouselStrategy;
import com.google.android.material.carousel.UncontainedCarouselStrategy;
import com.google.android.material.chip.Chip;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.common.widget.dialog.MaterialAlertHelper;
import io.coderf.arklab.common.widget.feedback.BadgeHelper;
import io.coderf.arklab.common.widget.feedback.ToastHelper;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityMaterial3KitBinding;
import io.coderf.arklab.demo.databinding.ItemCarouselBannerBinding;
import io.coderf.arklab.demo.databinding.ItemCarouselShelfBinding;

/**
 * Material3 产品能力演示。Carousel 只做封面/货架浏览，不替代 {@code BannerView}，也不做微信相册。
 */
@ExperimentalBadgeUtils
public class Material3KitActivity extends BaseActivity<EmptyViewModel, ActivityMaterial3KitBinding> {

    private static final List<String> SEARCH_SUGGESTIONS = Arrays.asList(
            "Chip 筛选标签",
            "日期选择 MaterialDatePicker",
            "相册选图 MediaHelper",
            "ToggleGroup 日周月",
            "SearchBar 整页搜索",
            "SideSheet 宽屏筛选",
            "表单 FormSpinner"
    );
    private static final List<String> CAROUSEL_URLS = Arrays.asList(
            "https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800",
            "https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg",
            "https://q8.itc.cn/images01/20240208/45d5ee19361f4f8fa824e93ebfc42a8a.jpeg"
    );

    private BadgeDrawable badge;
    private int badgeCount = 3;
    private SuggestionAdapter suggestionAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_material3_kit;
    }

    @Override
    public String setTitleBar() {
        return "Material3 能力";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        setupSearch();
        binding.carousel.setLayoutManager(new CarouselLayoutManager(new HeroCarouselStrategy()));
        binding.carousel.setAdapter(new HeroCarouselAdapter(CAROUSEL_URLS));
        binding.carouselShelf.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        binding.carouselShelf.setAdapter(new ShelfCarouselAdapter(CAROUSEL_URLS));

        binding.toggleGroup.check(R.id.toggle_day);
        binding.slider.addOnChangeListener((slider, value, fromUser) ->
                binding.tvSlider.setText("Slider / RangeSlider  " + Math.round(value)));
        badge = BadgeHelper.attach(binding.btnBadge, badgeCount);
        binding.btnBadge.setOnClickListener(v -> {
            badgeCount++;
            BadgeHelper.update(badge, badgeCount);
        });
        binding.btnSideSheet.setOnClickListener(v -> {
            SideSheetDialog sheet = new SideSheetDialog(this);
            sheet.setContentView(R.layout.activity_material3_kit_side);
            sheet.show();
        });
        binding.btnAlert.setOnClickListener(v ->
                MaterialAlertHelper.confirm(this, "MaterialAlertDialog",
                        "标准确认用官方 Dialog；富文本仍走 ConfirmDialog。",
                        "知道了", null));
        ArrayAdapter<String> educationAdapter = new ArrayAdapter<>(this,
                com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
                Arrays.asList("本科", "硕士", "博士"));
        MaterialAutoCompleteTextView dropdown = binding.exposedDropdownText;
        dropdown.setAdapter(educationAdapter);
        dropdown.setText("本科", false);

        bindInputChip(binding.chipTagJava);
        bindInputChip(binding.chipTagCompose);
        bindInputChip(binding.chipTagMvvm);
        binding.chipAssistImage.setOnClickListener(v -> ToastHelper.showShort(this, "Assist：选图"));
        binding.chipAssistVideo.setOnClickListener(v -> ToastHelper.showShort(this, "Assist：选视频"));
        binding.chipAssistSearch.setOnClickListener(v -> binding.searchView.show());
        setupBottomBar();
    }

    @Override
    public void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        UseCase useCase = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? bundle.getParcelable("args", UseCase.class)
                : bundle.getParcelable("args");
        if (useCase != null) {
            toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        }
    }

    private void setupSearch() {
        binding.searchView.setupWithSearchBar(binding.searchBar);
        binding.searchBar.inflateMenu(R.menu.menu_material3_search);
        binding.searchBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search_filter) {
                ToastHelper.showShort(this, "SearchBar 菜单：这里接筛选/语音");
                return true;
            }
            return false;
        });
        suggestionAdapter = new SuggestionAdapter(text -> {
            binding.searchBar.setText(text);
            binding.searchView.hide();
            ToastHelper.showShort(this, "搜索：" + text);
        });
        binding.searchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.searchResults.setAdapter(suggestionAdapter);
        suggestionAdapter.submit(SEARCH_SUGGESTIONS);
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                suggestionAdapter.submit(filterSuggestions(s == null ? "" : s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            CharSequence query = v.getText();
            if (query != null && query.length() > 0) {
                binding.searchBar.setText(query);
                binding.searchView.hide();
                ToastHelper.showShort(this, "搜索：" + query);
            }
            return true;
        });
    }

    private static List<String> filterSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return SEARCH_SUGGESTIONS;
        }
        String key = query.trim().toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        for (String item : SEARCH_SUGGESTIONS) {
            if (item.toLowerCase(Locale.ROOT).contains(key)) {
                matched.add(item);
            }
        }
        return matched;
    }

    private void bindInputChip(@NonNull Chip chip) {
        chip.setOnCloseIconClickListener(v -> binding.chipGroupInput.removeView(chip));
    }

    private void setupBottomBar(){
        binding.fabEdit.setOnClickListener(v -> ToastHelper.showShort(this, "FAB：提交"));
        binding.bottomBar.setNavigationOnClickListener(v ->
                ToastHelper.showShort(this, "BottomAppBar：更多"));
        binding.bottomBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_attach) {
                ToastHelper.showShort(this, "BottomAppBar：附件");
                return true;
            }
            if (id == R.id.action_edit_delete) {
                ToastHelper.showShort(this, "BottomAppBar：删除");
                return true;
            }
            return false;
        });
    }

    private static final class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.Holder> {
        interface OnPick {
            void onPick(@NonNull String text);
        }

        private final OnPick onPick;
        private final List<String> items = new ArrayList<>();

        SuggestionAdapter(OnPick onPick) {
            this.onPick = onPick;
        }

        void submit(@NonNull List<String> data) {
            items.clear();
            items.addAll(data);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_suggestion, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            String text = items.get(position);
            holder.text.setText(text);
            holder.text.setOnClickListener(v -> onPick.onPick(text));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final TextView text;

            Holder(TextView text) {
                super(text);
                this.text = text;
            }
        }
    }

    private static final class HeroCarouselAdapter extends RecyclerView.Adapter<HeroCarouselAdapter.Holder> {
        private final List<String> urls;

        HeroCarouselAdapter(List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemCarouselBannerBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Glide.with(holder.binding.carouselImage).load(urls.get(position)).into(holder.binding.carouselImage);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final ItemCarouselBannerBinding binding;

            Holder(ItemCarouselBannerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private static final class ShelfCarouselAdapter extends RecyclerView.Adapter<ShelfCarouselAdapter.Holder> {
        private final List<String> urls;

        ShelfCarouselAdapter(List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemCarouselShelfBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Glide.with(holder.binding.carouselImage).load(urls.get(position)).into(holder.binding.carouselImage);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final ItemCarouselShelfBinding binding;

            Holder(ItemCarouselShelfBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
