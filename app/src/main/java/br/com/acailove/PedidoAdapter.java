package br.com.acailove;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.acailove.data.DatabasePedidos;

public class PedidoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Pedido> pedidos;
    private final DatabasePedidos databasePedidos;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    public PedidoAdapter(List<Pedido> pedidos, DatabasePedidos databasePedidos) {
        this.pedidos = pedidos;
        this.databasePedidos = databasePedidos;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_pedido, parent, false);
            return new HeaderViewHolder(headerView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
            return new PedidoViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PedidoViewHolder) {
            Pedido pedido = pedidos.get(position - 1);
            ((PedidoViewHolder) holder).bind(pedido);

            if (position % 2 == 1) {
                holder.itemView.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return pedidos.size() + 1;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final TextView idTextView;
        private final TextView nomeClienteTextView;
        private final TextView enderecoTextView;
        private final TextView telefoneTextView;
        private final TextView statusTextView;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.textViewId);
            nomeClienteTextView = itemView.findViewById(R.id.textViewNomeCliente);
            enderecoTextView = itemView.findViewById(R.id.textViewEndereco);
            telefoneTextView = itemView.findViewById(R.id.textViewTelefone);
            statusTextView = itemView.findViewById(R.id.textViewStatus);
            ImageButton buttonAtualizarStatus = itemView.findViewById(R.id.buttonAtualizarStatus);

            buttonAtualizarStatus.setOnClickListener(v -> showPopupMenu(v, getAdapterPosition() - 1));
        }

        public void bind(Pedido pedido) {
            idTextView.setText(String.valueOf(pedido.getId()));
            nomeClienteTextView.setText(pedido.getNomeCliente());
            enderecoTextView.setText(pedido.getEndereco());
            telefoneTextView.setText(pedido.getTelefone());
            statusTextView.setText(pedido.getStatus());
        }

        private void showPopupMenu(View view, int position) {
            Pedido pedido = pedidos.get(position);
            String statusAtual = pedido.getStatus();

            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

            if (!statusAtual.equals("Em Preparo")) {
                popupMenu.getMenu().add(0, 2, 1, "Em Preparo");
            }

            if (!statusAtual.equals("Em Rota")) {
                popupMenu.getMenu().add(0, 3, 2, "Em Rota");
            }

            if (!statusAtual.equals("Finalizado")) {
                popupMenu.getMenu().add(0, 4, 3, "Finalizado");
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        atualizarStatus(position, "Pendente");
                        return true;
                    case 2:
                        atualizarStatus(position, "Em Preparo");
                        return true;
                    case 3:
                        atualizarStatus(position, "Em Rota");
                        return true;
                    case 4:
                        atualizarStatus(position, "Finalizado");
                        return true;
                    default:
                        return false;
                }
            });

            popupMenu.show();
        }

        private void recarregarPedidos() {
            List<Pedido> novosPedidos = databasePedidos.getPedidosNaoFinalizados();
            updatePedidos(novosPedidos);
        }

        private void atualizarStatus(int position, String novoStatus) {
            Pedido pedido = pedidos.get(position);
            pedido.setStatus(novoStatus);
            databasePedidos.atualizarStatusPedido(pedido.getId(), novoStatus);
            statusTextView.setText(novoStatus);

            if (novoStatus.equals("Finalizado")) {
                recarregarPedidos();
            }

            enviarMensagemWhatsApp(pedido.getTelefone(), "Seu pedido foi atualizado para: " + novoStatus);
        }

        private void enviarMensagemWhatsApp(String numeroCliente, String mensagem) {
            String url = "https://wa.me/" + "+55" + numeroCliente + "?text=" + Uri.encode(mensagem);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            itemView.getContext().startActivity(intent);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePedidos(List<Pedido> newPedidos) {
        this.pedidos.clear();
        this.pedidos.addAll(newPedidos);
        notifyDataSetChanged();
    }
}
