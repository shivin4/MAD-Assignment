package com.example.unitconverter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    String[] units = {"Feet", "Inches", "Centimeters", "Meters", "Yards"};
    EditText inputValue;
    TextView resultValue, formulaText;
    Spinner fromUnitSpinner, toUnitSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme setting from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_theme", false);

        // Apply theme before view setup
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        setTheme(isDark ? R.style.AppTheme_Dark : R.style.AppTheme_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar and dynamically apply popupTheme
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(isDark ? R.style.PopupTheme_Dark : R.style.PopupTheme_Light);
        setSupportActionBar(toolbar);

        inputValue = findViewById(R.id.inputValue);
        resultValue = findViewById(R.id.resultValue);
        formulaText = findViewById(R.id.formulaText);
        fromUnitSpinner = findViewById(R.id.fromUnitSpinner);
        toUnitSpinner = findViewById(R.id.toUnitSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units);
        fromUnitSpinner.setAdapter(adapter);
        toUnitSpinner.setAdapter(adapter);

        fromUnitSpinner.setSelection(3); // Default: Meters
        toUnitSpinner.setSelection(2);   // Default: Centimeters

        // Handle input changes
        inputValue.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateConversion();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Handle unit selections
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateConversion();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        fromUnitSpinner.setOnItemSelectedListener(spinnerListener);
        toUnitSpinner.setOnItemSelectedListener(spinnerListener);

        updateConversion();
    }

    private void updateConversion() {
        String inputStr = inputValue.getText().toString();
        String from = fromUnitSpinner.getSelectedItem().toString();
        String to = toUnitSpinner.getSelectedItem().toString();

        if (inputStr.isEmpty()) {
            resultValue.setText("0");
            formulaText.setText(getFormula(from, to));
            return;
        }

        double input;
        try {
            input = Double.parseDouble(inputStr);
        } catch (NumberFormatException e) {
            resultValue.setText("0");
            formulaText.setText(getFormula(from, to));
            return;
        }

        double result = convertLength(input, from, to);
        resultValue.setText(String.format("%.4f", result));
        formulaText.setText(getFormula(from, to));
    }

    private double convertLength(double value, String from, String to) {
        double meters;
        switch (from) {
            case "Feet": meters = value * 0.3048; break;
            case "Inches": meters = value * 0.0254; break;
            case "Centimeters": meters = value * 0.01; break;
            case "Yards": meters = value * 0.9144; break;
            case "Meters":
            default: meters = value; break;
        }

        switch (to) {
            case "Feet": return meters / 0.3048;
            case "Inches": return meters / 0.0254;
            case "Centimeters": return meters / 0.01;
            case "Yards": return meters / 0.9144;
            case "Meters":
            default: return meters;
        }
    }

    private String getFormula(String from, String to) {
        double testInput = 1.0;
        double output = convertLength(testInput, from, to);
        return String.format("Formula: multiply the %s value by %.4f", from.toLowerCase(), output);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings_menu) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
