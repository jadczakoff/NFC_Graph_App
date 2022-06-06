package com.jd.nfc_graph_app;

import android.content.Context;
import android.service.autofill.TextValueSanitizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements RecyclerViewInterface{
    // Tworzymy obiekt dla klasy
    private final RecyclerViewInterface recyclerViewInterface;

    String data1[];
    Context context;
    // Tworzymy konstruktor i przypisujemy zmienne
    public MyAdapter(Context ct, String s1[], RecyclerViewInterface recyclerViewInterface) {
        context = ct;
        data1 = s1;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    // W ponizszej metodzie przechowujemy layout ,,my_row"
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater Inflater = LayoutInflater.from(context);
        View view = Inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view, recyclerViewInterface);
    }

    // Z layout ,,my_row" pobieramy TextView i przypisujemy mu nowa wartosc z listy item'ow w pliku strings.xml
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(data1[position]);

    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    @Override
    public void onItemClick(int position) {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            textView = itemView.findViewById(R.id.types_params);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Sprawdzamy czy recyclerViewInterface nie jest pusty, jezeli nie jest to pobieramy
                    // aktualna pozycje zrobionego przycisku
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();
                        // Sprawdzamy czy aktualna pozycja jest dostepna do kliknecia, jezeli jest to
                        // wywolujemy metode onItemClick
                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
