package br.com.acailove;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.acailove.data.DatabasePedidos;
import br.com.acailove.data.DatabaseProdutos;

public class MenuActivity extends AppCompatActivity {

    private PedidoAdapter adapter;
    private DatabasePedidos databasePedidos;
    private DatabaseProdutos databaseProdutos;
    private HashMap<String, Integer> produtosSelecionados = new HashMap<>();


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_menu);

            databasePedidos = new DatabasePedidos(this);
            databaseProdutos = new DatabaseProdutos(this);

            RecyclerView recyclerView = findViewById(R.id.recyclerViewPedidos);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            List<Pedido> pedidosNaoFinalizados = databasePedidos.getPedidosNaoFinalizados();
            adapter = new PedidoAdapter(pedidosNaoFinalizados, databasePedidos);
            recyclerView.setAdapter(adapter);

        Button btnProdutos = findViewById(R.id.btnProdutos);
        btnProdutos.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ProdutosActivity.class);
            startActivity(intent);
        });

        Button btnAdicionarPedido = findViewById(R.id.btnAdicionarPedido);
        btnAdicionarPedido.setOnClickListener(v -> mostrarDialogoAdicionarPedido());
    }

    @SuppressLint("SetTextI18n")
    private void mostrarDialogoAdicionarPedido() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Configurar campo "Nome do Cliente"
        EditText editNomeCliente = new EditText(this);
        editNomeCliente.setHint("Nome do Cliente");
        editNomeCliente.setPadding(16, 16, 16, 16);
        layout.addView(editNomeCliente);

        // Configurar campo "Telefone"
        EditText editTelefone = new EditText(this);
        editTelefone.setHint("Telefone");
        editTelefone.setPadding(16, 16, 16, 16);
        layout.addView(editTelefone);

        // Configurar campo "Endereço"
        EditText editEndereco = new EditText(this);
        editEndereco.setHint("Endereço");
        editEndereco.setPadding(16, 16, 16, 16);
        layout.addView(editEndereco);

        // Layout para exibir "Produtos" e o botão de seleção
        LinearLayout produtoLayout = new LinearLayout(this);
        produtoLayout.setOrientation(LinearLayout.HORIZONTAL);
        produtoLayout.setPadding(0, 16, 0, 16);
        produtoLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView textProdutos = new TextView(this);
        textProdutos.setText("Produtos:");
        textProdutos.setTextSize(16);
        textProdutos.setPadding(0, 0, 16, 0);
        produtoLayout.addView(textProdutos);

        TextView produtosSelecionadosView = new TextView(this);
        produtosSelecionadosView.setHint("Selecionar Produtos");
        produtosSelecionadosView.setTextSize(16);
        produtosSelecionadosView.setPadding(16, 0, 16, 0);
        produtoLayout.addView(produtosSelecionadosView);

        Button buttonSelecionarProdutos = getButton(produtosSelecionadosView);
        buttonSelecionarProdutos.setText("Selecionar");
        buttonSelecionarProdutos.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
        buttonSelecionarProdutos.setTextColor(Color.WHITE);
        produtoLayout.addView(buttonSelecionarProdutos);
        layout.addView(produtoLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Novo Pedido")
                .setView(layout)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String nomeCliente = editNomeCliente.getText().toString();
                    String endereco = editEndereco.getText().toString();
                    String telefone = editTelefone.getText().toString();
                    String status = "Em Preparo";

                    if (!nomeCliente.isEmpty() && !endereco.isEmpty() && !telefone.isEmpty() && !produtosSelecionadosView.getText().toString().isEmpty()) {
                        int pedidoId = (int) databasePedidos.inserirPedido(nomeCliente, endereco, telefone, status);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            produtosSelecionados.forEach((produto, quantidade) -> {
                                int produtoId = obterIdProduto(produto);
                                if (produtoId != -1 && quantidade > 0) {
                                    for (int i = 0; i < quantidade; i++) {
                                        databasePedidos.adicionarProdutoAoPedido(pedidoId, produtoId);
                                    }
                                }
                            });
                        }

                        atualizarLista();
                        Toast.makeText(MenuActivity.this, "Pedido Criado!", Toast.LENGTH_SHORT).show();
                        enviarMensagemWhatsApp(telefone, "Seu pedido foi criado com o status: " + status);
                    } else {
                        Toast.makeText(MenuActivity.this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private @NonNull Button getButton(TextView produtosSelecionadosView) {
        Button buttonSelecionarProdutos = new Button(this);
        buttonSelecionarProdutos.setOnClickListener(v -> {
            List<String> produtos = obterListaProdutos();
            produtosSelecionados.clear();

            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(32, 32, 32, 32);

            for (String produto : produtos) {
                LinearLayout produtoItemLayout = new LinearLayout(this);
                produtoItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                produtoItemLayout.setPadding(0, 8, 0, 8);
                produtoItemLayout.setGravity(Gravity.CENTER_VERTICAL);

                TextView produtoNome = new TextView(this);
                produtoNome.setText(produto);
                produtoNome.setPadding(0, 0, 16, 0);
                produtoNome.setTextSize(16);
                produtoItemLayout.addView(produtoNome);

                Button btnMenos = getButton(produto, produtosSelecionados, produtosSelecionadosView);
                btnMenos.setText("-");
                btnMenos.setTextColor(Color.WHITE);
                btnMenos.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
                produtoItemLayout.addView(btnMenos);

                TextView quantidadeView = new TextView(this);
                quantidadeView.setText("0");
                quantidadeView.setPadding(6, 0, 6, 0);
                produtoItemLayout.addView(quantidadeView);

                Button btnMais = new Button(this);
                btnMais.setText("+");
                btnMais.setTextColor(Color.WHITE);
                btnMais.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
                btnMais.setOnClickListener(btn -> {
                    int quantidadeAtual = 0;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        quantidadeAtual = produtosSelecionados.getOrDefault(produto, 0);
                    }
                    produtosSelecionados.put(produto, quantidadeAtual + 1);
                    quantidadeView.setText(String.valueOf(produtosSelecionados.get(produto)));
                    atualizarProdutosSelecionados(produtosSelecionados, produtosSelecionadosView);
                });
                produtoItemLayout.addView(btnMais);

                dialogLayout.addView(produtoItemLayout);
            }

            AlertDialog.Builder produtosDialog = new AlertDialog.Builder(this);
            produtosDialog.setTitle("Selecione Produtos e Quantidades")
                    .setView(dialogLayout)
                    .setPositiveButton("Confirmar", (dialog, which) -> {
                        atualizarProdutosSelecionados(produtosSelecionados, produtosSelecionadosView);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
            produtosDialog.show();
        });
        return buttonSelecionarProdutos;
    }

    private @NonNull Button getButton(String produto, HashMap<String, Integer> produtosSelecionados, TextView produtosSelecionadosView) {
        Button btnMenos = new Button(this);
        btnMenos.setText("-");
        btnMenos.setOnClickListener(btn -> {
            int quantidadeAtual = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                quantidadeAtual = produtosSelecionados.getOrDefault(produto, 0);
            }
            if (quantidadeAtual > 0) {
                produtosSelecionados.put(produto, quantidadeAtual - 1);
                atualizarProdutosSelecionados(produtosSelecionados, produtosSelecionadosView);
            }
        });
        return btnMenos;
    }

    private void atualizarProdutosSelecionados(HashMap<String, Integer> produtosSelecionados, TextView produtosSelecionadosView) {
        StringBuilder produtosTexto = new StringBuilder();
        for (Map.Entry<String, Integer> entry : produtosSelecionados.entrySet()) {
            if (entry.getValue() > 0) {
                produtosTexto.append(entry.getKey()).append(" (").append(entry.getValue()).append("), ");
            }
        }
        produtosSelecionadosView.setText(produtosTexto.length() > 0 ? produtosTexto.substring(0, produtosTexto.length() - 2) : "Nenhum produto selecionado");
    }

    private List<String> obterListaProdutos() {
        List<String> produtos = new ArrayList<>();
        Cursor cursor = databaseProdutos.obterTodosOsProdutos();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String nomeProduto = cursor.getString(cursor.getColumnIndex("nome"));
                produtos.add(nomeProduto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return produtos;
    }

    private int obterIdProduto(String nomeProduto) {
        Cursor cursor = databaseProdutos.obterTodosOsProdutos();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String produto = cursor.getString(cursor.getColumnIndex("nome"));
                if (produto.equals(nomeProduto)) {
                    cursor.close();
                    return id;
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return -1;
    }

    private void atualizarLista() {
        List<Pedido> pedidosNaoFinalizados = databasePedidos.getPedidosNaoFinalizados();
        adapter.updatePedidos(pedidosNaoFinalizados);

        if (!pedidosNaoFinalizados.isEmpty()) {
            RecyclerView recyclerView = findViewById(R.id.recyclerViewPedidos);
            recyclerView.smoothScrollToPosition(pedidosNaoFinalizados.size() - 1);
        }
    }

    private void enviarMensagemWhatsApp(String numeroCliente, String mensagem) {
        String numeroFormatado = "+55" + numeroCliente;
        String url = "https://wa.me/" + numeroFormatado + "?text=" + Uri.encode(mensagem);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
