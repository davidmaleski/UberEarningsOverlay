package com.example.uberearningsoverlay.ui.theme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uberearningsoverlay.R;

public class SettingsActivity extends AppCompatActivity {
    private EditText editTextRate;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editTextRate = findViewById(R.id.editTextRate);
        Button buttonSave = findViewById(R.id.buttonSave);

        // Carregar a taxa salva (se existir)
        sharedPreferences = getSharedPreferences("UberEarningsPrefs", MODE_PRIVATE);
        float savedRate = sharedPreferences.getFloat("ratePerKm", 1.80f); // 1.80 é o valor padrão
        editTextRate.setText(String.valueOf(savedRate));

        // Salvar a nova taxa quando o botão for clicado
        buttonSave.setOnClickListener(v -> {
            try {
                float newRate = Float.parseFloat(editTextRate.getText().toString());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("ratePerKm", newRate);
                editor.apply();
                finish(); // Fechar a tela
            } catch (NumberFormatException e) {
                editTextRate.setError("Digite um valor válido");
            }
        });
    }
}