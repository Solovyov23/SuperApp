package com.example.gentl.superapp.Tabs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gentl.superapp.R;

import static android.content.Context.SENSOR_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GyroscopeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GyroscopeFragment extends Fragment implements SensorEventListener
{
    public GyroscopeFragment() 
	{
        // Required empty public constructor
    }

    public static GyroscopeFragment newInstance() 
	{
        GyroscopeFragment fragment = new GyroscopeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
    }

    private SensorManager sensorManager;

    // Image for effect VR
    ImageView mDrawable;

    private float x = 0;
    private float y = 0;

    private float accelerationMultiplier = 50.f; //speed
    private float margin = 10.f;

    TextView textView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) 
	{
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gyroscope, container, false);
        mDrawable = view.findViewById(R.id.movableImageView);
        mDrawable.setImageResource(R.drawable.nanure_image);

        // Add an image and set it's size
        Drawable d = getResources().getDrawable(R.drawable.nanure_image);
        mDrawable.getLayoutParams().width = d.getIntrinsicWidth();
        mDrawable.getLayoutParams().height = d.getIntrinsicHeight();

        sensorManager = (SensorManager) view.getContext().getSystemService(SENSOR_SERVICE);

        return view;
    }

    @Override
    public void onResume()
	{
        super.onResume();

        // Subscribe to the event
        sensorManager.registerListener
		(
			this,
			sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
			SensorManager.SENSOR_DELAY_GAME
		);
    }

    @Override
    public void onPause() 
	{
        super.onPause();
        // Unsubscribe from event
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    @Override
    public void onSensorChanged(SensorEvent event) 
	{

        // VR effect
        if( event.sensor.getType() == Sensor.TYPE_GYROSCOPE ) 
		{
            x +=  event.values[1] * accelerationMultiplier;
            y +=  event.values[0] * accelerationMultiplier;

            ViewGroup parent = (ViewGroup)mDrawable.getParent();

            // Max coords
            if(x >= margin) 
			{
                x = margin;
            }
			
            if(y >= margin) 
			{
                y = margin;
            }

            // Min coords
            if(x <=  -mDrawable.getLayoutParams().width  + (parent.getMeasuredWidth()  - margin) ) 
			{
                x = -mDrawable.getLayoutParams().width   + (parent.getMeasuredWidth()  - margin);
            }
            if(y <=  -mDrawable.getLayoutParams().height + (parent.getMeasuredHeight() - margin) ) 
			{
                y = -mDrawable.getLayoutParams().height  + (parent.getMeasuredHeight() - margin);
            }

            mDrawable.setY(y);
            mDrawable.setX(x);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{

    }

    @Override
    public void onAttach(Context context) 
	{
        super.onAttach(context);
    }

    @Override
    public void onDetach() 
	{
        super.onDetach();
    }
}
