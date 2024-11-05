package com.example.notemelab3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import androidx.appcompat.app.AlertDialog;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class EditNoteActivity extends AppCompatActivity {

    private String selectedColor = "#A7BED3";
    private Button lastSelectedButton;
    private Button color1, color2, color3, color4, color5;
    private Button editImageButton;
    private byte[] image = null;
    private ImageView noteImageView;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_note);

        // Initialize the class-level ImageView
        noteImageView = findViewById(R.id.noteImageView);
        Button deleteButton = findViewById(R.id.delete);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        int id = bundle.getInt("ID", 0);
        String title = bundle.getString("TITLE", "TITLE MISSING");
        String subtitle = bundle.getString("SUBTITLE", "SUBTITLE MISSING");
        String text = bundle.getString("TEXT", "TEXT MISSING");
        String color = bundle.getString("COLOR", "#A7BED3");

        EditText editTitle = (EditText)findViewById(R.id.title);
        EditText editSubtitle = (EditText)findViewById(R.id.subtitle);
        EditText editText = (EditText)findViewById(R.id.description);
        editTitle.setText(title);
        editSubtitle.setText(subtitle);
        editText.setText(text);
        Button selectedColor = null;
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(id));

        dbHandler = new DBHandler(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve the image byte array from the database (assuming dbHandler can retrieve image data)
        byte[] imageData = dbHandler.getImageById(id); // You may need to implement this method in DBHandler.

        if (imageData != null) {
            image = imageData;
            noteImageView.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            noteImageView.setImageBitmap(bitmap);
        }

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveNote(id));

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());

        editImageButton = findViewById(R.id.editImageButton);
        editImageButton.setOnClickListener(v -> displayImagePopUp(v));

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

        switch (color) {
            case "#A7BED3": selectedColor = color1; break;
            case "#C6E2E9": selectedColor = color2; break;
            case "#F1FFC4": selectedColor = color3; break;
            case "#FFCAAF": selectedColor = color4; break;
            case "#DAB894": selectedColor = color5; break;
            default: selectedColor = color1;
        }
        changeNoteColor(color, selectedColor);
    }

    private void changeNoteColor(String color, Button selectedButton) {
        selectedColor = color;
        View colorLayout = findViewById(R.id.noteLayout);
        colorLayout.setBackgroundColor(Color.parseColor(selectedColor));
        if (lastSelectedButton != null) {
            lastSelectedButton.setScaleX(1f);
            lastSelectedButton.setScaleY(1f);
        }
        selectedButton.setScaleX(0.7f);
        selectedButton.setScaleY(0.7f);
        lastSelectedButton = selectedButton;
    }

    private void saveNote(int id) {
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

        long result = dbHandler.updateNote(id, title, subtitle, description, selectedColor, image);
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

    // Method to handle selecting an image from the gallery
    public void selectImage() {
        pickImageLauncher.launch("image/*");
    }

    // Method to handle capturing an image using the camera
    public void captureImage() {
        captureImageLauncher.launch(null);
    }

    private void showDeleteConfirmationDialog(int id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> deleteNote(id))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteNote(int id) {
        long result = dbHandler.deleteNote(id);
        if (result != -1) {
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error deleting note", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the image byte array to the instance state
        if (image != null) {
            outState.putByteArray("image_data", image);
        }
        // Save other fields like selectedColor if necessary
        outState.putString("selected_color", selectedColor);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the image byte array from the instance state
        if (savedInstanceState.containsKey("image_data")) {
            image = savedInstanceState.getByteArray("image_data");
            if (image != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                noteImageView.setImageBitmap(bitmap);
            }
        }
        // Restore other fields like selectedColor if necessary
        selectedColor = savedInstanceState.getString("selected_color", "#A7BED3");
    }

}