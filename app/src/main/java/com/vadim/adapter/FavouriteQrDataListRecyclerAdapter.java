package com.vadim.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vadim.MainActivity;
import com.vadim.fragment.GalleryQrDataFragment;
import com.vadim.model.QrData;
import com.vadim.presenter.QrCodeElementPresenter;
import com.vadim.qr_code.R;
import com.vadim.ui.UtilsUI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteQrDataListRecyclerAdapter extends
        RecyclerView.Adapter<FavouriteQrDataListRecyclerAdapter.FavouriteQrDataListViewHolder>
        implements Filterable {
    private static final String TAG = FavouriteQrDataListRecyclerAdapter.class.getSimpleName();

    private final Context context;
    private final LayoutInflater layoutInflater;
    private List<QrData> qrDataList;
    private final List<QrData> filterQrDataList;
    private final UtilsUI.UtilsFragment utilsFragment;

    public FavouriteQrDataListRecyclerAdapter(UtilsUI.UtilsFragment utilsFragment,
                                              Context context,
                                              List<QrData> qrDataList) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.qrDataList = qrDataList;
        this.filterQrDataList = qrDataList;
        this.utilsFragment = utilsFragment;
    }

    @NonNull
    @Override
    public FavouriteQrDataListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(R.layout.qr_data_from_favourite, parent, false);
        return new FavouriteQrDataListRecyclerAdapter.FavouriteQrDataListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteQrDataListViewHolder holder, int position) {
        QrData qrData = qrDataList.get(position);
        String photoPath = qrData.getOriginalPhoto();
        UtilsUI utilsUI = new UtilsUI(context);
        File filePhotoFile = new File(photoPath);
        Bitmap originalPhoto = utilsUI.convertFileToBitmap(filePhotoFile);
        holder.qrOriginalPhoto.setImageBitmap(originalPhoto);
        holder.qrName.setText(qrData.getName());
    }

    @Override
    public int getItemCount() {
        return qrDataList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    constraint = constraint.toString().toLowerCase();
                    final List<QrData> filters = new ArrayList<>();
                    for (QrData qrData : filterQrDataList) {
                        if (qrData.getName().toLowerCase()
                                .contains(constraint)) {
                            filters.add(qrData);
                        }
                    }
                    results.count = filters.size();
                    results.values = filters;
                } else {
                    results.count = filterQrDataList.size();
                    results.values = filterQrDataList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence,
                                          FilterResults filterResults) {
                qrDataList = (List<QrData>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public QrData[] getFavouriteQrDataArray() {
        return new QrData[qrDataList.size()];
    }

    class FavouriteQrDataListViewHolder extends RecyclerView.ViewHolder implements QrCodeElementPresenter.QrDataElementFromFavouriteViewHolder {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.qrDataLayoutFavourite)
        LinearLayout qrDataLayout;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.qrFavouriteOriginalPhoto)
        ImageView qrOriginalPhoto;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.layoutFavouriteText)
        LinearLayout layoutFavouriteText;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.qrFavouriteName)
        TextView qrName;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.trash)
        ImageView trash;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.share)
        ImageView share;

        private final QrCodeElementPresenter qrCodeElementPresenter;

        FavouriteQrDataListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            qrCodeElementPresenter = new QrCodeElementPresenter(context);
            qrCodeElementPresenter.attachFavouriteView(this);
            qrCodeElementPresenter.attachFavouriteFragment(utilsFragment);
            qrDataLayout.setOnClickListener(v -> {
                final DialogFragment galleryFragment = new GalleryQrDataFragment();
                Bundle bundleGallery = new Bundle();
                final int position = getLayoutPosition();
                bundleGallery.putInt("qrDataPositionId", position);
                final QrData[] qrDataArray = qrDataList.toArray(new QrData[qrDataList.size()]);
                bundleGallery.putParcelableArray("qrDataList", qrDataArray);
                galleryFragment.setArguments(bundleGallery);
                final FragmentManager fragmentManager = ((MainActivity) itemView.getContext()).getSupportFragmentManager();
                galleryFragment.show(fragmentManager, "gallery_of_qr_data");
            });
            trash.setOnClickListener(v -> {
                final int position = getLayoutPosition();
                QrData qrDataForTrash = qrDataList.get(position);
                qrCodeElementPresenter.deleteQrCodeElementFromFavouriteView(qrDataForTrash);
            });
            share.setOnClickListener(v -> {
                UtilsUI utilsUI = new UtilsUI(context);
                BitmapDrawable drawable = (BitmapDrawable) qrOriginalPhoto.getDrawable();
                shareOfPhoto(utilsUI, drawable.getBitmap());
            });
            qrDataLayout.setOnLongClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Rounded_MaterialComponents_MaterialAlertDialog);
                View customEditLayout = View.inflate(context, R.layout.alert_dialog_edit_layout, null);
                EditText changeText = customEditLayout.findViewById(R.id.changeText);
                CharSequence text = qrName.getText();
                changeText.setText(text);
                changeText.setSelection(text.length());
                builder.setView(customEditLayout)
                        .setOnCancelListener(DialogInterface::dismiss)
                        .setPositiveButton(R.string.change_text, (dialog, which) -> {
                            final int position = getAdapterPosition();
                            QrData qrData = qrDataList.get(position);
                            Editable editable = changeText.getText();
                            qrData.setName(editable.toString());
                            qrCodeElementPresenter.changeTextOfFavouriteName(qrData);
                        });
                AlertDialog alertDialog  = builder.create();
                alertDialog.setOnShowListener(dialog -> {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(changeText, InputMethodManager.SHOW_IMPLICIT);
                });
                alertDialog.show();
                return true;
            });
        }

        private void shareOfPhoto(UtilsUI utilsUI, Bitmap bitmap) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            Uri photoUri = utilsUI.getImageUri(bitmap);
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
            shareIntent.setType("image/*");
            Resources resources = context.getResources();
            context.startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.action_share)));
        }

        @Override
        public void deleteElement(QrData qrData) {
            qrDataList.remove(qrData);
            FavouriteQrDataListRecyclerAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void notifyQrData(List<QrData> qrDataList) {
            FavouriteQrDataListRecyclerAdapter.this.qrDataList.clear();
            FavouriteQrDataListRecyclerAdapter.this.qrDataList.addAll(qrDataList);
            FavouriteQrDataListRecyclerAdapter.this.notifyDataSetChanged();
        }

    }

}
