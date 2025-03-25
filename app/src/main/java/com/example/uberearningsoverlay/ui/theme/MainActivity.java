package com.example.uberearningsoverlay.ui.theme;

import com.example.uberearningsoverlay.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView overlayText;
    private ActivityResultLauncher<Intent> overlayPermissionLauncher;
    private ActivityResultLauncher<Intent> accessibilityServiceLauncher;
    private Handler handler;
    private float ratePerKm;
    private SharedPreferences sharedPreferences;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private float initialX, initialY;
    private float initialTouchX, initialTouchY;
    private boolean isDragging = false;
    private boolean isOverlayActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Log.d(TAG, "onCreate: Iniciando MainActivity");
            setContentView(R.layout.activity_main);
            Log.d(TAG, "onCreate: Layout definido com sucesso");

            handler = new Handler(Looper.getMainLooper());
            sharedPreferences = getSharedPreferences("UberEarningsPrefs", MODE_PRIVATE);
            ratePerKm = sharedPreferences.getFloat("ratePerKm", 1.80f);
            Log.d(TAG, "onCreate: SharedPreferences carregado, ratePerKm = " + ratePerKm);

            // Configurar os botões
            Button buttonToggleOverlay = findViewById(R.id.buttonToggleOverlay);
            Button buttonSettings = findViewById(R.id.buttonSettings);

            buttonToggleOverlay.setOnClickListener(v -> {
                if (isOverlayActive) {
                    removeOverlay();
                    buttonToggleOverlay.setText("Ativar Sobreposição");
                } else {
                    checkOverlayPermission();
                }
            });

            buttonSettings.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            });

            // Launcher para permissão de sobreposição
            overlayPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Log.d(TAG, "overlayPermissionLauncher: Resultado recebido");
                        if (Settings.canDrawOverlays(this)) {
                            Log.d(TAG, "overlayPermissionLauncher: Permissão de sobreposição concedida");
                            showOverlay();
                            checkAccessibilityService();
                            Button buttonToggleOverlayInner = findViewById(R.id.buttonToggleOverlay);
                            buttonToggleOverlayInner.setText("Desativar Sobreposição");
                        } else {
                            Log.d(TAG, "overlayPermissionLauncher: Permissão de sobreposição negada");
                            Toast.makeText(this, "Permissão de sobreposição negada", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            // Launcher para ativar o AccessibilityService
            accessibilityServiceLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Log.d(TAG, "accessibilityServiceLauncher: Resultado recebido");
                        if (isAccessibilityServiceEnabled()) {
                            Log.d(TAG, "accessibilityServiceLauncher: AccessibilityService ativado");
                            startDistanceMonitoring();
                        } else {
                            Log.d(TAG, "accessibilityServiceLauncher: AccessibilityService não ativado");
                            Toast.makeText(this, "Por favor, ative o Accessibility Service", Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Erro ao iniciar MainActivity", e);
            Toast.makeText(this, "Erro ao iniciar o app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkOverlayPermission() {
        try {
            Log.d(TAG, "checkOverlayPermission: Verificando permissão de sobreposição");
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "checkOverlayPermission: Solicitando permissão de sobreposição");
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:" + getPackageName())
                );
                overlayPermissionLauncher.launch(intent);
            } else {
                Log.d(TAG, "checkOverlayPermission: Permissão de sobreposição já concedida");
                showOverlay();
                checkAccessibilityService();
                Button buttonToggleOverlay = findViewById(R.id.buttonToggleOverlay);
                buttonToggleOverlay.setText("Desativar Sobreposição");
            }
        } catch (Exception e) {
            Log.e(TAG, "checkOverlayPermission: Erro ao verificar permissão", e);
        }
    }

    private void checkAccessibilityService() {
        try {
            Log.d(TAG, "checkAccessibilityService: Verificando AccessibilityService");
            if (!isAccessibilityServiceEnabled()) {
                Log.d(TAG, "checkAccessibilityService: AccessibilityService não está ativado");
                Toast.makeText(this, "Por favor, ative o Accessibility Service para capturar dados", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                accessibilityServiceLauncher.launch(intent);
            } else {
                Log.d(TAG, "checkAccessibilityService: AccessibilityService já está ativado");
                startDistanceMonitoring();
            }
        } catch (Exception e) {
            Log.e(TAG, "checkAccessibilityService: Erro ao verificar AccessibilityService", e);
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        try {
            Log.d(TAG, "isAccessibilityServiceEnabled: Verificando se o serviço está ativado");
            String service = getPackageName() + "/" + UberAccessibilityService.class.getCanonicalName();
            int accessibilityEnabled = 0;
            try {
                accessibilityEnabled = Settings.Secure.getInt(
                        getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_ENABLED
                );
                Log.d(TAG, "isAccessibilityServiceEnabled: accessibilityEnabled = " + accessibilityEnabled);
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "isAccessibilityServiceEnabled: Erro ao verificar ACCESSIBILITY_ENABLED", e);
            }
            if (accessibilityEnabled == 1) {
                String settingValue = Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                );
                if (settingValue != null) {
                    boolean enabled = settingValue.contains(service);
                    Log.d(TAG, "isAccessibilityServiceEnabled: Serviço está ativado = " + enabled);
                    return enabled;
                }
            }
            Log.d(TAG, "isAccessibilityServiceEnabled: Serviço não está ativado");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "isAccessibilityServiceEnabled: Erro ao verificar serviço", e);
            return false;
        }
    }

    private void showOverlay() {
        try {
            Log.d(TAG, "showOverlay: Criando sobreposição");
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    android.graphics.PixelFormat.TRANSLUCENT
            );
            params.gravity = 0;

            overlayText = new TextView(this);
            overlayText.setText("R$ 0,00");
            overlayText.setTextColor(android.graphics.Color.WHITE);
            overlayText.setBackgroundColor(android.graphics.Color.BLACK);
            overlayText.setPadding(10, 10, 10, 10);
            overlayText.setTextSize(16);

            overlayText.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        long pressTime = System.currentTimeMillis();
                        handler.postDelayed(() -> {
                            if (System.currentTimeMillis() - pressTime < 200) {
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                            }
                        }, 200);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        isDragging = true;
                        params.x = (int) (initialX + (event.getRawX() - initialTouchX));
                        params.y = (int) (initialY + (event.getRawY() - initialTouchY));
                        windowManager.updateViewLayout(overlayText, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isDragging) {
                            isDragging = false;
                            return true;
                        }
                        return false;
                }
                return false;
            });

            windowManager.addView(overlayText, params);
            isOverlayActive = true;
            Log.d(TAG, "showOverlay: Sobreposição criada com sucesso");
        } catch (Exception e) {
            Log.e(TAG, "showOverlay: Erro ao criar sobreposição", e);
            Toast.makeText(this, "Erro ao criar sobreposição: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void removeOverlay() {
        try {
            if (overlayText != null && windowManager != null) {
                windowManager.removeView(overlayText);
                overlayText = null;
                isOverlayActive = false;
                handler.removeCallbacksAndMessages(null);
                Log.d(TAG, "removeOverlay: Sobreposição removida com sucesso");
            }
        } catch (Exception e) {
            Log.e(TAG, "removeOverlay: Erro ao remover sobreposição", e);
        }
    }

    private void startDistanceMonitoring() {
        try {
            Log.d(TAG, "startDistanceMonitoring: Iniciando monitoramento de distância");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateOverlay();
                    handler.postDelayed(this, 1000);
                }
            }, 1000);
        } catch (Exception e) {
            Log.e(TAG, "startDistanceMonitoring: Erro ao iniciar monitoramento", e);
        }
    }

    private void updateOverlay() {
        try {
            ratePerKm = sharedPreferences.getFloat("ratePerKm", 1.80f);
            String distanceText = UberAccessibilityService.distance;
            if (distanceText != null) {
                try {
                    String distanceStr = distanceText.replaceAll("[^0-9.]", "");
                    float distance = Float.parseFloat(distanceStr);
                    float earnings = distance * ratePerKm;
                    overlayText.setText(String.format("R$ %.2f", earnings));
                    Log.d(TAG, "updateOverlay: Sobreposição atualizada: " + String.format("R$ %.2f", earnings));
                } catch (Exception e) {
                    overlayText.setText("Erro ao calcular");
                    Log.e(TAG, "updateOverlay: Erro ao calcular ganhos", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "updateOverlay: Erro ao atualizar sobreposição", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            removeOverlay();
            Log.d(TAG, "onDestroy: MainActivity destruída");
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: Erro ao destruir MainActivity", e);
        }
    }
}