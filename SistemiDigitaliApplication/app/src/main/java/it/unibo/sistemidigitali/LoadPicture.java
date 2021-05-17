package it.unibo.sistemidigitali;

import android.content.Intent;
import android.provider.MediaStore;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class LoadPicture {
    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, actionCode);
    }
}
