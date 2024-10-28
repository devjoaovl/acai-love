package br.com.acailove;

import br.com.acailove.data.DatabasePedidos;

public class Pedido {

    private Integer id;
    private String nomeCliente;
    private String endereco;
    private String telefone;
    private String status;

    public Pedido(Integer id, String nomeCliente, String endereco, String telefone, String status) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.endereco = endereco;
        this.telefone = telefone;
        this.status = status;
    }

    public Integer getId() { return id; }
    public String getNomeCliente() { return nomeCliente; }
    public String getEndereco() { return endereco; }
    public String getTelefone() { return telefone; }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
    }

    public void adicionarProduto(DatabasePedidos dbPedidos, int produtoId) {
        dbPedidos.adicionarProdutoAoPedido(this.id, produtoId);
    }

}