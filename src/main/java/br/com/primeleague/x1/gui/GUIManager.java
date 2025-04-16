package br.com.primeleague.x1.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import br.com.primeleague.x1.rival.RivalData;
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
    private final Set<String> waitingForPlayerInput;
    private final Set<String> waitingForBetInput;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public GUIManager(Main plugin) {
        this.plugin = plugin;
        this.selectedPlayers = new HashMap<>();
        this.selectedDuelTypes = new HashMap<>();
        this.waitingForPlayerInput = new HashSet<>();
        this.waitingForBetInput = new HashSet<>();
        
        createMenus();
    }
    
    /**
     * Cria os menus principais
     */
    private void createMenus() {
        // Menu principal com design melhorado
        mainMenu = Bukkit.createInventory(null, 54, getMainMenuTitle());
        
        // Borda decorativa com vidro colorido para o menu principal
        decorateMenuBorders(mainMenu);
        
        // Itens do menu principal com ícones mais intuitivos
        ItemStack challenge = InventoryUtils.createNamedItem(Material.DIAMOND_SWORD, 
                plugin.getMessageManager().getItemDisplayName("gui.item-desafiar"),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-desafiar")));
        
        ItemStack stats = InventoryUtils.createNamedItem(Material.BOOK, 
                plugin.getMessageManager().getItemDisplayName("gui.item-estatisticas"),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-estatisticas")));
        
        ItemStack ranking = InventoryUtils.createNamedItem(Material.GOLD_INGOT, 
                plugin.getMessageManager().getItemDisplayName("gui.item-ranking"),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-ranking")));
        
        ItemStack team = InventoryUtils.createNamedItem(Material.IRON_CHESTPLATE, 
                plugin.getMessageManager().getItemDisplayName("gui.item-equipe"),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-equipe")));
        
        ItemStack spectate = InventoryUtils.createNamedItem(Material.EYE_OF_ENDER, 
                plugin.getMessageManager().getItemDisplayName("gui.item-espectador"),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-espectador")));
        
        ItemStack admin = InventoryUtils.createNamedItem(Material.REDSTONE, 
                plugin.getMessageManager().getItemDisplayName("gui.item-admin"),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-admin")));
        
        // Posicionamento mais ergonômico dos itens (formato de cruz para fácil acesso)
        mainMenu.setItem(21, challenge); // Esquerda
        mainMenu.setItem(22, stats);     // Centro
        mainMenu.setItem(23, ranking);   // Direita
        mainMenu.setItem(31, team);      // Centro-baixo
        
        // Botões adicionais posicionados onde são facilmente acessíveis
        mainMenu.setItem(40, spectate);  // Espectador no fim da cruz 
        mainMenu.setItem(49, admin);     // Admin na borda inferior
        
        // Menu de administração com design melhorado
        adminMenu = Bukkit.createInventory(null, 54, getAdminMenuTitle());
        
        // Borda decorativa para o menu de administração
        decorateMenuBorders(adminMenu);
        
        // Itens do menu de administração com melhores cores e visuais
        ItemStack setPos1 = InventoryUtils.createNamedItem(Material.WOOL, 
                "§a§lDefinir Posição 1", 
                "§7Clique para definir a primeira posição da arena",
                "§8Posição do jogador 1");
        setPos1.setDurability((short)5); // Verde
        
        ItemStack setPos2 = InventoryUtils.createNamedItem(Material.WOOL, 
                "§c§lDefinir Posição 2", 
                "§7Clique para definir a segunda posição da arena",
                "§8Posição do jogador 2");
        setPos2.setDurability((short)14); // Vermelho
        
        ItemStack setSpectator = InventoryUtils.createNamedItem(Material.WOOL, 
                "§e§lDefinir Local de Espectador", 
                "§7Clique para definir o local de espectador",
                "§8Onde os espectadores assistirão aos duelos");
        setSpectator.setDurability((short)4); // Amarelo
        
        ItemStack reload = InventoryUtils.createNamedItem(Material.NETHER_STAR, 
                "§6§lRecarregar Configuração", 
                "§7Clique para recarregar a configuração",
                "§8Atualiza alterações nos arquivos de configuração");
        
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                "§c§lVoltar", 
                "§7Clique para voltar ao menu principal");
        
        // Posicionamento melhorado dos itens do menu admin
        adminMenu.setItem(20, setPos1);
        adminMenu.setItem(22, setPos2);
        adminMenu.setItem(24, setSpectator);
        adminMenu.setItem(31, reload);
        adminMenu.setItem(49, back);
    }
    
    /**
     * Decora as bordas do menu com um padrão de vidro colorido mais estético
     * 
     * @param inventory Inventário a ser decorado
     */
    private void decorateMenuBorders(Inventory inventory) {
        int size = inventory.getSize();
        
        // Vidro preto para bordas externas
        ItemStack blackGlass = InventoryUtils.createNamedItem(Material.THIN_GLASS, " ");
        blackGlass.setDurability((short)15); // Preto
        
        // Vidro ciano para cantos (destaque)
        ItemStack cyanGlass = InventoryUtils.createNamedItem(Material.THIN_GLASS, " ");
        cyanGlass.setDurability((short)9); // Ciano
        
        // Bordas horizontais (superior e inferior)
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, blackGlass);                  // Borda superior
            inventory.setItem(size - 9 + i, blackGlass);       // Borda inferior
        }
        
        // Bordas verticais (esquerda e direita)
        for (int i = 0; i < size / 9; i++) {
            inventory.setItem(i * 9, blackGlass);              // Borda esquerda
            inventory.setItem(i * 9 + 8, blackGlass);          // Borda direita
        }
        
        // Cantos com vidro ciano (mais destaque)
        inventory.setItem(0, cyanGlass);                      // Superior esquerdo
        inventory.setItem(8, cyanGlass);                      // Superior direito
        inventory.setItem(size - 9, cyanGlass);               // Inferior esquerdo
        inventory.setItem(size - 1, cyanGlass);               // Inferior direito
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
     * Solicita ao jogador que digite o nome do jogador a desafiar
     * 
     * @param player Jogador que vai desafiar
     */
    public void promptPlayerSelection(Player player) {
        player.closeInventory();
        waitingForPlayerInput.add(player.getName());
        
        // Enviar mensagem direta sem tentar usar as mensagens do arquivo
        player.sendMessage("§a§lDigite o nome do jogador que deseja desafiar no chat:");
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
            meta.setDisplayName(truncateDisplayName("§e" + online.getName()));
            
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
        
        // Verificar se existe uma rivalidade entre os jogadores
        boolean isRivalry = plugin.getRivalManager().isRivalry(player.getName(), opponent);
        
        // Criar inventário para a confirmação de desafio
        // Se for uma rivalidade, usar um título diferente
        String menuTitle = isRivalry ? "§4✦ Revanche Oficial?" : getChallengeConfirmationMenuTitle();
        Inventory confirmMenu = Bukkit.createInventory(null, 45, menuTitle);
        
        // Se for uma rivalidade, adicionar decoração especial
        if (isRivalry) {
            decorateRivalInventory(confirmMenu);
            
            // Obter dados da rivalidade
            RivalData rivalData = plugin.getRivalManager().getRivalData(player.getName(), opponent);
            if (rivalData != null) {
                int p1Victories = rivalData.getVictories(player.getName());
                int p2Victories = rivalData.getVictories(opponent);
                
                // Adicionar item informativo da rivalidade
                ItemStack rivalInfo = InventoryUtils.createNamedItem(Material.NETHER_STAR, 
                        "§6§l⚔ Rivalidade ⚔", 
                        "§fVitórias: §c" + player.getName() + " §7" + p1Victories + " §fx §a" + opponent + " §7" + p2Victories,
                        "§7Total de duelos: §f" + rivalData.getTotalDuels(),
                        "§cA tensão aumenta a cada duelo!"); 
                
                confirmMenu.setItem(4, rivalInfo);
            }
        }
        
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
        ItemStack cancel = InventoryUtils.createNamedItem(Material.REDSTONE, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-cancelar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-cancelar")));
        confirmMenu.setItem(40, cancel);
        
        // Item de voltar para o menu anterior
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.voltar")),
                ColorUtils.colorize("§7Voltar para a seleção de jogadores"));
        confirmMenu.setItem(36, back);
        
        // Não adicionar mais o botão de apostas aqui, pois a GUI de apostas é separada
        
        player.openInventory(confirmMenu);
    }
    
    /**
     * Adiciona decoração especial para rivalidade no inventário
     * 
     * @param inventory Inventário para decorar
     */
    private void decorateRivalInventory(Inventory inventory) {
        // Moldura vermelha e dourada para o menu de rivais
        // Em 1.5.2, os tipos de lã com cores diferentes
        ItemStack redBorder = InventoryUtils.createNamedItem(Material.WOOL, (short) 14, " ");
        ItemStack goldBorder = InventoryUtils.createNamedItem(Material.WOOL, (short) 1, " ");
        
        // Bordas superior e inferior
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, (i % 2 == 0) ? redBorder : goldBorder);
            inventory.setItem(36 + i, (i % 2 == 0) ? redBorder : goldBorder);
        }
        
        // Bordas laterais
        for (int i = 1; i < 4; i++) {
            inventory.setItem(i * 9, redBorder);
            inventory.setItem(i * 9 + 8, goldBorder);
        }
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
        
        // Adicionar os itens de aposta com materiais diferentes para valores diferentes
        int slot = 10;
        Material[] materials = {
            Material.GOLD_NUGGET,   // Valor mais baixo
            Material.GOLD_INGOT,    // Valor médio-baixo
            Material.GOLD_BLOCK,    // Valor médio
            Material.DIAMOND,       // Valor médio-alto
            Material.EMERALD_BLOCK  // Valor mais alto
        };
        
        for (int i = 0; i < predefinedValues.size(); i++) {
            if (slot > 16) break; // Limitar a 7 valores pré-definidos
            
            Integer value = predefinedValues.get(i);
            
            // Formatar o valor
            String formattedValue = plugin.getEconomyManager().formatMoney(value);
            
            // Escolher material baseado no índice (ou usar gold_ingot se índice estiver fora do range)
            Material material = (i < materials.length) ? materials[i] : Material.GOLD_INGOT;
            
            ItemStack betItem = InventoryUtils.createNamedItem(material,
                    truncateDisplayName("§e§l" + formattedValue),
                    truncateDisplayName("§7Clique para apostar " + formattedValue));
            betMenu.setItem(slot, betItem);
            slot++;
        }
        
        // Preencher slots vazios com vidro preto
        ItemStack glass = new ItemStack(Material.WOOL, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < betMenu.getSize(); i++) {
            if (betMenu.getItem(i) == null) {
                betMenu.setItem(i, glass);
            }
        }
        
        // Item para valor personalizado (destacado com esmeralda)
        ItemStack customBet = InventoryUtils.createNamedItem(Material.EMERALD,
                truncateDisplayName("§a§lValor Personalizado"),
                truncateDisplayName("§7Clique para digitar um valor personalizado no chat"));
        betMenu.setItem(22, customBet);
        
        // Item para não apostar
        ItemStack noBet = InventoryUtils.createNamedItem(Material.BEDROCK, 
                truncateDisplayName("§c§lSem Aposta"), 
                truncateDisplayName("§7Clique para desafiar sem apostar"));
        betMenu.setItem(31, noBet);
        
        // Botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        betMenu.setItem(40, back);
        
        player.openInventory(betMenu);
    }
    
    /**
     * Solicita ao jogador que digite o valor da aposta
     * 
     * @param player Jogador que vai digitar o valor
     * @param opponent Nome do oponente
     */
    public void promptBetAmount(Player player, String opponent) {
        player.closeInventory();
        waitingForBetInput.add(player.getName());
        
        // Armazenar o oponente e o tipo de duelo para uso posterior
        String savedOpponent = selectedPlayers.get(player.getName());
        DuelType savedDuelType = selectedDuelTypes.get(player.getName());
        
        if (savedOpponent == null) {
            selectedPlayers.put(player.getName(), opponent);
        }
        
        // Enviar mensagem direta sem tentar usar as mensagens do arquivo
        player.sendMessage("§a§lDigite o valor que deseja apostar no chat:");
        player.sendMessage("§7Digite 0 para cancelar");
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
        skullMeta.setDisplayName(truncateDisplayName("§e§l" + player.getName()));
        skull.setItemMeta(skullMeta);
        statsMenu.setItem(4, skull);
        
        // Vitórias
        ItemStack victories = InventoryUtils.createNamedItem(Material.DIAMOND_SWORD, 
                truncateDisplayName("§a§lVitórias"), 
                truncateDisplayName("§7Total: §a" + stats.getVictories()));
        statsMenu.setItem(19, victories);
        
        // Derrotas
        ItemStack defeats = InventoryUtils.createNamedItem(Material.BONE, 
                truncateDisplayName("§c§lDerrotas"), 
                truncateDisplayName("§7Total: §c" + stats.getDefeats()));
        statsMenu.setItem(21, defeats);
        
        // Empates
        ItemStack draws = InventoryUtils.createNamedItem(Material.BUCKET, 
                truncateDisplayName("§e§lEmpates"), 
                truncateDisplayName("§7Total: §e" + stats.getDraws()));
        statsMenu.setItem(23, draws);
        
        // KDR
        ItemStack kdr = InventoryUtils.createNamedItem(Material.IRON_SWORD, 
                truncateDisplayName("§f§lKDR"), 
                truncateDisplayName("§7Relação V/D: §f" + String.format("%.2f", stats.getKDR())));
        statsMenu.setItem(25, kdr);
        
        // Taxa de vitórias
        ItemStack winRate = InventoryUtils.createNamedItem(Material.BLAZE_POWDER, 
                truncateDisplayName("§6§lTaxa de Vitórias"), 
                truncateDisplayName("§7Porcentagem: §6" + String.format("%.2f%%", stats.getWinRate())));
        statsMenu.setItem(29, winRate);
        
        // Elo
        if (plugin.getConfigManager().useElo()) {
            ItemStack elo = InventoryUtils.createNamedItem(Material.NETHER_STAR, 
                    truncateDisplayName("§b§lElo"), 
                    truncateDisplayName("§7Pontuação: §b" + stats.getElo()));
            statsMenu.setItem(31, elo);
        }
        
        // Total de duelos
        ItemStack total = InventoryUtils.createNamedItem(Material.COMPASS, 
                truncateDisplayName("§d§lTotal de Duelos"), 
                truncateDisplayName("§7Duelos: §d" + stats.getTotalDuels()));
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
            meta.setDisplayName(truncateDisplayName("§e§l#" + (i + 1) + " " + stats.getPlayerName()));
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
            String teamName = plugin.getTeamManager().getPlayerTeam(player.getName());
            String leader = plugin.getTeamManager().getTeamLeader(teamName);
            
            // Lista de membros para exibir no lore
            List<String> memberLore = new ArrayList<>();
            memberLore.add("§7Líder: §f" + (leader != null ? leader : "Nenhum"));
            memberLore.add("§7Membros: §f" + (members != null ? members.size() : 0) + "/3");
            
            if (members != null) {
                memberLore.add("§7§m---------------");
                for (String member : members) {
                    memberLore.add("§7- §f" + member);
                }
            }
            
            memberLore.add("§7§m---------------");
            memberLore.add("§7Clique com o botão direito para ver os membros");
            
            // Criar item de informações
            ItemStack teamInfo = InventoryUtils.createNamedItem(
                    Material.BOOK, 
                    truncateDisplayName("§e§lInformações da Equipe (" + teamName + ")"), 
                    memberLore.toArray(new String[0]));
            
            teamMenu.setItem(22, teamInfo);
            
            // Adicionar botão para desafiar outra equipe (apenas para líderes)
            ItemStack challengeTeam = InventoryUtils.createNamedItem(
                    isLeader ? Material.DIAMOND_SWORD : Material.BEDROCK,
                    isLeader ? "§e§lDesafiar Equipe" : "§c§lVocê não é líder",
                    isLeader ? 
                        "§7Clique para desafiar outra equipe" : 
                        "§7Apenas o líder pode desafiar outras equipes");
            
            // Verificar se a equipe tem pelo menos 2 membros
            if (members != null && members.size() >= 2) {
                teamMenu.setItem(4, teamInfo);
                teamMenu.setItem(22, challengeTeam);
            }
        }
        
        // Botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
                ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        teamMenu.setItem(31, back);
        
        player.openInventory(teamMenu);
    }
    
    /**
     * Abre o menu de adicionar jogador à equipe
     * 
     * @param player Jogador
     */
    public void openTeamAddMenu(Player player) {
        // Criar inventário para adicionar membros à equipe
        Inventory addMenu = Bukkit.createInventory(null, 54, "§8Convidar Jogador");
        
        // Adicionar jogadores online
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            // Não incluir o próprio jogador
            if (online.getName().equals(player.getName())) {
                continue;
            }
            
            // Verificar se o jogador está na mesma equipe
            String playerTeam = plugin.getTeamManager().getPlayerTeam(player.getName());
            String onlineTeam = plugin.getTeamManager().getPlayerTeam(online.getName());
            if (playerTeam != null && playerTeam.equals(onlineTeam)) {
                continue;
            }
            
            // Não incluir jogadores que já estão em outra equipe
            if (plugin.getTeamManager().isInTeam(online.getName())) {
                continue;
            }
            
            // Criar cabeça do jogador
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(online.getName());
            meta.setDisplayName("§e" + online.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Clique para convidar para sua equipe");
            meta.setLore(lore);
            
            skull.setItemMeta(meta);
            
            addMenu.setItem(slot++, skull);
        }
        
        // Adicionar botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
            ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
            ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        addMenu.setItem(49, back);
        
        player.openInventory(addMenu);
    }
    
    /**
     * Abre o menu de desafiar outras equipes
     * 
     * @param player Jogador
     */
    public void openTeamChallengeMenu(Player player) {
        // Criar inventário para o menu de desafio de equipes
        Inventory teamChallengeMenu = Bukkit.createInventory(null, 54, "§4Desafiar Equipe");
        
        // Adicionar informações sobre a equipe do jogador
        String playerTeamName = plugin.getTeamManager().getPlayerTeam(player.getName());
        List<String> playerTeam = plugin.getTeamManager().getTeam(player.getName());
        
        // O item no centro mostrará a equipe do jogador
        ItemStack playerTeamItem = new ItemStack(Material.WOOL, 1, (short) 14); // Vermelho
        ItemMeta meta = playerTeamItem.getItemMeta();
        meta.setDisplayName("§c§lSua Equipe: §f" + playerTeamName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Membros:");
        for (String member : playerTeam) {
            if (player.getName().equals(member)) {
                lore.add("§c→ §f" + member + " §7(Líder)");
            } else {
                lore.add("§7→ §f" + member);
            }
        }
        
        meta.setLore(lore);
        playerTeamItem.setItemMeta(meta);
        
        teamChallengeMenu.setItem(4, playerTeamItem); // Slot central na linha superior
        
        // Adicionar outras equipes
        Map<String, List<String>> allTeams = getAllTeams();
        int slot = 9; // Começar na segunda linha
        
        for (Map.Entry<String, List<String>> entry : allTeams.entrySet()) {
            String teamName = entry.getKey();
            List<String> members = entry.getValue();
            
            // Pular a equipe do próprio jogador
            if (teamName.equals(playerTeamName)) {
                continue;
            }
            
            // Verificar se o tamanho da equipe é compatível (deve ser igual)
            if (members.size() != playerTeam.size()) {
                continue;
            }
            
            // Criar item para a equipe
            ItemStack teamItem = new ItemStack(Material.WOOL, 1, (short) 11); // Azul
            ItemMeta teamMeta = teamItem.getItemMeta();
            teamMeta.setDisplayName("§a§l" + teamName);
            
            List<String> teamLore = new ArrayList<>();
            teamLore.add("§7Membros:");
            
            String leader = plugin.getTeamManager().getTeamLeader(teamName);
            
            for (String member : members) {
                if (leader != null && leader.equals(member)) {
                    teamLore.add("§b→ §f" + member + " §7(Líder)");
                } else {
                    teamLore.add("§7→ §f" + member);
                }
            }
            
            teamLore.add("");
            teamLore.add("§eClique para desafiar esta equipe");
            
            teamMeta.setLore(teamLore);
            teamItem.setItemMeta(teamMeta);
            
            teamChallengeMenu.setItem(slot++, teamItem);
            
            // Máximo de 7 times por página (21 slots)
            if (slot > 30) {
                break;
            }
        }
        
        // Adicionar botão voltar
        ItemStack back = InventoryUtils.createNamedItem(Material.ARROW, 
            ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.item-voltar")),
            ColorUtils.colorize(plugin.getMessageManager().getMessage("gui.desc-voltar")));
        teamChallengeMenu.setItem(49, back);
        
        player.openInventory(teamChallengeMenu);
    }
    
    /**
     * Obtém todas as equipes do plugin
     * Este método usa o TeamManager para obter todas as equipes e seus membros
     * 
     * @return Mapa com nome da equipe e lista de membros
     */
    private Map<String, List<String>> getAllTeams() {
        Map<String, List<String>> result = new HashMap<>();
        
        // Obter todos os líderes de equipe
        for (String leaderName : plugin.getTeamManager().getAllTeamLeaders()) {
            String teamName = plugin.getTeamManager().getPlayerTeam(leaderName);
            if (teamName != null) {
                List<String> members = plugin.getTeamManager().getTeam(leaderName);
                if (members != null && !members.isEmpty()) {
                    result.put(teamName, members);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Obtém o título do menu principal
     * 
     * @return Título do menu principal
     */
    public String getMainMenuTitle() {
        return plugin.getMessageManager().getShortMessage("gui.titulo-principal", 32);
    }
    
    /**
     * Obtém o título do menu de administração
     * 
     * @return Título do menu de administração
     */
    public String getAdminMenuTitle() {
        return plugin.getMessageManager().getShortMessage("gui.titulo-admin", 32);
    }
    
    /**
     * Obtém o título do menu de seleção de jogadores
     * 
     * @return Título do menu de seleção de jogadores
     */
    public String getPlayerSelectionMenuTitle() {
        return plugin.getMessageManager().getShortMessage("gui.titulo-selecao", 32);
    }
    
    /**
     * Obtém o título do menu de confirmação de desafio
     * 
     * @return Título do menu de confirmação de desafio
     */
    public String getChallengeConfirmationMenuTitle() {
        return plugin.getMessageManager().getShortMessage("gui.titulo-confirmacao", 32);
    }
    
    /**
     * Obtém o título do menu de apostas
     * 
     * @return Título do menu de apostas
     */
    public String getBetMenuTitle() {
        return plugin.getMessageManager().getShortMessage("gui.titulo-aposta", 32);
    }
    
    /**
     * Obtém o título do menu de estatísticas
     * 
     * @return Título do menu de estatísticas
     */
    public String getStatsMenuTitle() {
        return plugin.getMessageManager().getShortMessage("gui.titulo-estatisticas", 32);
    }
    
    /**
     * Obtém o título do menu de ranking
     * 
     * @return Título do menu de ranking
     */
    public String getRankingMenuTitle() {
        return plugin.getMessageManager().getShortMessage("gui.titulo-ranking", 32);
    }
    
    /**
     * Obtém o título do menu de equipe
     * 
     * @return Título do menu de equipe
     */
    public String getTeamMenuTitle() {
        return plugin.getMessageManager().getShortMessage("gui.titulo-equipe", 32);
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
    
    /**
     * Verifica se o jogador está aguardando input para desafio
     * 
     * @param playerName Nome do jogador
     * @return true se estiver aguardando, false caso contrário
     */
    public boolean isWaitingForPlayerInput(String playerName) {
        return waitingForPlayerInput.contains(playerName);
    }
    
    /**
     * Remove o jogador da lista de espera por input
     * 
     * @param playerName Nome do jogador
     */
    public void clearWaitingForPlayerInput(String playerName) {
        waitingForPlayerInput.remove(playerName);
    }
    
    /**
     * Verifica se o jogador está aguardando input para o valor da aposta
     * 
     * @param playerName Nome do jogador
     * @return true se estiver aguardando, false caso contrário
     */
    public boolean isWaitingForBetInput(String playerName) {
        return waitingForBetInput.contains(playerName);
    }
    
    /**
     * Remove o jogador da lista de espera por input da aposta
     * 
     * @param playerName Nome do jogador
     */
    public void clearWaitingForBetInput(String playerName) {
        waitingForBetInput.remove(playerName);
    }

    /**
     * Garante que o nome do item não exceda o limite de 32 caracteres do Minecraft 1.5.2
     * 
     * @param name Nome a ser verificado
     * @return Nome truncado se necessário
     */
    private String truncateDisplayName(String name) {
        return br.com.primeleague.x1.utils.InventoryUtils.ensureValidLength(name);
    }
} 