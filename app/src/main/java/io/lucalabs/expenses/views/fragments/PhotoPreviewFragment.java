package io.lucalabs.expenses.views.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.CameraActivity;
import io.lucalabs.expenses.activities.ExpenseReportActivity;
import io.lucalabs.expenses.activities.MainActivity;
import io.lucalabs.expenses.utils.ImageSaver;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoPreviewFragment extends Fragment implements View.OnClickListener {

    private byte[] mPreviewImage;
    private static final String TAG = Fragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_preview, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        ImageView preview = (ImageView) view.findViewById(R.id.photo_preview);

        Glide.with(this)
                .load(mPreviewImage)
                .asBitmap()
                .into((preview));

        view.findViewById(R.id.keep_photo).setOnClickListener(this);
        view.findViewById(R.id.dismiss_photo).setOnClickListener(this);
        new PhotoViewAttacher(preview);
    }

    public static PhotoPreviewFragment newInstance() {
        return new PhotoPreviewFragment();
    }

    public void setPreviewImage(byte[] previewImage) {
        mPreviewImage = previewImage;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.keep_photo:
                keepPhoto();
                break;
            case R.id.dismiss_photo:
                dismissPhoto();
                break;

        }
    }

    private void keepPhoto() {
        final String expenseReportRef = ((CameraActivity) getActivity()).getExpenseReportRef();

        Toast.makeText(getActivity(), expenseReportRef, Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                new ImageSaver(getActivity(), mPreviewImage, expenseReportRef).run();
            }
        }).start();

        startActivity(new Intent(getActivity(), MainActivity.class));
        if (expenseReportRef != null) {
            Intent toExpenseReportActivity = new Intent(getActivity(), ExpenseReportActivity.class);
            toExpenseReportActivity.putExtra("firebase_ref", expenseReportRef);
            startActivity(toExpenseReportActivity);
        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
        }
    }

    private void dismissPhoto() {
        getActivity().onBackPressed();
    }
}
