package br.com.primeleague.x1.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelType;
import br.com.primeleague.x1.models.PlayerStats;
import br.com.primeleague.x1.utils.ColorUtils;
import br.com.primeleague.x1.utils.InventoryUtils;

/**
 * Gerenciador de interfaces gráficas
 */
public class GUIManager {

    private final Main plugin;
    
    // Inventários de menu
    private Inventory mainMenu;
    private Inventory adminMenu;
    
    // Armazenamento temporário
    private final Map<String, String> selectedPlayers;
    private final Map<String, DuelType> selectedDuelTypes;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public GUIManager(Main plugin) {
        this.plugin = plugin;
        this.selectedPlayers = new HashMap<>();
        this.selectedDuelTypes = new HashMap<>();
        
        createMenus();
    }
    
    /**
     * Cria os menus principais
     */
    private void createMenus() {
        // Menu principal
        mainMenu = Bukkit.createInventory(null, 45, getMainMenuTitle());
        
        // Itens do menu principal
        ItemStack challenge = InventoryUtils.createNamedItem(Material.DIAMOND_SWORD, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-desafiar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-desafiar")));
        
        ItemStack stats = InventoryUtils.createNamedItem(Material.PAPER, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-estatisticas")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-estatisticas")));
        
        ItemStack ranking = InventoryUtils.createNamedItem(Material.GOLD_INGOT, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-ranking")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-ranking")));
        
        ItemStack team = InventoryUtils.createNamedItem(Material.IRON_CHESTPLATE, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-equipe")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-equipe")));
        
        ItemStack spectate = InventoryUtils.createNamedItem(Material.GLASS, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-espectador")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-espectador")));
        
        ItemStack admin = InventoryUtils.createNamedItem(Material.REDSTONE, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-admin")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-admin")));
        
        // Posicionamento dos itens
        mainMenu.setItem(10, challenge);
        mainMenu.setItem(12, stats);
        mainMenu.setItem(14, ranking);
        mainMenu.setItem(16, team);
        mainMenu.setItem(31, spectate);
        mainMenu.setItem(34, admin);
        
        // Preencher espaços vazios com vidro
        ItemStack glass = new ItemStack(Material.WOOL, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < mainMenu.getSize(); i++) {
            if (mainMenu.getItem(i) == null) {
                mainMenu.setItem(i, glass);
            }
        }
        
        // Menu de administração
        adminMenu = Bukkit.createInventory(null, 45, getAdminMenuTitle());
        
        // Itens do menu de administração
        ItemStack setPos1 = InventoryUtils.createNamedItem(Material.WOOL, "§a§lDefinir Posição 1", "§7Clique para definir a primeira posição da arena");
        ItemStack setPos2 = InventoryUtils.createNamedItem(Material.WOOL, "§c§lDefinir Posição 2", "§7Clique para definir a segunda posição da arena");
        ItemStack setSpectator = InventoryUtils.createNamedItem(Material.WOOL, "§e§lDefinir Local de Espectador", "§7Clique para definir o local de espectador");
        ItemStack reload = InventoryUtils.createNamedItem(Material.NETHER_STAR, "§6§lRecarregar Configuração", "§7Clique para recarregar a configuração");
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, "§c§lVoltar", "§7Clique para voltar ao menu principal");
        
        adminMenu.setItem(10, setPos1);
        adminMenu.setItem(12, setPos2);
        adminMenu.setItem(14, setSpectator);
        adminMenu.setItem(16, reload);
        adminMenu.setItem(31, back);
        
        // Preencher espaços vazios com vidro
        for (int i = 0; i < adminMenu.getSize(); i++) {
            if (adminMenu.getItem(i) == null) {
                adminMenu.setItem(i, glass);
            }
        }
    }
    
    /**
     * Abre o menu principal para um jogador
     * 
     * @param player Jogador
     */
    public void openMainMenu(Player player) {
        player.openInventory(mainMenu);
    }
    
    /**
     * Abre o menu de administração para um jogador
     * 
     * @param player Jogador
     */
    public void openAdminMenu(Player player) {
        if (!player.hasPermission("primeleaguex1.admin")) {
            plugin.getMessageManager().sendMessage(player, "geral.sem-permissao");
            return;
        }
        
        player.openInventory(adminMenu);
    }
    
    /**
     * Abre o menu de seleção de jogador
     * 
     * @param player Jogador
     */
    public void openPlayerSelectionMenu(Player player) {
        // Criar inventário para a seleção de jogadores
        Inventory playerMenu = Bukkit.createInventory(null, 54, getPlayerSelectionMenuTitle());
        
        // Adicionar jogadores online
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            // Não incluir o próprio jogador
            if (online.getName().equals(player.getName())) {
                continue;
            }
            
            // Não incluir jogadores em duelo
            if (plugin.getDuelManager().isInDuel(online.getName())) {
                continue;
            }
            
            // Criar cabeça do jogador
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(online.getName());
            meta.setDisplayName("§e" + online.getName());
            
            List<String> lore = new ArrayList<>();
            // Se está usando o sistema de Elo, mostrar o Elo do jogador
            if (plugin.getConfigManager().useElo()) {
                PlayerStats stats = plugin.getStatsManager().getPlayerStats(online.getName());
                lore.add("§7Elo: §b" + stats.getElo());
            }
            meta.setLore(lore);
            skull.setItemMeta(meta);
            
            playerMenu.setItem(slot, skull);
            slot++;
            
            // Limitar a 44 jogadores (deixar espaço para o botão voltar)
            if (slot >= 44) {
                break;
            }
        }
        
        // Botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        playerMenu.setItem(49, back);
        
        player.openInventory(playerMenu);
    }
    
    /**
     * Abre o menu de confirmação de desafio
     * 
     * @param player Jogador
     * @param opponent Nome do oponente
     */
    public void openChallengeConfirmationMenu(Player player, String opponent) {
        // Armazenar o oponente selecionado
        selectedPlayers.put(player.getName(), opponent);
        
        // Criar inventário para a confirmação de desafio
        Inventory confirmMenu = Bukkit.createInventory(null, 45, getChallengeConfirmationMenuTitle());
        
        // Item de duelo na arena (itens próprios)
        ItemStack arena = InventoryUtils.createNamedItem(Material.ENDER_PORTAL_FRAME, 
                "§a§lArena - Itens Próprios", 
                "§7Você será teleportado para a arena",
                "§7Mantém seus próprios itens durante o duelo");
        confirmMenu.setItem(10, arena);
        
        // Item de duelo local (itens próprios)
        ItemStack local = InventoryUtils.createNamedItem(Material.GRASS, 
                "§e§lLocal - Itens Próprios", 
                "§7Lute onde você está",
                "§7Mantém seus próprios itens durante o duelo");
        confirmMenu.setItem(12, local);
        
        // Item de duelo na arena (com kit)
        ItemStack arenaKit = InventoryUtils.createNamedItem(Material.DIAMOND_CHESTPLATE, 
                "§b§lArena - Com Kit", 
                "§7Você será teleportado para a arena",
                "§7Receberá o kit configurado para o duelo",
                "§c§lAtenção: §7Seu inventário deve estar vazio!");
        confirmMenu.setItem(14, arenaKit);
        
        // Item de duelo local (com kit)
        ItemStack localKit = InventoryUtils.createNamedItem(Material.DIAMOND_SWORD, 
                "§d§lLocal - Com Kit", 
                "§7Lute onde você está",
                "§7Receberá o kit configurado para o duelo",
                "§c§lAtenção: §7Seu inventário deve estar vazio!");
        confirmMenu.setItem(16, localKit);
        
        // Item de cancelar
        ItemStack cancel = InventoryUtils.createNamedItem(Material.BEDROCK, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-cancelar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-cancelar")));
        confirmMenu.setItem(31, cancel);
        
        player.openInventory(confirmMenu);
    }
    
    /**
     * Abre o menu de apostas
     * 
     * @param player Jogador
     * @param opponent Nome do oponente
     */
    public void openBetMenu(Player player, String opponent) {
        // Verificar se o uso de apostas está ativado
        if (!plugin.getConfigManager().useBets()) {
            plugin.getMessageManager().sendMessage(player, "apostas.desativadas");
            return;
        }
        
        // Criar inventário para apostas
        Inventory betMenu = Bukkit.createInventory(null, 45, getBetMenuTitle());
        
        // Obter os valores pré-definidos das apostas
        List<Integer> predefinedValues = plugin.getConfigManager().getConfig().getIntegerList("apostas.valores-predefinidos");
        if (predefinedValues.isEmpty()) {
            predefinedValues = Arrays.asList(100, 500, 1000, 5000, 10000);
        }
        
        // Adicionar os itens de aposta
        int slot = 10;
        for (Integer value : predefinedValues) {
            if (slot > 16) break; // Limitar a 7 valores pré-definidos
            
            // Formatar o valor
            String formattedValue = plugin.getEconomyManager().formatMoney(value);
            
            ItemStack betItem = InventoryUtils.createNamedItem(Material.GOLD_INGOT,
                    "§e§l" + formattedValue,
                    "§7Clique para apostar " + formattedValue);
            betMenu.setItem(slot, betItem);
            slot++;
        }
        
        // Item para não apostar
        ItemStack noBet = InventoryUtils.createNamedItem(Material.BEDROCK, 
                "§c§lSem Aposta", 
                "§7Clique para desafiar sem apostar");
        betMenu.setItem(31, noBet);
        
        // Botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        betMenu.setItem(40, back);
        
        player.openInventory(betMenu);
    }
    
    /**
     * Abre o menu de estatísticas
     * 
     * @param player Jogador
     */
    public void openStatsMenu(Player player) {
        // Criar inventário para estatísticas
        Inventory statsMenu = Bukkit.createInventory(null, 54, getStatsMenuTitle());
        
        // Obter as estatísticas do jogador
        PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getName());
        
        // Cabeça do jogador
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(player.getName());
        skullMeta.setDisplayName("§e§l" + player.getName());
        skull.setItemMeta(skullMeta);
        statsMenu.setItem(4, skull);
        
        // Vitórias
        ItemStack victories = InventoryUtils.createNamedItem(Material.DIAMOND_SWORD, 
                "§a§lVitórias", 
                "§7Total: §a" + stats.getVictories());
        statsMenu.setItem(19, victories);
        
        // Derrotas
        ItemStack defeats = InventoryUtils.createNamedItem(Material.BONE, 
                "§c§lDerrotas", 
                "§7Total: §c" + stats.getDefeats());
        statsMenu.setItem(21, defeats);
        
        // Empates
        ItemStack draws = InventoryUtils.createNamedItem(Material.BUCKET, 
                "§e§lEmpates", 
                "§7Total: §e" + stats.getDraws());
        statsMenu.setItem(23, draws);
        
        // KDR
        ItemStack kdr = InventoryUtils.createNamedItem(Material.IRON_SWORD, 
                "§f§lKDR", 
                "§7Relação V/D: §f" + String.format("%.2f", stats.getKDR()));
        statsMenu.setItem(25, kdr);
        
        // Taxa de vitórias
        ItemStack winRate = InventoryUtils.createNamedItem(Material.BLAZE_POWDER, 
                "§6§lTaxa de Vitórias", 
                "§7Porcentagem: §6" + String.format("%.2f%%", stats.getWinRate()));
        statsMenu.setItem(29, winRate);
        
        // Elo
        if (plugin.getConfigManager().useElo()) {
            ItemStack elo = InventoryUtils.createNamedItem(Material.NETHER_STAR, 
                    "§b§lElo", 
                    "§7Pontuação: §b" + stats.getElo());
            statsMenu.setItem(31, elo);
        }
        
        // Total de duelos
        ItemStack total = InventoryUtils.createNamedItem(Material.COMPASS, 
                "§d§lTotal de Duelos", 
                "§7Duelos: §d" + stats.getTotalDuels());
        statsMenu.setItem(33, total);
        
        // Botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        statsMenu.setItem(49, back);
        
        player.openInventory(statsMenu);
    }
    
    /**
     * Abre o menu de ranking
     * 
     * @param player Jogador
     */
    public void openRankingMenu(Player player) {
        // Criar inventário para o ranking
        Inventory rankingMenu = Bukkit.createInventory(null, 54, getRankingMenuTitle());
        
        // Obter a quantidade de jogadores a mostrar
        int topAmount = plugin.getConfigManager().getTopPlayersAmount();
        
        // Obter os melhores jogadores
        List<PlayerStats> topPlayers = plugin.getStatsManager().getTopPlayers(topAmount);
        
        // Adicionar os jogadores ao ranking
        for (int i = 0; i < topPlayers.size(); i++) {
            PlayerStats stats = topPlayers.get(i);
            
            // Criar cabeça do jogador
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(stats.getPlayerName());
            
            // Definir título e lore
            meta.setDisplayName("§e§l#" + (i + 1) + " " + stats.getPlayerName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Vitórias: §a" + stats.getVictories());
            lore.add("§7Derrotas: §c" + stats.getDefeats());
            lore.add("§7KDR: §f" + String.format("%.2f", stats.getKDR()));
            
            if (plugin.getConfigManager().useElo()) {
                lore.add("§7Elo: §b" + stats.getElo());
            }
            
            meta.setLore(lore);
            skull.setItemMeta(meta);
            
            // Adicionar ao inventário
            rankingMenu.setItem(i, skull);
        }
        
        // Botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        rankingMenu.setItem(49, back);
        
        player.openInventory(rankingMenu);
    }
    
    /**
     * Abre o menu de equipe
     * 
     * @param player Jogador
     */
    public void openTeamMenu(Player player) {
        // Criar inventário para o menu de equipe
        Inventory teamMenu = Bukkit.createInventory(null, 36, getTeamMenuTitle());
        
        // Verificar se o jogador já está em uma equipe
        boolean inTeam = plugin.getTeamManager().isInTeam(player.getName());
        boolean isLeader = plugin.getTeamManager().isTeamLeader(player.getName());
        
        // Criar item de equipe
        ItemStack createTeam = InventoryUtils.createNamedItem(
                inTeam ? Material.BEDROCK : Material.IRON_CHESTPLATE,
                inTeam ? "§c§lVocê já está em uma equipe" : "§a§lCriar Equipe",
                inTeam ? "§7Você já está em uma equipe" : "§7Clique para criar uma nova equipe");
        teamMenu.setItem(11, createTeam);
        
        // Convidar jogador (apenas para líderes)
        ItemStack invitePlayer = InventoryUtils.createNamedItem(
                isLeader ? Material.IRON_HELMET : Material.BEDROCK,
                isLeader ? "§a§lConvidar Jogador" : "§c§lVocê não é líder",
                isLeader ? "§7Clique para convidar um jogador" : "§7Você precisa ser líder da equipe");
        teamMenu.setItem(13, invitePlayer);
        
        // Sair da equipe
        ItemStack leaveTeam = InventoryUtils.createNamedItem(
                inTeam ? Material.IRON_DOOR : Material.BEDROCK,
                inTeam ? "§c§lSair da Equipe" : "§c§lVocê não está em uma equipe",
                inTeam ? "§7Clique para sair da equipe" : "§7Você não está em nenhuma equipe");
        teamMenu.setItem(15, leaveTeam);
        
        // Informações da equipe (se estiver em uma)
        if (inTeam) {
            List<String> members = plugin.getTeamManager().getTeam(player.getName());
            String leader = plugin.getTeamManager().getTeamLeader(player.getName());
            
            // Criar item de informações
            ItemStack teamInfo = InventoryUtils.createNamedItem(Material.BOOK, "§e§lInformações da Equipe", 
                    "§7Líder: §f" + leader,
                    "§7Membros: §f" + members.size() + "/3",
                    "§7Clique com o botão direito para ver os membros");
            teamMenu.setItem(22, teamInfo);
        }
        
        // Botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        teamMenu.setItem(31, back);
        
        player.openInventory(teamMenu);
    }
    
    /**
     * Abre o menu para adicionar jogadores à equipe
     * 
     * @param player Jogador
     */
    public void openTeamAddMenu(Player player) {
        // Verificar se o jogador é líder de uma equipe
        if (!plugin.getTeamManager().isTeamLeader(player.getName())) {
            plugin.getMessageManager().sendMessage(player, "equipes.nao-lider");
            return;
        }
        
        // Verificar se a equipe já está cheia
        List<String> team = plugin.getTeamManager().getTeam(player.getName());
        if (team.size() >= 3) {
            plugin.getMessageManager().sendMessage(player, "equipes.equipe-cheia");
            return;
        }
        
        // Criar inventário para adicionar jogadores
        Inventory addMenu = Bukkit.createInventory(null, 54, "§8Adicionar Jogador à Equipe");
        
        // Adicionar jogadores online
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            // Não incluir membros da equipe
            if (plugin.getTeamManager().isInTeam(online.getName()) && 
                plugin.getTeamManager().getTeamLeader(online.getName()).equals(player.getName())) {
                continue;
            }
            
            // Criar cabeça do jogador
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(online.getName());
            meta.setDisplayName("§e" + online.getName());
            
            List<String> lore = new ArrayList<>();
            if (plugin.getTeamManager().isInTeam(online.getName())) {
                lore.add("§cJogador já está em uma equipe");
            } else {
                lore.add("§7Clique para convidar este jogador");
            }
            meta.setLore(lore);
            skull.setItemMeta(meta);
            
            addMenu.setItem(slot, skull);
            slot++;
            
            // Limitar a 44 jogadores (deixar espaço para o botão voltar)
            if (slot >= 44) {
                break;
            }
        }
        
        // Botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        addMenu.setItem(49, back);
        
        player.openInventory(addMenu);
    }
    
    /**
     * Obtém o título do menu principal
     * 
     * @return Título do menu principal
     */
    public String getMainMenuTitle() {
        return ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.titulo-principal"));
    }
    
    /**
     * Obtém o título do menu de administração
     * 
     * @return Título do menu de administração
     */
    public String getAdminMenuTitle() {
        return ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.titulo-admin"));
    }
    
    /**
     * Obtém o título do menu de seleção de jogador
     * 
     * @return Título do menu de seleção de jogador
     */
    public String getPlayerSelectionMenuTitle() {
        return ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.titulo-desafio"));
    }
    
    /**
     * Obtém o título do menu de confirmação de desafio
     * 
     * @return Título do menu de confirmação de desafio
     */
    public String getChallengeConfirmationMenuTitle() {
        return ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.titulo-confirmar"));
    }
    
    /**
     * Obtém o título do menu de apostas
     * 
     * @return Título do menu de apostas
     */
    public String getBetMenuTitle() {
        return ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.titulo-apostas"));
    }
    
    /**
     * Obtém o título do menu de estatísticas
     * 
     * @return Título do menu de estatísticas
     */
    public String getStatsMenuTitle() {
        return ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.titulo-estatisticas"));
    }
    
    /**
     * Obtém o título do menu de ranking
     * 
     * @return Título do menu de ranking
     */
    public String getRankingMenuTitle() {
        return ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.titulo-ranking"));
    }
    
    /**
     * Obtém o título do menu de equipe
     * 
     * @return Título do menu de equipe
     */
    public String getTeamMenuTitle() {
        return ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.titulo-equipes"));
    }
    
    /**
     * Obtém o jogador selecionado
     * 
     * @param player Jogador
     * @return Nome do jogador selecionado ou null se não houver
     */
    public String getSelectedPlayer(Player player) {
        return selectedPlayers.get(player.getName());
    }
    
    /**
     * Obtém o tipo de duelo selecionado
     * 
     * @param player Jogador
     * @return Tipo de duelo selecionado ou null se não houver
     */
    public DuelType getSelectedDuelType(Player player) {
        return selectedDuelTypes.get(player.getName());
    }
    
    /**
     * Define o tipo de duelo selecionado
     * 
     * @param player Jogador
     * @param type Tipo de duelo
     */
    public void setSelectedDuelType(Player player, DuelType type) {
        selectedDuelTypes.put(player.getName(), type);
    }
    
    /**
     * Limpa dados temporários de um jogador
     * 
     * @param player Jogador
     */
    public void clearPlayerData(Player player) {
        selectedPlayers.remove(player.getName());
        selectedDuelTypes.remove(player.getName());
    }
} 