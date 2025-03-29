package com.kernelcrew.moodapp.ui.components;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.kernelcrew.moodapp.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modal to let the user either upload an existing photo from their gallery or capture a new
 * temporary photo.
 */
public class UploadPhotoFragment extends BottomSheetDialogFragment {
    public UploadPhotoFragment () {}

    /**
     * Listen to the upload/capture photo event by attaching a listener implementing this interface.
     */
    @FunctionalInterface
    public interface UploadPhotoListener {
        void onUpload(Bitmap image);
    }

    @Nullable UploadPhotoListener uploadPhotoListener;

    public void setUploadPhotoListener(@Nullable UploadPhotoListener uploadPhotoListener) {
        Log.i("UploadPhotoFragment", "setUploadPhotoListener");
        this.uploadPhotoListener = uploadPhotoListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton selectFromGallery = view.findViewById(R.id.select_from_gallery);
        MaterialButton takePhoto = view.findViewById(R.id.take_photo);

        selectFromGallery.setOnClickListener(v -> openImagePicker());

        takePhoto.setOnClickListener(v -> openImageCapture());
    }

    /**
     * Handler for the image picker submission action.
     */
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Log.i("UploadPhotoListener", "got Data");
                    if (data != null) {
                        Bitmap image = loadBitmapFromUri(data.getData());
                        Log.i(getTag(), "Got Image");
                        if (image != null && uploadPhotoListener != null) {
                            uploadPhotoListener.onUpload(image);
                        }
                    }
                }

                dismiss();
            });

    /**
     * Load a Bitmap from a URI.
     * Returns null if we cannot load the bitmap.
     * @param imageUri URI of image to load
     */
    private @Nullable Bitmap loadBitmapFromUri(Uri imageUri) {
        try {
            return MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            Log.e("MoodEventForm", e.toString());
        }

        return null;
    }

    /**
     * Open an image picker and update the photoUri and photoButton photo when a selection is made.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    /**
     * The image capture action expects this to point to the captured image temporary file.
     */
    private String currentPhotoPath;

    /**
     * Handler for the image capture action.
     */
    private final ActivityResultLauncher<Intent> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Try to load the image from the file path
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    if (bitmap != null) {
                        if (uploadPhotoListener != null) {
                            uploadPhotoListener.onUpload(bitmap);
                        }
                    } else {
                        Log.e("Error", "Bitmap could not be loaded.");
                    }
                }

                dismiss();
            });

    /**
     * Open an image picker and update the photoUri and photoButton photo when a selection is made.
     */
    private void openImageCapture() {
        File photoFile;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            photoFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to open camera", Toast.LENGTH_SHORT).show();
            Log.e("ModEventForm", e.toString());
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        currentPhotoPath = photoFile.getAbsolutePath();
        Uri photoURI = FileProvider.getUriForFile(
                requireActivity(),
                "com.kernelcrew.moodapp.fileprovider.authority",
                photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        captureImageLauncher.launch(intent);
    }
}