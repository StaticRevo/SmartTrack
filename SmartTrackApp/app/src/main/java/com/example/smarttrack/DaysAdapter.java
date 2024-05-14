package com.example.smarttrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.graphics.Color;
import java.util.Calendar;
import java.util.List;

public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayViewHolder> {

    private List<DayInfo> days;
    private Context context;
    private OnDayClickListener mListener;

    private int selectedPosition = -1;

    public DaysAdapter(Context context, List<DayInfo> days) {
        this.context = context;
        this.days = days;
    }

    public interface OnDayClickListener {
        void onDayClick(int position);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.day_item, parent, false);
        return new DayViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayInfo dayInfo = days.get(position);
        String dayOfWeekShort = dayInfo.getDayOfWeek().substring(0, Math.min(dayInfo.getDayOfWeek().length(), 3));
        holder.dayTextView.setText(dayOfWeekShort);
        holder.dateTextView.setText(dayInfo.getDate());

        // Get current date
        String currentDate = new SimpleDateFormat("d", Locale.getDefault()).format(Calendar.getInstance().getTime());

        // If the date of the DayInfo object matches the current date, change the text color to green
        // if (dayInfo.getDate().equals(currentDate)) {
        //     holder.dateTextView.setTextColor(Color.parseColor("#67B387"));
        //} else {
        //   holder.dateTextView.setTextColor(Color.BLACK); // Change color back to default for recycled views
        //}

        // Change the color based on the selected position
        if (position == selectedPosition) {
            holder.dateTextView.setTextColor(Color.parseColor("#67B387")); // Green color for selected day
        } else {
            holder.dateTextView.setTextColor(Color.BLACK); // Default color for other days
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }


    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        TextView dateTextView;

        public DayViewHolder(View itemView, final OnDayClickListener listener) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDayClick(position);
                        }
                    }
                }
            });
        }
    }

    // Add this method to update the selected position
    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(position);
    }

}