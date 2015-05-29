package com.example.kai.mulitactivityapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

/**
 * Created by Tobias on 5/6/2015.
 */
public class SettingsActivity extends Activity {
    private static final int SELECTED_PICTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.backgroundButton:

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, SELECTED_PICTURE);
                break;

            case R.id.resetBackground:
                Bitmap whiteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.white);
                PathDrawView.setGivenBackgroundBitmap(whiteBitmap);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case SELECTED_PICTURE:
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String[]projection = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    String filePath = cursor.getString(columnIndex);

                    Bitmap yourSelectionImage = BitmapFactory.decodeFile(filePath);
                    Bitmap img;
                    if(yourSelectionImage.getWidth()>yourSelectionImage.getHeight()) {
                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);
                        img = Bitmap.createBitmap(yourSelectionImage, 0, 0, yourSelectionImage.getWidth(), yourSelectionImage.getHeight(), matrix, true);

                    }else{
                        img = yourSelectionImage;
                    }

                    float pathViewWidth = getIntent().getExtras().getInt("width");

                    float ratio = Math.min(
                            pathViewWidth/img.getWidth(),
                            (float)MainActivity.pathViewHeight/img.getHeight());

                    img = Bitmap.createScaledBitmap( img, (int)(img.getWidth()*ratio),
                            (int)(img.getHeight()*ratio), true );

                    if(img.getWidth()>img.getHeight()){
                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);
                        img = Bitmap.createBitmap(img , 0, 0, img.getWidth(), img.getHeight(), matrix, true);
                        PathDrawView.setGivenBackgroundBitmap(img);
                    }else{
                        PathDrawView.setGivenBackgroundBitmap(img);
                    }

                }
        }
    }


}
