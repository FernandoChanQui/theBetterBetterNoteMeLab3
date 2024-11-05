package com.example.notemelab3;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class NewNoteActivity extends AppCompatActivity {

    private String selectedColor = "#A7BED3";
    private Button lastSelectedButton, saveButton, cancelButton, selectImageButton, color1, color2, color3, color4, color5;;
    DBHandler dbHandler;
    private View colorLayout;

    private byte[] image = null;
    private ImageView noteImageView;

    // Activity result launcher for getting an image from the gallery
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        byte[] byteArray = new byte[inputStream.available()];
                        inputStream.read(byteArray);
                        image = byteArray;

                        // Set the selected image in the ImageView
                        noteImageView.setImageURI(uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    // Activity result launcher for capturing an image from the camera
    private final ActivityResultLauncher<Void> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    image = stream.toByteArray();

                    // Set the captured image in the ImageView
                    noteImageView.setImageBitmap(bitmap);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_note);

        noteImageView = findViewById(R.id.noteImageView);
        dbHandler = new DBHandler(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveNote());

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());

        selectImageButton = findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(v -> displayImagePopUp(v));

        color1 = findViewById(R.id.color1);
        color1.setOnClickListener(v -> changeNoteColor("#A7BED3", color1));

        color2 = findViewById(R.id.color2);
        color2.setOnClickListener(v -> changeNoteColor("#C6E2E9", color2));

        color3 = findViewById(R.id.color3);
        color3.setOnClickListener(v -> changeNoteColor("#F1FFC4", color3));

        color4 = findViewById(R.id.color4);
        color4.setOnClickListener(v -> changeNoteColor("#FFCAAF", color4));

        color5 = findViewById(R.id.color5);
        color5.setOnClickListener(v -> changeNoteColor("#DAB894", color5));
    }

    private void changeNoteColor(String color, Button selectedButton) {
        selectedColor = color;
        colorLayout = findViewById(R.id.noteLayout);
        colorLayout.setBackgroundColor(Color.parseColor(selectedColor));
        if (lastSelectedButton != null) {
            lastSelectedButton.setScaleX(1f);
            lastSelectedButton.setScaleY(1f);
        }
        selectedButton.setScaleX(0.7f);
        selectedButton.setScaleY(0.7f);
        lastSelectedButton = selectedButton;
    }

    private void saveNote() {
        EditText noteTitle = findViewById(R.id.title);
        EditText noteSubtitle = findViewById(R.id.subtitle);
        EditText noteDescription = findViewById(R.id.description);

        String title = noteTitle.getText().toString();
        String subtitle = noteSubtitle.getText().toString();
        String description = noteDescription.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (subtitle.isEmpty()) {
            Toast.makeText(this, "Please enter a subtitle", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (description.isEmpty()) {
            Toast.makeText(this, "Please enter note content", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHandler.addNote(title, subtitle, description, selectedColor, image);
        if (result != -1) {
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();

            finish();
        } else {
            Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
        }
    }

    public void displayImagePopUp(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.imagepopup, popupMenu.getMenu());

        popupMenu.setGravity(Gravity.END);

        // Set the listener for menu item clicks
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId(); // Get the clicked menu item ID
            if (itemId == R.id.selectImage) {
                selectImage(); // Call selectImage method
                return true;
            } else if (itemId == R.id.captureImage) {
                captureImage(); // Call captureImage method
                return true;
            }
            return false; // Return false if no valid item was clicked
        });

        popupMenu.show(); // Show the popup menu
    }

    // Method to handle selecting an image from the gallery
    public void selectImage() {
        pickImageLauncher.launch("image/*");
    }

    // Method to handle capturing an image using the camera
    public void captureImage() {
        captureImageLauncher.launch(null);
    }
}