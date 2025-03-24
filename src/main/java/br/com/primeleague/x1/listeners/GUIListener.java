package br.com.primeleague.x1.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelType;

/**
 * Listener de eventos relacionados à GUI
 */
public class GUIListener implements Listener {

    private final Main plugin;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public GUIListener(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Evento disparado quando um jogador clica em um inventário
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        Inventory inventory = event.getInventory();
        
        // Verificar se o inventário é uma GUI do plugin
        if (inventory.getTitle() == null || clickedItem == null) {
            return;
        }
        
        String title = inventory.getTitle();
        
        // Menu principal
        if (title.equals(plugin.getGUIManager().getMainMenuTitle())) {
            event.setCancelled(true);
            handleMainMenuClick(player, clickedItem, event.getRawSlot());
            return;
        }
        
        // Menu de administração
        if (title.equals(plugin.getGUIManager().getAdminMenuTitle())) {
            event.setCancelled(true);
            handleAdminMenuClick(player, clickedItem, event.getRawSlot());
            return;
        }
        
        // Menu de seleção de jogador
        if (title.equals(plugin.getGUIManager().getPlayerSelectionMenuTitle())) {
            event.setCancelled(true);
            handlePlayerSelectionMenuClick(player, clickedItem, event.getRawSlot());
            return;
        }
        
        // Menu de confirmação de desafio
        if (title.equals(plugin.getGUIManager().getChallengeConfirmationMenuTitle())) {
            event.setCancelled(true);
            handleChallengeConfirmationMenuClick(player, clickedItem, event.getRawSlot());
            return;
        }
        
        // Menu de apostas
        if (title.equals(plugin.getGUIManager().getBetMenuTitle())) {
            event.setCancelled(true);
            handleBetMenuClick(player, clickedItem, event.getRawSlot());
            return;
        }
        
        // Menu de estatísticas
        if (title.equals(plugin.getGUIManager().getStatsMenuTitle())) {
            event.setCancelled(true);
            handleStatsMenuClick(player, clickedItem, event.getRawSlot());
            return;
        }
        
        // Menu de ranking
        if (title.equals(plugin.getGUIManager().getRankingMenuTitle())) {
            event.setCancelled(true);
            handleRankingMenuClick(player, clickedItem, event.getRawSlot());
            return;
        }
        
        // Menu de equipe
        if (title.equals(plugin.getGUIManager().getTeamMenuTitle())) {
            event.setCancelled(true);
            handleTeamMenuClick(player, clickedItem, event.getRawSlot());
            return;
        }
    }
    
    /**
     * Evento disparado quando um jogador arrasta itens em um inventário
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        
        // Verificar se o inventário é uma GUI do plugin
        if (inventory.getTitle() == null) {
            return;
        }
        
        String title = inventory.getTitle();
        
        // Cancelar o arrastar em qualquer GUI do plugin
        if (title.equals(plugin.getGUIManager().getMainMenuTitle()) || 
            title.equals(plugin.getGUIManager().getAdminMenuTitle()) || 
            title.equals(plugin.getGUIManager().getPlayerSelectionMenuTitle()) || 
            title.equals(plugin.getGUIManager().getChallengeConfirmationMenuTitle()) || 
            title.equals(plugin.getGUIManager().getBetMenuTitle()) || 
            title.equals(plugin.getGUIManager().getStatsMenuTitle()) || 
            title.equals(plugin.getGUIManager().getRankingMenuTitle()) || 
            title.equals(plugin.getGUIManager().getTeamMenuTitle())) {
            
            event.setCancelled(true);
        }
    }
    
    /**
     * Evento disparado quando um jogador fecha um inventário
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Implementação futura, se necessário
    }
    
    /**
     * Processa cliques no menu principal
     */
    private void handleMainMenuClick(Player player, ItemStack clickedItem, int slot) {
        switch (slot) {
            case 10: // Desafiar jogador
                plugin.getGUIManager().openPlayerSelectionMenu(player);
                break;
                
            case 12: // Estatísticas
                plugin.getGUIManager().openStatsMenu(player);
                break;
                
            case 14: // Ranking
                plugin.getGUIManager().openRankingMenu(player);
                break;
                
            case 16: // Equipes
                plugin.getGUIManager().openTeamMenu(player);
                break;
                
            case 31: // Assistir duelos
                if (!plugin.getArenaManager().isArenaConfigured()) {
                    plugin.getMessageManager().sendMessage(player, "arena.nao-configurada");
                    return;
                }
                
                player.closeInventory();
                plugin.getArenaManager().teleportToSpectator(player);
                plugin.getMessageManager().sendMessage(player, "duelo.modo-espectador");
                break;
                
            case 34: // Menu de administração
                if (player.hasPermission("primeleaguex1.admin")) {
                    plugin.getGUIManager().openAdminMenu(player);
                } else {
                    plugin.getMessageManager().sendMessage(player, "geral.sem-permissao");
                }
                break;
        }
    }
    
    /**
     * Processa cliques no menu de administração
     */
    private void handleAdminMenuClick(Player player, ItemStack clickedItem, int slot) {
        switch (slot) {
            case 10: // Definir posição 1
                player.closeInventory();
                plugin.getArenaManager().setPos1(player.getLocation());
                plugin.getMessageManager().sendMessage(player, "arena.pos1-definida");
                break;
                
            case 12: // Definir posição 2
                player.closeInventory();
                plugin.getArenaManager().setPos2(player.getLocation());
                plugin.getMessageManager().sendMessage(player, "arena.pos2-definida");
                break;
                
            case 14: // Definir local de espectador
                player.closeInventory();
                plugin.getArenaManager().setSpectatorLocation(player.getLocation());
                plugin.getMessageManager().sendMessage(player, "arena.espectador-definido");
                break;
                
            case 16: // Recarregar configuração
                player.closeInventory();
                plugin.getConfigManager().reloadConfigs();
                plugin.getMessageManager().loadMessages();
                plugin.getMessageManager().sendMessage(player, "geral.plugin-ativado");
                break;
                
            case 31: // Voltar
                plugin.getGUIManager().openMainMenu(player);
                break;
        }
    }
    
    /**
     * Processa cliques no menu de seleção de jogador
     */
    private void handlePlayerSelectionMenuClick(Player player, ItemStack clickedItem, int slot) {
        if (slot == 49) { // Botão voltar
            plugin.getGUIManager().openMainMenu(player);
            return;
        }
        
        // Verificar se o item é um jogador
        if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
            String displayName = clickedItem.getItemMeta().getDisplayName();
            String targetName = displayName.replaceAll("§[0-9a-fklmnor]", "");
            
            // Verificar se o jogador está online
            Player target = plugin.getServer().getPlayerExact(targetName);
            if (target == null || !target.isOnline()) {
                plugin.getMessageManager().sendMessage(player, "geral.jogador-offline");
                return;
            }
            
            // Verificar se o jogador tentou desafiar a si mesmo
            if (player.getName().equals(targetName)) {
                plugin.getMessageManager().sendMessage(player, "geral.voce-mesmo");
                return;
            }
            
            // Verificar se o jogador já está em duelo
            if (plugin.getDuelManager().isInDuel(player.getName())) {
                plugin.getMessageManager().sendMessage(player, "duelo.duelo-em-andamento");
                return;
            }
            
            // Verificar se o alvo já está em duelo
            if (plugin.getDuelManager().isInDuel(targetName)) {
                plugin.getMessageManager().sendMessage(player, "duelo.oponente-em-duelo");
                return;
            }
            
            // Abrir menu de confirmação de desafio
            plugin.getGUIManager().openChallengeConfirmationMenu(player, targetName);
        }
    }
    
    /**
     * Processa cliques no menu de confirmação de desafio
     */
    private void handleChallengeConfirmationMenuClick(Player player, ItemStack clickedItem, int slot) {
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
        
        switch (slot) {
            case 10: // Arena - Itens Próprios
                if (!plugin.getArenaManager().isArenaConfigured()) {
                    plugin.getMessageManager().sendMessage(player, "arena.nao-configurada");
                    return;
                }
                
                plugin.getGUIManager().setSelectedDuelType(player, DuelType.ARENA);
                
                // Verificar se apostas estão habilitadas
                if (plugin.getConfigManager().useBets()) {
                    plugin.getGUIManager().openBetMenu(player, opponent);
                } else {
                    player.closeInventory();
                    plugin.getDuelManager().sendChallenge(player, target, DuelType.ARENA, 0);
                }
                break;
                
            case 12: // Local - Itens Próprios
                if (!plugin.getArenaManager().arePlayersCloseEnough(player, target)) {
                    plugin.getMessageManager().sendMessage(player, "duelo.desafio-muito-longe");
                    return;
                }
                
                plugin.getGUIManager().setSelectedDuelType(player, DuelType.LOCAL);
                
                // Verificar se apostas estão habilitadas
                if (plugin.getConfigManager().useBets()) {
                    plugin.getGUIManager().openBetMenu(player, opponent);
                } else {
                    player.closeInventory();
                    plugin.getDuelManager().sendChallenge(player, target, DuelType.LOCAL, 0);
                }
                break;
                
            case 14: // Arena - Com Kit
                if (!plugin.getArenaManager().isArenaConfigured()) {
                    plugin.getMessageManager().sendMessage(player, "arena.nao-configurada");
                    return;
                }
                
                plugin.getGUIManager().setSelectedDuelType(player, DuelType.ARENA_KIT);
                
                // Verificar se apostas estão habilitadas
                if (plugin.getConfigManager().useBets()) {
                    plugin.getGUIManager().openBetMenu(player, opponent);
                } else {
                    player.closeInventory();
                    plugin.getDuelManager().sendChallenge(player, target, DuelType.ARENA_KIT, 0);
                }
                break;
                
            case 16: // Local - Com Kit
                if (!plugin.getArenaManager().arePlayersCloseEnough(player, target)) {
                    plugin.getMessageManager().sendMessage(player, "duelo.desafio-muito-longe");
                    return;
                }
                
                plugin.getGUIManager().setSelectedDuelType(player, DuelType.LOCAL_KIT);
                
                // Verificar se apostas estão habilitadas
                if (plugin.getConfigManager().useBets()) {
                    plugin.getGUIManager().openBetMenu(player, opponent);
                } else {
                    player.closeInventory();
                    plugin.getDuelManager().sendChallenge(player, target, DuelType.LOCAL_KIT, 0);
                }
                break;
                
            case 31: // Cancelar
                plugin.getGUIManager().openPlayerSelectionMenu(player);
                break;
        }
    }
    
    /**
     * Processa cliques no menu de apostas
     */
    private void handleBetMenuClick(Player player, ItemStack clickedItem, int slot) {
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
        
        // Valores pré-definidos de aposta
        if (slot >= 10 && slot <= 16) {
            // Obter o valor da aposta do item
            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                try {
                    String displayName = clickedItem.getItemMeta().getDisplayName();
                    String valueStr = displayName.replaceAll("[^0-9]", "");
                    double betAmount = Double.parseDouble(valueStr);
                    
                    // Verificar se o jogador tem saldo suficiente
                    if (!plugin.getEconomyManager().hasEnoughMoney(player.getName(), betAmount)) {
                        plugin.getEconomyManager().sendNotEnoughMoneyMessage(player, betAmount);
                        return;
                    }
                    
                    // Fechar inventário e enviar desafio
                    player.closeInventory();
                    
                    // Verificar o tipo de duelo escolhido
                    DuelType type = plugin.getGUIManager().getSelectedDuelType(player);
                    
                    if (type != null) {
                        plugin.getDuelManager().sendChallenge(player, target, type, betAmount);
                    } else {
                        // Tipo não definido, usar arena como padrão
                        plugin.getDuelManager().sendChallenge(player, target, DuelType.ARENA, betAmount);
                    }
                    
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "apostas.erro-valor");
                }
            }
        } else if (slot == 31) { // Sem aposta
            player.closeInventory();
            
            // Verificar o tipo de duelo escolhido
            DuelType type = plugin.getGUIManager().getSelectedDuelType(player);
            
            if (type != null) {
                plugin.getDuelManager().sendChallenge(player, target, type, 0);
            } else {
                // Tipo não definido, usar arena como padrão
                plugin.getDuelManager().sendChallenge(player, target, DuelType.ARENA, 0);
            }
        } else if (slot == 40) { // Voltar
            plugin.getGUIManager().openChallengeConfirmationMenu(player, opponent);
        }
    }
    
    /**
     * Processa cliques no menu de estatísticas
     */
    private void handleStatsMenuClick(Player player, ItemStack clickedItem, int slot) {
        if (slot == 49) { // Botão voltar
            plugin.getGUIManager().openMainMenu(player);
        }
    }
    
    /**
     * Processa cliques no menu de ranking
     */
    private void handleRankingMenuClick(Player player, ItemStack clickedItem, int slot) {
        if (slot == 49) { // Botão voltar
            plugin.getGUIManager().openMainMenu(player);
        }
    }
    
    /**
     * Processa cliques no menu de equipe
     */
    private void handleTeamMenuClick(Player player, ItemStack clickedItem, int slot) {
        switch (slot) {
            case 11: // Criar equipe
                if (plugin.getTeamManager().isInTeam(player.getName())) {
                    plugin.getMessageManager().sendMessage(player, "equipes.ja-em-equipe");
                    return;
                }
                
                player.closeInventory();
                plugin.getTeamManager().createTeam(player.getName());
                plugin.getMessageManager().sendMessage(player, "equipes.equipe-formada");
                break;
                
            case 13: // Convidar jogador
                if (!plugin.getTeamManager().isTeamLeader(player.getName())) {
                    plugin.getMessageManager().sendMessage(player, "equipes.nao-lider");
                    return;
                }
                
                plugin.getGUIManager().openTeamAddMenu(player);
                break;
                
            case 15: // Sair da equipe
                if (!plugin.getTeamManager().isInTeam(player.getName())) {
                    plugin.getMessageManager().sendMessage(player, "equipes.sem-equipe");
                    return;
                }
                
                player.closeInventory();
                plugin.getTeamManager().removeFromTeam(player.getName());
                plugin.getMessageManager().sendMessage(player, "equipes.saiu-equipe");
                break;
                
            case 31: // Voltar
                plugin.getGUIManager().openMainMenu(player);
                break;
        }
    }
} 