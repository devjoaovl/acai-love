package br.com.acailove;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acailove.data.DatabaseProdutos;

public class ProdutosActivity extends AppCompatActivity {

    private DatabaseProdutos databaseProdutos;
    private ProdutoAdapter produtoAdapter;
    private List<Produto> listaProdutos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produtos);

        databaseProdutos = new DatabaseProdutos(this);
        listaProdutos = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewProdutos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        produtoAdapter = new ProdutoAdapter(listaProdutos);
        recyclerView.setAdapter(produtoAdapter);

        Button buttonAdicionarProduto = findViewById(R.id.buttonAdicionarProduto);
        buttonAdicionarProduto.setOnClickListener(v -> mostrarDialogoAdicionarProduto());

        atualizarLista();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void atualizarLista() {
        listaProdutos.clear();
        Cursor cursor = databaseProdutos.obterTodosOsProdutos();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
            @SuppressLint("Range") String nome = cursor.getString(cursor.getColumnIndex("nome"));
            @SuppressLint("Range") double preco = cursor.getDouble(cursor.getColumnIndex("preco"));

            listaProdutos.add(new Produto(id, nome, preco));
        }
        cursor.close();
        produtoAdapter.notifyDataSetChanged();
    }

    private void mostrarDialogoAdicionarProduto() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText editNome = new EditText(this);
        editNome.setHint("Nome do Produto");
        layout.addView(editNome);

        EditText editPreco = new EditText(this);
        editPreco.setHint("Preço");
        layout.addView(editPreco);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Novo Produto")
                .setView(layout)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String nome = editNome.getText().toString();
                    double preco = Double.parseDouble(editPreco.getText().toString());

                    if (!nome.isEmpty()) {
                        databaseProdutos.inserirProduto(nome, preco);
                        atualizarLista();
                        Toast.makeText(ProdutosActivity.this, "Produto Adicionado!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProdutosActivity.this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
