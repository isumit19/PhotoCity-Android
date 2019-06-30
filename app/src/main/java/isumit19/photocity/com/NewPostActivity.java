package isumit19.photocity.com;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    ImageView newPostImage;
    EditText captionText;
    Button newPostBtn;
    ProgressBar progressBar;


    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    String current_uid;

    Uri postImageUri=null;


    Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_postactivity);

        newPostBtn = findViewById(R.id.post_btn);
        captionText = findViewById(R.id.new_post_desc);
        newPostImage = findViewById(R.id.new_post_image);
        progressBar = findViewById(R.id.new_post_progress);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser()!=null) {


            current_uid = firebaseAuth.getCurrentUser().getUid();

            Toolbar toolbar = findViewById(R.id.new_post_toolbar);
            setSupportActionBar(toolbar);

            //getSupportActionBar().setTitle("New Post");
            Objects.requireNonNull(getSupportActionBar()).setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            newPostImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .setMinCropWindowSize(512, 512)
                            .start(NewPostActivity.this);
                }
            });


            newPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final String desc = captionText.getText().toString();

                    if (!TextUtils.isEmpty(desc) && postImageUri != null) {

                        progressBar.setVisibility(View.VISIBLE);

                        final String randomName = UUID.randomUUID().toString();

                        final StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                        filePath.putFile(postImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull final Task<Uri> task) {

                                final String download_uri = task.getResult().toString();
                                if (task.isSuccessful()) {


                                    final File newImage = new File(postImageUri.getPath());

                                    try {
                                        compressedImageFile = new Compressor(NewPostActivity.this)
                                                .setMaxHeight(100)
                                                .setMaxWidth(100)
                                                .setQuality(1)
                                                .compressToBitmap(newImage);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] thumbData = baos.toByteArray();


                                    final StorageReference storage = storageReference.child("post_images/thumbs").child(randomName + "jpg");

                                    final UploadTask uploadTask = storage.putBytes(thumbData);

                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                @Override
                                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                    if (!task.isSuccessful())
                                                        throw task.getException();
                                                    return storage.getDownloadUrl();
                                                }
                                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        final String thumbUri = task.getResult().toString();
                                                        Map<String, Object> postsMap = new HashMap<>();
                                                        postsMap.put("image_url", download_uri);
                                                        postsMap.put("thumb_url", thumbUri);
                                                        postsMap.put("desc", desc);
                                                        postsMap.put("user_id", current_uid);
                                                        postsMap.put("timestamp", FieldValue.serverTimestamp());


                                                        firebaseFirestore.collection("Posts").add(postsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                                if (task.isSuccessful()) {

                                                                    Toast.makeText(NewPostActivity.this, "Post added", Toast.LENGTH_SHORT).show();
                                                                    Intent main = new Intent(NewPostActivity.this, MainActivity.class);
                                                                    startActivity(main);
                                                                    finish();

                                                                } else {

                                                                    String error = task.getException().getMessage();
                                                                    Toast.makeText(NewPostActivity.this, error, Toast.LENGTH_LONG).show();

                                                                }

                                                                progressBar.setVisibility(View.INVISIBLE);


                                                            }
                                                        });
                                                    }
                                                }
                                            });


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            //Error
                                        }
                                    });


                                } else {

                                    progressBar.setVisibility(View.INVISIBLE);
                                    String error = task.getException().getMessage();
                                    Toast.makeText(NewPostActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });


                    }


                }
            });
        }



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
        }

    }

}
