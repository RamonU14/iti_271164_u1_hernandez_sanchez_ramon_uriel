package upvictoria.pm_ene_abr_2024.iti_271164.pi1u1.hernandez_sanchez;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText functionInput = findViewById(R.id.functionInput);
        final EditText xValueInput = findViewById(R.id.xValueInput);
        final EditText toleranceInput = findViewById(R.id.toleranceInput);
        final RadioGroup toleranceTypeGroup = findViewById(R.id.toleranceTypeGroup);
        final Spinner precisionSpinner = findViewById(R.id.precisionSpinner);
        Button calculateButton = findViewById(R.id.calculateButton);
        Button newtonMethodButton = findViewById(R.id.newtonMethodButton);
        final TextView resultTextView = findViewById(R.id.resultTextView);
        TextView precisionTitle = findViewById(R.id.precisionTitle); // Asegúrate de tener un TextView con este id en tu layout
        precisionTitle.setText("Dígitos después del punto decimal:"); // Establece el texto del título


        // Configuración del Spinner para los dígitos de precisión
        Integer[] items = new Integer[21];
        for (int i = 0; i <= 20; i++) {
            items[i] = i;
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        precisionSpinner.setAdapter(adapter);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String function = functionInput.getText().toString();
                if (function.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingrese la función.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double xValue;
                try {
                    xValue = Double.parseDouble(xValueInput.getText().toString());
                } catch (NumberFormatException e) {
                    resultTextView.setText("Por favor, ingrese un valor válido para x.");
                    return;
                }
                double derivativeResult = calculateDerivative(function, xValue);
                resultTextView.setText("Derivada en x=" + xValue + ": " + derivativeResult);
            }
        });

        newtonMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String function = functionInput.getText().toString();
                if (function.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingrese la función.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double xValue;
                double tolerance;
                try {
                    xValue = Double.parseDouble(xValueInput.getText().toString());
                    tolerance = Double.parseDouble(toleranceInput.getText().toString());
                } catch (NumberFormatException e) {
                    resultTextView.setText("Por favor, ingrese valores válidos para x y la tolerancia.");
                    return;
                }
                int toleranceType = toleranceTypeGroup.getCheckedRadioButtonId() == R.id.radioFinalPoint ? 0 : 1;
                int precision = (int) precisionSpinner.getSelectedItem();

                double newtonResult = newtonMethod(function, xValue, tolerance, toleranceType, precision);
                if (Double.isNaN(newtonResult)) {
                    resultTextView.setText("El método no convergió con los parámetros dados.");
                } else {
                    resultTextView.setText(String.format("Raíz encontrada en x = %." + precision + "f", newtonResult));
                }
            }
        });

    }

    private double calculateDerivative(String function, double point) {
        // Implementación existente de la derivada
        double h = 0.0001;
        Expression exp = new ExpressionBuilder(function)
                .variables("x")
                .build()
                .setVariable("x", point + h);
        double f_x_plus_h = exp.evaluate();
        exp.setVariable("x", point);
        double f_x = exp.evaluate();
        return (f_x_plus_h - f_x) / h;
    }

    private double newtonMethod(String function, double initialGuess, double tolerance, int toleranceType, int precision) {
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.removeAllViews(); // Limpia la tabla antes de agregar nuevas filas

        // Agrega la cabecera de la tabla
        addTableRow(tableLayout, "Iteración", "x", "F(X)", "|xi - xi-1|");

        double x0 = initialGuess;
        double x1 = initialGuess; // Inicializa x1 con el valor inicial de x0
        double f_x = calculateFunctionValue(function, x0); // Calcula F(X) para x0
        double f_prime_x;
        double difference = 0; // Inicializa la diferencia a 0

        int i = 0; // Inicializa el contador de iteraciones

        // La primera fila de la tabla se agrega antes del bucle
        addTableRow(tableLayout, String.valueOf(i), String.format("%." + precision + "f", x0), String.format("%." + precision + "f", f_x), "N/A");

        do {
            i++; // Incrementa el contador de iteraciones al comienzo del bucle

            f_prime_x = calculateDerivative(function, x0);
            if (Math.abs(f_prime_x) < 1E-10) {
                addTableRow(tableLayout, String.valueOf(i), "Error", "División por cero en la derivada", "");
                return Double.NaN;
            }

            x1 = x0 - f_x / f_prime_x; // Calcula el nuevo valor de x
            difference = Math.abs(x1 - x0); // Calcula la diferencia

            // Verifica la condición de parada antes de añadir una nueva fila y actualizar x0
            if ((toleranceType == 0 && difference <= tolerance) || (toleranceType == 1 && Math.abs(f_x) <= tolerance)) {
                break;
            }

            f_x = calculateFunctionValue(function, x1); // Calcula F(X) para el nuevo x1
            // Añade la fila a la tabla para la iteración actual
            addTableRow(tableLayout, String.valueOf(i), String.format("%." + precision + "f", x1), String.format("%." + precision + "f", f_x), String.format("%." + precision + "f", difference));

            x0 = x1; // Actualiza x0 con el nuevo valor de x1 para la siguiente iteración

        } while (true); // El bucle se repite hasta que se cumple la condición de parada

        // Añade la última fila con el resultado final
        addTableRow(tableLayout, String.valueOf(i), String.format("%." + precision + "f", x1), String.format("%." + precision + "f", calculateFunctionValue(function, x1)), String.format("%." + precision + "f", difference));

        // Retorna el último valor de x1, que debería ser la raíz encontrada
        return x1;
    }



    private void addTableRow(TableLayout tableLayout, String... values) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        for (String value : values) {
            TextView textView = new TextView(this);
            textView.setText(value);
            textView.setPadding(5, 5, 5, 5);
            // Puedes ajustar aquí para diferenciar visualmente la cabecera de las otras filas si es necesario
            tableRow.addView(textView);
        }

        tableLayout.addView(tableRow);
    }




    private double calculateFunctionValue(String function, double x) {
        Expression exp = new ExpressionBuilder(function)
                .variables("x")
                .build()
                .setVariable("x", x);
        return exp.evaluate();
    }
}

