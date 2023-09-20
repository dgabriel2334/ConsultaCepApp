package com.example.projetopoo;
import android.app.Dialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText cepEditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permissão de política de rede estrita apenas para fins de demonstração. Não recomendado em produção.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        cepEditText = findViewById(R.id.cepEditText);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cep = cepEditText.getText().toString();
                if (!cep.isEmpty()) {
                    if (isValidCEP(cep)) {
                        // Fazer a chamada da API
                        String apiUrl = "https://viacep.com.br/ws/" + cep + "/json/";
                        String jsonResponse = makeAPICall(apiUrl);

                        // Processar o resultado JSON e exibir o modal
                        showAddressModal(jsonResponse);
                    } else {
                        Toast.makeText(MainActivity.this, "CEP inválido.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Por favor, insira um CEP.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidCEP(String cep) {
        return cep.matches("\\d{5}-\\d{3}");
    }

    private String makeAPICall(String apiUrl) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            } else {
                // Trate os casos em que a resposta não foi bem-sucedida
                System.out.println("Código de resposta da API não foi 200: " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    private void showAddressModal(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Extrair os campos do endereço
            String logradouro = jsonObject.optString("logradouro");
            String bairro = jsonObject.optString("bairro");
            String localidade = jsonObject.optString("localidade");
            String uf = jsonObject.optString("uf");

            // Criar um Dialog personalizado para exibir o resultado
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.modal_layout);

            // Configurar os campos no modal
            TextView modalTitle = dialog.findViewById(R.id.modalTitle);
            TextView cepResultTextView = dialog.findViewById(R.id.cepResultTextView);
            TextView logradouroResultTextView = dialog.findViewById(R.id.logradouroResultTextView);
            TextView bairroResultTextView = dialog.findViewById(R.id.bairroResultTextView);
            TextView cidadeUfResultTextView = dialog.findViewById(R.id.cidadeUfResultTextView);
            Button closeButton = dialog.findViewById(R.id.closeButton);

            modalTitle.setText("Endereço");
            String cep = "";
            cepResultTextView.setText("CEP: " + cep);
            logradouroResultTextView.setText("Logradouro: " + logradouro);
            bairroResultTextView.setText("Bairro: " + bairro);
            cidadeUfResultTextView.setText("Cidade/UF: " + localidade + "/" + uf);

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            // Exibir o modal
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
