package com.example.notemelab3;

import android.database.Cursor;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface {

    DBHandler dbHandler;
    RecyclerView notesRecyclerView;
    List<Note> noteList;
    NoteMeAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button newNoteButton = findViewById(R.id.newNote);
        newNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
            startActivity(intent);
        });
        dbHandler = new DBHandler(this);
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadNotes();
        SearchView searchView = findViewById(R.id.searchBox);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                notesAdapter.filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }
    private void loadNotes() {
        noteList = new ArrayList<>();
        Cursor cursor = dbHandler.getNotes();
        TextView noNotesMessage = findViewById(R.id.emptyNotes);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String subtitle = cursor.getString(cursor.getColumnIndexOrThrow("subtitle"));
                String text = cursor.getString(cursor.getColumnIndexOrThrow("text"));
                String color = cursor.getString(cursor.getColumnIndexOrThrow("color"));
                byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow("image"));
                noteList.add(new Note(id, title, subtitle, text, color, image));
            } while (cursor.moveToNext());
            noNotesMessage.setVisibility(View.GONE);
        } else {
            noNotesMessage.setVisibility(View.VISIBLE);
        }

        if (cursor != null) {
            cursor.close();
        }
        notesAdapter = new NoteMeAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "Editing " + noteList.get(position).getTitle(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt("ID", noteList.get(position).getId());
        bundle.putString("TITLE", noteList.get(position).getTitle());
        bundle.putString("SUBTITLE", noteList.get(position).getSubtitle());
        bundle.putString("TEXT", noteList.get(position).getText());
        bundle.putString("COLOR", noteList.get(position).getColor());
        intent.putExtras(bundle);

        startActivity(intent);
    }
}