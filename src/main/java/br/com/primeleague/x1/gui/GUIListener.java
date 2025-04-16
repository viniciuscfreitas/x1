package br.com.primeleague.x1.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelType;
import br.com.primeleague.x1.utils.InventoryUtils;

public class GUIListener {

    private Main plugin;

    public GUIListener(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Processa cliques no menu de confirmação de desafio
     */
    private void handleChallengeConfirmationMenuClick(Player player, ItemStack clickedItem, int slot) {
        try {
            String opponent = plugin.getGUIManager().getSelectedPlayer(player);
            
            if (opponent == null) {
                player.closeInventory();
                return;
            }
            
            Player target = plugin.getServer().getPlayerExact(opponent);
            
            // Verificar se o jogador está online
            if (target == null || !target.isOnline()) {
                plugin.getMessageManager().sendMessage(player, "geral.jogador-offline");
                player.closeInventory();
                return;
            }
            
            DuelType duelType = null;
            String duelTypeDebug = "Não definido";
            
            switch (slot) {
                case 10: // Arena - Itens Próprios
                    duelType = DuelType.ARENA;
                    duelTypeDebug = "ARENA (slot 10)";
                    break;
                    
                case 12: // Local - Itens Próprios
                    duelType = DuelType.LOCAL;
                    duelTypeDebug = "LOCAL (slot 12)";
                    break;
                    
                case 14: // Arena - Com Kit
                    duelType = DuelType.ARENA_KIT;
                    duelTypeDebug = "ARENA_KIT (slot 14)";
                    break;
                    
                case 16: // Local - Com Kit
                    duelType = DuelType.LOCAL_KIT;
                    duelTypeDebug = "LOCAL_KIT (slot 16)";
                    break;
                    
                case 31: // Cancelar
                    player.closeInventory();
                    return;
                    
                default:
                    System.out.println("[PrimeLeagueX1] Clique no slot desconhecido: " + slot);
                    return;
            }
            
            System.out.println("[PrimeLeagueX1] Jogador " + player.getName() + " selecionou tipo de duelo: " + duelTypeDebug);
            System.out.println("[PrimeLeagueX1] Detalhes do duelType - nome: " + 
                    (duelType != null ? duelType.name() : "null") + 
                    ", usa kit: " + (duelType != null ? duelType.usesKit() : "null"));
            
            // Verificar se o tipo de duelo é válido
            if (duelType == null) {
                plugin.getMessageManager().sendMessage(player, "duelo.erro-tipo");
                player.closeInventory();
                System.out.println("[PrimeLeagueX1] ERRO: Tipo de duelo é nulo para " + player.getName());
                return;
            }
            
            // Armazenar o tipo de duelo
            plugin.getGUIManager().setSelectedDuelType(player, duelType);
            
            // Verificar se o tipo do duelo é local e se os jogadores estão perto o suficiente
            if (duelType.isLocal() && !plugin.getArenaManager().arePlayersCloseEnough(player, target)) {
                plugin.getMessageManager().sendMessage(player, "duelo.muito-longe", 
                        "%player%", opponent);
                player.closeInventory();
                return;
            }
            
            // Se o tipo de duelo usa kit, verificar se está habilitado
            if (duelType.usesKit() && !plugin.getConfigManager().useDuelKit()) {
                plugin.getMessageManager().sendMessage(player, "duelo.kit-desabilitado");
                player.closeInventory();
                return;
            }
            
            // Verificar se o inventário está vazio, se necessário (somente para duelos com kit)
            if (duelType.usesKit() && plugin.getConfigManager().checkEmptyInventory() && !InventoryUtils.isInventoryEmpty(player)) {
                plugin.getMessageManager().sendMessage(player, "duelo.inventario-nao-vazio");
                player.closeInventory();
                return;
            }
            
            try {
                // Verificar se apostas estão habilitadas
                if (plugin.getConfigManager().useBets()) {
                    // Abrir menu de apostas
                    player.closeInventory();
                    System.out.println("[PrimeLeagueX1] Abrindo menu de apostas para " + player.getName() + 
                            " com oponente " + opponent + " e tipo " + duelType.name());
                    plugin.getGUIManager().openBetMenu(player, opponent);
                } else {
                    // Enviar desafio sem aposta
                    player.closeInventory();
                    System.out.println("[PrimeLeagueX1] Enviando desafio sem aposta de " + player.getName() + 
                            " para " + opponent + " do tipo " + duelType.name());
                    plugin.getDuelManager().sendChallenge(player, target, duelType, 0.0);
                }
            } catch (Exception e) {
                System.out.println("[PrimeLeagueX1] ERRO ao processar apostas ou enviar desafio: " + e.getMessage());
                e.printStackTrace();
                player.sendMessage("§cOcorreu um erro ao processar seu desafio. Tente novamente.");
            }
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO GRAVE ao processar clique no menu de desafio: " + e.getMessage());
            e.printStackTrace();
            player.closeInventory();
            player.sendMessage("§cOcorreu um erro interno. Tente novamente.");
        }
    }
}