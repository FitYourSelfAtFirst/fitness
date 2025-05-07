package com.example.fitness;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerGender, spinnerAge, spinnerStrength, spinnerSpeed, spinnerEndurance;
    EditText editStrength, editSpeed, editEndurance;
    Button btnCalculate;
    TextView textResult;

    Python py;
    PyObject pyModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Chaquopy init
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        py = Python.getInstance();
        pyModule = py.getModule("myscript"); // Имя вашего Python-файла без .py

        // Найдите элементы UI
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerAge = findViewById(R.id.spinnerAge);
        spinnerStrength = findViewById(R.id.spinnerStrength);
        spinnerSpeed = findViewById(R.id.spinnerSpeed);
        spinnerEndurance = findViewById(R.id.spinnerEndurance);

        editStrength = findViewById(R.id.editStrength);
        editSpeed = findViewById(R.id.editSpeed);
        editEndurance = findViewById(R.id.editEndurance);

        btnCalculate = findViewById(R.id.btnCalculate);
        textResult = findViewById(R.id.textResult);

        // Заполняем пол (статично)
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("мужчины", "женщины"));
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // При выборе пола динамически заполняем остальные спиннеры
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String gender = getSpinnerValue(spinnerGender);

                // Возраст
                PyObject ages = pyModule.callAttr("get_ages", gender);
                List<String> ageList = new ArrayList<>();
                for (PyObject item : ages.asList()) ageList.add(item.toString());
                ArrayAdapter<String> ageAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, ageList);
                ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAge.setAdapter(ageAdapter);

                // Сила
                PyObject strength = pyModule.callAttr("get_strength_exercises", gender);
                List<String> strengthList = new ArrayList<>();
                for (PyObject item : strength.asList()) strengthList.add(item.toString());
                ArrayAdapter<String> strengthAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, strengthList);
                strengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerStrength.setAdapter(strengthAdapter);

                // Быстрота
                PyObject speed = pyModule.callAttr("get_speed_exercises", gender);
                List<String> speedList = new ArrayList<>();
                for (PyObject item : speed.asList()) speedList.add(item.toString());
                ArrayAdapter<String> speedAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, speedList);
                speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSpeed.setAdapter(speedAdapter);

                // Выносливость
                PyObject endurance = pyModule.callAttr("get_endurance_exercises", gender);
                List<String> enduranceList = new ArrayList<>();
                for (PyObject item : endurance.asList()) enduranceList.add(item.toString());
                ArrayAdapter<String> enduranceAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, enduranceList);
                enduranceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEndurance.setAdapter(enduranceAdapter);

                // После обновления спиннеров обновляем подсказки для полей ввода
                updateHint(editStrength, gender, "сила", getSpinnerValue(spinnerStrength));
                updateHint(editSpeed, gender, "быстрота", getSpinnerValue(spinnerSpeed));
                updateHint(editEndurance, gender, "выносливость", getSpinnerValue(spinnerEndurance));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // При выборе упражнения обновляем подсказки
        spinnerStrength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                updateHint(editStrength, getSpinnerValue(spinnerGender), "сила", getSpinnerValue(spinnerStrength));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                updateHint(editSpeed, getSpinnerValue(spinnerGender), "быстрота", getSpinnerValue(spinnerSpeed));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerEndurance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                updateHint(editEndurance, getSpinnerValue(spinnerGender), "выносливость", getSpinnerValue(spinnerEndurance));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Инициализируем остальные спиннеры при старте
        spinnerGender.setSelection(0);

        // Кнопка "Рассчитать"
        btnCalculate.setOnClickListener(v -> {
            resetFieldColors();

            String gender = getSpinnerValue(spinnerGender);
            String age = getSpinnerValue(spinnerAge);
            String strengthEx = getSpinnerValue(spinnerStrength);
            String v_s = editStrength.getText().toString().trim();
            String speedEx = getSpinnerValue(spinnerSpeed);
            String v_f = editSpeed.getText().toString().trim();
            String enduranceEx = getSpinnerValue(spinnerEndurance);
            String v_e = editEndurance.getText().toString().trim();

            boolean hasEmpty = false;

            if (v_s.isEmpty()) {
                editStrength.setBackgroundColor(Color.parseColor("#33FF0000")); // полупрозрачный красный
                hasEmpty = true;
            }
            if (v_f.isEmpty()) {
                editSpeed.setBackgroundColor(Color.parseColor("#33FF0000"));
                hasEmpty = true;
            }
            if (v_e.isEmpty()) {
                editEndurance.setBackgroundColor(Color.parseColor("#33FF0000"));
                hasEmpty = true;
            }
            if (hasEmpty) {
                textResult.setText("Пожалуйста, заполните все поля с результатами!");
                return;
            }

            if (gender.isEmpty() || age.isEmpty() || strengthEx.isEmpty() || speedEx.isEmpty() || enduranceEx.isEmpty()) {
                textResult.setText("Пожалуйста, выберите все параметры!");
                return;
            }

            PyObject result = pyModule.callAttr(
                    "calculate_all",
                    gender, age,
                    strengthEx, v_s,
                    speedEx, v_f,
                    enduranceEx, v_e
            );

            Map<PyObject, PyObject> map = result.asMap();
            if (map.containsKey(PyObject.fromJava("error"))) {
                textResult.setText("Ошибка: " + map.get(PyObject.fromJava("error")).toString());
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Сила: ").append(map.get(PyObject.fromJava("strength_points"))).append(" баллов\n");
            sb.append("Быстрота: ").append(map.get(PyObject.fromJava("speed_points"))).append(" баллов\n");
            sb.append("Выносливость: ").append(map.get(PyObject.fromJava("endurance_points"))).append(" баллов\n");
            sb.append("Сумма: ").append(map.get(PyObject.fromJava("total"))).append("\n");
            sb.append("Оценка: ").append(map.get(PyObject.fromJava("mark")));
            textResult.setText(sb.toString());
        });
    }

    // Обновление подсказки в EditText с диапазоном допустимых значений
    private void updateHint(EditText editText, String gender, String category, String exercise) {
        if (gender.isEmpty() || exercise.isEmpty()) {
            editText.setHint("");
            return;
        }
        PyObject range = pyModule.callAttr("get_exercise_range", gender, category, exercise);
        Map<PyObject, PyObject> rangeMap = range.asMap();
        String min = rangeMap.get(PyObject.fromJava("min")).toString();
        String max = rangeMap.get(PyObject.fromJava("max")).toString();
        editText.setHint("Допустимый диапазон: " + min + " - " + max);
    }

    // Сброс цвета фона полей ввода
    private void resetFieldColors() {
        editStrength.setBackgroundColor(Color.TRANSPARENT);
        editSpeed.setBackgroundColor(Color.TRANSPARENT);
        editEndurance.setBackgroundColor(Color.TRANSPARENT);
    }

    // Безопасное получение выбранного значения из Spinner
    private String getSpinnerValue(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return (item != null) ? item.toString() : "";
    }
}
