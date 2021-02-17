package com.vadim.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.chrisbanes.photoview.PhotoView;
import com.vadim.model.QrData;
import com.vadim.presenter.GalleryQrDataPresenter;
import com.vadim.qr_code.R;
import com.vadim.ui.UtilsUI;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GalleryQrDataFragment extends DialogFragment {
    private final static String TAG = GalleryQrDataFragment.class.getSimpleName();

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.headerLayoutOfQrGallery)
    RelativeLayout headerLayoutOfQrGallery;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.backFromQrGallery)
    ImageView backFromQrGallery;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.contentOfQrData)
    TextView contentOfQrData;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.shareQrData)
    ImageView shareQrData;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.qrDataPager)
    ViewPager qrDataPager;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_qr_data, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        backFromQrGallery.setOnClickListener(v -> dismiss());
        Bundle bundle = getArguments();
        assert bundle != null;
        QrData[] qrDataArray = (QrData[]) bundle.get("qrDataList");
        if (qrDataArray != null) {
            final List<QrData> qrDataList = Arrays.asList(qrDataArray);
            final GalleryQrDataFragment.PhotoAdapter photoAdapter =
                    new GalleryQrDataFragment.PhotoAdapter(qrDataList);
            int artPosition = (int) bundle.get("qrDataPositionId");
            qrDataPager.setAdapter(photoAdapter);
            qrDataPager.setCurrentItem(artPosition);
            photoAdapter.notifyDataSetChanged();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Objects.requireNonNull(dialog.getWindow())
                    .setWindowAnimations(R.style.dialog_animation_fade);
        }
    }

    class PhotoAdapter extends PagerAdapter implements GalleryQrDataPresenter.GalleryView {
        private final List<QrData> qrDataList;
        private QrData selectedQrData;
        private PhotoView photoQrCodeView;

        PhotoAdapter(List<QrData> qrDataList) {
            this.qrDataList = qrDataList;
        }

        @Override
        public int getCount() {
            return qrDataList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @NonNull
        @SuppressLint({"ResourceType", "ClickableViewAccessibility"})
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            photoQrCodeView = new PhotoView(context);
            photoQrCodeView.setZoomable(true);
            photoQrCodeView.setMinimumScale(1.0f);
            photoQrCodeView.setMaximumScale(2.0f);
            final QrData qrData = qrDataList.get(position);
            UtilsUI utilsUI = new UtilsUI(context);
            File file = new File(qrData.getOriginalPhoto());
            Bitmap bitmap = utilsUI.convertFileToBitmap(file);
            GalleryQrDataPresenter galleryQrDataPresenter = new GalleryQrDataPresenter(this);
            galleryQrDataPresenter.generateQrCodeFromContent(qrData.getContent());

            photoQrCodeView.setOnPhotoTapListener((view, x, y) -> {
                if (headerLayoutOfQrGallery.getVisibility() == View.VISIBLE) {
                    headerLayoutOfQrGallery.setVisibility(View.INVISIBLE);
                } else {
                    headerLayoutOfQrGallery.setVisibility(View.VISIBLE);
                }
            });

            shareQrData.setOnClickListener(v -> {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                Uri photoUri = utilsUI.getImageUri(bitmap);
                shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
                shareIntent.setType("image/*");
                Resources resources = context.getResources();
                context.startActivity(Intent.createChooser(shareIntent,
                        resources.getString(R.string.action_share)));
            });

            qrDataPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                    selectedQrData = qrDataList.get(position);
                    contentOfQrData.setText(selectedQrData.getName());
                }

                @Override
                public void onPageSelected(int position) { }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
            container.addView(photoQrCodeView, 0);
            return photoQrCodeView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((ImageView) object);
        }

        @Override
        public void displayQrData(Bitmap photoOFQrCode) {
            photoQrCodeView.setImageBitmap(photoOFQrCode);
        }
    }

}