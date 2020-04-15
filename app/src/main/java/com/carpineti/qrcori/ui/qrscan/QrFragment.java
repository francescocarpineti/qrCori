package com.carpineti.qrcori.ui.qrscan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.carpineti.qrcori.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Objects;


public class QrFragment extends Fragment {

    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_qr, container, false);
        surfaceView = root.findViewById(R.id.camera_preview);

        barcodeDetector = new BarcodeDetector.Builder(getActivity()).setBarcodeFormats(Barcode.QR_CODE).build();

        cameraSource = new CameraSource.Builder(getActivity(), barcodeDetector).setRequestedPreviewSize(640, 480).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Permission error", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            //here we get data from QR codes and trigger actions
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() != 0) {
                    if (surfaceView.isActivated()) {
                        // needed to prevent multiple scans of the same QR Code
                        surfaceView.setActivated(false);
                        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(100);
                        // check if the QR code returns correct data (monument names)
                        String qrCodeText = qrCodes.valueAt(0).displayValue;
                        Log.d("QRCODETEXT", qrCodeText);
                        if (qrCodeText.equals("Tempio d'Ercole") || qrCodeText.equals("Tempio di Castore e Polluce")
                                                || qrCodeText.equals("Chiesa di S.Oliva")) {
                            getActivity().runOnUiThread(() -> {
                                Bundle qrCodeData = new Bundle();
                                String qrCodeText1 = qrCodes.valueAt(0).displayValue;
                                qrCodeData.putString("qrCodeText", qrCodeText1);
                                Navigation.findNavController(surfaceView).navigate(R.id.action_nav_main_qr_to_monumentFragment,
                                        qrCodeData);
                            });
                        }
                        else {
                            // invalid QR code
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Invalid QR Code", Toast.LENGTH_LONG).show());
                            try {
                                // wait 1 second before activating surfaceView again
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            surfaceView.setActivated(true);
                        }
                    }
                }
            }
        });

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (surfaceView != null) {
            // needed to reactivate QR code view
            surfaceView.setActivated(true);
        }
    }


}
