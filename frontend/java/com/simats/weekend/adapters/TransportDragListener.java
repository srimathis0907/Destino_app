package com.simats.weekend.adapters;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.View;
import com.simats.weekend.R;
import com.simats.weekend.models.Transport;
import java.util.List;

public class TransportDragListener implements View.OnDragListener {

    private final TransportDropListener listener;
    private final List<Transport> transportOptions;

    public interface TransportDropListener {
        void onTransportDropped(Transport transport);
    }

    public TransportDragListener(TransportDropListener listener, List<Transport> transportOptions) {
        this.listener = listener;
        this.transportOptions = transportOptions;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return event.getClipDescription().hasMimeType("text/plain");

            case DragEvent.ACTION_DRAG_ENTERED:
                v.setBackgroundResource(R.drawable.drag_hover_background);
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                v.setBackgroundResource(R.drawable.drag_normal_background);
                return true;

            case DragEvent.ACTION_DROP:
                ClipData.Item item = event.getClipData().getItemAt(0);
                int position = Integer.parseInt(item.getText().toString());
                Transport droppedTransport = transportOptions.get(position);
                if (listener != null) {
                    listener.onTransportDropped(droppedTransport);
                }
                v.setBackgroundResource(R.drawable.drag_normal_background);
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                v.setBackgroundResource(R.drawable.drag_normal_background);
                return true;

            default:
                return false;
        }
    }
}