package br.com.acailove.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import br.com.acailove.Pedido;

public class DatabasePedidos extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pedidos.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "pedidos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOME_CLIENTE = "nome_cliente";
    public static final String COLUMN_ENDERECO = "endereco";
    public static final String COLUMN_TELEFONE = "telefone";
    public static final String COLUMN_STATUS = "status";

    public DatabasePedidos(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOME_CLIENTE + " TEXT, " +
                COLUMN_ENDERECO + " TEXT, " +
                COLUMN_TELEFONE + " TEXT, " +
                COLUMN_STATUS + " TEXT)";
        db.execSQL(createTable);

        String createTablePedidoProduto = "CREATE TABLE pedido_produto (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pedido_id INTEGER, " +
                "produto_id INTEGER, " +
                "quantidade INTEGER, " +
                "FOREIGN KEY (pedido_id) REFERENCES pedidos(id), " +
                "FOREIGN KEY (produto_id) REFERENCES produtos(id))";
        db.execSQL(createTablePedidoProduto);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long inserirPedido(String nomeCliente, String endereco, String telefone, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOME_CLIENTE, nomeCliente);
        values.put(COLUMN_ENDERECO, endereco);
        values.put(COLUMN_TELEFONE, telefone);
        values.put(COLUMN_STATUS, status);

        long pedidoId = db.insert(TABLE_NAME, null, values);

        db.close();
        return pedidoId;
    }

    public List<Pedido> getPedidosNaoFinalizados() {
        List<Pedido> pedidos = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase(); Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COLUMN_STATUS + " != ?",
                new String[]{"Finalizado"},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    @SuppressLint("Range") String nomeCliente = cursor.getString(cursor.getColumnIndex(COLUMN_NOME_CLIENTE));
                    @SuppressLint("Range") String endereco = cursor.getString(cursor.getColumnIndex(COLUMN_ENDERECO));
                    @SuppressLint("Range") String telefone = cursor.getString(cursor.getColumnIndex(COLUMN_TELEFONE));
                    @SuppressLint("Range") String status = cursor.getString(cursor.getColumnIndex(COLUMN_STATUS));
                    pedidos.add(new Pedido(id, nomeCliente, endereco, telefone, status));
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return pedidos;
    }

    public void atualizarStatusPedido(int id, String novoStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, novoStatus);

        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Pedido com ID " + id + " não encontrado.");
        }

        db.close();
    }

    public void adicionarProdutoAoPedido(int pedidoId, int produtoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pedido_id", pedidoId);
        values.put("produto_id", produtoId);

        db.insert("pedido_produto", null, values);
    }

}
