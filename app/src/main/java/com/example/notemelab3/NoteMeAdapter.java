package com.example.notemelab3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class NoteMeAdapter extends RecyclerView.Adapter<NoteMeAdapter.NoteViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    private List<Note> noteList;
    private List<Note> filteredNotes;

    public NoteMeAdapter(List<Note> noteList, RecyclerViewInterface recyclerViewInterface) {
        this.noteList = noteList;
        this.filteredNotes = new ArrayList<>(noteList);
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_activity_note, parent, false);
        return new NoteViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = filteredNotes.get(position);

        holder.title.setText(note.getTitle());
        holder.subtitle.setText(note.getSubtitle());
        holder.text.setText(note.getText());
        holder.itemView.setBackgroundColor(Color.parseColor(note.getColor()));

        byte[] imageData = note.getImage();
        if (imageData != null && imageData.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            holder.noteImageView.setImageBitmap(bitmap);
            holder.noteImageView.setVisibility(View.VISIBLE); // Make the image visible
        } else {
            holder.noteImageView.setVisibility(View.GONE); // Hide if there's no image
        }
    }

    @Override
    public int getItemCount() {
        return filteredNotes.size();
    }

    public void filter(String query) {
        query = query.toLowerCase();
        filteredNotes.clear();

        if (query.isEmpty()) {
            filteredNotes.addAll(noteList);
        } else {
            for (Note note : noteList) {

                if (note.getTitle().toLowerCase().contains(query)) {
                    filteredNotes.add(note);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView title, subtitle, text;
        public ImageView noteImageView;

        public NoteViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            subtitle = itemView.findViewById(R.id.noteSubtitle);
            text = itemView.findViewById(R.id.noteText);
            noteImageView = itemView.findViewById(R.id.noteImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
